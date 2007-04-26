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

package net.sourceforge.eclipsetrader.ats.ui.tradingsystem.providers;

import java.text.NumberFormat;

import net.sourceforge.eclipsetrader.ats.core.runnables.ComponentRunnable;
import net.sourceforge.eclipsetrader.core.db.Account;

import org.eclipse.jface.viewers.LabelProvider;

public class PositionColumn extends LabelProvider {
	private NumberFormat numberFormat = NumberFormat.getInstance();

	public PositionColumn() {
		numberFormat.setGroupingUsed(true);
		numberFormat.setMinimumIntegerDigits(1);
		numberFormat.setMinimumFractionDigits(0);
		numberFormat.setMaximumFractionDigits(0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof ComponentRunnable) {
			Account account = ((ComponentRunnable) element).getParent().getParent().getTradingSystem().getAccount();
			int position = account.getPosition(((ComponentRunnable) element).getSecurity());
			return numberFormat.format(position);
		}
		return "";
	}
}
