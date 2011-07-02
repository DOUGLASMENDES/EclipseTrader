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

package org.eclipsetrader.core.ats;

import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.TimeSpan;

public interface IBarFactory {

    public TimeSpan getTimeSpan();

    public IOHLC[] getBars(TimeSpan backfillTimeSpan);

    public void addBarFactoryListener(IBarFactoryListener listener);

    public void removeBarFactoryListener(IBarFactoryListener listener);

    public void dispose();
}
