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

package org.eclipsetrader.internal.brokers.paper.transactions;

import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.osgi.util.NLS;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.Cash;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.ITransaction;
import org.eclipsetrader.internal.brokers.paper.types.DateTimeAdapter;
import org.eclipsetrader.internal.brokers.paper.types.DoubleValueAdapter;
import org.eclipsetrader.internal.brokers.paper.types.SecurityAdapter;

@XmlRootElement(name = "stock")
public class StockTransaction implements ITransaction {
	@XmlAttribute(name = "date")
	@XmlJavaTypeAdapter(DateTimeAdapter.class)
	private Date date;

	@XmlAttribute(name = "security")
	@XmlJavaTypeAdapter(SecurityAdapter.class)
	private ISecurity security;

	@XmlAttribute(name = "quantity")
	private Long quantity;

	@XmlAttribute(name = "price")
	@XmlJavaTypeAdapter(DoubleValueAdapter.class)
	private Double price;

	protected StockTransaction() {
	}

	public StockTransaction(ISecurity security, Long quantity, Double price) {
	    this.date = new Date();
	    this.security = security;
	    this.quantity = quantity;
	    this.price = price;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.ITransaction#getId()
	 */
	@XmlTransient
	public String getId() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.ITransaction#getDate()
	 */
	@XmlTransient
	public Date getDate() {
		return date;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.ITransaction#getDescription()
	 */
	@XmlTransient
	public String getDescription() {
		return NLS.bind("{1} {0} at {2}", new Object[] {
				security.getName(),
				String.valueOf(quantity),
				String.valueOf(price),
			});
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.ITransaction#getAmount()
	 */
	@XmlTransient
	public Cash getAmount() {
		return new Cash(quantity * price, Currency.getInstance(Locale.getDefault()));
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.ITransaction#getOrder()
	 */
	@XmlTransient
	public IOrder getOrder() {
		return null;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITransaction#getTransactions()
     */
	@XmlTransient
    public ITransaction[] getTransactions() {
	    return null;
    }
}
