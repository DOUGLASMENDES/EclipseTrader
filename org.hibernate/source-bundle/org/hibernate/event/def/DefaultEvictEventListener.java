//$Id: DefaultEvictEventListener.java 10225 2006-08-04 20:33:48Z steve.ebersole@jboss.com $
package org.hibernate.event.def;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.engine.Cascade;
import org.hibernate.engine.CascadingAction;
import org.hibernate.engine.EntityEntry;
import org.hibernate.engine.EntityKey;
import org.hibernate.engine.PersistenceContext;
import org.hibernate.event.EventSource;
import org.hibernate.event.EvictEvent;
import org.hibernate.event.EvictEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

/**
 * Defines the default evict event listener used by hibernate for evicting entities
 * in response to generated flush events.  In particular, this implementation will
 * remove any hard references to the entity that are held by the infrastructure
 * (references held by application or other persistent instances are okay)
 *
 * @author Steve Ebersole
 */
public class DefaultEvictEventListener implements EvictEventListener {

	private static final Log log = LogFactory.getLog(DefaultEvictEventListener.class);

	/** 
	 * Handle the given evict event.
	 *
	 * @param event The evict event to be handled.
	 * @throws HibernateException
	 */
	public void onEvict(EvictEvent event) throws HibernateException {
		EventSource source = event.getSession();
		final Object object = event.getObject();
		final PersistenceContext persistenceContext = source.getPersistenceContext();

		if ( object instanceof HibernateProxy ) {
			LazyInitializer li = ( (HibernateProxy) object ).getHibernateLazyInitializer();
			Serializable id = li.getIdentifier();
			EntityPersister persister = source.getFactory().getEntityPersister( li.getEntityName() );
			if ( id == null ) {
				throw new IllegalArgumentException("null identifier");
			}
			EntityKey key = new EntityKey( id, persister, source.getEntityMode() );
			persistenceContext.removeProxy( key );
			if ( !li.isUninitialized() ) {
				final Object entity = persistenceContext.removeEntity(key);
				if ( entity != null ) {
					EntityEntry e = event.getSession().getPersistenceContext().removeEntry(entity);
					doEvict( entity, key, e.getPersister(), event.getSession() );
				}
			}
			li.setSession( null );
		}
		else {
			EntityEntry e = persistenceContext.removeEntry( object );
			if ( e != null ) {
				EntityKey key = new EntityKey( e.getId(), e.getPersister(), source.getEntityMode()  );
				persistenceContext.removeEntity( key );
				doEvict( object, key, e.getPersister(), source );
			}
			
		}
	}

	protected void doEvict(
		final Object object, 
		final EntityKey key, 
		final EntityPersister persister,
		final EventSource session) throws HibernateException {

		if ( log.isTraceEnabled() ) {
			log.trace( "evicting " + MessageHelper.infoString(persister) );
		}

		// remove all collections for the entity from the session-level cache
		if ( persister.hasCollections() ) {
			new EvictVisitor( session ).process( object, persister );
		}

		new Cascade( CascadingAction.EVICT, Cascade.AFTER_EVICT, session )
				.cascade( persister, object );
	}
}
