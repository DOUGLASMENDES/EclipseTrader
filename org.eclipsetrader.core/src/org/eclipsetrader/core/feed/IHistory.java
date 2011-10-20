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

package org.eclipsetrader.core.feed;

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.instruments.ISecurity;

public interface IHistory extends IAdaptable {

    public ISecurity getSecurity();

    public IOHLC[] getOHLC();

    public IOHLC getHighest();

    public IOHLC getLowest();

    public IOHLC getFirst();

    public IOHLC getLast();

    public IHistory getSubset(Date first, Date last);

    public IHistory getSubset(Date first, Date last, TimeSpan aggregation);

    public TimeSpan getTimeSpan();

    public ISplit[] getSplits();

    public IOHLC[] getAdjustedOHLC();

    public IHistory[] getDay(Date date);
}
