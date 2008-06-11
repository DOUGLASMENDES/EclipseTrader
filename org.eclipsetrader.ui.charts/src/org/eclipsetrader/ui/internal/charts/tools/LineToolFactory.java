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

package org.eclipsetrader.ui.internal.charts.tools;

import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IChartParameters;
import org.eclipsetrader.ui.charts.ChartToolEditor.ChartObjectEditorEvent;
import org.eclipsetrader.ui.internal.charts.tools.LineToolObject.Value;

public class LineToolFactory implements IChartObjectFactory, IExecutableExtension {
    private String id;
    private String name;

	private Value value1;
	private Value value2;
    private LineToolObject object;

	public LineToolFactory() {
		object = new LineToolObject() {
            @Override
            public void handleMouseUp(ChartObjectEditorEvent e) {
	            super.handleMouseUp(e);
	            value1 = getValues()[0];
	            value2 = getValues()[1];
            }
		};
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
    	id = config.getAttribute("id");
    	name = config.getAttribute("name");
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#createObject(org.eclipsetrader.core.charts.IDataSeries)
	 */
	public IChartObject createObject(IDataSeries source) {
		return object;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
	 */
	public void setParameters(IChartParameters parameters) {
	    name = parameters.hasParameter("name") ? parameters.getString("name") : name;

	    Date d1 = parameters.hasParameter("d1") ? parameters.getDate("d1") : null;
	    Number v1 = parameters.hasParameter("v1") ? parameters.getDouble("v1") : null;
	    if (d1 != null && v1 != null)
	    	value1 = new Value(d1, v1);

	    Date d2 = parameters.hasParameter("d2") ? parameters.getDate("d2") : null;
	    Number v2 = parameters.hasParameter("v2") ? parameters.getDouble("v2") : null;
	    if (d2 != null && v2 != null)
	    	value2 = new Value(d2, v2);

	    if (value1 != null && value2 != null)
	    	object.setValues(new Value[] { value1, value2 });
	}
}
