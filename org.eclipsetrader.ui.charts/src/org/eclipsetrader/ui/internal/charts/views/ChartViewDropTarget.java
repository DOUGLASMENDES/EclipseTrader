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

package org.eclipsetrader.ui.internal.charts.views;

import java.util.Iterator;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.dialogs.PropertyDialog;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.PropertyPageManager;
import org.eclipsetrader.ui.charts.BaseChartViewer;
import org.eclipsetrader.ui.charts.ChartCanvas;
import org.eclipsetrader.ui.charts.ChartRowViewItem;
import org.eclipsetrader.ui.charts.ChartView;
import org.eclipsetrader.ui.charts.ChartViewItem;
import org.eclipsetrader.ui.charts.IChartEditorListener;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IEditableChartObject;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

@SuppressWarnings("restriction")
public class ChartViewDropTarget extends DropTargetAdapter {
	Shell shell;
	BaseChartViewer viewer;
	ChartView view;

	public ChartViewDropTarget(BaseChartViewer viewer) {
		this.shell = viewer.getControl().getShell();
	    this.viewer = viewer;
    }

	public void setView(ChartView view) {
    	this.view = view;
    }

	/* (non-Javadoc)
     * @see org.eclipse.swt.dnd.DropTargetAdapter#drop(org.eclipse.swt.dnd.DropTargetEvent)
     */
    @Override
    public void drop(DropTargetEvent event) {
    	ChartRowViewItem rowItem = null;

    	ChartCanvas[] chartCanvas = viewer.getChildren();
    	for (int i = 0; i < chartCanvas.length; i++) {
    		Rectangle bounds = chartCanvas[i].getCanvas().getBounds();
    		if (bounds.contains(chartCanvas[i].getCanvas().toControl(event.x, event.y))) {
    			rowItem = (ChartRowViewItem) view.getItems()[i];
    			break;
    		}
    	}

		String[] factories = (String[]) event.data;
        for (int i = 0; i < factories.length; i++) {
			final IChartObjectFactory factory = ChartsUIActivator.getDefault().getChartObjectFactory(factories[i]);
			if (factory != null) {
            	final IChartObject chartObject = factory.createObject(null);
            	if (chartObject instanceof IEditableChartObject) {
    				viewer.getEditor().addListener(new IChartEditorListener() {
    			        public void applyEditorValue() {
    						viewer.getEditor().removeListener(this);

    						IChartObject[] currentObject = viewer.getSelectedChartCanvas().getChartObject();
    						int index = viewer.getSelectedChartCanvasIndex();

    						if (index != -1) {
    							IChartObject[] newObject = new IChartObject[currentObject.length + 1];
    							System.arraycopy(currentObject, 0, newObject, 0, currentObject.length);
    							newObject[currentObject.length] = chartObject;

    							viewer.getSelectedChartCanvas().setChartObject(newObject);

    							((ChartRowViewItem) view.getItems()[index]).addFactory(factory);
    							viewer.setSelection(new StructuredSelection(chartObject));
    						}
    			        }

    			        public void cancelEditor() {
    						viewer.getEditor().removeListener(this);
    			        }
    				});
    				viewer.activateEditor((IEditableChartObject) chartObject);
            	}
            	else {
            		boolean addToNewRow = false;
	            	IConfigurationElement configurationElement = ChartsUIActivator.getDefault().getChartObjectConfiguration(factories[i]);
	            	if (!"false".equals(configurationElement.getAttribute("exclusive")))
	            		addToNewRow = true;

	            	PropertyPageManager pageManager = new PropertyPageManager();
    				if (addToNewRow) {
	    				ChartRowViewItem newRowItem = new ChartRowViewItem(view, factory.getName());
	    				ChartViewItem viewItem = new ChartViewItem(newRowItem, factory);
	    				newRowItem.addChildItem(viewItem);

	    				PropertyPageContributorManager.getManager().contribute(pageManager, viewItem);
	    				Iterator<?> pages = pageManager.getElements(PreferenceManager.PRE_ORDER).iterator();
	    				if (pages.hasNext()) {
		    				PropertyDialog dlg = PropertyDialog.createDialogOn(shell, null, viewItem);
	    					if (dlg.open() == PropertyDialog.OK)
			    				view.addRowAfter(rowItem, newRowItem);
	    				}
    				}
    				else {
	    				ChartViewItem viewItem = new ChartViewItem(rowItem, factory);
	    				PropertyPageContributorManager.getManager().contribute(pageManager, viewItem);
	    				Iterator<?> pages = pageManager.getElements(PreferenceManager.PRE_ORDER).iterator();
	    				if (pages.hasNext()) {
		    				PropertyDialog dlg = PropertyDialog.createDialogOn(shell, null, viewItem);
	    					if (dlg.open() == PropertyDialog.OK)
			    				rowItem.addChildItem(viewItem);
	    				}
    				}
            	}
			}
        }
    }
}
