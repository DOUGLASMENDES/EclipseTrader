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

package org.eclipsetrader.repository.local.internal.stores;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.repository.local.LocalRepository;
import org.eclipsetrader.repository.local.internal.Activator;
import org.eclipsetrader.repository.local.internal.types.HistoryType;

public class LazyStoreProperties extends StoreProperties {

    private Integer id;

    public LazyStoreProperties(Integer id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.StoreProperties#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(String name) {
        if (IPropertyConstants.BARS.equals(name) || IPropertyConstants.SPLITS.equals(name)) {
            if (getProperties().get(name) == null) {
                IPath path = LocalRepository.getInstance().getLocation().append(LocalRepository.SECURITIES_HISTORY_FILE);
                path.toFile().mkdirs();
                HistoryType historyType = (HistoryType) unmarshal(HistoryType.class, path.append(String.valueOf(id) + ".xml").toFile());
                if (historyType != null) {
                    getProperties().put(IPropertyConstants.BARS, historyType.toArray());
                    getProperties().put(IPropertyConstants.SPLITS, historyType.getSplits());
                }
            }
        }
        return super.getProperty(name);
    }

    @SuppressWarnings("unchecked")
    protected Object unmarshal(Class clazz, File file) {
        try {
            if (file.exists()) {
                JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                unmarshaller.setEventHandler(new ValidationEventHandler() {

                    @Override
                    public boolean handleEvent(ValidationEvent event) {
                        Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                        Activator.getDefault().getLog().log(status);
                        return true;
                    }
                });
                return unmarshaller.unmarshal(file);
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error loading identifiers", null); //$NON-NLS-1$
            Activator.getDefault().getLog().log(status);
        }
        return null;
    }
}
