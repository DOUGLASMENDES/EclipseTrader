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

package org.eclipsetrader.ui.internal.trading;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.ui.INotification;

public class AlertNotification implements INotification, Comparable<INotification> {

    private static final String LABEL_TEXT = Messages.AlertNotification_Label;

    private ISecurity security;
    private String description;

    public AlertNotification() {
    }

    public void setSecurity(ISecurity security) {
        this.security = security;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.INotification#getLabel()
     */
    @Override
    public String getLabel() {
        return NLS.bind(LABEL_TEXT, new Object[] {
            security.getName()
        });
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.INotification#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.INotification#getNotificationImage()
     */
    @Override
    public Image getNotificationImage() {
        return Activator.getDefault().getImageRegistry().get(Activator.ALERT_NOTIFICATION_IMAGE);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.INotification#open()
     */
    @Override
    public void open() {
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(INotification o) {
        return 0;
    }
}
