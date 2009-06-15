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

package org.eclipsetrader.ui.internal.views;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
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
import org.eclipsetrader.ui.UIConstants;
import org.eclipsetrader.ui.internal.SelectionProvider;
import org.eclipsetrader.ui.internal.UIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Level2View extends ViewPart {
	public static final String VIEW_ID = "org.eclipsetrader.ui.views.level2";
	private static final String K_FADE_LEVELS = "fade-levels";
	private static final int FADE_TIMER = 500;

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
	private Color tickBackgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
	private Color[] tickFade = new Color[3];

	private IMemento memento;
	private IFeedConnector2 connector;
	private IFeedSubscription2 subscription;
	private ILastClose lastClose;
	private IBook book;

	private Action showMarketMakerAction;
	private Action hideSummaryAction;

	private ISubscriptionListener subscriptionListener = new ISubscriptionListener() {
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

	private Runnable fadeUpdateRunnable = new Runnable() {
		public void run() {
			if (!table.isDisposed()) {
				table.setRedraw(false);
				try {
					for (TableItem tableItem : table.getItems()) {
						int[] timers = (int[]) tableItem.getData(K_FADE_LEVELS);
						if (timers != null) {
							for (int i = 0; i < timers.length; i++) {
								if (timers[i] > 0) {
									timers[i]--;
									updateBackground(tableItem, i);
								}
							}
						}
					}
				} finally {
					table.setRedraw(true);
				}
				table.getDisplay().timerExec(FADE_TIMER, fadeUpdateRunnable);
			}
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

		showMarketMakerAction = new Action("Show Market Maker", Action.AS_CHECK_BOX) {
			@Override
			public void run() {
				updateViewer();
				if (book != null)
					onBookUpdate(book);
			}
		};

		hideSummaryAction = new Action("Hide Summary", Action.AS_CHECK_BOX) {
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

			public void linkActivated(HyperlinkEvent e) {
				if (dropDownMenu != null)
					dropDownMenu.dispose();

				dropDownMenu = new Menu(connectorButton);
				List<IFeedConnector> c = Arrays.asList(getFeedService().getConnectors());
				Collections.sort(c, new Comparator<IFeedConnector>() {
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

			public void linkEntered(HyperlinkEvent e) {
			}

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

		setTickBackground(Display.getDefault().getSystemColor(SWT.COLOR_TITLE_BACKGROUND).getRGB());
		table.getDisplay().timerExec(FADE_TIMER, fadeUpdateRunnable);

		if (memento != null) {
			String s = memento.getString("symbol");
			if (s != null) {
				symbol.setText(s);
				ISecurity security = getSecurityFromSymbol(s);
				if (security != null)
					getSite().getSelectionProvider().setSelection(new StructuredSelection(security));
			}

			String id = memento.getString("connector");
			if (id != null) {
				IFeedConnector connector = getFeedService().getConnector(id);
				if (connector == null)
					connector = CoreActivator.getDefault().getDefaultConnector();
				if (connector instanceof IFeedConnector2) {
					this.connector = (IFeedConnector2) connector;
					activeConnector.setText(this.connector.getName());
				}
			}

			if (s != null && connector != null) {
				subscription = this.connector.subscribeLevel2(s);

				subscription.addSubscriptionListener(subscriptionListener);

				lastClose = subscription.getLastClose();
				update(subscription.getTrade());
				update(subscription.getTodayOHL());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		memento.putString("symbol", symbol.getText());
		if (connector != null)
			memento.putString("connector", connector.getId());
		if (hideSummaryAction.isChecked())
			memento.putString("hide-summary", "true");
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
		for (int i = 0; i < tickFade.length; i++)
			tickFade[i].dispose();
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
		tableLayout.setColumnData(tableColumn, new ColumnWeightData(showMarketMakerAction.isChecked() ? 22 : 24));
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

		while (table.getColumnCount() > columnIndex)
			table.getColumn(table.getColumnCount() - 1).dispose();

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

	protected void updateBackground(TableItem tableItem, int columnIndex) {
		tableItem.setBackground(columnIndex, null);

		int[] timers = (int[]) tableItem.getData(K_FADE_LEVELS);
		if (timers[columnIndex] > 0) {
			switch (timers[columnIndex]) {
				case 4:
					tableItem.setBackground(columnIndex, tickFade[0]);
					break;
				case 3:
					tableItem.setBackground(columnIndex, tickFade[1]);
					break;
				case 2:
					tableItem.setBackground(columnIndex, tickFade[2]);
					break;
				case 1:
					break;
				default:
					tableItem.setBackground(columnIndex, tickBackgroundColor);
					break;
			}
		}
	}

	protected void updateBid(TableItem tableItem, IBookEntry entry) {
		int columnIndex = 0;
		int[] timers = (int[]) tableItem.getData(K_FADE_LEVELS);

		if (entry != null) {
			if (showMarketMakerAction.isChecked()) {
				String s = entry.getMarketMaker() != null ? entry.getMarketMaker() : "";
				if (!s.equals(tableItem.getText(columnIndex)))
					timers[columnIndex] = 6;
				tableItem.setText(columnIndex, s);
				updateBackground(tableItem, columnIndex++);
			}

			String s = numberFormatter.format(entry.getProposals());
			if (!s.equals(tableItem.getText(columnIndex)))
				timers[columnIndex] = 6;
			tableItem.setText(columnIndex, s);
			updateBackground(tableItem, columnIndex++);

			s = numberFormatter.format(entry.getQuantity());
			if (!s.equals(tableItem.getText(columnIndex)))
				timers[columnIndex] = 6;
			tableItem.setText(columnIndex, s);
			updateBackground(tableItem, columnIndex++);

			s = priceFormatter.format(entry.getPrice());
			if (!s.equals(tableItem.getText(columnIndex)))
				timers[columnIndex] = 6;
			tableItem.setText(columnIndex, s);
			updateBackground(tableItem, columnIndex++);
		}
		else {
			if (showMarketMakerAction.isChecked()) {
				tableItem.setText(columnIndex, "");
				timers[columnIndex] = 0;
				tableItem.setBackground(columnIndex++, null);
			}

			tableItem.setText(columnIndex, "");
			timers[columnIndex] = 0;
			tableItem.setBackground(columnIndex++, null);

			tableItem.setText(columnIndex, "");
			timers[columnIndex] = 0;
			tableItem.setBackground(columnIndex++, null);

			tableItem.setText(columnIndex, "");
			timers[columnIndex] = 0;
			tableItem.setBackground(columnIndex++, null);
		}

		tableItem.setData("bid", entry);
	}

	protected void updateAsk(TableItem tableItem, IBookEntry entry) {
		int columnIndex = tableItem.getParent().getColumnCount() - 1;
		int[] timers = (int[]) tableItem.getData(K_FADE_LEVELS);

		if (entry != null) {
			if (showMarketMakerAction.isChecked()) {
				String s = entry.getMarketMaker() != null ? entry.getMarketMaker() : "";
				if (!s.equals(tableItem.getText(columnIndex)))
					timers[columnIndex] = 6;
				tableItem.setText(columnIndex, s);
				updateBackground(tableItem, columnIndex--);
			}

			String s = numberFormatter.format(entry.getProposals());
			if (!s.equals(tableItem.getText(columnIndex)))
				timers[columnIndex] = 6;
			tableItem.setText(columnIndex, s);
			updateBackground(tableItem, columnIndex--);

			s = numberFormatter.format(entry.getQuantity());
			if (!s.equals(tableItem.getText(columnIndex)))
				timers[columnIndex] = 6;
			tableItem.setText(columnIndex, s);
			updateBackground(tableItem, columnIndex--);

			s = priceFormatter.format(entry.getPrice());
			if (!s.equals(tableItem.getText(columnIndex)))
				timers[columnIndex] = 6;
			tableItem.setText(columnIndex, s);
			updateBackground(tableItem, columnIndex--);
		}
		else {
			if (showMarketMakerAction.isChecked()) {
				tableItem.setText(columnIndex, "");
				timers[columnIndex] = 0;
				tableItem.setBackground(columnIndex--, null);
			}

			tableItem.setText(columnIndex, "");
			timers[columnIndex] = 0;
			tableItem.setBackground(columnIndex--, null);

			tableItem.setText(columnIndex, "");
			timers[columnIndex] = 0;
			tableItem.setBackground(columnIndex--, null);

			tableItem.setText(columnIndex, "");
			timers[columnIndex] = 0;
			tableItem.setBackground(columnIndex--, null);
		}

		tableItem.setData("ask", entry);
	}

	protected void onBookUpdate(IBook book) {
		this.book = book;

		IBookEntry[] bidEntries = book.getBidProposals();
		IBookEntry[] askEntries = book.getAskProposals();
		int rows = Math.max(bidEntries.length, askEntries.length);

		table.setRedraw(false);
		boolean doLayout = rows != table.getItemCount();
		try {
			for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
				TableItem tableItem = rowIndex < table.getItemCount() ? table.getItem(rowIndex) : new TableItem(table, SWT.NONE);

				int[] timers = (int[]) tableItem.getData(K_FADE_LEVELS);
				if (timers == null || timers.length != table.getColumnCount()) {
					timers = new int[table.getColumnCount()];
					for (int i = 0; i < timers.length; i++)
						timers[i] = 6;
					tableItem.setData(K_FADE_LEVELS, timers);
				}

				updateBid(tableItem, rowIndex < bidEntries.length ? bidEntries[rowIndex] : null);
				updateAsk(tableItem, rowIndex < askEntries.length ? askEntries[rowIndex] : null);
			}
			while (table.getItemCount() > rows)
				table.getItem(table.getItemCount() - 1).dispose();
		} finally {
			table.setRedraw(true);
		}

		if (doLayout) {
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

	protected IMarketService getMarketService() {
		BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(IMarketService.class.getName());
		IMarketService service = (IMarketService) context.getService(serviceReference);
		context.ungetService(serviceReference);
		return service;
	}

	protected IFeedService getFeedService() {
		BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(IFeedService.class.getName());
		IFeedService service = (IFeedService) context.getService(serviceReference);
		context.ungetService(serviceReference);
		return service;
	}

	protected void onSetSymbol() {
		if (subscription != null && subscription.getSymbol().equals(symbol.getText()))
			return;

		if (connector != null) {
			if (subscription != null) {
				subscription.removeSubscriptionListener(subscriptionListener);
				subscription.dispose();
			}

			subscription = this.connector.subscribeLevel2(symbol.getText());
			subscription.addSubscriptionListener(subscriptionListener);

			lastClose = subscription.getLastClose();
			update(subscription.getTrade());
			update(subscription.getTodayOHL());

			IBook book = subscription.getBook();
			if (book != null)
				onBookUpdate(book);
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
				update(subscription.getTrade());
				update(subscription.getTodayOHL());

				IBook book = subscription.getBook();
				if (book != null)
					onBookUpdate(book);
			}
		}
	}

	public void setSecurity(ISecurity security) {
		IFeedConnector connector = null;

		IMarket[] market = getMarketService().getMarkets();
		for (int i = 0; i < market.length; i++) {
			if (market[i].hasMember(security)) {
				connector = market[i].getLiveFeedConnector();
				break;
			}
		}

		if (connector == null)
			connector = CoreActivator.getDefault().getDefaultConnector();

		if (connector instanceof IFeedConnector2)
			this.connector = (IFeedConnector2) connector;

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
				update(subscription.getTrade());
				update(subscription.getTodayOHL());

				IBook book = subscription.getBook();
				if (book != null)
					onBookUpdate(book);
			}
		}

		getSite().getSelectionProvider().setSelection(new StructuredSelection(security));
	}

	protected void onQuoteUpdate(final QuoteEvent event) {
		try {
			table.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (!table.isDisposed()) {
						for (QuoteDelta delta : event.getDelta()) {
							if (delta.getNewValue() instanceof IBook)
								onBookUpdate((IBook) delta.getNewValue());
							if (delta.getNewValue() instanceof ITrade) {
								ITrade trade = (ITrade) delta.getNewValue();
								update(trade);
							}
							if (delta.getNewValue() instanceof ITodayOHL) {
								ITodayOHL todayOHL = (ITodayOHL) delta.getNewValue();
								update(todayOHL);
							}
							if (delta.getNewValue() instanceof ILastClose)
								lastClose = (ILastClose) delta.getNewValue();
						}
					}
				}
			});
		} catch (SWTException e) {
			// Ignore
		}
	}

	protected void update(ITrade trade) {
		time.setText(trade != null && trade.getTime() != null ? timeFormatter.format(trade.getTime()) : "");
		last.setText(trade != null && trade.getPrice() != null ? priceFormatter.format(trade.getPrice()) : "");
		volume.setText(trade != null && trade.getVolume() != null ? numberFormatter.format(trade.getVolume()) : "");

		if (trade != null && lastClose != null && lastClose.getPrice() != null && trade.getPrice() != null) {
			double changePercent = (trade.getPrice() - lastClose.getPrice()) / lastClose.getPrice() * 100.0;
			change.setText(NLS.bind("{0}{1}%", new Object[] {
			    changePercent < 0 ? "-" : (changePercent > 0 ? "+" : ""),
			    percentageFormatter.format(Math.abs(changePercent)),
			}));
		}
		else
			change.setText("");
	}

	protected void update(ITodayOHL todayOHL) {
		high.setText(todayOHL != null && todayOHL.getHigh() != null ? priceFormatter.format(todayOHL.getHigh()) : "");
		low.setText(todayOHL != null && todayOHL.getLow() != null ? priceFormatter.format(todayOHL.getLow()) : "");
	}

	public void setTickBackground(RGB color) {
		int steps = 100 / (tickFade.length + 1);
		for (int i = 0, ratio = 100 - steps; i < tickFade.length; i++, ratio -= steps) {
			RGB rgb = blend(tickBackgroundColor.getRGB(), table.getBackground().getRGB(), ratio);
			tickFade[i] = new Color(Display.getDefault(), rgb);
		}
	}

	private RGB blend(RGB c1, RGB c2, int ratio) {
		int r = blend(c1.red, c2.red, ratio);
		int g = blend(c1.green, c2.green, ratio);
		int b = blend(c1.blue, c2.blue, ratio);
		return new RGB(r, g, b);
	}

	private int blend(int v1, int v2, int ratio) {
		return (ratio * v1 + (100 - ratio) * v2) / 100;
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
				if (security != null)
					break;
			}

			context.ungetService(serviceReference);
		}

		return security;
	}
}
