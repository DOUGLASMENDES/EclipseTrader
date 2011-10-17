/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.charts;

/**
 * Interface implemented by data series visitors.
 *
 * @since 1.0
 */
public interface IDataSeriesVisitor {

    /**
     * Visits the given data series.
     *
     * @param data the data to visit.
     * @return <code>true</code> if the visitor should visit the child series.
     */
    public boolean visit(IDataSeries data);
}
