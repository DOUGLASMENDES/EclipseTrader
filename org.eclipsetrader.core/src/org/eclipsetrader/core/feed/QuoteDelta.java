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

public class QuoteDelta {

    private IFeedIdentifier identifier;
    private Object oldValue;
    private Object newValue;

    public QuoteDelta(IFeedIdentifier identifier, Object oldValue, Object newValue) {
        this.identifier = identifier;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public IFeedIdentifier getIdentifier() {
        return identifier;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }
}
