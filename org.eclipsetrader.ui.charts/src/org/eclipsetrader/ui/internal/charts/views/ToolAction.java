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
import org.eclipse.ui.IViewPart;
import org.eclipsetrader.ui.charts.BaseChartViewer;
import org.eclipsetrader.ui.charts.ChartToolEditor;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IEditableChartObject;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class ToolAction extends Action {
	private IViewPart viewPart;
	private String chartObjectId;

	private IChartObjectFactory factory;

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
		IChartObjectFactory factory = ChartsUIActivator.getDefault().getChartObjectFactory(chartObjectId);
		if (factory != null) {
			IChartObject chartObject = factory.createObject(null);
			if (chartObject instanceof IEditableChartObject) {
				final BaseChartViewer viewer = (BaseChartViewer) viewPart.getAdapter(BaseChartViewer.class);
				viewer.setEditor(new ChartToolEditor() {
		            @Override
		            public void deactivate() {
		            	IChartObject object = getObject();
		            	viewer.getSelectedChartCanvas().getChartObject().add(object);
		                super.deactivate();

		                viewer.redraw();
		                setChecked(false);
		            }
		    	});
				viewer.activateEditor((IEditableChartObject) chartObject);
			}
		}
    }

	protected IChartObjectFactory getFactory() {
    	return factory;
    }
}
