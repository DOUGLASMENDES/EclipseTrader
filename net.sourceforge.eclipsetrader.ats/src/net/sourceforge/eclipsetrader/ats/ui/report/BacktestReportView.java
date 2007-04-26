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

package net.sourceforge.eclipsetrader.ats.ui.report;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.eclipsetrader.ats.ATSPlugin;
import net.sourceforge.eclipsetrader.ats.core.internal.Backtest;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class BacktestReportView extends ViewPart {
	public static final String VIEW_ID = "net.sourceforge.eclipsetrader.ats.backtestreport";

	Composite content;

	StackLayout layout;

	Map pages = new HashMap();

	Action historyMenu;

	Action removeCurrentAction;

	Action removeAllAction;

	IMenuCreator menuCreator = new IMenuCreator() {
		private Menu menu;

		public void dispose() {
		}

		public Menu getMenu(Control parent) {
			if (menu != null)
				menu.dispose();
			menu = new Menu(parent);

			for (Iterator iter = pages.values().iterator(); iter.hasNext();) {
				StatisticsPage page = (StatisticsPage) iter.next();
				MenuItem item = new MenuItem(menu, SWT.RADIO);
				item.setText(page.getTitle());
				item.setData(page);
				item.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						StatisticsPage selectedPage = (StatisticsPage) e.widget.getData();
						setCurrentPage(selectedPage);
					}
				});
				if (page.getControl() == layout.topControl)
					item.setSelection(true);
			}

			return menu;
		}

		public Menu getMenu(Menu parent) {
			return menu;
		}
	};

	public BacktestReportView() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
	 */
	public void init(IViewSite site) throws PartInitException {
		historyMenu = new Action("History", Action.AS_DROP_DOWN_MENU) {
			public void run() {
				StatisticsPage[] p = (StatisticsPage[]) pages.values().toArray(new StatisticsPage[pages.values().size()]);
				for (int i = 0; i < p.length; i++) {
					if (p[i].getControl() == layout.topControl) {
						i++;
						if (i >= pages.size())
							i = 0;
						updateControlsEnablement();
						setCurrentPage(p[i]);
						break;
					}
				}
			}
		};
		historyMenu.setImageDescriptor(ATSPlugin.getImageDescriptor("icons/full/elcl16/time_go.png"));
		historyMenu.setToolTipText("Backtests History");
		historyMenu.setMenuCreator(menuCreator);
		historyMenu.setEnabled(false);

		removeCurrentAction = new Action() {
			public void run() {
				StatisticsPage[] p = (StatisticsPage[]) pages.values().toArray(new StatisticsPage[pages.values().size()]);
				for (int i = 0; i < p.length; i++) {
					if (p[i].getControl() == layout.topControl) {
						pages.remove(p[i].getTitle());
						if (i > 0)
							i--;
						updateControlsEnablement();
						setCurrentPage(pages.size() > 0 ? p[i] : null);
						break;
					}
				}
			}
		};
		removeCurrentAction.setToolTipText("Remove Current Search");
		removeCurrentAction.setImageDescriptor(ATSPlugin.getImageDescriptor("icons/full/elcl16/search_rem.gif"));
		removeCurrentAction.setDisabledImageDescriptor(ATSPlugin.getImageDescriptor("icons/full/dlcl16/search_rem.gif"));
		removeCurrentAction.setEnabled(false);

		removeAllAction = new Action() {
			public void run() {
				pages.clear();
				updateControlsEnablement();
				setCurrentPage(null);
			}
		};
		removeAllAction.setToolTipText("Remove All Searches");
		removeAllAction.setImageDescriptor(ATSPlugin.getImageDescriptor("icons/full/elcl16/search_remall.gif"));
		removeAllAction.setDisabledImageDescriptor(ATSPlugin.getImageDescriptor("icons/full/dlcl16/search_remall.gif"));
		removeAllAction.setEnabled(false);

		IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
		toolBarManager.add(removeCurrentAction);
		toolBarManager.add(removeAllAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(historyMenu);

		site.getActionBars().updateActionBars();

		super.init(site);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		content = new Composite(parent, SWT.NONE);
		layout = new StackLayout();
		layout.marginWidth = layout.marginHeight = 0;
		content.setLayout(layout);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}

	public void setInput(Backtest test) {
		StatisticsPage page = new StatisticsPage(test.getName());
		page.createPartControl(content);
		page.setInput(test);

		pages.put(page.getTitle(), page);
		updateControlsEnablement();
		setCurrentPage(page);
	}

	void setCurrentPage(StatisticsPage page) {
		setContentDescription(page != null ? page.getTitle() : "");
		layout.topControl = page != null ? page.getControl() : null;
		content.layout();
	}

	void updateControlsEnablement() {
		historyMenu.setEnabled(pages.size() != 0);
		removeCurrentAction.setEnabled(pages.size() != 0);
		removeAllAction.setEnabled(pages.size() != 0);
	}
}
