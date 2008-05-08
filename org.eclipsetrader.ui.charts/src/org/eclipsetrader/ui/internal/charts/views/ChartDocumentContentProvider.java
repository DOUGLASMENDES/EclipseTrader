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

import org.eclipsetrader.ui.charts.ChartViewer;
import org.eclipsetrader.ui.charts.IChartContentProvider;

public class ChartDocumentContentProvider implements IChartContentProvider {

	public ChartDocumentContentProvider() {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartContentProvider#inputChanged(org.eclipsetrader.ui.charts.ChartViewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(ChartViewer viewer, Object oldInput, Object newInput) {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartContentProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof ChartDocument)
			return ((ChartDocument) inputElement).getSections();
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ChartRowSection)
			return ((ChartRowSection) parentElement).getElements().toArray();
		return new Object[0];
	}
}
