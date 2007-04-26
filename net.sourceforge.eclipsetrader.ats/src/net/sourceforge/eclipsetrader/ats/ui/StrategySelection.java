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

import net.sourceforge.eclipsetrader.ats.core.db.Strategy;
import net.sourceforge.eclipsetrader.ats.core.runnables.StrategyRunnable;

public class StrategySelection extends TradingSystemSelection {
	Strategy strategy;

	public StrategySelection(StrategyRunnable runnable) {
		super(runnable.getParent());
		this.strategy = runnable.getStrategy();
	}

	public Strategy getStrategy() {
		return strategy;
	}
}
