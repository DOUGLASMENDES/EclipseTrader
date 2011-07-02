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
import java.io.FileWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.repository.local.LocalRepository;
import org.eclipsetrader.repository.local.internal.Activator;
import org.eclipsetrader.repository.local.internal.types.HistoryDayType;
import org.eclipsetrader.repository.local.internal.types.HistoryType;

public class IntradayHistoryStore implements IStore {

    private Integer id;
    private ISecurity security;

    private Date date;
    private Map<TimeSpan, IOHLC[]> bars = new HashMap<TimeSpan, IOHLC[]>();

    private HistoryDayType dayType;

    protected IntradayHistoryStore() {
    }

    public IntradayHistoryStore(Integer id, ISecurity security, Date date) {
        this.id = id;
        this.security = security;
        this.date = date;
    }

    public IntradayHistoryStore(Integer id, ISecurity security, HistoryDayType dayType) {
        this.id = id;
        this.security = security;
        this.date = dayType.getDate();

        this.dayType = dayType;
        for (HistoryType type : dayType.getPeriods()) {
            List<IOHLC> l = type.getData();
            this.bars.put(type.getPeriod(), l.toArray(new IOHLC[l.size()]));
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#createChild()
     */
    @Override
    public IStore createChild() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void delete(IProgressMonitor monitor) throws CoreException {
        File file = getFile();
        if (file.exists()) {
            file.delete();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStore[] fetchChilds(IProgressMonitor monitor) {
        return null;
    }

    protected void loadHistoryDayType() {
        if (dayType == null) {
            File file = getFile();
            if (file.exists()) {
                dayType = (HistoryDayType) unmarshal(HistoryDayType.class, file);
            }
            if (dayType == null) {
                dayType = new HistoryDayType(security, date);
            }

            for (HistoryType type : dayType.getPeriods()) {
                if (!bars.containsKey(type.getPeriod())) {
                    List<IOHLC> l = type.getData();
                    bars.put(type.getPeriod(), l.toArray(new IOHLC[l.size()]));
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStoreProperties fetchProperties(IProgressMonitor monitor) {
        StoreProperties properties = new StoreProperties() {

            @Override
            public String[] getPropertyNames() {
                loadHistoryDayType();
                Set<String> s = new HashSet<String>(Arrays.asList(super.getPropertyNames()));
                for (HistoryType type : dayType.getPeriods()) {
                    s.add(type.getPeriod().toString());
                }
                return s.toArray(new String[s.size()]);
            }

            @Override
            public Object getProperty(String name) {
                Object o = super.getProperty(name);
                if (o != null) {
                    return o;
                }
                TimeSpan timeSpan = TimeSpan.fromString(name);
                if (timeSpan != null) {
                    loadHistoryDayType();
                    return bars.get(timeSpan);
                }
                return null;
            }
        };

        properties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());

        properties.setProperty(IPropertyConstants.SECURITY, security);
        properties.setProperty(IPropertyConstants.BARS_DATE, date);

        return properties;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
        security = (ISecurity) properties.getProperty(IPropertyConstants.SECURITY);
        date = (Date) properties.getProperty(IPropertyConstants.BARS_DATE);

        loadHistoryDayType();

        for (String name : properties.getPropertyNames()) {
            TimeSpan timeSpan = TimeSpan.fromString(name);
            if (timeSpan != null) {
                IOHLC[] ohlc = (IOHLC[]) properties.getProperty(name);
                bars.put(timeSpan, ohlc);
                dayType.addHistory(new HistoryType(security, ohlc, timeSpan));
            }
        }

        File file = getFile();
        if (bars.size() != 0) {
            marshal(dayType, HistoryDayType.class, file);
        }
        else {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#getRepository()
     */
    @Override
    public IRepository getRepository() {
        return Activator.getDefault().getRepository();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#toURI()
     */
    @Override
    public URI toURI() {
        try {
            return new URI(LocalRepository.URI_SCHEMA, LocalRepository.URI_SECURITY_HISTORY_PART + "/" + new SimpleDateFormat("yyyyMMdd").format(date), String.valueOf(id));
        } catch (URISyntaxException e) {
        }
        return null;
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
                        Activator.log(status);
                        return true;
                    }
                });
                return unmarshaller.unmarshal(file);
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error loading history", null); //$NON-NLS-1$
            Activator.log(status);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected void marshal(Object object, Class clazz, File file) {
        try {
            if (file.exists()) {
                file.delete();
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setEventHandler(new ValidationEventHandler() {

                @Override
                public boolean handleEvent(ValidationEvent event) {
                    return true;
                }
            });
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$
            marshaller.marshal(object, new FileWriter(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected File getFile() {
        IPath path = LocalRepository.getInstance().getLocation().append(LocalRepository.SECURITIES_HISTORY_FILE).append("." + String.valueOf(id));
        path.toFile().mkdirs();
        return path.append(new SimpleDateFormat("yyyyMMdd").format(date) + ".xml").toFile();
    }
}
