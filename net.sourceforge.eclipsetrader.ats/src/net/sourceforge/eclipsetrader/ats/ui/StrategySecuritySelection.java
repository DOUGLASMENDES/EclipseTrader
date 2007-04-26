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

import net.sourceforge.eclipsetrader.ats.core.runnables.ComponentRunnable;
import net.sourceforge.eclipsetrader.core.db.Security;

public class StrategySecuritySelection extends StrategySelection {
	Security security;

	public StrategySecuritySelection(ComponentRunnable runnable) {
		super(runnable.getParent());
		this.security = runnable.getSecurity();
	}

	public Security getSecurity() {
		return security;
	}
}
