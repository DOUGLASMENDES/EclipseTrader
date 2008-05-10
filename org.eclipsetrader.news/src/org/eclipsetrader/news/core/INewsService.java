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

package org.eclipsetrader.news.core;

import org.eclipsetrader.core.instruments.ISecurity;

public interface INewsService {

	public IHeadLine[] getHeadLines();

	public IHeadLine[] getHeadLinesFor(ISecurity security);

	public boolean hasHeadLinesFor(ISecurity security);

	public boolean hasUnreadedHeadLinesFor(ISecurity security);

	public void addNewsServiceListener(INewsServiceListener listener);

	public void removeNewsServiceListener(INewsServiceListener listener);
}
