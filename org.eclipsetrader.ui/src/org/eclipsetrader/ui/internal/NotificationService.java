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

package org.eclipsetrader.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.ui.INotification;
import org.eclipsetrader.ui.INotificationService;

public class NotificationService implements INotificationService {

    private List<INotification> queue = new ArrayList<INotification>();

    private Runnable notificationRunnable = new Runnable() {

        @Override
        public void run() {
            synchronized (queue) {
                NotificationPopup popup = new NotificationPopup(Display.getDefault());
                popup.setContents(queue);
                queue.clear();
                popup.setBlockOnOpen(false);
                popup.open();
            }
        }
    };

    public NotificationService() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.commons.INotificationService#popupNotification(org.eclipsetrader.ui.commons.INotification[])
     */
    @Override
    public void popupNotification(INotification[] notifications) {
        synchronized (queue) {
            if (queue.size() == 0 && notifications.length != 0) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        Display.getDefault().timerExec(2000, notificationRunnable);
                    }
                });
            }
            queue.addAll(Arrays.asList(notifications));
        }
    }
}
