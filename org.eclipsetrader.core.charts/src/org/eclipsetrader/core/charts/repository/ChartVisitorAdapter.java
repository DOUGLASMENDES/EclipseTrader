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

package org.eclipsetrader.core.charts.repository;

/**
 * This adapter class provides default implementations for the methods
 * described by <code>IChartVisitor</code> interface.
 *
 * <p>The default implementation visits the entire chart tree.</p>
 *
 * @since 1.0
 */
public class ChartVisitorAdapter implements IChartVisitor {

    public ChartVisitorAdapter() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartVisitor#visit(org.eclipsetrader.core.charts.repository.ChartTemplate)
     */
    @Override
    public boolean visit(IChartTemplate chart) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartVisitor#visit(org.eclipsetrader.core.charts.repository.ChartSection)
     */
    @Override
    public boolean visit(IChartSection section) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartVisitor#visit(org.eclipsetrader.core.charts.repository.SecuritySection)
     */
    @Override
    public boolean visit(ISecuritySection section) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartVisitor#visit(org.eclipsetrader.core.charts.repository.IndicatorSection)
     */
    @Override
    public void visit(IElementSection section) {
    }
}
