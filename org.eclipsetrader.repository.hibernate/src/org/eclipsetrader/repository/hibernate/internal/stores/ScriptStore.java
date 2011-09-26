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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.IScript;
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
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Target;

@Entity
@Table(name = "scripts")
public class ScriptStore implements IStore {

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
    private HibernateRepository repository;

    public ScriptStore() {
    }

    public ScriptStore(HibernateRepository repository) {
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
            return new URI(repository.getSchema(), HibernateRepository.URI_SCRIPT_PART, id);
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

        properties.setProperty(IScript.NAME, name);
        properties.setProperty(IScript.LANGUAGE, language);
        properties.setProperty(IScript.TEXT, text);

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

        this.name = (String) properties.getProperty(IScript.NAME);
        this.language = (String) properties.getProperty(IScript.LANGUAGE);
        this.text = (String) properties.getProperty(IScript.TEXT);

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
