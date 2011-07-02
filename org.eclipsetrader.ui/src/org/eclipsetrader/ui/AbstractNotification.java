/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipsetrader.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Image;

public abstract class AbstractNotification implements INotification, IAdaptable, Comparable<AbstractNotification> {

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.commons.INotification#getDescription()
     */
    @Override
    public String getDescription() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.commons.INotification#getLabel()
     */
    @Override
    public String getLabel() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.commons.INotification#getNotificationImage()
     */
    @Override
    public Image getNotificationImage() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.commons.INotification#open()
     */
    @Override
    public void open() {
    }
}
