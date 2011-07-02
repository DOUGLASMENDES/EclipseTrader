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

package org.eclipsetrader.news.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipsetrader.core.instruments.ISecurity;

public interface INewsService {

    public IHeadLine[] getHeadLines();

    public IHeadLine[] getHeadLinesFor(ISecurity security);

    public boolean hasHeadLinesFor(ISecurity security);

    public boolean hasUnreadedHeadLinesFor(ISecurity security);

    public void addNewsServiceListener(INewsServiceListener listener);

    public void removeNewsServiceListener(INewsServiceListener listener);

    public void addHeadLines(IHeadLine[] headlines);

    public void removeHeadLines(IHeadLine[] headlines);

    public void updateHeadLines(IHeadLine[] headlines);

    public IStatus runInService(INewsServiceRunnable runnable, IProgressMonitor monitor);

    public IStatus runInService(INewsServiceRunnable runnable, ISchedulingRule rule, IProgressMonitor monitor);

    public INewsProvider[] getProviders();
}
