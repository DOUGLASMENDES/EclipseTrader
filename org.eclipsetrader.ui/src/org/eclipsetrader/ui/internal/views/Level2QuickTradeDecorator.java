/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.views;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipsetrader.core.feed.IBookEntry;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.ui.internal.UIActivator;

public class Level2QuickTradeDecorator implements MouseTrackListener, MouseMoveListener, SelectionListener {
	Table table;
	ICommandService commandService;
	IHandlerService handlerService;

	Cursor buyCursor;
	Cursor sellCursor;

	boolean entered;

	public Level2QuickTradeDecorator(Table table, ICommandService commandService, IHandlerService handlerService) {
		this.table = table;
		this.commandService = commandService;
		this.handlerService = handlerService;

		initializeCursors();

		table.addMouseTrackListener(this);
		table.addMouseMoveListener(this);
		table.addSelectionListener(this);

		table.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				buyCursor.dispose();
				sellCursor.dispose();
			}
		});
	}

	void initializeCursors() {
		ImageDescriptor sourceDescriptor = UIActivator.getImageDescriptor("icons/pointers/buysell-source.bmp");
		ImageDescriptor buyMaskDescriptor = UIActivator.getImageDescriptor("icons/pointers/buy-mask.bmp");
		ImageDescriptor sellMaskDescriptor = UIActivator.getImageDescriptor("icons/pointers/sell-mask.bmp");

		buyCursor = new Cursor(Display.getCurrent(), sourceDescriptor.getImageData(), buyMaskDescriptor.getImageData(), 15, 7);
		sellCursor = new Cursor(Display.getCurrent(), sourceDescriptor.getImageData(), sellMaskDescriptor.getImageData(), 15, 7);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseEnter(MouseEvent e) {
		entered = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseMove(MouseEvent e) {
		int x = 0;

		if (!entered)
			return;
		if (table.getItem(new Point(e.x, e.y)) == null) {
			table.setCursor(null);
			return;
		}

		TableColumn[] column = table.getColumns();

		int i = 0;
		for (; i < column.length / 2; i++) {
			if (e.x >= x && e.x <= x + column[i].getWidth()) {
				if (table.getCursor() != buyCursor)
					table.setCursor(buyCursor);
				return;
			}
			x += column[i].getWidth();
		}
		for (; i < column.length; i++) {
			if (e.x >= x && e.x <= x + column[i].getWidth()) {
				if (table.getCursor() != sellCursor)
					table.setCursor(sellCursor);
				return;
			}
			x += column[i].getWidth();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseExit(MouseEvent e) {
		if (entered)
			table.setCursor(null);
		entered = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseHover(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		IBookEntry entry = null;
		IOrderSide orderSide = null;

		if (table.getCursor() == buyCursor) {
			entry = (IBookEntry) e.item.getData("bid");
			orderSide = IOrderSide.Buy;
		}
		else if (table.getCursor() == sellCursor) {
			entry = (IBookEntry) e.item.getData("ask");
			orderSide = IOrderSide.Sell;
		}
		if (entry == null)
			return;

		Command tradeCommand = commandService.getCommand("org.eclipsetrader.ui.file.trade");
		if (tradeCommand != null) {
			try {
				IParameter limitPrice = tradeCommand.getParameter("limitPrice");
				IParameter side = tradeCommand.getParameter("side");
				ParameterizedCommand parmCommand = new ParameterizedCommand(tradeCommand, new Parameterization[] {
				    new Parameterization(limitPrice, Double.toString(entry.getPrice())),
				    new Parameterization(side, orderSide.getId())
				});

				handlerService.executeCommand(parmCommand, null);
			} catch (Exception e1) {
				UIActivator.log("Error executing org.eclipsetrader.ui.file.trade command", e1);
			}
		}
	}
}
