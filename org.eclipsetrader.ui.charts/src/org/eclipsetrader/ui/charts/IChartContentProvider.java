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

package org.eclipsetrader.ui.charts;


public interface IChartContentProvider {

	/**
     * Disposes of this content provider.
     * This is called by the viewer when it is disposed.
     * <p>
     * The viewer should not be updated during this call, as it is in the process
     * of being disposed.
     * </p>
     */
    public void dispose();

    /**
     * Returns the elements to display in the viewer
     * when its input is set to the given element.
     * The result is not modified by the viewer.
     *
     * @param inputElement the input element
     * @return the array of elements to display in the viewer
     */
	public Object[] getElements(Object inputElement);

    /**
     * Notifies this content provider that the given viewer's input
     * has been switched to a different element.
     * <p>
     * A typical use for this method is registering the content provider as a listener
     * to changes on the new input (using model-specific means), and deregistering the viewer
     * from the old input. In response to these change notifications, the content provider
     * should update the viewer (see the add, remove, update and refresh methods on the viewers).
     * </p>
     * <p>
     * The viewer should not be updated during this call, as it might be in the process
     * of being disposed.
     * </p>
     *
     * @param viewer the viewer
     * @param oldInput the old input element, or <code>null</code> if the viewer did not previously have an input
     * @param newInput the new input element, or <code>null</code> if the viewer does not have an input
     */
	public void inputChanged(ChartViewer viewer, Object oldInput, Object newInput);

	public Object[] getChildren(Object parentElement);
}
