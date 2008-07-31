/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.ats;

import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;

public interface ITradeSystem {

	public boolean isActive();

	public ISecurity getInstrument();

	public TimeSpan getTimeSpan();

	public ITradeStrategy getTradeStrategy();

	public ITradeSystemParameter[] getParameters();
}
