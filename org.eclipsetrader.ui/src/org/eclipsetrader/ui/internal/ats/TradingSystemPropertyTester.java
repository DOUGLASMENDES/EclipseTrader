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

package org.eclipsetrader.ui.internal.ats;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.ats.ITradingSystem;
import org.eclipsetrader.ui.internal.ats.monitor.TradingSystemItem;

public class TradingSystemPropertyTester extends PropertyTester {

    public TradingSystemPropertyTester() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
     */
    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof IAdaptable) {
            receiver = ((IAdaptable) receiver).getAdapter(TradingSystemItem.class);
        }

        if (receiver == null || !(receiver instanceof TradingSystemItem)) {
            return false;
        }

        if ("status".equals(property)) { //$NON-NLS-1$
            ITradingSystem system = ((TradingSystemItem) receiver).getTradingSystem();
            return Integer.parseInt(expectedValue.toString()) == system.getStatus();
        }

        return false;
    }

}
