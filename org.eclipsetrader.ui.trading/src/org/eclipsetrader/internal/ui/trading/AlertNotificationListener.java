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

package org.eclipsetrader.internal.ui.trading;

import org.eclipse.osgi.util.NLS;
import org.eclipsetrader.core.trading.AlertEvent;
import org.eclipsetrader.core.trading.IAlert;
import org.eclipsetrader.core.trading.IAlertListener;
import org.eclipsetrader.ui.INotification;
import org.eclipsetrader.ui.INotificationService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class AlertNotificationListener implements IAlertListener {

    public AlertNotificationListener() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlertListener#alertTriggered(org.eclipsetrader.core.trading.AlertEvent)
     */
    @Override
    public void alertTriggered(AlertEvent event) {
        IAlert[] alerts = event.getTriggeredAlerts();
        if (alerts.length == 0) {
            return;
        }

        AlertNotification notification = new AlertNotification();
        notification.setSecurity(event.getInstrument());

        String description = alerts[0].getDescription();
        if (alerts.length > 1) {
            description += NLS.bind(Messages.AlertNotificationListener_MoreAlerts, new Object[] {
                String.valueOf(alerts.length - 1)
            });
        }
        notification.setDescription(description);

        BundleContext context = Activator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(INotificationService.class.getName());
        if (serviceReference != null) {
            INotificationService notificationService = (INotificationService) context.getService(serviceReference);
            notificationService.popupNotification(new INotification[] {
                notification
            });
        }
        context.ungetService(serviceReference);
    }
}
