//$
package org.hibernate.cfg.search;

import java.util.Properties;
import java.lang.reflect.Method;

import org.hibernate.util.ReflectHelper;
import org.hibernate.AnnotationException;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.EventListeners;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper methods initializing Hibernate Search event listeners
 *
 * @author Emmanuel Bernard
 */
public class SearchConfiguration {
	private static Log log = LogFactory.getLog( SearchConfiguration.class );

	public static void enableHibernateSearch(EventListeners eventListeners, Properties properties) {
		//add search events if the jar is available
		boolean enableSearchListeners = !"false".equalsIgnoreCase( properties.getProperty( "hibernate.search.autoregister_listeners" ) );
		boolean enableCollectionSearchListeners = false;
		Class searchEventListenerClass = null;
		Class nonCollectionSearchEventListener = null;
		try {
			searchEventListenerClass = ReflectHelper.classForName(
					"org.hibernate.search.event.FullTextIndexEventListener",
					SearchConfiguration.class );
			nonCollectionSearchEventListener = searchEventListenerClass;
			//after Hibernate Core 3.2.6 and Hibernate Search 3.0.1
			try {
				ReflectHelper.classForName(
						"org.hibernate.event.AbstractCollectionEvent",
						SearchConfiguration.class );
				// Core 3.2.6 is here
				searchEventListenerClass = ReflectHelper.classForName(
						"org.hibernate.search.event.FullTextIndexCollectionEventListener",
						SearchConfiguration.class );
				// Search 3.0.1 is here
				enableCollectionSearchListeners = true;

			}
			catch (ClassNotFoundException e) {
				//collection listeners not present
				log.debug( "Hibernate Search collection listeners not present " +
						"(upgrate to Hibernate Core 3.2.6 and above and Hibernate Search 3.0.1 and above)" );
			}
		}
		catch (ClassNotFoundException e) {
			//search is not present
			log.debug( "Search not present in classpath, ignoring event listener registration" );
		}
		if ( enableSearchListeners && searchEventListenerClass != null ) {
			//TODO so much duplication
			Object searchEventListener;
			try {
				searchEventListener = searchEventListenerClass.newInstance();
			}
			catch (Exception e) {
				throw new AnnotationException( "Unable to load Search event listener", e );
			}
			{
				boolean present = false;
				PostInsertEventListener[] listeners = eventListeners.getPostInsertEventListeners();
				if ( listeners != null ) {
					for (Object eventListener : listeners) {
						//not isAssignableFrom since the user could subclass
						present = present ||
								searchEventListenerClass == eventListener.getClass() ||
								( enableCollectionSearchListeners && nonCollectionSearchEventListener == eventListener.getClass() );
					}
					if ( !present ) {
						int length = listeners.length + 1;
						PostInsertEventListener[] newListeners = new PostInsertEventListener[length];
						System.arraycopy( listeners, 0, newListeners, 0, length - 1 );
						newListeners[length - 1] = (PostInsertEventListener) searchEventListener;
						eventListeners.setPostInsertEventListeners( newListeners );
					}
				}
				else {
					eventListeners.setPostInsertEventListeners(
							new PostInsertEventListener[] { (PostInsertEventListener) searchEventListener }
					);
				}
			}
			{
				boolean present = false;
				PostUpdateEventListener[] listeners = eventListeners.getPostUpdateEventListeners();
				if ( listeners != null ) {
					for (Object eventListener : listeners) {
						//not isAssignableFrom since the user could subclass
						present = present ||
								searchEventListenerClass == eventListener.getClass() ||
								( enableCollectionSearchListeners && nonCollectionSearchEventListener == eventListener.getClass() );
					}
					if ( !present ) {
						int length = listeners.length + 1;
						PostUpdateEventListener[] newListeners = new PostUpdateEventListener[length];
						System.arraycopy( listeners, 0, newListeners, 0, length - 1 );
						newListeners[length - 1] = (PostUpdateEventListener) searchEventListener;
						eventListeners.setPostUpdateEventListeners( newListeners );
					}
				}
				else {
					eventListeners.setPostUpdateEventListeners(
							new PostUpdateEventListener[] { (PostUpdateEventListener) searchEventListener }
					);
				}
			}
			{
				boolean present = false;
				PostDeleteEventListener[] listeners = eventListeners.getPostDeleteEventListeners();
				if ( listeners != null ) {
					for (Object eventListener : listeners) {
						//not isAssignableFrom since the user could subclass
						present = present ||
								searchEventListenerClass == eventListener.getClass() ||
								( enableCollectionSearchListeners && nonCollectionSearchEventListener == eventListener.getClass() );
					}
					if ( !present ) {
						int length = listeners.length + 1;
						PostDeleteEventListener[] newListeners = new PostDeleteEventListener[length];
						System.arraycopy( listeners, 0, newListeners, 0, length - 1 );
						newListeners[length - 1] = (PostDeleteEventListener) searchEventListener;
						eventListeners.setPostDeleteEventListeners( newListeners );
					}
				}
				else {
					eventListeners.setPostDeleteEventListeners(
							new PostDeleteEventListener[] { (PostDeleteEventListener) searchEventListener }
					);
				}
			}
			if (enableCollectionSearchListeners) {
				try {
					Class collectionSearchConfigurationClass = ReflectHelper.classForName(
						"org.hibernate.cfg.search.CollectionSearchConfiguration",
						SearchConfiguration.class );
					Method method = collectionSearchConfigurationClass.getDeclaredMethod( "enableHibernateSearch",
							EventListeners.class, Object.class, Class.class );
					method.invoke( null, eventListeners, searchEventListener, searchEventListenerClass );
				}
				catch (Exception e) {
					throw new AnnotationException( "Unable to load Search event listener", e );
				}
			}
		}
	}
}
