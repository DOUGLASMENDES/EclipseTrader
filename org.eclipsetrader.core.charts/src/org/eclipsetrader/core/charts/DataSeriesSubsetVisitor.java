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

package org.eclipsetrader.core.charts;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Data series visitor implementation used to build a subset of a data
 * series and all its childrens.
 *
 * @since 1.0
 */
public class DataSeriesSubsetVisitor implements IDataSeriesVisitor {
	private IAdaptable first;
	private IAdaptable last;
	private IDataSeries subset;

	/**
	 * Constructor.
	 *
	 * @param first the first element in the subset.
	 * @param last the last element in the subset.
	 */
	public DataSeriesSubsetVisitor(IAdaptable first, IAdaptable last) {
        this.first = first;
        this.last = last;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.charts.core.IDataSeriesVisitor#visit(org.eclipsetrader.charts.core.IDataSeries)
     */
    public boolean visit(IDataSeries data) {
    	subset = data.getSeries(first, last);

    	IDataSeries[] childs = data.getChildren();
    	if (childs != null) {
        	IDataSeries[] childSubsets = new IDataSeries[childs.length];
    		for (int i = 0; i < childs.length; i++) {
    			DataSeriesSubsetVisitor visitor = new DataSeriesSubsetVisitor(first, last);
    			visitor.visit(childs[i]);
   				childSubsets[i] = visitor.getSubset();
    		}
   			subset.setChildren(childSubsets);
    	}

    	return false;
    }

    /**
     * Returns the subset series.
     *
     * @return the subset series.
     */
    public IDataSeries getSubset() {
    	return subset;
    }
}
