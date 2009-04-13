//$Id: PrimitiveArrayBinder.java 11282 2007-03-14 22:05:59Z epbernard $
package org.hibernate.cfg.annotations;

import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.PrimitiveArray;

/**
 * @author Emmanuel Bernard
 */
public class PrimitiveArrayBinder extends ArrayBinder {
	@Override
	protected Collection createCollection(PersistentClass persistentClass) {
		return new PrimitiveArray( persistentClass );
	}
}
