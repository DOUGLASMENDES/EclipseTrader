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

import java.util.Date;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.ObservableList;
import net.sourceforge.eclipsetrader.core.db.visitors.IChartVisitor;

public class Chart extends PersistentObject implements Observer
{
    private String title = ""; //$NON-NLS-1$
    private Security security;
    private int compression = BarData.INTERVAL_DAILY;
    private int period = 0;
    private Date beginDate;
    private Date endDate;
    private boolean autoScale = true;
    private ObservableList rows = new ObservableList();

    public Chart()
    {
    }

    public Chart(Integer id)
    {
        super(id);
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String label)
    {
        this.title = label;
        setChanged();
    }

    public Security getSecurity()
    {
        return security;
    }

    public void setSecurity(Security security)
    {
        if (security != null && !security.equals(this.security))
            setChanged();
        else if (security == null && this.security != null)
            setChanged();

        if (this.security != null)
            this.security.deleteObserver(this);
        this.security = security;
        if (this.security != null)
            this.security.addObserver(this);
    }

    public int getCompression()
    {
        return compression;
    }

    public void setCompression(int compression)
    {
        this.compression = compression;
        setChanged();
    }

    public Date getBeginDate()
    {
        return beginDate;
    }

    public void setBeginDate(Date beginDate)
    {
        this.beginDate = beginDate;
        setChanged();
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
        setChanged();
    }

    public int getPeriod()
    {
        return period;
    }

    public void setPeriod(int period)
    {
        this.period = period;
        setChanged();
    }

    public boolean isAutoScale()
    {
        return autoScale;
    }

    public void setAutoScale(boolean autoScale)
    {
        this.autoScale = autoScale;
    }

    public ObservableList getRows()
    {
        return rows;
    }

    public void setRows(ObservableList rows)
    {
        this.rows = rows;
    }
    
    public void add(ChartRow row)
    {
        row.setParent(this);
        getRows().add(row);
    }
    
    public void add(int index, ChartRow row)
    {
        row.setParent(this);
        getRows().add(index, row);

        ChartRow[] rows = (ChartRow[])getRows().toArray(new ChartRow[getRows().size()]);
        for (int i = 0; i < rows.length; i++)
            rows[i].setId(new Integer(i));
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.PersistentObject#clearChanged()
     */
    public synchronized void clearChanged()
    {
        for (Iterator iter = getRows().iterator(); iter.hasNext(); )
            ((ChartRow)iter.next()).clearChanged();
        super.clearChanged();
    }

    /* (non-Javadoc)
     * @see java.util.Observable#notifyObservers()
     */
    public void notifyObservers()
    {
        for (Iterator iter = getRows().iterator(); iter.hasNext(); )
            ((ChartRow)iter.next()).notifyObservers();
        super.notifyObservers();
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg)
    {
        setChanged();
        notifyObservers();
    }
    
    public void accept(IChartVisitor visitor)
    {
        visitor.visit(this);

        ChartRow[] rows = (ChartRow[])getRows().toArray(new ChartRow[getRows().size()]);
        for (int i = 0; i < rows.length; i++)
            rows[i].accept(visitor);
    }
}
