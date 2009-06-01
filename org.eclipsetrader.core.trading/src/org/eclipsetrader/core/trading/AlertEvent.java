/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
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

public class AlertEvent {
	private ISecurity instrument;
	private IAlert[] alerts;

	public AlertEvent(ISecurity instrument, IAlert[] alerts) {
		this.instrument = instrument;
		this.alerts = alerts;
	}

	public ISecurity getInstrument() {
		return instrument;
	}

	public IAlert[] getTriggeredAlerts() {
		return alerts;
	}
}
