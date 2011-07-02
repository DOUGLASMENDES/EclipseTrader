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

package org.eclipsetrader.news.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipsetrader.news.internal.ui.HeadLineViewer;
import org.eclipsetrader.ui.INotification;

public class HeadLineCountNotification implements INotification, Comparable<HeadLineCountNotification> {

    private static final String LABEL_TEXT = "{0}";
    private static final String DESCRIPTION_TEXT = "News has {0} new unreaded headline(s)";

    private String label;
    private String description;

    public HeadLineCountNotification(int count) {
        this.label = NLS.bind(LABEL_TEXT, new Object[] {
            "News"
        });
        this.description = NLS.bind(DESCRIPTION_TEXT, new Object[] {
            count
        });
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.commons.INotification#getLabel()
     */
    @Override
    public String getLabel() {
        return label;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.commons.INotification#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.commons.INotification#getNotificationImage()
     */
    @Override
    public Image getNotificationImage() {
        return Activator.getDefault().getImageRegistry().get("normal_icon");
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.commons.INotification#open()
     */
    @Override
    public void open() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
            page.showView(HeadLineViewer.VIEW_ID);
        } catch (PartInitException e) {
            Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Unexpected error activating view", null);
            Activator.log(status);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(HeadLineCountNotification o) {
        return 0;
    }
}
