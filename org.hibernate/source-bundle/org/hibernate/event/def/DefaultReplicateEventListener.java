//$Id: DefaultReplicateEventListener.java 11088 2007-01-24 14:29:43Z max.andersen@jboss.com $
package org.hibernate.event.def;

import org.hibernate.HibernateException;
import org.hibernate.TransientObjectException;
import org.hibernate.ReplicationMode;
import org.hibernate.LockMode;
import org.hibernate.engine.Cascade;
import org.hibernate.engine.CascadingAction;
import org.hibernate.engine.EntityKey;
import org.hibernate.engine.Status;
import org.hibernate.event.EventSource;
import org.hibernate.event.ReplicateEvent;
import org.hibernate.event.ReplicateEventListener;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.Type;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Defines the default replicate event listener used by Hibernate to replicate
 * entities in response to generated replicate events.
 *
 * @author Steve Ebersole
 */
public class DefaultReplicateEventListener extends AbstractSaveEventListener implements ReplicateEventListener {

	private static final Log log = LogFactory.getLog( DefaultReplicateEventListener.class );

	/**
	 * Handle the given replicate event.
	 *
	 * @param event The replicate event to be handled.
	 *
	 * @throws TransientObjectException An invalid attempt to replicate a transient entity.
	 */
	public void onReplicate(ReplicateEvent event) {
		final EventSource source = event.getSession();
		if ( source.getPersistenceContext().reassociateIfUninitializedProxy( event.getObject() ) ) {
			log.trace( "uninitialized proxy passed to replicate()" );
			return;
		}

		Object entity = source.getPersistenceContext().unproxyAndReassociate( event.getObject() );

		if ( source.getPersistenceContext().isEntryFor( entity ) ) {
			log.trace( "ignoring persistent instance passed to replicate()" );
			//hum ... should we cascade anyway? throw an exception? fine like it is?
			return;
		}

		EntityPersister persister = source.getEntityPersister( event.getEntityName(), entity );

		// get the id from the object
		/*if ( persister.isUnsaved(entity, source) ) {
			throw new TransientObjectException("transient instance passed to replicate()");
		}*/
		Serializable id = persister.getIdentifier( entity, source.getEntityMode() );
		if ( id == null ) {
			throw new TransientObjectException( "instance with null id passed to replicate()" );
		}

		final ReplicationMode replicationMode = event.getReplicationMode();

		final Object oldVersion;
		if ( replicationMode == ReplicationMode.EXCEPTION ) {
			//always do an INSERT, and let it fail by constraint violation
			oldVersion = null;
		}
		else {
			//what is the version on the database?
			oldVersion = persister.getCurrentVersion( id, source );			
		}

		if ( oldVersion != null ) { 			
			if ( log.isTraceEnabled() ) {
				log.trace(
						"found existing row for " +
								MessageHelper.infoString( persister, id, source.getFactory() )
				);
			}

			/// HHH-2378
			final Object realOldVersion = persister.isVersioned() ? oldVersion : null;
			
			boolean canReplicate = replicationMode.shouldOverwriteCurrentVersion(
					entity,
					realOldVersion,
					persister.getVersion( entity, source.getEntityMode() ),
					persister.getVersionType()
			);

			if ( canReplicate ) {
				//will result in a SQL UPDATE:
				performReplication( entity, id, realOldVersion, persister, replicationMode, source );
			}
			else {
				//else do nothing (don't even reassociate object!)
				log.trace( "no need to replicate" );
			}

			//TODO: would it be better to do a refresh from db?
		}
		else {
			// no existing row - do an insert
			if ( log.isTraceEnabled() ) {
				log.trace(
						"no existing row, replicating new instance " +
								MessageHelper.infoString( persister, id, source.getFactory() )
				);
			}

			final boolean regenerate = persister.isIdentifierAssignedByInsert(); // prefer re-generation of identity!
			final EntityKey key = regenerate ?
					null : new EntityKey( id, persister, source.getEntityMode() );

			performSaveOrReplicate(
					entity,
					key,
					persister,
					regenerate,
					replicationMode,
					source,
					true
			);

		}
	}

	protected boolean visitCollectionsBeforeSave(Object entity, Serializable id, Object[] values, Type[] types, EventSource source) {
		//TODO: we use two visitors here, inefficient!
		OnReplicateVisitor visitor = new OnReplicateVisitor( source, id, entity, false );
		visitor.processEntityPropertyValues( values, types );
		return super.visitCollectionsBeforeSave( entity, id, values, types, source );
	}

	protected boolean substituteValuesIfNecessary(
			Object entity,
			Serializable id,
			Object[] values,
			EntityPersister persister,
			SessionImplementor source) {
		return false;
	}

	protected boolean isVersionIncrementDisabled() {
		return true;
	}

	private void performReplication(
			Object entity,
			Serializable id,
			Object version,
			EntityPersister persister,
			ReplicationMode replicationMode,
			EventSource source) throws HibernateException {

		if ( log.isTraceEnabled() ) {
			log.trace(
					"replicating changes to " +
							MessageHelper.infoString( persister, id, source.getFactory() )
			);
		}

		new OnReplicateVisitor( source, id, entity, true ).process( entity, persister );

		source.getPersistenceContext().addEntity(
				entity,
				Status.MANAGED,
				null,
				new EntityKey( id, persister, source.getEntityMode() ),
				version,
				LockMode.NONE,
				true,
				persister,
				true,
				false
		);

		cascadeAfterReplicate( entity, persister, replicationMode, source );
	}

	private void cascadeAfterReplicate(
			Object entity,
			EntityPersister persister,
			ReplicationMode replicationMode,
			EventSource source) {
		source.getPersistenceContext().incrementCascadeLevel();
		try {
			new Cascade( CascadingAction.REPLICATE, Cascade.AFTER_UPDATE, source )
					.cascade( persister, entity, replicationMode );
		}
		finally {
			source.getPersistenceContext().decrementCascadeLevel();
		}
	}

	protected CascadingAction getCascadeAction() {
		return CascadingAction.REPLICATE;
	}
}
