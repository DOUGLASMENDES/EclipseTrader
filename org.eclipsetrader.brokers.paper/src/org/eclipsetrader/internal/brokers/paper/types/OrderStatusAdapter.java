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

package org.eclipsetrader.internal.brokers.paper.types;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipsetrader.core.trading.IOrderStatus;

public class OrderStatusAdapter extends XmlAdapter<String, IOrderStatus> {

    public OrderStatusAdapter() {
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(IOrderStatus v) throws Exception {
        return v != null ? v.getId() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public IOrderStatus unmarshal(String v) throws Exception {
        if (IOrderStatus.Canceled.getId().equals(v)) {
            return IOrderStatus.Canceled;
        }
        if (IOrderStatus.Expired.getId().equals(v)) {
            return IOrderStatus.Expired;
        }
        if (IOrderStatus.Filled.getId().equals(v)) {
            return IOrderStatus.Filled;
        }
        if (IOrderStatus.New.getId().equals(v)) {
            return IOrderStatus.New;
        }
        if (IOrderStatus.Partial.getId().equals(v)) {
            return IOrderStatus.Partial;
        }
        if (IOrderStatus.PendingCancel.getId().equals(v)) {
            return IOrderStatus.PendingCancel;
        }
        if (IOrderStatus.PendingNew.getId().equals(v)) {
            return IOrderStatus.PendingNew;
        }
        if (IOrderStatus.Rejected.getId().equals(v)) {
            return IOrderStatus.Rejected;
        }
        return null;
    }
}
