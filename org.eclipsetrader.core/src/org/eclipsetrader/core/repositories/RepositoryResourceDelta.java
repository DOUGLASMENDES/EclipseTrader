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

package org.eclipsetrader.core.repositories;

public class RepositoryResourceDelta {

    public static final int NO_CHANGE = 0;
    public static final int ADDED = 0x1;
    public static final int REMOVED = 0x2;
    public static final int CHANGED = 0x4;
    public static final int MOVED_FROM = 0x1000;
    public static final int MOVED_TO = 0x2000;

    private int kind = NO_CHANGE;
    private Object resource;
    private IRepository movedFrom;
    private IRepository movedTo;
    private IStoreProperties oldProperties;
    private IStoreProperties newProperties;

    protected RepositoryResourceDelta() {
    }

    public RepositoryResourceDelta(int kind, Object resource, IRepository movedFrom, IRepository movedTo, IStoreProperties oldProperties, IStoreProperties newProperties) {
        this.kind = kind;
        this.resource = resource;
        this.movedFrom = movedFrom;
        this.movedTo = movedTo;
        this.oldProperties = oldProperties;
        this.newProperties = newProperties;
    }

    public int getKind() {
        return kind;
    }

    public Object getResource() {
        return resource;
    }

    public IRepository getMovedFrom() {
        return movedFrom;
    }

    public IRepository getMovedTo() {
        return movedTo;
    }

    public IStoreProperties getOldProperties() {
        return oldProperties;
    }

    public IStoreProperties getNewProperties() {
        return newProperties;
    }
}
