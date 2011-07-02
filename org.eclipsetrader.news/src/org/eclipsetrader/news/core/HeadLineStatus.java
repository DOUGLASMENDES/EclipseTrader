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

public class HeadLineStatus {

    public static final int UPDATED = 0;
    public static final int ADDED = 1;
    public static final int REMOVED = 2;

    private int kind;
    private IHeadLine headLine;

    public HeadLineStatus(int kind, IHeadLine headLine) {
        this.kind = kind;
        this.headLine = headLine;
    }

    public int getKind() {
        return kind;
    }

    public IHeadLine getHeadLine() {
        return headLine;
    }
}
