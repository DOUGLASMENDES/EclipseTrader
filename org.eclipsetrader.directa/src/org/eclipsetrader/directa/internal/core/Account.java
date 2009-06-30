/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.directa.internal.core;

import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.trading.IPositionListener;
import org.eclipsetrader.core.trading.ITransaction;

public class Account implements IAccount {
	String id;
	String name;

	public Account(String id) {
		this.id = id;
	}

	public Account(String id, String name) {
		this.id = id;
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IAccount#getDescription()
	 */
	public String getDescription() {
		return name != null ? name : id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IAccount#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IAccount#addPositionListener(org.eclipsetrader.core.trading.IPositionListener)
	 */
	public void addPositionListener(IPositionListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IAccount#removePositionListener(org.eclipsetrader.core.trading.IPositionListener)
	 */
	public void removePositionListener(IPositionListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IAccount#getPositions()
	 */
	public IPosition[] getPositions() {
		return new IPosition[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IAccount#getTransactions()
	 */
	public ITransaction[] getTransactions() {
		return new ITransaction[0];
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Account))
			return false;
		return id.equals(((Account) obj).id);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 11 * id.hashCode();
	}
}
