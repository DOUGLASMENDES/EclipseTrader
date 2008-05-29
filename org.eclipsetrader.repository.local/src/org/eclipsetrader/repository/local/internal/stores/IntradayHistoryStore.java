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
import java.net.URI;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
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

	private IStore[] childs;

	protected IntradayHistoryStore() {
	}

	public IntradayHistoryStore(Integer id, ISecurity security, Date date) {
	    this.id = id;
	    this.security = security;
	    this.date = date;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#createChild()
	 */
	public IStore createChild() {
		return new AggregateHistoryStore(id, security);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void delete(IProgressMonitor monitor) throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStore[] fetchChilds(IProgressMonitor monitor) {
		if (childs == null) {
	    	IPath path = Activator.getDefault().getStateLocation().append(LocalRepository.SECURITIES_HISTORY_FILE);
			File file = path.append("." + String.valueOf(id)).toFile();
			HistoryDayType dayType = (HistoryDayType) unmarshal(HistoryDayType.class, file);
			if (dayType != null) {
				HistoryType[] periods = dayType.getPeriods();
				childs = new AggregateHistoryStore[periods.length];
				for (int i = 0; i < periods.length; i++)
					childs[i] = new AggregateHistoryStore(id, security, dayType, periods[i]);
			}
		}
		return childs;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStoreProperties fetchProperties(IProgressMonitor monitor) {
		StoreProperties properties = new StoreProperties();

		properties.setProperty(IPropertyConstants.SECURITY, security);
		properties.setProperty(IPropertyConstants.BARS_DATE, date);

		return properties;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#getRepository()
	 */
	public IRepository getRepository() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#toURI()
	 */
	public URI toURI() {
		return null;
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
    		Status status = new Status(Status.WARNING, Activator.PLUGIN_ID, 0, "Error loading history", null); //$NON-NLS-1$
    		Activator.getDefault().getLog().log(status);
        }
        return null;
	}
}
