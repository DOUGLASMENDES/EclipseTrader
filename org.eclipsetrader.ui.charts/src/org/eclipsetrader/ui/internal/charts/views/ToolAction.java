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

package org.eclipsetrader.ui.internal.charts.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipsetrader.ui.charts.BaseChartViewer;
import org.eclipsetrader.ui.charts.ChartRowViewItem;
import org.eclipsetrader.ui.charts.ChartView;
import org.eclipsetrader.ui.charts.IChartEditorListener;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IEditableChartObject;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class ToolAction extends Action {
	private IViewPart viewPart;
	private String chartObjectId;

	private IChartObjectFactory factory;
	private IChartObject chartObject;

	private IChartEditorListener editorListener = new IChartEditorListener() {
        public void applyEditorValue() {
			BaseChartViewer viewer = (BaseChartViewer) viewPart.getAdapter(BaseChartViewer.class);
			viewer.getEditor().removeListener(editorListener);

			handleApplyEditorValue();
        }

        public void cancelEditor() {
			BaseChartViewer viewer = (BaseChartViewer) viewPart.getAdapter(BaseChartViewer.class);
			viewer.getEditor().removeListener(editorListener);

        	handleCancelEditor();
        }
	};

	public ToolAction(String title, IViewPart viewPart, String chartObjectId) {
		super(title, AS_CHECK_BOX);
		setId(chartObjectId);
		this.viewPart = viewPart;
		this.chartObjectId = chartObjectId;
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
		factory = ChartsUIActivator.getDefault().getChartObjectFactory(chartObjectId);
		if (factory != null) {
			chartObject = factory.createObject(null);
			if (chartObject instanceof IEditableChartObject) {
				BaseChartViewer viewer = (BaseChartViewer) viewPart.getAdapter(BaseChartViewer.class);
				viewer.getEditor().addListener(editorListener);
				viewer.activateEditor((IEditableChartObject) chartObject);
			}
		}
    }

    protected void handleApplyEditorValue() {
		BaseChartViewer viewer = (BaseChartViewer) viewPart.getAdapter(BaseChartViewer.class);

		IChartObject[] currentObject = viewer.getSelectedChartCanvas().getChartObject();
		int index = viewer.getSelectedChartCanvasIndex();

		if (index != -1) {
			IChartObject[] newObject = new IChartObject[currentObject.length + 1];
			System.arraycopy(currentObject, 0, newObject, 0, currentObject.length);
			newObject[currentObject.length] = chartObject;

			viewer.getSelectedChartCanvas().setChartObject(newObject);

			ChartView view = (ChartView) viewPart.getAdapter(ChartView.class);
			((ChartRowViewItem) view.getItems()[index]).addFactory(factory);

			viewer.setSelection(new StructuredSelection(chartObject));
		}

    	factory = null;
    	chartObject = null;
    	setChecked(false);
    }

    protected void handleCancelEditor() {
    	factory = null;
    	chartObject = null;
    	setChecked(false);
    }

	protected IChartObjectFactory getFactory() {
    	return factory;
    }
}
