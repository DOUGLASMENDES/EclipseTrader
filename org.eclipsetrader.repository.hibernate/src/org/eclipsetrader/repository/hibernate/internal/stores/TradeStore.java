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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.core.views.IHolding;
import org.eclipsetrader.repository.hibernate.HibernateRepository;
import org.eclipsetrader.repository.hibernate.internal.types.SecurityType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Target;

@Entity
@Table(name = "trades")
public class TradeStore implements IStore {

    @Id
    @Column(name = "id", length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String id;

    @Version
    @Column(name = "version")
    @SuppressWarnings("unused")
    private Integer version;

    @Column(name = "date")
    private Date date;

    @Column(name = "instrument")
    @Target(SecurityType.class)
    private ISecurity security;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "price")
    private Double price;

    @Transient
    private HibernateRepository repository;

    public TradeStore() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStoreProperties fetchProperties(IProgressMonitor monitor) {
        StoreProperties properties = new StoreProperties();

        properties.setProperty(IPropertyConstants.OBJECT_TYPE, IHolding.class.getName());

        properties.setProperty(IPropertyConstants.PURCHASE_DATE, date);
        properties.setProperty(IPropertyConstants.SECURITY, security);
        properties.setProperty(IPropertyConstants.PURCHASE_DATE, security);
        properties.setProperty(IPropertyConstants.PURCHASE_QUANTITY, quantity);
        properties.setProperty(IPropertyConstants.PURCHASE_PRICE, price);

        return properties;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
        date = (Date) properties.getProperty(IPropertyConstants.PURCHASE_DATE);
        security = (ISecurity) properties.getProperty(IPropertyConstants.SECURITY);
        quantity = (Long) properties.getProperty(IPropertyConstants.PURCHASE_QUANTITY);
        price = (Double) properties.getProperty(IPropertyConstants.PURCHASE_PRICE);
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
    public IStore[] fetchChilds(IProgressMonitor monitor) {
        return new IStore[0];
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
    public IRepository getRepository() {
        return repository;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#toURI()
     */
    @Override
    public URI toURI() {
        try {
            return new URI(repository.getSchema(), HibernateRepository.URI_TRADE_PART, id);
        } catch (URISyntaxException e) {
        }
        return null;
    }
}
