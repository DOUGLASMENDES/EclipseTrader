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
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipsetrader.core.ats.ITradingSystem;
import org.eclipsetrader.core.ats.ITradingSystemService;
import org.eclipsetrader.ui.DataProviderFactoryMock;
import org.eclipsetrader.ui.DatabindingTestCase;
import org.eclipsetrader.ui.internal.ats.ViewColumn;

public class TradingSystemsViewPartTest extends DatabindingTestCase {

    Shell shell;

    private class TradingSystemsViewPartMock extends TradingSystemsViewPart {

        IWorkbenchPartSite site;

        public TradingSystemsViewPartMock() {
            site = EasyMock.createNiceMock(IWorkbenchPartSite.class);

            tradingSystemService = EasyMock.createNiceMock(ITradingSystemService.class);
            EasyMock.expect(tradingSystemService.getTradeSystems()).andStubReturn(new ITradingSystem[0]);

            dialogSettings = new DialogSettings(VIEW_ID);
            dialogSettings.put(COLUMNS, new String[0]);
            dialogSettings.addNewSection(COLUMN_NAMES);
            dialogSettings.addNewSection(COLUMN_WIDTHS);

            EasyMock.replay(site, tradingSystemService);
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.part.WorkbenchPart#getSite()
         */
        @Override
        public IWorkbenchPartSite getSite() {
            return site;
        }
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        shell = new Shell(Display.getDefault());
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        shell.dispose();
    }

    public void testUpdateColumns() throws Exception {
        TradingSystemsViewPart view = new TradingSystemsViewPartMock();
        view.createPartControl(shell);

        Tree tree = view.viewer.getTree();
        assertEquals(1, tree.getColumnCount());

        view.model.setDataProviders(Arrays.asList(new ViewColumn[] {
            new ViewColumn("Last", new DataProviderFactoryMock("LastTrade", "org.eclipsetrader.ui.providers.LastTrade"))
        }));

        assertEquals(2, tree.getColumnCount());
        assertEquals("Last", tree.getColumn(1).getText());
    }

    public void testUpdateColumnName() throws Exception {
        TradingSystemsViewPart view = new TradingSystemsViewPartMock();
        view.createPartControl(shell);

        Tree tree = view.viewer.getTree();

        view.model.setDataProviders(Arrays.asList(new ViewColumn[] {
            new ViewColumn("Last", new DataProviderFactoryMock("LastTrade", "org.eclipsetrader.ui.providers.LastTrade"))
        }));

        assertEquals("Last", tree.getColumn(1).getText());

        view.model.setDataProviders(Arrays.asList(new ViewColumn[] {
            new ViewColumn("Last Price", new DataProviderFactoryMock("LastTrade", "org.eclipsetrader.ui.providers.LastTrade"))
        }));

        assertEquals("Last Price", tree.getColumn(1).getText());
    }

    public void testUpdateDialogSettings() throws Exception {
        TradingSystemsViewPart view = new TradingSystemsViewPartMock();
        view.createPartControl(shell);

        assertEquals(0, view.dialogSettings.getArray(TradingSystemsViewPart.COLUMNS).length);

        view.model.setDataProviders(Arrays.asList(new ViewColumn[] {
            new ViewColumn("Last", new DataProviderFactoryMock("LastTrade", "org.eclipsetrader.ui.providers.LastTrade"))
        }));

        assertEquals(1, view.dialogSettings.getArray(TradingSystemsViewPart.COLUMNS).length);
    }
}
