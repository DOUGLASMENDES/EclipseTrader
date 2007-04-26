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

package net.sourceforge.eclipsetrader.ats.ui.report;

public class ContributionStatistic {
	String name = "";

	double grossProfit = 0;

	double grossLoss = 0;

	int winningTrades = 0;

	int losingTrades = 0;

	double totalAmount = 0;

	ContributionStatistic(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return name.equals(((ContributionStatistic) o).name);
	}
}
