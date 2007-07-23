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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Implements the drop down menu action that allows the user to selection
 * how the groups hierachy should be presented.
 */
public class GroupsPresentationAction extends Action implements IMenuCreator {
	public static final int FLAT = 0;

	public static final int HIERARCHICAL = 1;

	private Menu menu;
	
	private MenuItem flatItem;
	
	private MenuItem hierarchicalItem;

	private int presentation = FLAT;

	public GroupsPresentationAction() {
		super(Messages.GroupsPresentationAction_Label, Action.AS_DROP_DOWN_MENU);
		setMenuCreator(this);
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

			flatItem = new MenuItem(menu, SWT.RADIO);
			flatItem.setText(Messages.GroupsPresentationAction_FlatLabel);
			flatItem.setSelection(presentation == FLAT);
			flatItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					presentation = FLAT;
					selectFlatPresentation();
				}
			});

			hierarchicalItem = new MenuItem(menu, SWT.RADIO);
			hierarchicalItem.setText(Messages.GroupsPresentationAction_HierarchicalLabel);
			hierarchicalItem.setSelection(presentation == HIERARCHICAL);
			hierarchicalItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					presentation = HIERARCHICAL;
					selectHierarchicalPresentation();
				}
			});
		}
		return menu;
	}

	protected void selectHierarchicalPresentation() {
	}

	protected void selectFlatPresentation() {
	}

	public int getPresentation() {
		return presentation;
	}

	public void setPresentation(int presentation) {
		this.presentation = presentation;
		if (flatItem != null)
			flatItem.setSelection(presentation == FLAT);
		if (hierarchicalItem != null)
			hierarchicalItem.setSelection(presentation == HIERARCHICAL);
	}
}
