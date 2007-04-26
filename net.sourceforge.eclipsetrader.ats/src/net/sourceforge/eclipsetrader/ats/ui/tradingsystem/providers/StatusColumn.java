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

import net.sourceforge.eclipsetrader.ats.core.runnables.StrategyRunnable;

import org.eclipse.jface.viewers.LabelProvider;

public class StatusColumn extends LabelProvider {

	public StatusColumn() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof StrategyRunnable)
			return ((StrategyRunnable) element).isRunning() ? "Running" : "Stopped";
		return "";
	}
}
