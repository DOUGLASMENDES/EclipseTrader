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
import java.io.FileFilter;
import java.io.FileWriter;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.ISplit;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.repository.local.LocalRepository;
import org.eclipsetrader.repository.local.internal.Activator;
import org.eclipsetrader.repository.local.internal.types.HistoryType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class HistoryStore implements IStore {

    private Integer id;
    private ISecurity security;

    private Map<String, WeakReference<IntradayHistoryStore>> intradayStores = new HashMap<String, WeakReference<IntradayHistoryStore>>();

    public HistoryStore(Integer id) {
        this.id = id;
        try {
            URI uri = new URI(LocalRepository.URI_SCHEMA, LocalRepository.URI_SECURITY_PART, String.valueOf(id));
            this.security = getSecurity(uri);
        } catch (URISyntaxException e) {
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStoreProperties fetchProperties(IProgressMonitor monitor) {
        StoreProperties properties = new LazyStoreProperties(id);

        properties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());

        properties.setProperty(IPropertyConstants.SECURITY, security);
        properties.setProperty(IPropertyConstants.TIME_SPAN, TimeSpan.days(1));

        return properties;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
        security = (ISecurity) properties.getProperty(IPropertyConstants.SECURITY);

        IOHLC[] bars = (IOHLC[]) properties.getProperty(IPropertyConstants.BARS);
        ISplit[] splits = (ISplit[]) properties.getProperty(IPropertyConstants.SPLITS);

        HistoryType historyType = new HistoryType(security, bars, splits, null);
        saveHistoryType(historyType);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void delete(IProgressMonitor monitor) throws CoreException {
        IPath path = LocalRepository.getInstance().getLocation().append(LocalRepository.SECURITIES_HISTORY_FILE);
        File file = path.append(String.valueOf(id) + ".xml").toFile();
        if (file.exists()) {
            file.delete();
        }

        file = path.append("." + String.valueOf(id)).toFile();
        if (file.exists()) {
            File[] childFiles = file.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().endsWith(".xml");
                }
            });
            for (int i = 0; i < childFiles.length; i++) {
                childFiles[i].delete();
            }
            file.delete();
        }

        intradayStores.clear();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStore[] fetchChilds(IProgressMonitor monitor) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        List<IStore> l = new ArrayList<IStore>();

        for (Iterator<WeakReference<IntradayHistoryStore>> iter = intradayStores.values().iterator(); iter.hasNext();) {
            WeakReference<IntradayHistoryStore> ref = iter.next();
            IntradayHistoryStore store = ref.get();
            if (store != null && store.getFile().exists()) {
                l.add(store);
            }
            else {
                iter.remove();
            }
        }

        IPath path = LocalRepository.getInstance().getLocation().append(LocalRepository.SECURITIES_HISTORY_FILE);
        File file = path.append("." + String.valueOf(id)).toFile();
        if (file.exists()) {
            File[] childFiles = file.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().endsWith(".xml");
                }
            });
            for (int i = 0; i < childFiles.length; i++) {
                String name = childFiles[i].getName();
                if (!intradayStores.containsKey(name)) {
                    try {
                        Date date = dateFormat.parse(name.substring(0, name.length() - 4));
                        IntradayHistoryStore store = new IntradayHistoryStore(id, security, date);
                        intradayStores.put(name, new WeakReference<IntradayHistoryStore>(store));
                        l.add(store);
                    } catch (ParseException e) {
                        // Do nothing
                    }
                }
            }
        }

        return l.toArray(new IStore[l.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#createChild()
     */
    @Override
    public IStore createChild() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#toURI()
     */
    @Override
    public URI toURI() {
        try {
            return new URI(LocalRepository.URI_SCHEMA, LocalRepository.URI_SECURITY_HISTORY_PART, String.valueOf(id));
        } catch (URISyntaxException e) {
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#getRepository()
     */
    @Override
    public IRepository getRepository() {
        return Activator.getDefault().getRepository();
    }

    protected void saveHistoryType(HistoryType historyType) {
        IPath path = Activator.getDefault().getStateLocation().append(LocalRepository.SECURITIES_HISTORY_FILE);
        path.toFile().mkdirs();
        marshal(historyType, HistoryType.class, path.append(String.valueOf(id) + ".xml").toFile());
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
                    Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                    Activator.getDefault().getLog().log(status);
                    return true;
                }
            });
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$
            marshaller.marshal(object, new FileWriter(file));
        } catch (Exception e) {
            Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error saving securities", null); //$NON-NLS-1$
            Activator.getDefault().getLog().log(status);
        }
    }

    protected ISecurity getSecurity(URI uri) {
        IRepositoryService repositoryService = null;

        if (Activator.getDefault() != null) {
            try {
                BundleContext context = Activator.getDefault().getBundle().getBundleContext();
                ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
                repositoryService = (IRepositoryService) context.getService(serviceReference);
                context.ungetService(serviceReference);
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading repository service", e);
                Activator.getDefault().getLog().log(status);
            }
        }

        return repositoryService != null ? repositoryService.getSecurityFromURI(uri) : null;
    }
}
