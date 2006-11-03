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

package net.sourceforge.eclipsetrader.core.db;

import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.ObservableList;
import net.sourceforge.eclipsetrader.core.db.visitors.IChartVisitor;

public class ChartTab extends PersistentObject
{
    private ChartRow parent;
    private String label = "";
    private ObservableList indicators = new ObservableList();
    private ObservableList objects = new ObservableList();

    public ChartTab()
    {
    }

    public ChartTab(Integer id)
    {
        super(id);
    }

    public ChartRow getParent()
    {
        return parent;
    }

    public void setParent(ChartRow chartRow)
    {
        this.parent = chartRow;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        if (!this.label.equals(label))
            setChanged();
        this.label = label;
    }

    public ObservableList getIndicators()
    {
        return indicators;
    }

    public void setIndicators(ObservableList rows)
    {
        this.indicators = rows;
    }

    public ObservableList getObjects()
    {
        return objects;
    }

    public void setObjects(ObservableList objects)
    {
        this.objects = objects;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.PersistentObject#clearChanged()
     */
    public synchronized void clearChanged()
    {
        for (Iterator iter = getIndicators().iterator(); iter.hasNext(); )
            ((ChartIndicator)iter.next()).clearChanged();
        super.clearChanged();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.PersistentObject#setChanged()
     */
    public synchronized void setChanged()
    {
        super.setChanged();
        if (getParent() != null)
            getParent().setChanged();
    }

    /* (non-Javadoc)
     * @see java.util.Observable#notifyObservers()
     */
    public void notifyObservers()
    {
        for (Iterator iter = getIndicators().iterator(); iter.hasNext(); )
            ((ChartIndicator)iter.next()).notifyObservers();
        super.notifyObservers();
    }
    
    public void accept(IChartVisitor visitor)
    {
        visitor.visit(this);
        
        ChartIndicator[] indicators = (ChartIndicator[])getIndicators().toArray(new ChartIndicator[getIndicators().size()]);
        for (int i = 0; i < indicators.length; i++)
            visitor.visit(indicators[i]);

        ChartObject[] objects = (ChartObject[])getObjects().toArray(new ChartObject[getObjects().size()]);
        for (int i = 0; i < objects.length; i++)
            visitor.visit(objects[i]);
    }
}
