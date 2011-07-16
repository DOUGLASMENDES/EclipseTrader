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

package org.eclipsetrader.directa.internal.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.directa.internal.Activator;

public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

    public PropertyTester() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
     */
    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        ISecurity security = null;

        if (receiver instanceof ISecurity) {
            security = (ISecurity) receiver;
        }
        else if (receiver instanceof IAdaptable) {
            security = (ISecurity) ((IAdaptable) receiver).getAdapter(ISecurity.class);
        }

        if (security == null) {
            security = (ISecurity) Platform.getAdapterManager().getAdapter(receiver, ISecurity.class);
        }

        if (security == null) {
            return false;
        }

        if ("canTrade".equals(property)) { //$NON-NLS-1$
            IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);
            if (identifier != null) {
                String code = null;
                String isin = null;

                IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
                if (properties != null) {
                    if (properties.getProperty(Activator.PROP_ISIN) != null) {
                        isin = properties.getProperty(Activator.PROP_ISIN);
                    }
                    if (properties.getProperty(Activator.PROP_CODE) != null) {
                        code = properties.getProperty(Activator.PROP_CODE);
                    }
                }

                if (code != null && isin != null) {
                    return true;
                }
            }
        }

        return false;
    }
}
