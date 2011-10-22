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

package org.eclipsetrader.ui.internal.ats.monitor;

import java.util.Arrays;

import org.easymock.classextension.EasyMock;
import org.eclipsetrader.core.ats.ITradingSystem;
import org.eclipsetrader.core.ats.ITradingSystemService;
import org.eclipsetrader.ui.DataProviderFactoryMock;
import org.eclipsetrader.ui.DatabindingTestCase;
import org.eclipsetrader.ui.internal.ats.ViewColumn;

public class TradingSystemsViewModelTest extends DatabindingTestCase {

    ITradingSystemService tradingSystemService;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        tradingSystemService = EasyMock.createNiceMock(ITradingSystemService.class);
        EasyMock.expect(tradingSystemService.getTradeSystems()).andStubReturn(new ITradingSystem[0]);
        EasyMock.replay(tradingSystemService);
    }

    public void testSetDataProviders() throws Exception {
        TradingSystemsViewModel model = new TradingSystemsViewModel(tradingSystemService);

        model.setDataProviders(Arrays.asList(new ViewColumn[] {
            new ViewColumn("Last", new DataProviderFactoryMock("LastTrade", "org.eclipsetrader.ui.providers.LastTrade"))
        }));
        assertEquals(1, model.getObservableDataProviders().size());
    }

    public void testKeepExistingsAndAddNewColumns() throws Exception {
        TradingSystemsViewModel model = new TradingSystemsViewModel(tradingSystemService);

        ViewColumn viewColumn1 = new ViewColumn("Last", new DataProviderFactoryMock("LastTrade", "org.eclipsetrader.ui.providers.LastTrade"));
        model.setDataProviders(Arrays.asList(new ViewColumn[] {
            viewColumn1
        }));

        assertSame(viewColumn1, model.getObservableDataProviders().get(0));

        ViewColumn viewColumn2 = new ViewColumn("Bid", new DataProviderFactoryMock("BidPrice", "org.eclipsetrader.ui.providers.BidPrice"));
        model.setDataProviders(Arrays.asList(new ViewColumn[] {
            new ViewColumn("Last", new DataProviderFactoryMock("LastTrade", "org.eclipsetrader.ui.providers.LastTrade")),
            viewColumn2
        }));

        assertSame(viewColumn1, model.getObservableDataProviders().get(0));
        assertSame(viewColumn2, model.getObservableDataProviders().get(1));
    }

    public void testRemoveColumns() throws Exception {
        TradingSystemsViewModel model = new TradingSystemsViewModel(tradingSystemService);

        model.setDataProviders(Arrays.asList(new ViewColumn[] {
            new ViewColumn("Last", new DataProviderFactoryMock("LastTrade", "org.eclipsetrader.ui.providers.LastTrade")),
            new ViewColumn("Bid", new DataProviderFactoryMock("BidPrice", "org.eclipsetrader.ui.providers.BidPrice"))
        }));
        assertEquals(2, model.getObservableDataProviders().size());

        model.setDataProviders(Arrays.asList(new ViewColumn[] {
            new ViewColumn("Bid", new DataProviderFactoryMock("BidPrice", "org.eclipsetrader.ui.providers.BidPrice"))
        }));
        assertEquals(1, model.getObservableDataProviders().size());
    }

    public void testChangeColumnsOrder() throws Exception {
        TradingSystemsViewModel model = new TradingSystemsViewModel(tradingSystemService);

        model.setDataProviders(Arrays.asList(new ViewColumn[] {
            new ViewColumn("Last", new DataProviderFactoryMock("LastTrade", "org.eclipsetrader.ui.providers.LastTrade")),
            new ViewColumn("Bid", new DataProviderFactoryMock("BidPrice", "org.eclipsetrader.ui.providers.BidPrice"))
        }));
        assertEquals(2, model.getObservableDataProviders().size());
        assertEquals("Last", model.getDataProviders().get(0).getName());
        assertEquals("Bid", model.getDataProviders().get(1).getName());

        model.setDataProviders(Arrays.asList(new ViewColumn[] {
            new ViewColumn("Bid", new DataProviderFactoryMock("BidPrice", "org.eclipsetrader.ui.providers.BidPrice")),
            new ViewColumn("Last", new DataProviderFactoryMock("LastTrade", "org.eclipsetrader.ui.providers.LastTrade")),
        }));
        assertEquals(2, model.getObservableDataProviders().size());
        assertEquals("Bid", model.getDataProviders().get(0).getName());
        assertEquals("Last", model.getDataProviders().get(1).getName());
    }

    public void testAddNewAndChangeColumnsOrder() throws Exception {
        TradingSystemsViewModel model = new TradingSystemsViewModel(tradingSystemService);

        model.setDataProviders(Arrays.asList(new ViewColumn[] {
            new ViewColumn("Last", new DataProviderFactoryMock("LastTrade", "org.eclipsetrader.ui.providers.LastTrade")),
            new ViewColumn("Bid", new DataProviderFactoryMock("BidPrice", "org.eclipsetrader.ui.providers.BidPrice"))
        }));
        assertEquals(2, model.getObservableDataProviders().size());
        assertEquals("Last", model.getDataProviders().get(0).getName());
        assertEquals("Bid", model.getDataProviders().get(1).getName());

        model.setDataProviders(Arrays.asList(new ViewColumn[] {
            new ViewColumn("Ask", new DataProviderFactoryMock("AskPrice", "org.eclipsetrader.ui.providers.AskPrice")),
            new ViewColumn("Bid", new DataProviderFactoryMock("BidPrice", "org.eclipsetrader.ui.providers.BidPrice")),
            new ViewColumn("Last", new DataProviderFactoryMock("LastTrade", "org.eclipsetrader.ui.providers.LastTrade")),
        }));
        assertEquals(3, model.getObservableDataProviders().size());
        assertEquals("Ask", model.getDataProviders().get(0).getName());
        assertEquals("Bid", model.getDataProviders().get(1).getName());
        assertEquals("Last", model.getDataProviders().get(2).getName());
    }
}
