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

package org.eclipsetrader.core.internal.trading;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.trading.IBroker;

public class MarketBrokerAdapterFactory implements IAdapterFactory {

    private File file;
    private List<MarketBroker> list;

    public MarketBrokerAdapterFactory(File file) {
        this.file = file;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (list == null) {
            list = new ArrayList<MarketBroker>();
            try {
                if (file.exists()) {
                    load(file);
                }
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error loading market brokers settings", null); //$NON-NLS-1$
                CoreActivator.getDefault().getLog().log(status);
            }
        }

        if (!(adaptableObject instanceof IMarket)) {
            return null;
        }

        if (adapterType.isAssignableFrom(getClass())) {
            return this;
        }

        if (adapterType.isAssignableFrom(IBroker.class)) {
            for (MarketBroker broker : list) {
                if (broker.getMarket() == adaptableObject) {
                    return broker.getConnector();
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
            IBroker.class, MarketBrokerAdapterFactory.class,
        };
    }

    void load(File file) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(MarketBroker[].class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                Status status = new Status(IStatus.WARNING, CoreActivator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                CoreActivator.log(status);
                return true;
            }
        });
        JAXBElement<MarketBroker[]> element = unmarshaller.unmarshal(new StreamSource(file), MarketBroker[].class);
        list.addAll(Arrays.asList(element.getValue()));
    }

    public void addOverride(MarketBroker override) {
        for (Iterator<MarketBroker> iter = list.iterator(); iter.hasNext();) {
            if (iter.next().getMarket() == override.getMarket()) {
                iter.remove();
            }
        }
        list.add(override);
    }

    public void removeOverride(MarketBroker override) {
        for (Iterator<MarketBroker> iter = list.iterator(); iter.hasNext();) {
            if (iter.next().getMarket() == override.getMarket()) {
                iter.remove();
            }
        }
    }

    public void clearOverride(IMarket market) {
        for (Iterator<MarketBroker> iter = list.iterator(); iter.hasNext();) {
            if (iter.next().getMarket() == market) {
                iter.remove();
            }
        }
    }

    public void save(File file) throws JAXBException, IOException {
        if (list == null) {
            return;
        }

        if (file.exists()) {
            file.delete();
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(MarketBroker[].class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                Status status = new Status(IStatus.WARNING, CoreActivator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                CoreActivator.log(status);
                return true;
            }
        });
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$

        JAXBElement<MarketBroker[]> element = new JAXBElement<MarketBroker[]>(new QName("list"), MarketBroker[].class, list.toArray(new MarketBroker[list.size()]));
        marshaller.marshal(element, new FileWriter(file));
    }
}
