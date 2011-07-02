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

package org.eclipsetrader.ui;

import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IComparableContribution;

public class PriorityPreferenceNode extends PreferenceNode implements IComparableContribution {

    private int priority;

    public PriorityPreferenceNode(String id, int priority) {
        super(id);
        this.priority = priority;
    }

    public PriorityPreferenceNode(String id, IPreferencePage preferencePage, int priority) {
        super(id, preferencePage);
        this.priority = priority;
    }

    public PriorityPreferenceNode(String id, String label, ImageDescriptor image, String className, int priority) {
        super(id, label, image, className);
        this.priority = priority;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IComparableContribution#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IComparableContribution#getLabel()
     */
    @Override
    public String getLabel() {
        return getLabelText();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IComparableContribution#getPriority()
     */
    @Override
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
