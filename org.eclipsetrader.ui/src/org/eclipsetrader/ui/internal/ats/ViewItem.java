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

package org.eclipsetrader.ui.internal.ats;

import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.runtime.IAdaptable;

public interface ViewItem extends IAdaptable {

    public ViewItem getParent();

    public boolean hasChildren();

    public ObservableList getItems();

    public Object getValue(String name);

    public void putValue(String name, Object value);

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);
}
