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

public class ChartRow extends PersistentObject
{
    private Chart parent;
    private ObservableList tabs = new ObservableList();

    public ChartRow()
    {
    }

    public ChartRow(Integer id)
    {
        super(id);
    }

    public Chart getParent()
    {
        return parent;
    }

    public void setParent(Chart chart)
    {
        this.parent = chart;
    }

    public ObservableList getTabs()
    {
        return tabs;
    }

    public void setTabs(ObservableList rows)
    {
        this.tabs = rows;
    }
    
    public void add(ChartTab tab)
    {
        tab.setParent(this);
        getTabs().add(tab);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.PersistentObject#clearChanged()
     */
    public synchronized void clearChanged()
    {
        for (Iterator iter = getTabs().iterator(); iter.hasNext(); )
            ((ChartTab)iter.next()).clearChanged();
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
        for (Iterator iter = getTabs().iterator(); iter.hasNext(); )
            ((ChartTab)iter.next()).notifyObservers();
        super.notifyObservers();
    }
    
    public void accept(IChartVisitor visitor)
    {
        visitor.visit(this);

        ChartTab[] tabs = (ChartTab[])getTabs().toArray(new ChartTab[getTabs().size()]);
        for (int i = 0; i < tabs.length; i++)
            tabs[i].accept(visitor);
    }
}
