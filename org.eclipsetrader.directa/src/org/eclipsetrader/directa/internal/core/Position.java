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

package org.eclipsetrader.directa.internal.core;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.directa.internal.Activator;
import org.eclipsetrader.directa.internal.core.repository.SecurityAdapter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@XmlRootElement(name = "position")
public class Position implements IPosition {

    private static final int IDX_SYMBOL = 5;
    private static final int IDX_PF_QUANTITY = 9;
    private static final int IDX_AVERAGE_PRICE = 10;

    @XmlAttribute(name = "security")
    @XmlJavaTypeAdapter(SecurityAdapter.class)
    ISecurity security;

    @XmlAttribute(name = "quantity")
    Long quantity;

    @XmlAttribute(name = "price")
    Double price;

    public Position() {
    }

    public Position(String line) {
        String[] item = line.split(";"); //$NON-NLS-1$
        security = getSecurityFromSymbol(item[IDX_SYMBOL]);
        quantity = Long.parseLong(item[IDX_PF_QUANTITY]);
        price = Double.parseDouble(item[IDX_AVERAGE_PRICE]);
    }

    public Position(ISecurity security, Long quantity, Double price) {
        this.security = security;
        this.quantity = quantity;
        this.price = price;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IPosition#getPrice()
     */
    @Override
    @XmlTransient
    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IPosition#getQuantity()
     */
    @Override
    @XmlTransient
    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IPosition#getSecurity()
     */
    @Override
    @XmlTransient
    public ISecurity getSecurity() {
        return security;
    }

    ISecurity getSecurityFromSymbol(String symbol) {
        ISecurity security = null;

        if (Activator.getDefault() != null) {
            BundleContext context = Activator.getDefault().getBundle().getBundleContext();
            ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
            if (serviceReference != null) {
                IRepositoryService service = (IRepositoryService) context.getService(serviceReference);

                ISecurity[] securities = service.getSecurities();
                for (int i = 0; i < securities.length; i++) {
                    String feedSymbol = getSymbolFromSecurity(securities[i]);
                    if (feedSymbol != null && feedSymbol.equals(symbol)) {
                        security = securities[i];
                        break;
                    }
                }

                context.ungetService(serviceReference);
            }
        }

        if (security == null) {
            security = new Security(symbol, null);
        }

        return security;
    }

    String getSymbolFromSecurity(ISecurity security) {
        IFeedIdentifier identifier = security.getIdentifier();
        if (identifier == null) {
            return null;
        }

        IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
        if (properties != null) {
            for (int p = 0; p < WebConnector.PROPERTIES.length; p++) {
                if (properties.getProperty(WebConnector.PROPERTIES[p]) != null) {
                    return properties.getProperty(WebConnector.PROPERTIES[p]);
                }
            }
        }

        return null;
    }
}
