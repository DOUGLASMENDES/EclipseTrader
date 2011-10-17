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

package org.eclipsetrader.ui.internal.trading.portfolio;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipsetrader.internal.ui.trading.portfolio.messages"; //$NON-NLS-1$

    public static String PortfolioViewPart_CollapseAll;
    public static String PortfolioViewPart_ExpandAll;
    public static String PortfolioViewPart_LoadingPortfolio;
    public static String PortfolioViewPart_PL;
    public static String PortfolioViewPart_Position;
    public static String PortfolioViewPart_Price;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
