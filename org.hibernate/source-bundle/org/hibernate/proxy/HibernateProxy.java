package org.hibernate.proxy;

import java.io.Serializable;

/**
 * Marker interface for entity proxies
 *
 * @author Gavin King
 */
public interface HibernateProxy extends Serializable {
	/**
	 * Perform serialization-time write-replacement of this proxy.
	 *
	 * @return The serializable proxy replacement.
	 */
	public Object writeReplace();

	/**
	 * Get the underlying lazy initialization handler.
	 *
	 * @return The lazy initializer.
	 */
	public LazyInitializer getHibernateLazyInitializer();
}







