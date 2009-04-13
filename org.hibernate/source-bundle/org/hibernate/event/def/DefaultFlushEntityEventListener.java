//$Id: DefaultFlushEntityEventListener.java 14016 2007-09-20 21:59:46Z cbredesen $
package org.hibernate.event.def;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.AssertionFailure;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.action.EntityUpdateAction;
import org.hibernate.action.DelayedPostInsertIdentifier;
import org.hibernate.classic.Validatable;
import org.hibernate.engine.EntityEntry;
import org.hibernate.engine.EntityKey;
import org.hibernate.engine.Nullability;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.engine.Status;
import org.hibernate.engine.Versioning;
import org.hibernate.event.EventSource;
import org.hibernate.event.FlushEntityEvent;
import org.hibernate.event.FlushEntityEventListener;
import org.hibernate.intercept.FieldInterceptionHelper;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.Type;
import org.hibernate.util.ArrayHelper;

/**
 * An event that occurs for each entity instance at flush time
 *
 * @author Gavin King
 */
public class DefaultFlushEntityEventListener implements FlushEntityEventListener {

	private static final Log log = LogFactory.getLog(DefaultFlushEntityEventListener.class);

	/**
	 * make sure user didn't mangle the id
	 */
	public void checkId(Object object, EntityPersister persister, Serializable id, EntityMode entityMode)
	throws HibernateException {

		if ( id != null && id instanceof DelayedPostInsertIdentifier ) {
			// this is a situation where the entity id is assigned by a post-insert generator
			// and was saved outside the transaction forcing it to be delayed
			return;
		}

		if ( persister.canExtractIdOutOfEntity() ) {

			Serializable oid = persister.getIdentifier( object, entityMode );
			if (id==null) {
				throw new AssertionFailure("null id in " + persister.getEntityName() + " entry (don't flush the Session after an exception occurs)");
			}
			if ( !persister.getIdentifierType().isEqual(id, oid, entityMode) ) {
				throw new HibernateException(
						"identifier of an instance of " +
						persister.getEntityName() +
						" was altered from " + id +
						" to " + oid
					);
			}
		}

	}

	private void checkNaturalId(
			EntityPersister persister,
	        EntityEntry entry,
	        Object[] current,
	        Object[] loaded,
	        EntityMode entityMode,
	        SessionImplementor session) {
		if ( persister.hasNaturalIdentifier() && entry.getStatus() != Status.READ_ONLY ) {
 			Object[] snapshot = null;			
			Type[] types = persister.getPropertyTypes();
			int[] props = persister.getNaturalIdentifierProperties();
			boolean[] updateable = persister.getPropertyUpdateability();
			for ( int i=0; i<props.length; i++ ) {
				int prop = props[i];
				if ( !updateable[prop] ) {
 					Object loadedVal;
 					if ( loaded == null ) {
 						if ( snapshot == null) {
 							snapshot = session.getPersistenceContext().getNaturalIdSnapshot( entry.getId(), persister );
 						}
 						loadedVal = snapshot[i];
 					} else {
 						loadedVal = loaded[prop];
 					}
 					if ( !types[prop].isEqual( current[prop], loadedVal, entityMode ) ) {						
						throw new HibernateException(
								"immutable natural identifier of an instance of " +
								persister.getEntityName() +
								" was altered"
							);
					}
				}
			}
		}
	}

	/**
	 * Flushes a single entity's state to the database, by scheduling
	 * an update action, if necessary
	 */
	public void onFlushEntity(FlushEntityEvent event) throws HibernateException {
		final Object entity = event.getEntity();
		final EntityEntry entry = event.getEntityEntry();
		final EventSource session = event.getSession();
		final EntityPersister persister = entry.getPersister();
		final Status status = entry.getStatus();
		final EntityMode entityMode = session.getEntityMode();
		final Type[] types = persister.getPropertyTypes();

		final boolean mightBeDirty = entry.requiresDirtyCheck(entity);

		final Object[] values = getValues( entity, entry, entityMode, mightBeDirty, session );

		event.setPropertyValues(values);

		//TODO: avoid this for non-new instances where mightBeDirty==false
		boolean substitute = wrapCollections( session, persister, types, values);

		if ( isUpdateNecessary( event, mightBeDirty ) ) {
			substitute = scheduleUpdate( event ) || substitute;
		}

		if ( status != Status.DELETED ) {
			// now update the object .. has to be outside the main if block above (because of collections)
			if (substitute) persister.setPropertyValues( entity, values, entityMode );

			// Search for collections by reachability, updating their role.
			// We don't want to touch collections reachable from a deleted object
			if ( persister.hasCollections() ) {
				new FlushVisitor(session, entity).processEntityPropertyValues(values, types);
			}
		}

	}

	private Object[] getValues(
			Object entity,
			EntityEntry entry,
			EntityMode entityMode,
			boolean mightBeDirty,
	        SessionImplementor session
	) {
		final Object[] loadedState = entry.getLoadedState();
		final Status status = entry.getStatus();
		final EntityPersister persister = entry.getPersister();

		final Object[] values;
		if ( status == Status.DELETED ) {
			//grab its state saved at deletion
			values = entry.getDeletedState();
		}
		else if ( !mightBeDirty && loadedState!=null ) {
			values = loadedState;
		}
		else {
			checkId( entity, persister, entry.getId(), entityMode );

			// grab its current state
			values = persister.getPropertyValues( entity, entityMode );

			checkNaturalId( persister, entry, values, loadedState, entityMode, session );
		}
		return values;
	}

	private boolean wrapCollections(
			EventSource session,
			EntityPersister persister,
			Type[] types,
			Object[] values
	) {
		if ( persister.hasCollections() ) {

			// wrap up any new collections directly referenced by the object
			// or its components

			// NOTE: we need to do the wrap here even if its not "dirty",
			// because collections need wrapping but changes to _them_
			// don't dirty the container. Also, for versioned data, we
			// need to wrap before calling searchForDirtyCollections

			WrapVisitor visitor = new WrapVisitor(session);
			// substitutes into values by side-effect
			visitor.processEntityPropertyValues(values, types);
			return visitor.isSubstitutionRequired();
		}
		else {
			return false;
		}
	}

	private boolean isUpdateNecessary(final FlushEntityEvent event, final boolean mightBeDirty) {
		final Status status = event.getEntityEntry().getStatus();
		if ( mightBeDirty || status==Status.DELETED ) {
			// compare to cached state (ignoring collections unless versioned)
			dirtyCheck(event);
			if ( isUpdateNecessary(event) ) {
				return true;
			}
			else {
				FieldInterceptionHelper.clearDirty( event.getEntity() );
				return false;
			}
		}
		else {
			return hasDirtyCollections( event, event.getEntityEntry().getPersister(), status );
		}
	}

	private boolean scheduleUpdate(final FlushEntityEvent event) {
		
		final EntityEntry entry = event.getEntityEntry();
		final EventSource session = event.getSession();
		final Object entity = event.getEntity();
		final Status status = entry.getStatus();
		final EntityMode entityMode = session.getEntityMode();
		final EntityPersister persister = entry.getPersister();
		final Object[] values = event.getPropertyValues();
		
		if ( log.isTraceEnabled() ) {
			if ( status == Status.DELETED ) {
				log.trace(
						"Updating deleted entity: " +
						MessageHelper.infoString( persister, entry.getId(), session.getFactory() )
					);
			}
			else {
				log.trace(
						"Updating entity: " +
						MessageHelper.infoString( persister, entry.getId(), session.getFactory()  )
					);
			}
		}

		boolean intercepted;
		
		if ( !entry.isBeingReplicated() ) {
			// give the Interceptor a chance to process property values, if the properties 
			// were modified by the Interceptor, we need to set them back to the object
			intercepted = handleInterception( event );
		}
		else {
			intercepted = false;
		}

		validate( entity, persister, status, entityMode );

		// increment the version number (if necessary)
		final Object nextVersion = getNextVersion(event);

		// if it was dirtied by a collection only
		int[] dirtyProperties = event.getDirtyProperties();
		if ( event.isDirtyCheckPossible() && dirtyProperties == null ) {
			if ( ! intercepted && !event.hasDirtyCollection() ) {
				throw new AssertionFailure( "dirty, but no dirty properties" );
			}
			dirtyProperties = ArrayHelper.EMPTY_INT_ARRAY;
		}

		// check nullability but do not perform command execute
		// we'll use scheduled updates for that.
		new Nullability(session).checkNullability( values, persister, true );

		// schedule the update
		// note that we intentionally do _not_ pass in currentPersistentState!
		session.getActionQueue().addAction(
				new EntityUpdateAction(
						entry.getId(),
						values,
						dirtyProperties,
						event.hasDirtyCollection(),
						entry.getLoadedState(),
						entry.getVersion(),
						nextVersion,
						entity,
						entry.getRowId(),
						persister,
						session
					)
			);
		
		return intercepted;
	}

	protected void validate(Object entity, EntityPersister persister, Status status, EntityMode entityMode) {
		// validate() instances of Validatable
		if ( status == Status.MANAGED && persister.implementsValidatable( entityMode ) ) {
			( (Validatable) entity ).validate();
		}
	}
	
	protected boolean handleInterception(FlushEntityEvent event) {
		SessionImplementor session = event.getSession();
		EntityEntry entry = event.getEntityEntry();
		EntityPersister persister = entry.getPersister();
		Object entity = event.getEntity();
		
		//give the Interceptor a chance to modify property values
		final Object[] values = event.getPropertyValues();
		final boolean intercepted = invokeInterceptor( session, entity, entry, values, persister );

		//now we might need to recalculate the dirtyProperties array
		if ( intercepted && event.isDirtyCheckPossible() && !event.isDirtyCheckHandledByInterceptor() ) {
			int[] dirtyProperties;
			if ( event.hasDatabaseSnapshot() ) {
				dirtyProperties = persister.findModified( event.getDatabaseSnapshot(), values, entity, session );
			}
			else {
				dirtyProperties = persister.findDirty( values, entry.getLoadedState(), entity, session );
			}
			event.setDirtyProperties(dirtyProperties);
		}
		
		return intercepted;
	}

	protected boolean invokeInterceptor(
			SessionImplementor session, 
			Object entity, 
			EntityEntry entry, 
			final Object[] values,
			EntityPersister persister) {
		return session.getInterceptor().onFlushDirty(
				entity,
				entry.getId(),
				values,
				entry.getLoadedState(),
				persister.getPropertyNames(),
				persister.getPropertyTypes()
		);
	}

	/**
	 * Convience method to retreive an entities next version value
	 */
	private Object getNextVersion(FlushEntityEvent event) throws HibernateException {
		
		EntityEntry entry = event.getEntityEntry();
		EntityPersister persister = entry.getPersister();
		if ( persister.isVersioned() ) {

			Object[] values = event.getPropertyValues();
		    
			if ( entry.isBeingReplicated() ) {
				return Versioning.getVersion(values, persister);
			}
			else {
				int[] dirtyProperties = event.getDirtyProperties();
				
				final boolean isVersionIncrementRequired = isVersionIncrementRequired( 
						event, 
						entry, 
						persister, 
						dirtyProperties 
					);
				
				final Object nextVersion = isVersionIncrementRequired ?
						Versioning.increment( entry.getVersion(), persister.getVersionType(), event.getSession() ) :
						entry.getVersion(); //use the current version
						
				Versioning.setVersion(values, nextVersion, persister);
				
				return nextVersion;
			}
		}
		else {
			return null;
		}
		
	}

	private boolean isVersionIncrementRequired(
			FlushEntityEvent event, 
			EntityEntry entry, 
			EntityPersister persister, 
			int[] dirtyProperties
	) {
		final boolean isVersionIncrementRequired = entry.getStatus()!=Status.DELETED && ( 
				dirtyProperties==null || 
				Versioning.isVersionIncrementRequired( 
						dirtyProperties, 
						event.hasDirtyCollection(),
						persister.getPropertyVersionability()
					) 
			);
		return isVersionIncrementRequired;
	}

	/**
	 * Performs all necessary checking to determine if an entity needs an SQL update
	 * to synchronize its state to the database. Modifies the event by side-effect!
	 * Note: this method is quite slow, avoid calling if possible!
	 */
	protected final boolean isUpdateNecessary(FlushEntityEvent event) throws HibernateException {

		EntityPersister persister = event.getEntityEntry().getPersister();
		Status status = event.getEntityEntry().getStatus();
		
		if ( !event.isDirtyCheckPossible() ) {
			return true;
		}
		else {
			
			int[] dirtyProperties = event.getDirtyProperties();
			if ( dirtyProperties!=null && dirtyProperties.length!=0 ) {
				return true; //TODO: suck into event class
			}
			else {
				return hasDirtyCollections( event, persister, status );
			}
			
		}
	}

	private boolean hasDirtyCollections(FlushEntityEvent event, EntityPersister persister, Status status) {
		if ( isCollectionDirtyCheckNecessary(persister, status) ) {
			DirtyCollectionSearchVisitor visitor = new DirtyCollectionSearchVisitor( 
					event.getSession(),
					persister.getPropertyVersionability()
				);
			visitor.processEntityPropertyValues( event.getPropertyValues(), persister.getPropertyTypes() );
			boolean hasDirtyCollections = visitor.wasDirtyCollectionFound();
			event.setHasDirtyCollection(hasDirtyCollections);
			return hasDirtyCollections;
		}
		else {
			return false;
		}
	}

	private boolean isCollectionDirtyCheckNecessary(EntityPersister persister, Status status) {
		return status==Status.MANAGED && 
				persister.isVersioned() && 
				persister.hasCollections();
	}
	
	/**
	 * Perform a dirty check, and attach the results to the event
	 */
	protected void dirtyCheck(FlushEntityEvent event) throws HibernateException {
		
		final Object entity = event.getEntity();
		final Object[] values = event.getPropertyValues();
		final SessionImplementor session = event.getSession();
		final EntityEntry entry = event.getEntityEntry();
		final EntityPersister persister = entry.getPersister();
		final Serializable id = entry.getId();
		final Object[] loadedState = entry.getLoadedState();

		int[] dirtyProperties = session.getInterceptor().findDirty( 
				entity, 
				id, 
				values, 
				loadedState, 
				persister.getPropertyNames(), 
				persister.getPropertyTypes() 
			);
		
		event.setDatabaseSnapshot(null);

		final boolean interceptorHandledDirtyCheck;
		boolean cannotDirtyCheck;
		
		if ( dirtyProperties==null ) {
			// Interceptor returned null, so do the dirtycheck ourself, if possible
			interceptorHandledDirtyCheck = false;
			
			cannotDirtyCheck = loadedState==null; // object loaded by update()
			if ( !cannotDirtyCheck ) {
				// dirty check against the usual snapshot of the entity
				dirtyProperties = persister.findDirty( values, loadedState, entity, session );
				
			}
			else {
				// dirty check against the database snapshot, if possible/necessary
				final Object[] databaseSnapshot = getDatabaseSnapshot(session, persister, id);
				if ( databaseSnapshot != null ) {
					dirtyProperties = persister.findModified(databaseSnapshot, values, entity, session);
					cannotDirtyCheck = false;
					event.setDatabaseSnapshot(databaseSnapshot);
				}
			}
		}
		else {
			// the Interceptor handled the dirty checking
			cannotDirtyCheck = false;
			interceptorHandledDirtyCheck = true;
		}
		
		event.setDirtyProperties(dirtyProperties);
		event.setDirtyCheckHandledByInterceptor(interceptorHandledDirtyCheck);
		event.setDirtyCheckPossible(!cannotDirtyCheck);
		
	}

	private Object[] getDatabaseSnapshot(SessionImplementor session, EntityPersister persister, Serializable id) {
		if ( persister.isSelectBeforeUpdateRequired() ) {
			Object[] snapshot = session.getPersistenceContext()
					.getDatabaseSnapshot(id, persister);
			if (snapshot==null) {
				//do we even really need this? the update will fail anyway....
				if ( session.getFactory().getStatistics().isStatisticsEnabled() ) {
					session.getFactory().getStatisticsImplementor()
							.optimisticFailure( persister.getEntityName() );
				}
				throw new StaleObjectStateException( persister.getEntityName(), id );
			}
			else {
				return snapshot;
			}
		}
		else {
			//TODO: optimize away this lookup for entities w/o unsaved-value="undefined"
			EntityKey entityKey = new EntityKey( id, persister, session.getEntityMode() );
			return session.getPersistenceContext()
					.getCachedDatabaseSnapshot( entityKey ); 
		}
	}

}
