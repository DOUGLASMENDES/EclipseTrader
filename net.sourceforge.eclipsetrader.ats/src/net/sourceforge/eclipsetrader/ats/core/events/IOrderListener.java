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

public interface IOrderListener {

	public void orderSubmitted(OrderEvent e);

	public void orderCancelled(OrderEvent e);

	public void orderFilled(OrderEvent e);

	public void orderRejected(OrderEvent e);

	public void orderStatusChanged(OrderEvent e);
}
