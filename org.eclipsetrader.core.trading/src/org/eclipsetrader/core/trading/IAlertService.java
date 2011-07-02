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

public interface IAlertService {

    public void addAlertListener(IAlertListener l);

    public void removeAlertListener(IAlertListener l);

    public void resetTrigger(IAlert alert);

    public void resetAllTriggers();

    public IAlert[] getAlerts(ISecurity instrument);

    public void setAlerts(ISecurity instrument, IAlert[] alerts);

    public boolean hasTriggeredAlerts(ISecurity instrument);

    public IAlert[] getTriggeredAlerts(ISecurity instrument);

    public void resetTriggers(ISecurity instrument);
}
