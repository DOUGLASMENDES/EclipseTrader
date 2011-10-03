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

package org.eclipsetrader.core.internal.ats;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipsetrader.core.ats.IScriptStrategy;
import org.eclipsetrader.core.ats.ScriptStrategy;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepositoryElementFactory;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;

public class RepositoryElementFactory implements IRepositoryElementFactory, IExecutableExtension, IExecutableExtensionFactory {

    public static final String EXTENSION_ID = "org.eclipsetrader.core.ats.factory";

    private static RepositoryElementFactory instance;

    public RepositoryElementFactory() {
    }

    public static RepositoryElementFactory getInstance() {
        if (instance == null) {
            instance = new RepositoryElementFactory();
        }
        return instance;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtensionFactory#create()
     */
    @Override
    public Object create() throws CoreException {
        if (instance == null) {
            instance = this;
        }
        return instance;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryElementFactory#getId()
     */
    @Override
    public String getId() {
        return EXTENSION_ID;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryElementFactory#createElement(org.eclipsetrader.core.repositories.IStore, org.eclipsetrader.core.repositories.IStoreProperties)
     */
    @Override
    public IStoreObject createElement(IStore store, IStoreProperties properties) {
        String type = (String) properties.getProperty(IPropertyConstants.OBJECT_TYPE);
        if (type != null) {
            if (IScriptStrategy.class.getName().equals(type)) {
                return new ScriptStrategy(store, properties);
            }
        }
        return null;
    }
}
