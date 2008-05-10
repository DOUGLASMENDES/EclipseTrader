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

package org.eclipsetrader.news.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.news.core.IHeadLine;
import org.eclipsetrader.news.core.INewsService;
import org.eclipsetrader.news.core.INewsServiceListener;
import org.eclipsetrader.news.internal.repository.HeadLine;

public class NewsService implements INewsService, Runnable {
	private List<HeadLine> headLines = new ArrayList<HeadLine>();
	private Map<ISecurity, List<HeadLine>> securityMap = new HashMap<ISecurity, List<HeadLine>>();
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	public NewsService() {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.news.core.INewsService#getHeadLines()
	 */
	public IHeadLine[] getHeadLines() {
		return headLines.toArray(new IHeadLine[headLines.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.news.core.INewsService#getHeadLinesFor(org.eclipsetrader.core.instruments.ISecurity)
	 */
	public IHeadLine[] getHeadLinesFor(ISecurity security) {
		List<HeadLine> l = securityMap.get(security);
		return l != null ? l.toArray(new IHeadLine[l.size()]) : new IHeadLine[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.news.core.INewsService#hasHeadLinesFor(org.eclipsetrader.core.instruments.ISecurity)
	 */
	public boolean hasHeadLinesFor(ISecurity security) {
		List<HeadLine> l = securityMap.get(security);
		return l != null && l.size() != 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.news.core.INewsService#hasUnreadedHeadLinesFor(org.eclipsetrader.core.instruments.ISecurity)
	 */
	public boolean hasUnreadedHeadLinesFor(ISecurity security) {
		List<HeadLine> l = securityMap.get(security);
		if (l != null) {
			for (HeadLine h : l) {
				if (!h.isReaded())
					return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.news.core.INewsService#addNewsServiceListener(org.eclipsetrader.news.core.INewsServiceListener)
	 */
	public void addNewsServiceListener(INewsServiceListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.news.core.INewsService#removeNewsServiceListener(org.eclipsetrader.news.core.INewsServiceListener)
	 */
	public void removeNewsServiceListener(INewsServiceListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
    }
}
