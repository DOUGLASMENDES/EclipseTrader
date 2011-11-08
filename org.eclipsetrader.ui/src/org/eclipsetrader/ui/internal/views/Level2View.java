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

package org.eclipsetrader.ui.internal.views;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.IBookEntry;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedConnector2;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedService;
import org.eclipsetrader.core.feed.IFeedSubscription2;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.ISubscriptionListener;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.QuoteDelta;
import org.eclipsetrader.core.feed.QuoteEvent;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.ITradingService;
import org.eclipsetrader.ui.SelectionProvider;
import org.eclipsetrader.ui.UIConstants;
import org.eclipsetrader.ui.internal.UIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Level2View extends ViewPart {

    public static final String VIEW_ID = "org.eclipsetrader.ui.views.level2";
    private static final String VIEW_TITLE_TOOLTIP = "Level II - {0}";

    private IMarketService marketService;
    private IFeedService feedService;

    private Text symbol;
    private Label activeConnector;

    private Composite summaryGroup;
    private Label time;
    private Label volume;
    private Label last;
    private Label high;
    private Label change;
    private Label low;
    private PressureBar pressureBar;
    private Table table;

    private DateFormat timeFormatter = DateFormat.getTimeInstance(SimpleDateFormat.MEDIUM);
    private NumberFormat numberFormatter = NumberFormat.getInstance();
    private NumberFormat priceFormatter = NumberFormat.getInstance();
    private NumberFormat percentageFormatter = NumberFormat.getInstance();

    private IMemento memento;
    private IFeedConnector2 connector;
    private IFeedSubscription2 subscription;
    private ITrade lastTrade;
    private ILastClose lastClose;
    private IBook lastBook;
    private IBook nextBook;

    private Action showMarketMakerAction;
    private Action hideSummaryAction;

    private ISubscriptionListener subscriptionListener = new ISubscriptionListener() {

        @Override
        public void quoteUpdate(QuoteEvent event) {
            onQuoteUpdate(event);
        }
    };

    private SelectionAdapter connectionSelectionListener = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (e.widget.getData() instanceof IFeedConnector2) {
                IFeedConnector2 newConnector = (IFeedConnector2) e.widget.getData();
                onChangeConnector(newConnector);
            }
        }
    };

    private Runnable bookUpdateRunnable = new Runnable() {

        @Override
        public void run() {
            if (table == null || table.isDisposed()) {
                return;
            }
            if (lastBook != nextBook) {
                onBookUpdate(lastBook = nextBook);
            }
            Display.getDefault().timerExec(100, bookUpdateRunnable);
        }
    };

    public Level2View() {
        numberFormatter.setGroupingUsed(true);
        numberFormatter.setMinimumIntegerDigits(1);
        numberFormatter.setMinimumFractionDigits(0);
        numberFormatter.setMaximumFractionDigits(0);

        priceFormatter.setGroupingUsed(true);
        priceFormatter.setMinimumIntegerDigits(1);
        priceFormatter.setMinimumFractionDigits(2);
        priceFormatter.setMaximumFractionDigits(4);

        percentageFormatter.setGroupingUsed(true);
        percentageFormatter.setMinimumIntegerDigits(1);
        percentageFormatter.setMinimumFractionDigits(2);
        percentageFormatter.setMaximumFractionDigits(2);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        this.memento = memento;

        BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();

        ServiceReference<IMarketService> marketServiceReference = context.getServiceReference(IMarketService.class);
        marketService = context.getService(marketServiceReference);

        ServiceReference<IFeedService> feedServiceReference = context.getServiceReference(IFeedService.class);
        feedService = context.getService(feedServiceReference);

        showMarketMakerAction = new Action("Show Market Maker", IAction.AS_CHECK_BOX) {

            @Override
            public void run() {
                updateViewer();
                if (lastBook != null) {
                    onBookUpdate(lastBook);
                }
            }
        };

        hideSummaryAction = new Action("Hide Summary", IAction.AS_CHECK_BOX) {

            @Override
            public void run() {
                summaryGroup.setVisible(!isChecked());
                ((GridData) summaryGroup.getLayoutData()).exclude = isChecked();
                summaryGroup.getParent().layout();
            }
        };

        if (memento != null) {
            hideSummaryAction.setChecked("true".equals(memento.getString("hide-summary")));
        }

        IActionBars actionBars = site.getActionBars();

        IMenuManager menuManager = actionBars.getMenuManager();
        menuManager.add(showMarketMakerAction);
        menuManager.add(new Separator());
        menuManager.add(hideSummaryAction);

        actionBars.updateActionBars();

        site.setSelectionProvider(new SelectionProvider());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        content.setLayout(gridLayout);

        GC gc = new GC(content);
        gc.setFont(JFaceResources.getDialogFont());
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();

        Composite group = new Composite(content, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        group.setLayout(new GridLayout(4, false));

        Label label = new Label(group, SWT.NONE);
        label.setText("Symbol");
        symbol = new Text(group, SWT.BORDER);
        symbol.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 15), SWT.DEFAULT));
        symbol.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                onSetSymbol();
            }
        });
        symbol.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                onSetSymbol();
            }
        });

        final ImageHyperlink connectorButton = new ImageHyperlink(group, SWT.NONE);
        connectorButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        connectorButton.setImage(UIActivator.getDefault().getImageRegistry().get(UIConstants.TOOLBAR_ARROW_RIGHT));
        connectorButton.setToolTipText("Select Data Source");
        connectorButton.addHyperlinkListener(new IHyperlinkListener() {

            private Menu dropDownMenu;

            @Override
            public void linkActivated(HyperlinkEvent e) {
                if (dropDownMenu != null) {
                    dropDownMenu.dispose();
                }

                dropDownMenu = new Menu(connectorButton);
                List<IFeedConnector> c = Arrays.asList(feedService.getConnectors());
                Collections.sort(c, new Comparator<IFeedConnector>() {

                    @Override
                    public int compare(IFeedConnector o1, IFeedConnector o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                for (IFeedConnector connector : c) {
                    if (connector instanceof IFeedConnector2) {
                        MenuItem menuItem = new MenuItem(dropDownMenu, SWT.CHECK);
                        menuItem.setText(connector.getName());
                        menuItem.setData(connector);
                        menuItem.setSelection(Level2View.this.connector == connector);
                        menuItem.addSelectionListener(connectionSelectionListener);
                    }
                }
                dropDownMenu.setVisible(true);
            }

            @Override
            public void linkEntered(HyperlinkEvent e) {
            }

            @Override
            public void linkExited(HyperlinkEvent e) {
            }
        });

        activeConnector = new Label(group, SWT.NONE);
        activeConnector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        summaryGroup = createSummary(content);
        if (hideSummaryAction != null) {
            summaryGroup.setVisible(!hideSummaryAction.isChecked());
            ((GridData) summaryGroup.getLayoutData()).exclude = hideSummaryAction.isChecked();
        }

        pressureBar = new PressureBar(content, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.heightHint = 16;
        pressureBar.getControl().setLayoutData(gridData);

        createBookViewer(content);

        if (memento != null) {
            String s = memento.getString("symbol");
            if (s != null) {
                symbol.setText(s);
                ISecurity security = getSecurityFromSymbol(s);
                if (security != null) {
                    getSite().getSelectionProvider().setSelection(new StructuredSelection(security));
                    setPartName(security.getName());
                    setTitleToolTip(NLS.bind(VIEW_TITLE_TOOLTIP, new Object[] {
                        security.getName()
                    }));
                }
            }

            String id = memento.getString("connector");
            if (id != null) {
                IFeedConnector connector = feedService.getConnector(id);
                if (connector == null) {
                    connector = CoreActivator.getDefault().getDefaultConnector();
                }
                if (connector instanceof IFeedConnector2) {
                    this.connector = (IFeedConnector2) connector;
                    activeConnector.setText(this.connector.getName());
                }
            }

            if (s != null && connector != null) {
                subscription = this.connector.subscribeLevel2(s);

                subscription.addSubscriptionListener(subscriptionListener);

                lastClose = subscription.getLastClose();
                lastTrade = subscription.getTrade();
                update(subscription.getTodayOHL());
                update();
            }
        }

        Display.getDefault().timerExec(100, bookUpdateRunnable);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
     */
    @Override
    public void saveState(IMemento memento) {
        memento.putString("symbol", symbol.getText());
        if (connector != null) {
            memento.putString("connector", connector.getId());
        }
        if (hideSummaryAction.isChecked()) {
            memento.putString("hide-summary", "true");
        }
        super.saveState(memento);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        symbol.setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        if (subscription != null) {
            subscription.removeSubscriptionListener(subscriptionListener);
            subscription.dispose();
        }

        BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();

        ServiceReference<IMarketService> marketServiceReference = context.getServiceReference(IMarketService.class);
        context.ungetService(marketServiceReference);
        ServiceReference<IFeedService> feedServiceReference = context.getServiceReference(IFeedService.class);
        context.ungetService(feedServiceReference);

        super.dispose();
    }

    protected void createBookViewer(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        TableColumnLayout tableLayout = new TableColumnLayout();
        content.setLayout(tableLayout);

        table = new Table(content, SWT.MULTI | SWT.FULL_SELECTION | SWT.NO_FOCUS | SWT.V_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);

        table.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                table.deselectAll();
            }
        });

        ICommandService commandService = (ICommandService) getSite().getService(ICommandService.class);
        IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
        new Level2QuickTradeDecorator(table, commandService, handlerService);

        updateViewer();
    }

    protected void updateViewer() {
        TableColumnLayout tableLayout = (TableColumnLayout) table.getParent().getLayout();

        int columnIndex = 0;
        TableColumn tableColumn;

        if (showMarketMakerAction.isChecked()) {
            tableColumn = columnIndex < table.getColumnCount() ? table.getColumn(columnIndex) : new TableColumn(table, SWT.CENTER);
            tableColumn.setText("MM");
            tableLayout.setColumnData(tableColumn, new ColumnWeightData(5));
            columnIndex++;
        }

        tableColumn = columnIndex < table.getColumnCount() ? table.getColumn(columnIndex) : new TableColumn(table, SWT.CENTER);
        tableColumn.setText("#");
        tableLayout.setColumnData(tableColumn, new ColumnWeightData(showMarketMakerAction.isChecked() ? 5 : 6));
        columnIndex++;

        tableColumn = columnIndex < table.getColumnCount() ? table.getColumn(columnIndex) : new TableColumn(table, SWT.CENTER);
        tableColumn.setText("Q.ty");
        tableLayout.setColumnData(tableColumn, new ColumnWeightData(showMarketMakerAction.isChecked() ? 22 : 24));
        columnIndex++;

        tableColumn = columnIndex < table.getColumnCount() ? table.getColumn(columnIndex) : new TableColumn(table, SWT.CENTER);
        tableColumn.setText("Bid");
        tableLayout.setColumnData(tableColumn, new ColumnWeightData(showMarketMakerAction.isChecked() ? 18 : 20));
        columnIndex++;

        tableColumn = columnIndex < table.getColumnCount() ? table.getColumn(columnIndex) : new TableColumn(table, SWT.CENTER);
        tableColumn.setText("Ask");
        tableLayout.setColumnData(tableColumn, new ColumnWeightData(showMarketMakerAction.isChecked() ? 18 : 20));
        columnIndex++;

        tableColumn = columnIndex < table.getColumnCount() ? table.getColumn(columnIndex) : new TableColumn(table, SWT.CENTER);
        tableColumn.setText("Q.ty");
        tableLayout.setColumnData(tableColumn, new ColumnWeightData(showMarketMakerAction.isChecked() ? 21 : 24));
        columnIndex++;

        tableColumn = columnIndex < table.getColumnCount() ? table.getColumn(columnIndex) : new TableColumn(table, SWT.CENTER);
        tableColumn.setText("#");
        tableLayout.setColumnData(tableColumn, new ColumnWeightData(showMarketMakerAction.isChecked() ? 5 : 6));
        columnIndex++;

        if (showMarketMakerAction.isChecked()) {
            tableColumn = columnIndex < table.getColumnCount() ? table.getColumn(columnIndex) : new TableColumn(table, SWT.CENTER);
            tableColumn.setText("MM");
            tableLayout.setColumnData(tableColumn, new ColumnWeightData(5));
            columnIndex++;
        }

        while (table.getColumnCount() > columnIndex) {
            table.getColumn(table.getColumnCount() - 1).dispose();
        }

        table.getParent().layout();
    }

    protected Composite createSummary(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout gridLayout = new GridLayout(4, false);
        gridLayout.horizontalSpacing = gridLayout.horizontalSpacing * 2;
        content.setLayout(gridLayout);

        Label label = new Label(content, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));

        label = new Label(content, SWT.NONE);
        label.setText("Time");
        time = new Label(content, SWT.RIGHT);
        time.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        label = new Label(content, SWT.NONE);
        label.setText("Volume");
        volume = new Label(content, SWT.RIGHT);
        volume.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        label = new Label(content, SWT.NONE);
        label.setText("Last Price");
        last = new Label(content, SWT.RIGHT);
        last.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        label = new Label(content, SWT.NONE);
        label.setText("High");
        high = new Label(content, SWT.RIGHT);
        high.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        label = new Label(content, SWT.NONE);
        label.setText("Change");
        change = new Label(content, SWT.RIGHT);
        change.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        label = new Label(content, SWT.NONE);
        label.setText("Low");
        low = new Label(content, SWT.RIGHT);
        low.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        return content;
    }

    protected void updateBid(TableItem tableItem, IBookEntry entry) {
        int columnIndex = 0;

        if (entry != null) {
            if (showMarketMakerAction.isChecked()) {
                String s = entry.getMarketMaker() != null ? entry.getMarketMaker() : "";
                if (!s.equals(tableItem.getText(columnIndex))) {
                    tableItem.setText(columnIndex, s);
                }
                columnIndex++;
            }

            String s = numberFormatter.format(entry.getProposals());
            if (!s.equals(tableItem.getText(columnIndex))) {
                tableItem.setText(columnIndex, s);
            }
            columnIndex++;

            s = numberFormatter.format(entry.getQuantity());
            if (!s.equals(tableItem.getText(columnIndex))) {
                tableItem.setText(columnIndex, s);
            }
            columnIndex++;

            s = priceFormatter.format(entry.getPrice());
            if (!s.equals(tableItem.getText(columnIndex))) {
                tableItem.setText(columnIndex, s);
            }
            columnIndex++;
        }
        else {
            if (showMarketMakerAction.isChecked()) {
                tableItem.setText(columnIndex, "");
                tableItem.setBackground(columnIndex++, null);
            }

            tableItem.setText(columnIndex, "");
            tableItem.setBackground(columnIndex++, null);

            tableItem.setText(columnIndex, "");
            tableItem.setBackground(columnIndex++, null);

            tableItem.setText(columnIndex, "");
            tableItem.setBackground(columnIndex++, null);
        }

        tableItem.setData("bid", entry);
    }

    protected void updateAsk(TableItem tableItem, IBookEntry entry) {
        int columnIndex = tableItem.getParent().getColumnCount() - 1;

        if (entry != null) {
            if (showMarketMakerAction.isChecked()) {
                String s = entry.getMarketMaker() != null ? entry.getMarketMaker() : "";
                if (!s.equals(tableItem.getText(columnIndex))) {
                    tableItem.setText(columnIndex, s);
                }
                columnIndex--;
            }

            String s = numberFormatter.format(entry.getProposals());
            if (!s.equals(tableItem.getText(columnIndex))) {
                tableItem.setText(columnIndex, s);
            }
            columnIndex--;

            s = numberFormatter.format(entry.getQuantity());
            if (!s.equals(tableItem.getText(columnIndex))) {
                tableItem.setText(columnIndex, s);
            }
            columnIndex--;

            s = priceFormatter.format(entry.getPrice());
            if (!s.equals(tableItem.getText(columnIndex))) {
                tableItem.setText(columnIndex, s);
            }
            columnIndex--;
        }
        else {
            if (showMarketMakerAction.isChecked()) {
                tableItem.setText(columnIndex, "");
                tableItem.setBackground(columnIndex--, null);
            }

            tableItem.setText(columnIndex, "");
            tableItem.setBackground(columnIndex--, null);

            tableItem.setText(columnIndex, "");
            tableItem.setBackground(columnIndex--, null);

            tableItem.setText(columnIndex, "");
            tableItem.setBackground(columnIndex--, null);
        }

        tableItem.setData("ask", entry);
    }

    protected void onBookUpdate(IBook book) {
        IBookEntry[] bidEntries = book.getBidProposals();
        IBookEntry[] askEntries = book.getAskProposals();
        int rows = Math.max(bidEntries.length, askEntries.length);

        table.setRedraw(false);
        boolean doLayout = rows != table.getItemCount();
        try {
            for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
                TableItem tableItem = rowIndex < table.getItemCount() ? table.getItem(rowIndex) : new TableItem(table, SWT.NONE);
                updateBid(tableItem, rowIndex < bidEntries.length ? bidEntries[rowIndex] : null);
                updateAsk(tableItem, rowIndex < askEntries.length ? askEntries[rowIndex] : null);
            }
            while (table.getItemCount() > rows) {
                table.getItem(table.getItemCount() - 1).dispose();
            }
        } finally {
            table.setRedraw(true);
        }

        if (doLayout) {
            table.layout();
            table.getParent().layout();
            table.layout();
            table.getParent().layout();
        }

        long[] leftWeights = new long[Math.min(bidEntries.length, 5)];
        for (int i = 0; i < leftWeights.length; i++) {
            leftWeights[i] = bidEntries[i] != null && bidEntries[i].getQuantity() != null ? bidEntries[i].getQuantity() : 0;
        }

        long[] rightWeights = new long[Math.min(askEntries.length, 5)];
        for (int i = 0; i < rightWeights.length; i++) {
            rightWeights[i] = askEntries[i] != null && askEntries[i].getQuantity() != null ? askEntries[i].getQuantity() : 0;
        }

        pressureBar.setWeights(leftWeights, rightWeights);
    }

    protected void onSetSymbol() {
        if (subscription != null && subscription.getSymbol().equals(symbol.getText())) {
            return;
        }

        if (connector != null) {
            if (subscription != null) {
                subscription.removeSubscriptionListener(subscriptionListener);
                subscription.dispose();
            }

            subscription = connector.subscribeLevel2(symbol.getText());
            subscription.addSubscriptionListener(subscriptionListener);

            lastClose = subscription.getLastClose();
            lastTrade = subscription.getTrade();
            update(subscription.getTodayOHL());

            update();

            IBook book = subscription.getBook();
            if (book != null) {
                onBookUpdate(book);
            }

            ISecurity security = getSecurityFromSymbol(symbol.getText());
            if (security != null) {
                getSite().getSelectionProvider().setSelection(new StructuredSelection(security));
                setPartName(security.getName());
                setTitleToolTip(NLS.bind(VIEW_TITLE_TOOLTIP, new Object[] {
                    security.getName()
                }));
            }
        }
    }

    protected void onChangeConnector(IFeedConnector2 newConnector) {
        if (newConnector != connector) {
            if (connector != null && subscription != null) {
                subscription.removeSubscriptionListener(subscriptionListener);
                subscription.dispose();
                subscription = null;
            }

            connector = newConnector;
            activeConnector.setText(connector != null ? connector.getName() : "");

            if (connector != null && !symbol.getText().equals("")) {
                subscription = connector.subscribeLevel2(symbol.getText());
                subscription.addSubscriptionListener(subscriptionListener);

                lastClose = subscription.getLastClose();
                lastTrade = subscription.getTrade();
                update(subscription.getTodayOHL());

                update();

                IBook book = subscription.getBook();
                if (book != null) {
                    onBookUpdate(book);
                }
            }
        }
    }

    public void setSecurity(ISecurity security) {
        IFeedConnector connector = null;

        IMarket[] market = marketService.getMarkets();
        for (int i = 0; i < market.length; i++) {
            if (market[i].hasMember(security)) {
                connector = market[i].getLiveFeedConnector();
                break;
            }
        }

        if (connector == null) {
            connector = CoreActivator.getDefault().getDefaultConnector();
        }

        if (connector instanceof IFeedConnector2) {
            this.connector = (IFeedConnector2) connector;
        }

        if (this.connector != null) {
            activeConnector.setText(this.connector.getName());

            if (subscription != null) {
                subscription.removeSubscriptionListener(subscriptionListener);
                subscription.dispose();
            }

            IFeedIdentifier feedIdentifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);
            if (feedIdentifier != null) {
                subscription = this.connector.subscribeLevel2(feedIdentifier);

                subscription.addSubscriptionListener(subscriptionListener);

                symbol.setText(subscription.getSymbol());

                lastClose = subscription.getLastClose();
                lastTrade = subscription.getTrade();
                update(subscription.getTodayOHL());

                update();

                IBook book = subscription.getBook();
                if (book != null) {
                    onBookUpdate(book);
                }
            }
        }

        getSite().getSelectionProvider().setSelection(new StructuredSelection(security));
    }

    protected void onQuoteUpdate(final QuoteEvent event) {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (table.isDisposed()) {
                    return;
                }

                QuoteDelta[] delta = event.getDelta();
                for (int i = 0; i < delta.length; i++) {
                    if (delta[i].getNewValue() instanceof IBook) {
                        nextBook = (IBook) delta[i].getNewValue();
                    }
                    if (delta[i].getNewValue() instanceof ITrade) {
                        lastTrade = (ITrade) delta[i].getNewValue();
                        update();
                    }
                    if (delta[i].getNewValue() instanceof ITodayOHL) {
                        ITodayOHL todayOHL = (ITodayOHL) delta[i].getNewValue();
                        update(todayOHL);
                    }
                    if (delta[i].getNewValue() instanceof ILastClose) {
                        lastClose = (ILastClose) delta[i].getNewValue();
                        update();
                    }
                }
            }
        });
    }

    protected void update() {
        time.setText(lastTrade != null && lastTrade.getTime() != null ? timeFormatter.format(lastTrade.getTime()) : "");
        last.setText(lastTrade != null && lastTrade.getPrice() != null ? priceFormatter.format(lastTrade.getPrice()) : "");
        volume.setText(lastTrade != null && lastTrade.getVolume() != null ? numberFormatter.format(lastTrade.getVolume()) : "");

        if (lastTrade != null && lastClose != null && lastClose.getPrice() != null && lastTrade.getPrice() != null) {
            double changePercent = (lastTrade.getPrice() - lastClose.getPrice()) / lastClose.getPrice() * 100.0;
            change.setText(NLS.bind("{0}{1}%", new Object[] {
                changePercent < 0 ? "-" : changePercent > 0 ? "+" : "",
                percentageFormatter.format(Math.abs(changePercent)),
            }));
        }
        else {
            change.setText("");
        }
    }

    protected void update(ITodayOHL todayOHL) {
        high.setText(todayOHL != null && todayOHL.getHigh() != null ? priceFormatter.format(todayOHL.getHigh()) : "");
        low.setText(todayOHL != null && todayOHL.getLow() != null ? priceFormatter.format(todayOHL.getLow()) : "");
    }

    protected ISecurity getSecurityFromSymbol(String symbol) {
        ISecurity security = null;

        BundleContext context = UIActivator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(ITradingService.class.getName());
        if (serviceReference != null) {
            ITradingService service = (ITradingService) context.getService(serviceReference);
            IBroker[] broker = service.getBrokers();
            for (int i = 0; i < broker.length; i++) {
                security = broker[i].getSecurityFromSymbol(symbol);
                if (security != null) {
                    break;
                }
            }

            context.ungetService(serviceReference);
        }

        return security;
    }
}
