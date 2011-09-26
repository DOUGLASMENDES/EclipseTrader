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

package org.eclipsetrader.repository.hibernate.internal.stores;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.feed.IDividend;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.IUserProperties;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryElementFactory;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.repository.hibernate.HibernateRepository;
import org.eclipsetrader.repository.hibernate.internal.types.DividendType;
import org.eclipsetrader.repository.hibernate.internal.types.IdentifierType;
import org.eclipsetrader.repository.hibernate.internal.types.RepositoryFactoryType;
import org.eclipsetrader.repository.hibernate.internal.types.SecurityUnknownPropertyType;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Target;

@Entity
@Table(name = "securities")
public class SecurityStore implements IStore {

    @Id
    @Column(name = "id", length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String id;

    @Version
    @Column(name = "version")
    @SuppressWarnings("unused")
    private Integer version;

    @Column(name = "factory")
    @Target(RepositoryFactoryType.class)
    private IRepositoryElementFactory factory;

    @Column(name = "type")
    private String type;

    @Column(name = "name", unique = true)
    private String name;

    @ManyToOne
    private IdentifierType identifier;

    @Column(name = "currency")
    private Currency currency;

    @OneToMany(mappedBy = "security", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @OrderBy("exDate")
    private List<DividendType> dividends = new ArrayList<DividendType>();

    @Transient
    private IUserProperties userProperties;

    @OneToMany(mappedBy = "security", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<SecurityUnknownPropertyType> unknownProperties = new ArrayList<SecurityUnknownPropertyType>();

    @Transient
    private HibernateRepository repository;

    @Transient
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

    public SecurityStore(HibernateRepository repository) {
        this.repository = repository;
    }

    public String getId() {
        return id;
    }

    public void setRepository(HibernateRepository repository) {
        this.repository = repository;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#toURI()
     */
    @Override
    public URI toURI() {
        try {
            return new URI(repository.getSchema(), HibernateRepository.URI_SECURITY_PART, id);
        } catch (URISyntaxException e) {
            e.printStackTrace();
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
            properties.setProperty(IPropertyConstants.IDENTIFIER, identifier.getIdentifier());
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
            for (SecurityUnknownPropertyType property : unknownProperties) {
                properties.setProperty(property.getName(), SecurityUnknownPropertyType.convert(property));
            }
        }

        return properties;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
        Session session = repository.getSession();

        this.factory = (IRepositoryElementFactory) properties.getProperty(IPropertyConstants.ELEMENT_FACTORY);
        this.type = (String) properties.getProperty(IPropertyConstants.OBJECT_TYPE);

        this.name = (String) properties.getProperty(IPropertyConstants.NAME);

        IFeedIdentifier feedIdentifier = (IFeedIdentifier) properties.getProperty(IPropertyConstants.IDENTIFIER);
        this.identifier = feedIdentifier != null ? repository.getIdentifierTypeFromFeedIdentifier(feedIdentifier) : null;
        if (this.identifier != null) {
            this.identifier.updateProperties((IFeedProperties) feedIdentifier.getAdapter(IFeedProperties.class));
            session.save(this.identifier);
        }

        this.currency = (Currency) properties.getProperty(IPropertyConstants.CURRENCY);
        this.userProperties = (IUserProperties) properties.getProperty(IPropertyConstants.USER_PROPERTIES);

        this.dividends.clear();
        IDividend[] dividends = (IDividend[]) properties.getProperty(IPropertyConstants.DIVIDENDS);
        if (dividends != null) {
            for (int i = 0; i < dividends.length; i++) {
                this.dividends.add(new DividendType(this, dividends[i]));
            }
        }

        this.unknownProperties.clear();
        for (String name : properties.getPropertyNames()) {
            if (!knownProperties.contains(name)) {
                this.unknownProperties.add(SecurityUnknownPropertyType.create(this, name, properties.getProperty(name)));
            }
        }

        session.save(this);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void delete(IProgressMonitor monitor) throws CoreException {
        Session session = repository.getSession();

        session.delete(this);

        Query query = session.createQuery("from HistoryStore where instrument = :instrument");
        query.setString("instrument", toURI().toString());
        List l = query.list();
        if (l != null) {
            for (Object o : l) {
                session.delete(o);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public IStore[] fetchChilds(IProgressMonitor monitor) {
        if (historyStore == null) {
            Query query = repository.getSession().createQuery("from HistoryStore where instrument = :instrument and type = :type");
            query.setString("instrument", toURI().toString());
            query.setString("type", "history");
            List l = query.list();
            if (l != null && l.size() == 1) {
                historyStore = (HistoryStore) l.get(0);
                historyStore.setRepository(repository);
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
        return new RepositoryStore(repository);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#getRepository()
     */
    @Override
    @Transient
    public IRepository getRepository() {
        return repository;
    }
}
