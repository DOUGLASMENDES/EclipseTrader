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

package net.sourceforge.eclipsetrader.ats.core;

import net.sourceforge.eclipsetrader.ats.core.events.IBarListener;
import net.sourceforge.eclipsetrader.ats.core.events.IMarketListener;
import net.sourceforge.eclipsetrader.ats.core.events.IOrderListener;
import net.sourceforge.eclipsetrader.ats.core.events.IPositionListener;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Order;
import net.sourceforge.eclipsetrader.core.db.Security;

public interface IComponentContext {

	public Security getSecurity();

	public Bar[] getBars();

	public void addMarketListener(IMarketListener l);

	public void removeMarketListener(IMarketListener l);

	public void addBarListener(IBarListener l);

	public void removeBarListener(IBarListener l);

	public int getPosition();

	public double getPositionValue();

	public void addOrderListener(IOrderListener listener);

	public void removeOrderListener(IOrderListener listener);

	public void addPositionListener(IPositionListener listener);

	public void removePositionListener(IPositionListener listener);

	public Order createOrder(Signal signal);
}
