/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.internal.ui.views.explorer;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.feed.FeedSource;
import net.sourceforge.eclipsetrader.core.db.feed.TradeSource;
import net.sourceforge.eclipsetrader.core.ui.dialogs.FeedSelectionDialog;
import net.sourceforge.eclipsetrader.core.ui.dialogs.IntradayChartsDialog;
import net.sourceforge.eclipsetrader.core.ui.dialogs.TradingOptionsDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Implements a drop down menu action to allow the user to perform
 * bulk changes to the selected securities.
 */
public class BulkChangesAction extends Action implements IMenuCreator {
	private Security[] selection;

	private Menu menu;

	private MenuItem changeQuoteFeedAction;

	private MenuItem changeLevel2FeedAction;

	private MenuItem changeHistoryFeedAction;

	private MenuItem changeIntradayOptionsAction;

	private MenuItem changeTradingOptionsAction;

	public BulkChangesAction() {
		super("Change", Action.AS_DROP_DOWN_MENU);
		setMenuCreator(this);
		setEnabled(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		if (menu != null) {
			menu.dispose();
			menu = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		if (menu == null) {
			menu = new Menu(parent);

			changeQuoteFeedAction = new MenuItem(menu, SWT.NONE);
			changeQuoteFeedAction.setText("Quote Feed");
			changeQuoteFeedAction.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					changeFeed();
				}
			});

			changeLevel2FeedAction = new MenuItem(menu, SWT.NONE);
			changeLevel2FeedAction.setText("Level 2 Feed");
			changeLevel2FeedAction.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					changeLevel2Feed();
				}
			});

			changeHistoryFeedAction = new MenuItem(menu, SWT.NONE);
			changeHistoryFeedAction.setText("History Feed");
			changeHistoryFeedAction.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					changeHistoryFeed();
				}
			});
			
			new MenuItem(menu, SWT.SEPARATOR);

			changeIntradayOptionsAction = new MenuItem(menu, SWT.NONE);
			changeIntradayOptionsAction.setText("Intraday Charts");
			changeIntradayOptionsAction.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					changeIntradayOptions();
				}
			});
			
			new MenuItem(menu, SWT.SEPARATOR);

			changeTradingOptionsAction = new MenuItem(menu, SWT.NONE);
			changeTradingOptionsAction.setText("Trading Provider");
			changeTradingOptionsAction.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					changeTradingOptions();
				}
			});
		}
		return menu;
	}

	protected void changeFeed() {
		FeedSelectionDialog dlg = new FeedSelectionDialog(menu.getShell(), "quote"); //$NON-NLS-1$
		if (dlg.open() == Dialog.OK) {
			FeedSource source = dlg.getFeedSource();
			for (int i = 0; i < selection.length; i++) {
				if (source != null) {
					FeedSource newSource = new FeedSource();
					newSource.setId(source.getId());
					newSource.setExchange(source.getExchange());
					newSource.setSymbol(selection[i].getQuoteFeed() != null ? selection[i].getQuoteFeed().getSymbol() : ""); //$NON-NLS-1$
					selection[i].setQuoteFeed(newSource);
				} else
					selection[i].setQuoteFeed(null);
				CorePlugin.getRepository().save(selection[i]);
			}
		}
	}

	protected void changeLevel2Feed() {
		FeedSelectionDialog dlg = new FeedSelectionDialog(menu.getShell(), "level2"); //$NON-NLS-1$
		if (dlg.open() == Dialog.OK) {
			FeedSource source = dlg.getFeedSource();
			for (int i = 0; i < selection.length; i++) {
				if (source != null) {
					FeedSource newSource = new FeedSource();
					newSource.setId(source.getId());
					newSource.setExchange(source.getExchange());
					newSource.setSymbol(selection[i].getLevel2Feed() != null ? selection[i].getLevel2Feed().getSymbol() : ""); //$NON-NLS-1$
					selection[i].setLevel2Feed(newSource);
				} else
					selection[i].setLevel2Feed(null);
				CorePlugin.getRepository().save(selection[i]);
			}
		}
	}

	protected void changeHistoryFeed() {
		FeedSelectionDialog dlg = new FeedSelectionDialog(menu.getShell(), "history"); //$NON-NLS-1$
		if (dlg.open() == Dialog.OK) {
			FeedSource source = dlg.getFeedSource();
			for (int i = 0; i < selection.length; i++) {
				if (source != null) {
					FeedSource newSource = new FeedSource();
					newSource.setId(source.getId());
					newSource.setExchange(source.getExchange());
					newSource.setSymbol(selection[i].getHistoryFeed() != null ? selection[i].getHistoryFeed().getSymbol() : ""); //$NON-NLS-1$
					selection[i].setHistoryFeed(newSource);
				} else
					selection[i].setHistoryFeed(null);
				CorePlugin.getRepository().save(selection[i]);
			}
		}
	}

	protected void changeIntradayOptions() {
		IntradayChartsDialog dlg = new IntradayChartsDialog(menu.getShell()) {
			protected void okPressed() {
				for (int i = 0; i < selection.length; i++) {
					intradayDataOptions.saveSettings(selection[i]);
					CorePlugin.getRepository().save(selection[i]);
				}
				super.okPressed();
			}
		};
		dlg.open();
	}

	protected void changeTradingOptions() {
		TradingOptionsDialog dlg = new TradingOptionsDialog(menu.getShell()) {
			protected void okPressed() {
				for (int i = 0; i < selection.length; i++) {
					TradeSource source = options.getSource();

					TradeSource oldSource = selection[i].getTradeSource();
					if (oldSource != null)
						source.setSymbol(oldSource.getSymbol());
					selection[i].setTradeSource(source);

					CorePlugin.getRepository().save(selection[i]);
				}
				super.okPressed();
			}
		};
		dlg.open();
	}

	public void setSelection(Security[] selection) {
    	this.selection = selection;
		setEnabled(selection != null && selection.length != 0);
    }
}
