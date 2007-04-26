/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.ats.ui;

import net.sourceforge.eclipsetrader.ats.core.db.TradingSystem;
import net.sourceforge.eclipsetrader.ats.core.runnables.TradingSystemRunnable;
import net.sourceforge.eclipsetrader.core.db.Account;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;

public class TradingSystemSelection implements ISelection {
	TradingSystem system;

	Account account;

	public TradingSystemSelection(TradingSystemRunnable runnable) {
		Assert.isNotNull(runnable);
		this.system = runnable.getTradingSystem();
		this.account = system.getAccount();
	}

	public TradingSystemSelection(TradingSystemRunnable runnable, Account account) {
		Assert.isNotNull(runnable);
		Assert.isNotNull(account);
		this.system = runnable.getTradingSystem();
		this.account = account;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelection#isEmpty()
	 */
	public boolean isEmpty() {
		return false;
	}

	public TradingSystem getTradingSystem() {
		return system;
	}

	public Account getAccount() {
		return account;
	}
}
