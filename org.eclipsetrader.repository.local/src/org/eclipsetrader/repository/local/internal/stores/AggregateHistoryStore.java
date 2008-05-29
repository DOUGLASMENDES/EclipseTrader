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
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
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

public class AggregateHistoryStore implements IStore {
	private Integer id;
	private ISecurity security;

	private TimeSpan timeSpan;
	private IOHLC[] bars;

	private HistoryDayType dayType;
	private HistoryType type;

	public AggregateHistoryStore(Integer id, ISecurity security) {
		this.id = id;
		this.security = security;
	}

	public AggregateHistoryStore(Integer id, ISecurity security, HistoryDayType dayType, HistoryType type) {
		this.id = id;
		this.security = security;

		this.dayType = dayType;
		this.type = type;

		this.timeSpan = type.getPeriod();
        this.bars = type.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStoreProperties fetchProperties(IProgressMonitor monitor) {
		StoreProperties properties = new StoreProperties();

		properties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());
    	properties.setProperty(IPropertyConstants.SECURITY, security);

    	properties.setProperty(IPropertyConstants.TIME_SPAN, timeSpan);
    	properties.setProperty(IPropertyConstants.BARS, bars);

		return properties;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
		security = (ISecurity) properties.getProperty(IPropertyConstants.SECURITY);

		timeSpan = (TimeSpan) properties.getProperty(IPropertyConstants.TIME_SPAN);
		bars = (IOHLC[]) properties.getProperty(IPropertyConstants.BARS);

		if (bars != null && bars.length != 0) {
			File file = getFile();

			if (dayType == null) {
				if (file.exists())
					dayType = (HistoryDayType) unmarshal(HistoryDayType.class, file);
				if (dayType == null)
					dayType = new HistoryDayType(security, bars[0].getDate());
			}

			type = new HistoryType(security, bars, timeSpan);

			dayType.removeHistory(type);
			dayType.addHistory(type);

			if (file.exists())
				file.delete();
			marshal(dayType, HistoryDayType.class, file);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#createChild()
	 */
	public IStore createChild() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void delete(IProgressMonitor monitor) throws CoreException {
		if (dayType != null && type != null) {
			dayType.removeHistory(type);

			File file = getFile();
			if (file.exists())
				file.delete();

			if (dayType.getPeriods().length != 0)
				marshal(dayType, HistoryDayType.class, file);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStore[] fetchChilds(IProgressMonitor monitor) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#getRepository()
	 */
	public IRepository getRepository() {
		return LocalRepository.getInstance();
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IStore#toURI()
	 */
	public URI toURI() {
    	DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	    try {
	        return new URI(LocalRepository.URI_SCHEMA,
	        		NLS.bind(LocalRepository.URI_SECURITY_INTRADAY_HISTORY_PART, new Object[] {
	        				dateFormat.format(bars[0].getDate()),
	        				timeSpan.toString(),
	        			}),
	        		String.valueOf(id));
        } catch (URISyntaxException e) {
        }
        return null;
	}

	protected File getFile() {
		IPath path = LocalRepository.getInstance().getLocation().append(LocalRepository.SECURITIES_HISTORY_FILE).append("." + String.valueOf(id));
		path.toFile().mkdirs();
		return path.append(new SimpleDateFormat("yyyyMMdd").format(bars[0].getDate()) + ".xml").toFile();
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
