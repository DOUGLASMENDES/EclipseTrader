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

package org.eclipsetrader.core.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Describes the changes occurred in a view item.
 *
 * @since 1.0
 */
public class ViewItemDelta {

    /**
     * Delta kind constant indicating that the item has not been changed in any way.
     */
    public static final int NO_CHANGE = 0;

    /**
     * Delta kind constant indicating that the item has been added to its parent.
     */
    public static final int ADDED = 0x1;

    /**
     * Delta kind constant indicating that the item has been removed from its parent.
     */
    public static final int REMOVED = 0x2;

    /**
     * Delta kind constant indicating that the item has been changed.
     */
    public static final int CHANGED = 0x4;

    private int kind = NO_CHANGE;
    private IViewItem viewItem;

    private IAdaptable[] oldValues;
    private IAdaptable[] newValues;

    private List<ViewItemDelta> childs = new ArrayList<ViewItemDelta>();

    protected ViewItemDelta() {
    }

    public ViewItemDelta(int kind, IViewItem viewItem) {
        this.kind = kind;
        this.viewItem = viewItem;
    }

    public ViewItemDelta(int kind, IViewItem viewItem, IAdaptable[] oldValues, IAdaptable[] newValues) {
        this.kind = kind;
        this.viewItem = viewItem;
        this.oldValues = oldValues;
        this.newValues = newValues;
    }

    /**
     * Returns the kind of this delta.
     * Normally, one of <code>ADDED</code>, <code>REMOVED</code>, <code>CHANGED</code>.
     *
     * @return the kind of this delta.
     */
    public int getKind() {
        return kind;
    }

    public IViewItem getViewItem() {
        return viewItem;
    }

    public int getChildCount() {
        return childs.size();
    }

    public ViewItemDelta[] getChilds() {
        return childs.toArray(new ViewItemDelta[childs.size()]);
    }

    public ViewItemDelta createChild(IViewItem viewItem, int kind) {
        ViewItemDelta delta = new ViewItemDelta(kind, viewItem);
        childs.add(delta);
        return delta;
    }

    public ViewItemDelta createChild(int kind, IViewItem viewItem, IAdaptable[] oldValues, IAdaptable[] newValues) {
        ViewItemDelta delta = new ViewItemDelta(kind, viewItem, oldValues, newValues);
        childs.add(delta);
        return delta;
    }

    /**
     * Gets the old values associated with the view item.
     * Can be <code>null</code> for <code>ADDED</code> items.
     *
     * @return the array of values.
     */
    public IAdaptable[] getOldValues() {
        return oldValues;
    }

    /**
     * Gets the new values associated with the view item.
     * Can be <code>null</code> for <code>REMOVED</code> items.
     *
     * @return the array of values.
     */
    public IAdaptable[] getNewValues() {
        return newValues;
    }
}
