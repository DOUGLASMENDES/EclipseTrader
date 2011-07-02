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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.internal.brokers.paper.Activator;
import org.eclipsetrader.internal.brokers.paper.IExpenseScheme;

public class ExpenseSchemeAdapter extends XmlAdapter<String, IExpenseScheme> {

    public ExpenseSchemeAdapter() {
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(IExpenseScheme v) throws Exception {
        return v != null ? v.getClass().getName() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public IExpenseScheme unmarshal(String v) throws Exception {
        if (v == null) {
            return null;
        }

        try {
            return (IExpenseScheme) Class.forName(v).newInstance();
        } catch (Throwable t) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error instantiating expense scheme " + v, t); //$NON-NLS-1$
            Activator.log(status);
        }

        return null;
    }
}
