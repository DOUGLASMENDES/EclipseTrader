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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.core.views.IHolding;
import org.eclipsetrader.repository.local.LocalRepository;
import org.eclipsetrader.repository.local.internal.Activator;
import org.eclipsetrader.repository.local.internal.types.DateTimeAdapter;
import org.eclipsetrader.repository.local.internal.types.SecurityAdapter;

@XmlRootElement(name = "trade")
public class TradeStore implements IStore {

    @XmlAttribute(name = "id")
    private Integer id;

    @XmlAttribute(name = "date")
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private Date date;

    @XmlAttribute(name = "security")
    @XmlJavaTypeAdapter(SecurityAdapter.class)
    private ISecurity security;

    @XmlAttribute(name = "quantity")
    private Long quantity;

    @XmlAttribute(name = "price")
    private Double price;

    protected TradeStore() {
    }

    public TradeStore(Integer id) {
        this.id = id;
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
    @XmlTransient
    public IRepository getRepository() {
        return Activator.getDefault().getRepository();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#toURI()
     */
    @Override
    public URI toURI() {
        try {
            return new URI(LocalRepository.URI_SCHEMA, LocalRepository.URI_TRADE_PART, String.valueOf(id));
        } catch (URISyntaxException e) {
        }
        return null;
    }
}
