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

/**
 * Event describing changes to views.
 *
 * @since 1.0
 */
public class ViewEvent {

    private IView view;
    private ViewItemDelta[] delta;

    /**
     * Constructor.
     *
     * @param view the source view.
     * @param delta the array of items that are changed.
     */
    public ViewEvent(IView view, ViewItemDelta[] delta) {
        this.view = view;
        this.delta = delta;
    }

    /**
     * Gets the view instance that generated the event.
     *
     * @return the source view.
     */
    public IView getView() {
        return view;
    }

    /**
     * Gets the array of changes occurrent in the view since the
     * last event was generted.
     *
     * @return the array of changes.
     */
    public ViewItemDelta[] getDelta() {
        return delta;
    }
}
