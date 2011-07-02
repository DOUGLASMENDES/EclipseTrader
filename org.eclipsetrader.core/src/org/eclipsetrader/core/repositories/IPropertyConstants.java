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

package org.eclipsetrader.core.repositories;

/**
 * Constants used to define the store object's properties.
 *
 * @since 1.0
 */
public interface IPropertyConstants {

    /**
     * The type of object the properties set is referring to.
     */
    public static final String OBJECT_TYPE = "type";

    /**
     * A java.lang.String property representing the name of the object.
     */
    public static final String NAME = "name";

    /**
     * A java.util.Currency property.
     */
    public static final String CURRENCY = "currency";

    /**
     * An <code>ISecurity</code> property.
     * @see org.eclipsetrader.core.instruments.ISecurity
     */
    public static final String SECURITY = "security";

    /**
     * A class implementing the org.eclipsetrader.core.model.ISecurityIdentifier interface.
     */
    public static final String IDENTIFIER = "identifier";

    /**
     * A class implementing the org.eclipsetrader.core.model.IDividendSchedule interface.
     */
    public static final String DIVIDENDS = "dividends";

    /**
     * A class implementing the org.eclipsetrader.core.model.ISplitSchedule interface.
     */
    public static final String SPLITS = "splits";

    /**
     * A org.eclipsetrader.core.model.UserProperties property containing the user
     * defined properties assigned to the object.
     */
    public static final String USER_PROPERTIES = "user-properties";

    /**
     * A class implementing the org.eclipsetrader.core.feed.IHistory interface.
     */
    public static final String HISTORY = "history";

    /**
     * The id of an extension implementing org.eclipsetrader.core.storage.IObjectElementFactory used
     * to create the object.
     */
    public static final String ELEMENT_FACTORY = "factory";

    /**
     * An array of IOHLC elements.
     */
    public static final String BARS = "bars";

    /**
     * An instance of <code>TimeSpan</code> class representing the aggregation
     * level of an IOHLC array.
     */
    public static final String TIME_SPAN = "bars-time-span";

    /**
     * An instance of java.util.Date representing the starting date of
     * an IOHLC array.
     */
    public static final String BARS_DATE = "bars-date";

    public static final String SYMBOL = "symbol";

    public static final String MARKET = "market";

    /**
     * An array of <code>IHolding</code> objects
     * @see org.eclipsetrader.core.views.IHolding
     */
    public static final String HOLDINGS = "holdings";

    /**
     * An array of <code>IColumn</code> objects
     * @see org.eclipsetrader.core.views.IColumn
     */
    public static final String COLUMNS = "columns";

    public static final String PURCHASE_DATE = "purchase-date";

    public static final String PURCHASE_PRICE = "purchase-price";

    public static final String PURCHASE_QUANTITY = "purchase-quantity";
}
