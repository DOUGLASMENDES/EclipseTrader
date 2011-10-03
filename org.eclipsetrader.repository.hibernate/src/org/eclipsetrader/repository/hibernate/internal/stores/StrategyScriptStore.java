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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.IScript;
import org.eclipsetrader.core.ats.IScriptStrategy;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryElementFactory;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.repository.hibernate.HibernateRepository;
import org.eclipsetrader.repository.hibernate.internal.types.RepositoryFactoryType;
import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Target;

@Entity
@Table(name = "strategies")
public class StrategyScriptStore implements IStore {

    public static final String K_INCLUDE = "include";
    public static final String K_INSTRUMENT = "instrument";
    public static final String K_BARS = "bars";

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

    @Column(name = "language")
    private String language;

    @Column(name = "name", unique = true)
    private String name;

    @Lob
    private String text;

    @Transient
    TimeSpan[] barsData;

    @Transient
    ISecurity[] instrumentsData;

    @Transient
    IScript[] includesData;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    List<StrategyScriptProperties> properties = new ArrayList<StrategyScriptProperties>();

    @Transient
    private HibernateRepository repository;

    public StrategyScriptStore() {
    }

    public StrategyScriptStore(HibernateRepository repository) {
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
            return new URI(repository.getSchema(), HibernateRepository.URI_STRATEGY_PART, id);
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

        properties.setProperty(IScriptStrategy.PROP_NAME, name);
        properties.setProperty(IScriptStrategy.PROP_LANGUAGE, language);
        properties.setProperty(IScriptStrategy.PROP_TEXT, text);

        if (includesData == null) {
            List<IScript> l = new ArrayList<IScript>();
            for (StrategyScriptProperties prop : this.properties) {
                if (K_INCLUDE.equals(prop.getName()) && IScript.class.getName().equals(prop.getType())) {
                    l.add((IScript) StrategyScriptProperties.convert(prop));
                }
            }
            includesData = l.toArray(new IScript[l.size()]);
        }
        properties.setProperty(IScriptStrategy.PROP_INCLUDES, includesData);

        if (instrumentsData == null) {
            List<ISecurity> l = new ArrayList<ISecurity>();
            for (StrategyScriptProperties prop : this.properties) {
                if (K_INSTRUMENT.equals(prop.getName()) && ISecurity.class.getName().equals(prop.getType())) {
                    l.add((ISecurity) StrategyScriptProperties.convert(prop));
                }
            }
            instrumentsData = l.toArray(new ISecurity[l.size()]);
        }
        properties.setProperty(IScriptStrategy.PROP_INSTRUMENTS, instrumentsData);

        if (barsData == null) {
            List<TimeSpan> l = new ArrayList<TimeSpan>();
            for (StrategyScriptProperties prop : this.properties) {
                if (K_BARS.equals(prop.getName()) && TimeSpan.class.getName().equals(prop.getType())) {
                    l.add((TimeSpan) StrategyScriptProperties.convert(prop));
                }
            }
            barsData = l.toArray(new TimeSpan[l.size()]);
        }
        properties.setProperty(IScriptStrategy.PROP_BARS_TIMESPAN, barsData);

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

        this.name = (String) properties.getProperty(IScriptStrategy.PROP_NAME);
        this.language = (String) properties.getProperty(IScriptStrategy.PROP_LANGUAGE);
        this.text = (String) properties.getProperty(IScriptStrategy.PROP_TEXT);

        this.includesData = (IScript[]) properties.getProperty(IScriptStrategy.PROP_INCLUDES);
        this.instrumentsData = (ISecurity[]) properties.getProperty(IScriptStrategy.PROP_INSTRUMENTS);
        this.barsData = (TimeSpan[]) properties.getProperty(IScriptStrategy.PROP_BARS_TIMESPAN);

        List<StrategyScriptProperties> list = new ArrayList<StrategyScriptProperties>();
        if (this.includesData != null) {
            for (int i = 0; i < this.includesData.length; i++) {
                list.add(StrategyScriptProperties.create(K_INCLUDE, this.includesData[i]));
            }
        }
        if (this.instrumentsData != null) {
            for (int i = 0; i < this.instrumentsData.length; i++) {
                list.add(StrategyScriptProperties.create(K_INSTRUMENT, this.instrumentsData[i]));
            }
        }
        if (this.barsData != null) {
            for (int i = 0; i < this.barsData.length; i++) {
                list.add(StrategyScriptProperties.create(K_BARS, this.barsData[i]));
            }
        }

        for (StrategyScriptProperties prop : list) {
            if (!this.properties.contains(prop)) {
                this.properties.add(prop);
            }
        }
        for (Iterator<StrategyScriptProperties> iter = this.properties.iterator(); iter.hasNext();) {
            if (!list.contains(iter.next())) {
                iter.remove();
            }
        }

        session.save(this);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void delete(IProgressMonitor monitor) throws CoreException {
        Session session = repository.getSession();
        session.delete(this);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStore[] fetchChilds(IProgressMonitor monitor) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#createChild()
     */
    @Override
    public IStore createChild() {
        return null;
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
