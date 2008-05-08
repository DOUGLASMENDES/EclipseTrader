/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
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
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.repository.local.LocalRepository;
import org.eclipsetrader.repository.local.internal.Activator;
import org.eclipsetrader.repository.local.internal.types.HistoryType;

public class HistoryStore implements IStore {
	private Integer id;

	private ISecurity security;
	private WeakReference<HistoryType> historyTypeRef = new WeakReference<HistoryType>(null);

	public HistoryStore(Integer id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStoreProperties fetchProperties(IProgressMonitor monitor) {
		StoreProperties properties = new StoreProperties();

		properties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());

    	HistoryType historyType = historyTypeRef.get();
    	if (historyType == null) {
        	historyType = loadHistoryType();
        	security = historyType.getSecurity();
    		historyTypeRef = new WeakReference<HistoryType>(historyType);
    	}

    	properties.setProperty(IPropertyConstants.SECURITY, historyType.getSecurity());
		properties.setProperty(IPropertyConstants.BARS, historyType != null ? historyType.toArray() : new IOHLC[0]);

		return properties;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
		security = (ISecurity) properties.getProperty(IPropertyConstants.SECURITY);

		IOHLC[] h = (IOHLC[]) properties.getProperty(IPropertyConstants.BARS);
		HistoryType historyType = new HistoryType(security, h);
		historyTypeRef = new WeakReference<HistoryType>(historyType);

		saveHistoryType(historyType);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void delete(IProgressMonitor monitor) throws CoreException {
		IPath path = Activator.getDefault().getStateLocation().append(LocalRepository.SECURITIES_HISTORY_FILE);
		File file = path.append(String.valueOf(id) + ".xml").toFile();
		if (file.exists())
			file.delete();
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStore[] fetchChilds(IProgressMonitor monitor) {
	    return new IStore[0];
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#createChild()
     */
    public IStore createChild() {
	    return null;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#toURI()
	 */
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
	public IRepository getRepository() {
	    return Activator.getDefault().getRepository();
	}

	protected HistoryType loadHistoryType() {
    	HistoryType historyType = historyTypeRef.get();
		if (historyType == null) {
			IPath path = Activator.getDefault().getStateLocation().append(LocalRepository.SECURITIES_HISTORY_FILE);
			path.toFile().mkdirs();
			historyType = (HistoryType) unmarshal(HistoryType.class, path.append(String.valueOf(id) + ".xml").toFile());
			if (historyType == null)
				historyType = new HistoryType();
    		historyTypeRef = new WeakReference<HistoryType>(historyType);
		}
		return historyType;
	}

	protected void saveHistoryType(HistoryType historyType) {
		IPath path = Activator.getDefault().getStateLocation().append(LocalRepository.SECURITIES_HISTORY_FILE);
		path.toFile().mkdirs();
		marshal(historyType, HistoryType.class, path.append(String.valueOf(id) + ".xml").toFile());
	}

	@SuppressWarnings("unchecked")
	protected void marshal(Object object, Class clazz, File file) {
		try {
			if (file.exists())
				file.delete();
			JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setEventHandler(new ValidationEventHandler() {
				public boolean handleEvent(ValidationEvent event) {
					Status status = new Status(Status.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
					Activator.getDefault().getLog().log(status);
					return true;
				}
			});
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$
			marshaller.marshal(object, new FileWriter(file));
        } catch (Exception e) {
    		Status status = new Status(Status.WARNING, Activator.PLUGIN_ID, 0, "Error saving securities", null); //$NON-NLS-1$
    		Activator.getDefault().getLog().log(status);
        }
	}

	@SuppressWarnings("unchecked")
    protected Object unmarshal(Class clazz, File file) {
		try {
			if (file.exists()) {
	            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
	            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	            unmarshaller.setEventHandler(new ValidationEventHandler() {
	            	public boolean handleEvent(ValidationEvent event) {
	            		Status status = new Status(Status.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
	            		Activator.getDefault().getLog().log(status);
	            		return true;
	            	}
	            });
	            return unmarshaller.unmarshal(file);
			}
        } catch (Exception e) {
    		Status status = new Status(Status.WARNING, Activator.PLUGIN_ID, 0, "Error loading identifiers", null); //$NON-NLS-1$
    		Activator.getDefault().getLog().log(status);
        }
        return null;
	}
}
