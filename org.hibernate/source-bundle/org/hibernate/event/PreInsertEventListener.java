//$Id: PreInsertEventListener.java 11271 2007-03-12 00:16:48Z epbernard $
package org.hibernate.event;

import java.io.Serializable;

/**
 * Called before inserting an item in the datastore
 * 
 * @author Gavin King
 */
public interface PreInsertEventListener extends Serializable {
	/**
	 * Return true if the operation should be vetoed
	 */
	public boolean onPreInsert(PreInsertEvent event);
}
