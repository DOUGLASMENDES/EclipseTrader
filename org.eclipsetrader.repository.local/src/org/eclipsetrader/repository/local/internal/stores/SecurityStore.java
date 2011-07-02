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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.feed.IDividend;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.IUserProperties;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryElementFactory;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.repository.local.LocalRepository;
import org.eclipsetrader.repository.local.internal.Activator;
import org.eclipsetrader.repository.local.internal.SecurityCollection;
import org.eclipsetrader.repository.local.internal.types.CurrencyAdapter;
import org.eclipsetrader.repository.local.internal.types.DividendType;
import org.eclipsetrader.repository.local.internal.types.FeedIdentifierAdapter;
import org.eclipsetrader.repository.local.internal.types.PropertyType;
import org.eclipsetrader.repository.local.internal.types.RepositoryFactoryAdapter;

@XmlRootElement(name = "security")
public class SecurityStore implements IStore {

    @XmlAttribute(name = "id")
    private Integer id;

    @XmlAttribute(name = "factory")
    @XmlJavaTypeAdapter(RepositoryFactoryAdapter.class)
    private IRepositoryElementFactory factory;

    @XmlAttribute(name = "type")
    private String type;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "identifier")
    @XmlJavaTypeAdapter(FeedIdentifierAdapter.class)
    private IFeedIdentifier identifier;

    @XmlElement(name = "currency")
    @XmlJavaTypeAdapter(CurrencyAdapter.class)
    private Currency currency;

    @XmlElementWrapper(name = "dividends")
    @XmlElementRef
    private TreeSet<DividendType> dividends;

    private IUserProperties userProperties;

    @XmlElementWrapper(name = "properties")
    @XmlElementRef
    private List<PropertyType> unknownProperties;

    private HistoryStore historyStore;

    private static Set<String> knownProperties = new HashSet<String>();
    static {
        knownProperties.add(IPropertyConstants.ELEMENT_FACTORY);
        knownProperties.add(IPropertyConstants.OBJECT_TYPE);

        knownProperties.add(IPropertyConstants.NAME);
        knownProperties.add(IPropertyConstants.IDENTIFIER);
        knownProperties.add(IPropertyConstants.CURRENCY);
        knownProperties.add(IPropertyConstants.DIVIDENDS);
        knownProperties.add(IPropertyConstants.USER_PROPERTIES);
    }

    public SecurityStore() {
    }

    public SecurityStore(Integer id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#toURI()
     */
    @Override
    public URI toURI() {
        try {
            return new URI(LocalRepository.URI_SCHEMA, LocalRepository.URI_SECURITY_PART, String.valueOf(id));
        } catch (URISyntaxException e) {
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStoreProperties fetchProperties(IProgressMonitor monitor) {
        StoreProperties properties = new StoreProperties();

        if (factory != null) {
            properties.setProperty(IPropertyConstants.ELEMENT_FACTORY, factory);
        }
        properties.setProperty(IPropertyConstants.OBJECT_TYPE, type == null ? ISecurity.class.getName() : type);

        properties.setProperty(IPropertyConstants.NAME, name);
        if (identifier != null) {
            properties.setProperty(IPropertyConstants.IDENTIFIER, identifier);
        }
        if (currency != null) {
            properties.setProperty(IPropertyConstants.CURRENCY, currency);
        }
        properties.setProperty(IPropertyConstants.USER_PROPERTIES, userProperties);

        if (dividends != null) {
            int i = 0;
            IDividend[] dividends = new IDividend[this.dividends.size()];
            for (DividendType type : this.dividends) {
                dividends[i++] = type.getDividend();
            }
            properties.setProperty(IPropertyConstants.DIVIDENDS, dividends);
        }

        if (unknownProperties != null) {
            for (PropertyType property : unknownProperties) {
                properties.setProperty(property.getName(), PropertyType.convert(property));
            }
        }

        return properties;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
        this.factory = (IRepositoryElementFactory) properties.getProperty(IPropertyConstants.ELEMENT_FACTORY);
        this.type = (String) properties.getProperty(IPropertyConstants.OBJECT_TYPE);

        this.name = (String) properties.getProperty(IPropertyConstants.NAME);
        this.identifier = (IFeedIdentifier) properties.getProperty(IPropertyConstants.IDENTIFIER);
        this.currency = (Currency) properties.getProperty(IPropertyConstants.CURRENCY);
        this.userProperties = (IUserProperties) properties.getProperty(IPropertyConstants.USER_PROPERTIES);

        IDividend[] dividends = (IDividend[]) properties.getProperty(IPropertyConstants.DIVIDENDS);
        if (dividends != null) {
            this.dividends = new TreeSet<DividendType>();
            for (int i = 0; i < dividends.length; i++) {
                this.dividends.add(new DividendType(dividends[i]));
            }
        }
        else {
            this.dividends = null;
        }

        this.unknownProperties = new ArrayList<PropertyType>();
        for (String name : properties.getPropertyNames()) {
            if (!knownProperties.contains(name)) {
                this.unknownProperties.add(PropertyType.create(name, properties.getProperty(name)));
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void delete(IProgressMonitor monitor) throws CoreException {
        SecurityCollection.getInstance().delete(this);

        if (historyStore == null) {
            IPath path = LocalRepository.getInstance().getLocation().append(LocalRepository.SECURITIES_HISTORY_FILE);
            File file = path.append(String.valueOf(id) + ".xml").toFile();
            if (file.exists()) {
                historyStore = new HistoryStore(id);
            }
        }
        if (historyStore != null) {
            historyStore.delete(monitor);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStore[] fetchChilds(IProgressMonitor monitor) {
        if (historyStore == null) {
            IPath path = LocalRepository.getInstance().getLocation().append(LocalRepository.SECURITIES_HISTORY_FILE);
            File file = path.append(String.valueOf(id) + ".xml").toFile();
            if (file.exists()) {
                historyStore = new HistoryStore(id);
            }
        }

        List<IStore> l = new ArrayList<IStore>();
        if (historyStore != null) {
            l.add(historyStore);
        }
        return l.toArray(new IStore[l.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#createChild()
     */
    @Override
    public IStore createChild() {
        return new RepositoryStore();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#getRepository()
     */
    @Override
    @XmlTransient
    public IRepository getRepository() {
        return Activator.getDefault().getRepository();
    }

    public HistoryStore createHistoryStore() {
        historyStore = new HistoryStore(id);
        return historyStore;
    }

    public IStore createHistoryStore(Date date) {
        return new IntradayHistoryStore(id, null, date);
    }
}
