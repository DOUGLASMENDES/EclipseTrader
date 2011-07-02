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
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.eclipse.core.runtime.CoreException;
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
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.repository.hibernate.HibernateRepository;
import org.eclipsetrader.repository.hibernate.internal.Activator;
import org.eclipsetrader.repository.hibernate.internal.types.HistoryData;
import org.eclipsetrader.repository.hibernate.internal.types.SecurityType;
import org.eclipsetrader.repository.hibernate.internal.types.SplitData;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Target;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@Entity
@Table(name = "histories")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING, length = 16)
@DiscriminatorValue("history")
public class HistoryStore implements IStore {

    @Id
    @Column(name = "id", length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    String id;

    @Version
    @Column(name = "version")
    @SuppressWarnings("unused")
    private Integer version;

    @Column(name = "instrument")
    @Target(SecurityType.class)
    ISecurity security;

    @OneToMany(mappedBy = "history", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    List<HistoryData> data = new ArrayList<HistoryData>();

    @OneToMany(mappedBy = "history", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<SplitData> splits = new ArrayList<SplitData>();

    @Transient
    HibernateRepository repository;

    public HistoryStore() {
    }

    public HistoryStore(ISecurity security, HibernateRepository repository) {
        this.repository = repository;
        this.security = security;
    }

    public void setRepository(HibernateRepository repository) {
        this.repository = repository;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStoreProperties fetchProperties(IProgressMonitor monitor) {
        StoreProperties properties = new StoreProperties();

        properties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());

        properties.setProperty(IPropertyConstants.SECURITY, security);
        properties.setProperty(IPropertyConstants.TIME_SPAN, TimeSpan.days(1));
        properties.setProperty(IPropertyConstants.BARS, data.toArray(new IOHLC[data.size()]));
        properties.setProperty(IPropertyConstants.SPLITS, splits.toArray(new ISplit[splits.size()]));

        return properties;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
        Session session = repository.getSession();

        this.security = (ISecurity) properties.getProperty(IPropertyConstants.SECURITY);

        List<HistoryData> newData = new ArrayList<HistoryData>(2048);
        IOHLC[] bars = (IOHLC[]) properties.getProperty(IPropertyConstants.BARS);
        if (bars != null) {
            TimeSpan timeSpan = TimeSpan.days(1);
            for (int i = 0; i < bars.length; i++) {
                newData.add(new HistoryData(this, bars[i], timeSpan));
            }
        }
        for (HistoryData ohlc : newData) {
            if (!this.data.contains(ohlc)) {
                this.data.add(ohlc);
            }
        }
        for (Iterator<HistoryData> iter = this.data.iterator(); iter.hasNext();) {
            if (!newData.contains(iter.next())) {
                iter.remove();
            }
        }

        ISplit[] s = (ISplit[]) properties.getProperty(IPropertyConstants.SPLITS);
        splits.clear();
        if (s != null) {
            for (int i = 0; i < s.length; i++) {
                splits.add(new SplitData(this, s[i]));
            }
        }

        session.save(this);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void delete(IProgressMonitor monitor) throws CoreException {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    @SuppressWarnings("unchecked")
    public IStore[] fetchChilds(IProgressMonitor monitor) {
        List<IStore> l = new ArrayList<IStore>();

        Query query = repository.getSession().createQuery("from IntradayHistoryStore where instrument = :instrument");
        IStoreObject storeObject = (IStoreObject) security.getAdapter(IStoreObject.class);
        query.setString("instrument", storeObject.getStore().toURI().toString());

        List list = query.list();
        if (list != null) {
            for (Object o : list) {
                l.add((IStore) o);
            }
        }

        return l.toArray(new IStore[l.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#createChild()
     */
    @Override
    public IStore createChild() {
        return new IntradayHistoryStore(security, repository);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#toURI()
     */
    @Override
    public URI toURI() {
        try {
            return new URI(repository.getSchema(), HibernateRepository.URI_SECURITY_HISTORY_PART, id);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#getRepository()
     */
    @Override
    public IRepository getRepository() {
        return repository;
    }

    protected ISecurity getSecurityFromURI(URI uri) {
        ISecurity security = null;
        try {
            BundleContext context = Activator.getDefault().getBundle().getBundleContext();
            ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
            IRepositoryService repositoryService = (IRepositoryService) context.getService(serviceReference);
            security = repositoryService.getSecurityFromURI(uri);
            if (security != null) {
                Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Failed to load security " + uri.toString(), null);
                Activator.log(status);
            }
            context.ungetService(serviceReference);
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading repository service", e);
            Activator.log(status);
        }
        return security;
    }
}
