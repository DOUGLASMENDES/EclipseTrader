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

package net.sourceforge.eclipsetrader.ta_lib;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class IndicatorPreferencesFactory implements IExecutableExtension, IExecutableExtensionFactory
{
    private String id;

    public IndicatorPreferencesFactory()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtensionFactory#create()
     */
    public Object create() throws CoreException
    {
        try {
            Factory factory = (Factory) Class.forName(TALibPlugin.PLUGIN_ID + ".indicators." + id).newInstance(); //$NON-NLS-1$
            return factory.createPreferencePage();
        } catch(Exception e) {
        }

        throw new CoreException(new Status(IStatus.ERROR, TALibPlugin.PLUGIN_ID,
                0, "Unknown id in data argument for " + getClass(), null)); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException
    {
        if (data instanceof String)
            id = (String) data;
        else
        {
            throw new CoreException(new Status(IStatus.ERROR,
                    TALibPlugin.PLUGIN_ID, 0,
                    "Data argument must be a String for " + getClass(), null)); //$NON-NLS-1$
        }
    }
}
