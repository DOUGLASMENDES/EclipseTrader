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

package org.eclipsetrader.ui.charts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.charts.repository.IChartSection;
import org.eclipsetrader.core.charts.repository.IChartTemplate;
import org.eclipsetrader.core.internal.charts.repository.ChartTemplate;
import org.eclipsetrader.core.views.IView;
import org.eclipsetrader.core.views.IViewChangeListener;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewVisitor;
import org.eclipsetrader.core.views.ViewEvent;
import org.eclipsetrader.core.views.ViewItemDelta;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class ChartView implements IView {
	private String name;
	private List<ChartRowViewItem> rows = new ArrayList<ChartRowViewItem>();
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	private IDataSeries rootDataSeries;

	protected ChartView() {
	}

	public ChartView(String name) {
		this.name = name;
	}

	public ChartView(IChartTemplate template) {
		this.name = template.getName();

		IChartSection[] section = template.getSections();
		for (int i = 0; i < section.length; i++) {
			ChartRowViewItem viewItem = new ChartRowViewItem(this, section[i]);
			rows.add(viewItem);
		}
	}

	public String getName() {
    	return name;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IView#dispose()
	 */
	public void dispose() {
		listeners.clear();
		rows.clear();
	}

	public void addRow(ChartRowViewItem viewItem) {
		rows.add(viewItem);
		fireViewChangedEvent(new ViewItemDelta[] {
				new ViewItemDelta(ViewItemDelta.ADDED, viewItem),
			});
	}

	public void addRowBefore(ChartRowViewItem referenceViewItem, ChartRowViewItem viewItem) {
		int index = rows.indexOf(referenceViewItem);
		rows.add(index != -1 ? index : 0, viewItem);
		fireViewChangedEvent(new ViewItemDelta[] {
				new ViewItemDelta(ViewItemDelta.ADDED, viewItem),
			});
	}

	public void addRowAfter(ChartRowViewItem referenceViewItem, ChartRowViewItem viewItem) {
		int index = rows.indexOf(referenceViewItem);
		rows.add(index != -1 ? index + 1 : rows.size(), viewItem);
		fireViewChangedEvent(new ViewItemDelta[] {
				new ViewItemDelta(ViewItemDelta.ADDED, viewItem),
			});
	}

	public void removeRow(ChartRowViewItem viewItem) {
		rows.remove(viewItem);
		fireViewChangedEvent(new ViewItemDelta[] {
				new ViewItemDelta(ViewItemDelta.REMOVED, viewItem),
			});
	}

	public IDataSeries getRootDataSeries() {
    	return rootDataSeries;
    }

	public void setRootDataSeries(IDataSeries rootDataSeries) {
    	this.rootDataSeries = rootDataSeries;

    	for (ChartRowViewItem viewItem : rows)
    		viewItem.setRootDataSeries(rootDataSeries);
    }

	protected void fireViewChangedEvent(ViewItemDelta[] delta) {
		ViewEvent event = new ViewEvent(this, delta);

		Object[] l = listeners.getListeners();
		for (int i = 0; i < l.length; i++) {
			try {
				((IViewChangeListener) l[i]).viewChanged(event);
			} catch(Throwable e) {
				Status status = new Status(IStatus.ERROR, ChartsUIActivator.PLUGIN_ID, Messages.ChartView_NotificationErrorMessage, e);
				ChartsUIActivator.log(status);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IView#getItems()
	 */
	public IViewItem[] getItems() {
		return rows.toArray(new IViewItem[rows.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IView#addViewChangeListener(org.eclipsetrader.core.views.IViewChangeListener)
	 */
	public void addViewChangeListener(IViewChangeListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IView#removeViewChangeListener(org.eclipsetrader.core.views.IViewChangeListener)
	 */
	public void removeViewChangeListener(IViewChangeListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(ChartRowViewItem[].class))
			return rows.toArray(new ChartRowViewItem[rows.size()]);

		if (adapter.isAssignableFrom(getClass()))
			return this;

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IView#accept(org.eclipsetrader.core.views.IViewVisitor)
	 */
	public void accept(IViewVisitor visitor) {
		if (visitor.visit(this)) {
			IViewItem[] child = getItems();
			for (int i = 0; i < child.length; i++)
				child[i].accept(visitor);
		}
	}

	public IChartTemplate getTemplate() {
		ChartTemplate template = new ChartTemplate(name);

		IChartSection[] section = new IChartSection[rows.size()];
		for (int i = 0; i < section.length; i++)
			section[i] = rows.get(i).getTemplate();
		template.setSections(section);

		return template;
	}
}
