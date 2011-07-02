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

package org.eclipsetrader.core.internal.feed;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IConnectorOverride;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;

public class ConnectorOverrideAdapter implements IAdapterFactory {

    private List<ConnectorOverride> list = new ArrayList<ConnectorOverride>();

    public ConnectorOverrideAdapter(File file) throws Exception {
        if (file.exists()) {
            JAXBContext jaxbContext = JAXBContext.newInstance(ConnectorOverride[].class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(new ValidationEventHandler() {

                @Override
                public boolean handleEvent(ValidationEvent event) {
                    Status status = new Status(IStatus.WARNING, CoreActivator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                    CoreActivator.log(status);
                    return true;
                }
            });
            JAXBElement<ConnectorOverride[]> element = unmarshaller.unmarshal(new StreamSource(file), ConnectorOverride[].class);
            list.addAll(Arrays.asList(element.getValue()));
        }
    }

    public void save(File file) throws Exception {
        if (file.exists()) {
            file.delete();
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(ConnectorOverride[].class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                Status status = new Status(IStatus.WARNING, CoreActivator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                CoreActivator.getDefault().getLog().log(status);
                return true;
            }
        });
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$

        JAXBElement<ConnectorOverride[]> element = new JAXBElement<ConnectorOverride[]>(new QName("list"), ConnectorOverride[].class, list.toArray(new ConnectorOverride[list.size()]));
        marshaller.marshal(element, new FileWriter(file));
    }

    public void addOverride(ConnectorOverride override) {
        for (Iterator<ConnectorOverride> iter = list.iterator(); iter.hasNext();) {
            if (iter.next().getSecurity() == override.getSecurity()) {
                iter.remove();
            }
        }
        list.add(override);
    }

    public void removeOverride(ConnectorOverride override) {
        for (Iterator<ConnectorOverride> iter = list.iterator(); iter.hasNext();) {
            if (iter.next().getSecurity() == override.getSecurity()) {
                iter.remove();
            }
        }
    }

    public void clearOverride(ISecurity security) {
        for (Iterator<ConnectorOverride> iter = list.iterator(); iter.hasNext();) {
            if (iter.next().getSecurity() == security) {
                iter.remove();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof ISecurity) {
            if (adapterType.isAssignableFrom(getClass())) {
                return this;
            }
            if (adapterType.isAssignableFrom(ConnectorOverride.class)) {
                for (ConnectorOverride override : list) {
                    if (override.getSecurity() == adaptableObject) {
                        return override;
                    }
                }
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
        return new Class[] {
                IConnectorOverride.class,
                ConnectorOverride.class,
                ConnectorOverrideAdapter.class,
        };
    }

    List<ConnectorOverride> getList() {
        return list;
    }
}
