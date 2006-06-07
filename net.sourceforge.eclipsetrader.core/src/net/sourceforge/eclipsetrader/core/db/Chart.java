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

public class Chart extends PersistentObject implements Observer
{
    private String title = "";
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

    public void dump()
    {
        System.out.println("Chart - " + getId() + ", Rows = " + getRows().size());
        for (int r = 0; r < getRows().size(); r++)
        {
            ChartRow row = (ChartRow)(ChartRow)getRows().get(r);
            System.out.println("   Row" + r + ", Tabs = " + row.getTabs().size());
            for (int t = 0; t < row.getTabs().size(); t++)
            {
                ChartTab tab = (ChartTab)row.getTabs().get(t);
                System.out.println("      Tab" + t + " (" + tab.getLabel() + "), Indicators=" + tab.getIndicators().size());
                for (int i = 0; i < tab.getIndicators().size(); i++)
                {
                    ChartIndicator indicator = (ChartIndicator)tab.getIndicators().get(i);
                    System.out.println("         Indicator" + i + " (" + indicator.getPluginId() + "), Parameters=" + indicator.getParameters().size());
                    for (Iterator iter = indicator.getParameters().keySet().iterator(); iter.hasNext(); )
                    {
                        String key = (String)iter.next();
                        System.out.println("            " + key + "=" + (String)indicator.getParameters().get(key));
                    }
                }
            }
        }
    }
}
