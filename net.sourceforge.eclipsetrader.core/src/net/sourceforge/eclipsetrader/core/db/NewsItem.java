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

import java.util.Calendar;
import java.util.Date;

public class NewsItem extends PersistentObject
{
    private Date date = Calendar.getInstance().getTime();
    private String title = "";
    private String source = "";
    private String url = "";
    private Security security;
    private boolean recent = false;
    private boolean readed = false;

    public NewsItem()
    {
    }

    public NewsItem(Integer id)
    {
        super(id);
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        if (!this.date.equals(date))
            setChanged();
        this.date = date;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        if (!this.title.equals(title))
            setChanged();
        this.title = title;
    }

    public Security getSecurity()
    {
        return security;
    }

    public void setSecurity(Security security)
    {
        if ((this.security == null && security != null) || (this.security != null && !this.security.equals(security)))
            setChanged();
        this.security = security;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        if (!this.source.equals(source))
            setChanged();
        this.source = source;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        if (!this.url.equals(url))
            setChanged();
        this.url = url;
    }

    public boolean isRecent()
    {
        return recent;
    }

    public void setRecent(boolean recent)
    {
        if (this.recent != recent)
            setChanged();
        this.recent = recent;
    }

    public boolean isReaded()
    {
        return readed;
    }

    public void setReaded(boolean readed)
    {
        if (this.readed != readed)
            setChanged();
        this.readed = readed;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof NewsItem))
            return false;
        NewsItem that = (NewsItem)obj;
        return this.date.equals(that.date) && this.title.equals(that.title);
    }
}
