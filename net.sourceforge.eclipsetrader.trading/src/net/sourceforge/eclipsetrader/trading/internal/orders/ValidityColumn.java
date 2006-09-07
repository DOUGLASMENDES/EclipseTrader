/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.trading.internal.orders;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.db.Order;
import net.sourceforge.eclipsetrader.core.db.OrderValidity;

import org.eclipse.jface.viewers.LabelProvider;

public class ValidityColumn extends LabelProvider
{
    static Map labels = new HashMap();
    static {
        labels.put(OrderValidity.DAY, "Day");
        labels.put(OrderValidity.IMMEDIATE_OR_CANCEL, "Imm. or Cancel");
        labels.put(OrderValidity.AT_OPENING, "At Opening");
        labels.put(OrderValidity.AT_CLOSING, "At Closing");
        labels.put(OrderValidity.GOOD_TILL_CANCEL, "Good Till Cancel");
    }

    public ValidityColumn()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element)
    {
        if (element instanceof Order)
        {
            Order order = (Order)element;
            String text = (String)labels.get(order.getValidity());
            return text != null ? text : "";
        }
        return "";
    }
}
