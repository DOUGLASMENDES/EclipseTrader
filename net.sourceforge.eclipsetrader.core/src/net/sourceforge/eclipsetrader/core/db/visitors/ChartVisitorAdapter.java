/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.core.db.visitors;

import net.sourceforge.eclipsetrader.core.db.Chart;
import net.sourceforge.eclipsetrader.core.db.ChartIndicator;
import net.sourceforge.eclipsetrader.core.db.ChartObject;
import net.sourceforge.eclipsetrader.core.db.ChartRow;
import net.sourceforge.eclipsetrader.core.db.ChartTab;

/**
 * This adapter class provides default implementations for the
 * methods described by the <code>IChartVisitor</code> interface.
 * <p>
 * Classes that wish to deal with <code>IChartVisitor</code>s can
 * extend this class and override only the methods which they are
 * interested in.
 * </p>
 *
 * @see IChartVisitor
 */
public class ChartVisitorAdapter implements IChartVisitor
{

    public ChartVisitorAdapter()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.visitors.IChartVisitor#visit(net.sourceforge.eclipsetrader.core.db.Chart)
     */
    public void visit(Chart chart)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.visitors.IChartVisitor#visit(net.sourceforge.eclipsetrader.core.db.ChartRow)
     */
    public void visit(ChartRow row)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.visitors.IChartVisitor#visit(net.sourceforge.eclipsetrader.core.db.ChartTab)
     */
    public void visit(ChartTab tab)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.visitors.IChartVisitor#visit(net.sourceforge.eclipsetrader.core.db.ChartIndicator)
     */
    public void visit(ChartIndicator indicator)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.visitors.IChartVisitor#visit(net.sourceforge.eclipsetrader.core.db.ChartObject)
     */
    public void visit(ChartObject object)
    {
    }
}
