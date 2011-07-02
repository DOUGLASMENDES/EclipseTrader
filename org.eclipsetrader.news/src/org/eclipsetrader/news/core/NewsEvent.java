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

public class NewsEvent {

    private INewsService service;
    private HeadLineStatus[] status;

    public NewsEvent(INewsService service, HeadLineStatus[] status) {
        this.service = service;
        this.status = status;
    }

    public HeadLineStatus[] getStatus() {
        return status;
    }

    public INewsService getService() {
        return service;
    }
}
