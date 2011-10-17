/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.trading;

import org.eclipsetrader.core.instruments.ISecurity;

public interface ITradingService {

    public IBroker[] getBrokers();

    public IBroker getBroker(String id);

    public IBroker getBrokerForSecurity(ISecurity security);

    public IOrderMonitor[] getOrders();

    public void addOrderChangeListener(IOrderChangeListener listener);

    public void removeOrderChangeListener(IOrderChangeListener listener);

    public void addPositionListener(IPositionListener listener);

    public void removePositionListener(IPositionListener listener);
}
