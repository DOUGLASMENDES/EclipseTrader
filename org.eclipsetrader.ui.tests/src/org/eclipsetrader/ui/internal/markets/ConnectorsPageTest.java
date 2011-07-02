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

package org.eclipsetrader.ui.internal.markets;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipsetrader.core.internal.markets.Market;
import org.eclipsetrader.core.internal.markets.MarketTime;
import org.eclipsetrader.ui.internal.TestBackfillConnector;
import org.eclipsetrader.ui.internal.TestFeedConnector;

public class ConnectorsPageTest extends TestCase {

    private Shell shell;
    private Market market;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        shell = new Shell(Display.getCurrent());

        market = new Market("Test", new ArrayList<MarketTime>());
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        shell.dispose();
        while (Display.getCurrent().readAndDispatch()) {
            ;
        }
    }

    public void testFillFieldsFromEmptyElement() throws Exception {
        ConnectorsPage page = new ConnectorsPage();
        page.setElement(market);
        page.createContents(shell);
        assertEquals("Default (None)", page.liveFeed.getCombo().getText());
        assertEquals("None", page.backfillFeed.getCombo().getText());
        assertEquals("Default (None)", page.intradayBackfillFeed.getCombo().getText());
    }

    public void testFillFieldsFromElement() throws Exception {
        ConnectorsPage page = new ConnectorsPage();
        market.setLiveFeedConnector(new TestFeedConnector("feed.1", "Test Feed"));
        market.setBackfillConnector(new TestBackfillConnector("backfill.1", "Test Backfill"));
        page.setElement(market);
        page.createContents(shell);
        assertEquals("Test Feed", page.liveFeed.getCombo().getText());
        assertEquals("Test Backfill", page.backfillFeed.getCombo().getText());
        assertEquals("Default (Test Backfill)", page.intradayBackfillFeed.getCombo().getText());
    }
}
