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

package net.sourceforge.eclipsetrader.ats.core.events;

/**
 * Default implementation of the IOrderListener interface.
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public class OrderAdapter implements IOrderListener {

	public OrderAdapter() {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.events.IOrderListener#orderCancelled(net.sourceforge.eclipsetrader.ats.core.events.OrderEvent)
	 */
	public void orderCancelled(OrderEvent e) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.events.IOrderListener#orderFilled(net.sourceforge.eclipsetrader.ats.core.events.OrderEvent)
	 */
	public void orderFilled(OrderEvent e) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.events.IOrderListener#orderRejected(net.sourceforge.eclipsetrader.ats.core.events.OrderEvent)
	 */
	public void orderRejected(OrderEvent e) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.events.IOrderListener#orderStatusChanged(net.sourceforge.eclipsetrader.ats.core.events.OrderEvent)
	 */
	public void orderStatusChanged(OrderEvent e) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.events.IOrderListener#orderSubmitted(net.sourceforge.eclipsetrader.ats.core.events.OrderEvent)
	 */
	public void orderSubmitted(OrderEvent e) {
	}
}
