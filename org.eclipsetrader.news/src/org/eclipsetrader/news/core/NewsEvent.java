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

public class NewsEvent {
	private INewsService service;
	private IHeadLine[] addedHeadLines;
	private IHeadLine[] removedHeadLines;

	public NewsEvent(INewsService service, IHeadLine[] addedHeadLines, IHeadLine[] removedHeadLines) {
	    this.service = service;
	    this.addedHeadLines = addedHeadLines;
	    this.removedHeadLines = removedHeadLines;
    }

	public IHeadLine[] getAddedHeadLines() {
    	return addedHeadLines;
    }

	public IHeadLine[] getRemovedHeadLines() {
    	return removedHeadLines;
    }

	public INewsService getService() {
    	return service;
    }
}
