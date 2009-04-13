//$
package org.hibernate.cfg.search;

import org.hibernate.event.EventListeners;
import org.hibernate.event.PostCollectionRecreateEventListener;
import org.hibernate.event.PostCollectionRemoveEventListener;
import org.hibernate.event.PostCollectionUpdateEventListener;

/**
 * Enable collection event listeners for Hibernate Search
 *
 * @author Emmanuel Bernard
 */
public class CollectionSearchConfiguration {
	public static void enableHibernateSearch(EventListeners eventListeners, Object searchEventListener, Class searchEventListenerClass) {
		{
			boolean present = false;
			PostCollectionRecreateEventListener[] listeners = eventListeners.getPostCollectionRecreateEventListeners();
			if ( listeners != null ) {
				for (Object eventListener : listeners) {
					//not isAssignableFrom since the user could subclass
					present = present || searchEventListenerClass == eventListener.getClass();
				}
				if ( !present ) {
					int length = listeners.length + 1;
					PostCollectionRecreateEventListener[] newListeners = new PostCollectionRecreateEventListener[length];
					System.arraycopy( listeners, 0, newListeners, 0, length - 1 );
					newListeners[length - 1] = (PostCollectionRecreateEventListener) searchEventListener;
					eventListeners.setPostCollectionRecreateEventListeners( newListeners );
				}
			}
			else {
				eventListeners.setPostCollectionRecreateEventListeners(
						new PostCollectionRecreateEventListener[] { (PostCollectionRecreateEventListener) searchEventListener }
				);
			}
		}
		{
			boolean present = false;
			PostCollectionRemoveEventListener[] listeners = eventListeners.getPostCollectionRemoveEventListeners();
			if ( listeners != null ) {
				for (Object eventListener : listeners) {
					//not isAssignableFrom since the user could subclass
					present = present || searchEventListenerClass == eventListener.getClass();
				}
				if ( !present ) {
					int length = listeners.length + 1;
					PostCollectionRemoveEventListener[] newListeners = new PostCollectionRemoveEventListener[length];
					System.arraycopy( listeners, 0, newListeners, 0, length - 1 );
					newListeners[length - 1] = (PostCollectionRemoveEventListener) searchEventListener;
					eventListeners.setPostCollectionRemoveEventListeners( newListeners );
				}
			}
			else {
				eventListeners.setPostCollectionRemoveEventListeners(
						new PostCollectionRemoveEventListener[] { (PostCollectionRemoveEventListener) searchEventListener }
				);
			}
		}
		{
			boolean present = false;
			PostCollectionUpdateEventListener[] listeners = eventListeners.getPostCollectionUpdateEventListeners();
			if ( listeners != null ) {
				for (Object eventListener : listeners) {
					//not isAssignableFrom since the user could subclass
					present = present || searchEventListenerClass == eventListener.getClass();
				}
				if ( !present ) {
					int length = listeners.length + 1;
					PostCollectionUpdateEventListener[] newListeners = new PostCollectionUpdateEventListener[length];
					System.arraycopy( listeners, 0, newListeners, 0, length - 1 );
					newListeners[length - 1] = (PostCollectionUpdateEventListener) searchEventListener;
					eventListeners.setPostCollectionUpdateEventListeners( newListeners );
				}
			}
			else {
				eventListeners.setPostCollectionUpdateEventListeners(
						new PostCollectionUpdateEventListener[] { (PostCollectionUpdateEventListener) searchEventListener }
				);
			}
		}
	}
}
