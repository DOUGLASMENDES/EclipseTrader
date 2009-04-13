//$Id: CacheConcurrencyStrategy.java 11282 2007-03-14 22:05:59Z epbernard $
package org.hibernate.annotations;

/**
 * Cache concurrency strategy
 *
 * @author Emmanuel Bernard
 */
public enum CacheConcurrencyStrategy {
	NONE,
	READ_ONLY,
	NONSTRICT_READ_WRITE,
	READ_WRITE,
	TRANSACTIONAL
}
