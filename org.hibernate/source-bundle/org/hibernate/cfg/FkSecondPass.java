//$Id: FkSecondPass.java 11650 2007-06-07 16:44:52Z epbernard $
package org.hibernate.cfg;

import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Value;

/**
 * @author Emmanuel Bernard
 */
public abstract class FkSecondPass implements SecondPass {
	protected SimpleValue value;
	protected Ejb3JoinColumn[] columns;
	/**
	 * unique counter is needed to differenciate 2 instances of FKSecondPass
	 * as they are compared.
	 * Fairly hacky but IBM VM sometimes returns the same hashCode for 2 different objects
	 * TODO is it doable to rely on the Ejb3JoinColumn names? Not sure at they could be inferred
	 */
	private int uniqueCounter;
	private static AtomicInteger globalCounter = new AtomicInteger();

	public FkSecondPass(SimpleValue value, Ejb3JoinColumn[] columns) {
		this.value = value;
		this.columns = columns;
		this.uniqueCounter = globalCounter.getAndIncrement();
	}

	public int getUniqueCounter() {
		return uniqueCounter;
	}

	public Value getValue() {
		return value;
	}

	public boolean equals(Object o) {
		if ( this == o ) return true;
		if ( !( o instanceof FkSecondPass ) ) return false;

		FkSecondPass that = (FkSecondPass) o;

		if ( uniqueCounter != that.uniqueCounter ) return false;

		return true;
	}

	public int hashCode() {
		return uniqueCounter;
	}

	public abstract String getReferencedEntityName();

	public abstract boolean isInPrimaryKey();
}
