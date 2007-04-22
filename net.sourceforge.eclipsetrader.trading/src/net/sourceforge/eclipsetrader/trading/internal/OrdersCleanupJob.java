/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.trading.internal;

import java.util.Calendar;
import java.util.Date;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Order;
import net.sourceforge.eclipsetrader.core.db.OrderStatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;

public class OrdersCleanupJob extends Job
{
    Log log = LogFactory.getLog(getClass());

    public OrdersCleanupJob()
    {
        super("Orders Cleanup");
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected IStatus run(IProgressMonitor monitor)
    {
        IPreferenceStore preferences = CorePlugin.getDefault().getPreferenceStore(); 

        log.debug("Start orders cleanup");
        
        Date today = Calendar.getInstance().getTime();
        Order[] orders = (Order[])CorePlugin.getRepository().allOrders().toArray(new Order[0]);
        for (int i = 0; i < orders.length; i++)
        {
            if (orders[i].getStatus().equals(OrderStatus.CANCELED) || orders[i].getStatus().equals(OrderStatus.REJECTED))
            {
                long diff = (today.getTime() - orders[i].getDate().getTime()) / (1000 * 60 * 60 * 24); 
                if (preferences.getBoolean(CorePlugin.PREFS_DELETE_CANCELED_ORDERS) && diff >= preferences.getInt(CorePlugin.PREFS_DELETE_CANCELED_ORDERS_DAYS))
                {
                    if (orders[i].getProvider() != null)
                        log.info("Deleting order " + CorePlugin.getPluginName(CorePlugin.TRADING_PROVIDERS_EXTENSION_POINT, orders[i].getPluginId()) + " / " + orders[i].getOrderId());
                    else
                        log.info("Deleting order " + orders[i].getOrderId());
                    CorePlugin.getRepository().delete(orders[i]);
                }
            }
            else if (orders[i].getStatus().equals(OrderStatus.FILLED))
            {
                long diff = (today.getTime() - orders[i].getDate().getTime()) / (1000 * 60 * 60 * 24); 
                if (preferences.getBoolean(CorePlugin.PREFS_DELETE_FILLED_ORDERS) && diff >= preferences.getInt(CorePlugin.PREFS_DELETE_FILLED_ORDERS_DAYS))
                {
                    if (orders[i].getProvider() != null)
                        log.info("Deleting order " + CorePlugin.getPluginName(CorePlugin.TRADING_PROVIDERS_EXTENSION_POINT, orders[i].getPluginId()) + " / " + orders[i].getOrderId());
                    else
                        log.info("Deleting order " + orders[i].getOrderId());
                    CorePlugin.getRepository().delete(orders[i]);
                }
            }
        }

        log.debug("Orders cleanup completed");
        
        return Status.OK_STATUS;
    }
}
