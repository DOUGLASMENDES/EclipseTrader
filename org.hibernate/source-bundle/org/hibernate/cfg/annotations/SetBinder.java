package org.hibernate.cfg.annotations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.OrderBy;
import org.hibernate.cfg.Environment;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;

/**
 * Bind a set.
 *
 * @author Matthew Inger
 */
public class SetBinder extends CollectionBinder {
	private static Log log = LogFactory.getLog( SetBinder.class );

	public SetBinder() {
	}

	public SetBinder(boolean sorted) {
		super( sorted );
	}

	protected Collection createCollection(PersistentClass persistentClass) {
		return new org.hibernate.mapping.Set( persistentClass );
	}

	public void setSqlOrderBy(OrderBy orderByAnn) {
		if ( orderByAnn != null ) {
			if ( Environment.jvmSupportsLinkedHashCollections() ) {
				super.setSqlOrderBy( orderByAnn );
			}
			else {
				log.warn( "Attribute \"order-by\" ignored in JDK1.3 or less" );
			}
		}
	}
}
