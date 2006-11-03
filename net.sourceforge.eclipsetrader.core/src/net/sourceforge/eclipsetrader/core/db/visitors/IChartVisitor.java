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
 * Interface for chart objects visitors.
 */
public interface IChartVisitor
{
    
    public void visit(Chart chart);

    public void visit(ChartRow row);
    
    public void visit(ChartTab tab);
    
    public void visit(ChartIndicator indicator);
    
    public void visit(ChartObject object);
}
