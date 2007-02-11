/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.core.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Instances of this class represents a news headline.
 */
public class NewsItem extends PersistentObject
{
    Date date = Calendar.getInstance().getTime();
    String title = "";
    String source = "";
    String url = "";
    List securities = new ArrayList();
    boolean recent = false;
    boolean readed = false;

    /**
     * Construct an empty news item.
     */
    public NewsItem()
    {
    }

    /**
     * Construct an empty news item with the given id.
     */
    public NewsItem(Integer id)
    {
        super(id);
    }

    /**
     * Returns the headline's date.
     * 
     * @return the date.
     */
    public Date getDate()
    {
        return date;
    }

    /**
     * Sets the headline's date.
     * 
     * @param date the date to set.
     */
    public void setDate(Date date)
    {
        if (!this.date.equals(date))
            setChanged();
        this.date = date;
    }

    /**
     * Returns the headline's title.
     * 
     * @return the title.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Sets the headline's title.
     * 
     * @param title the title to set.
     */
    public void setTitle(String title)
    {
        if (!this.title.equals(title))
            setChanged();
        this.title = title;
    }

    /**
     * Returns an unmodifiable list of securities related to this headline.
     * 
     * @return a list of securities.
     */
    public List getSecurities()
    {
        return Collections.unmodifiableList(securities);
    }
    
    /**
     * Sets the list of securities related to this headline.
     * 
     * @param the list of securities to set.
     */
    public void setSecurities(List securities)
    {
        this.securities = securities;
        setChanged();
    }

    /**
     * Adds a security to the list of securities related to this headline.
     * <br>Duplicates are ignored.
     * 
     * @param security the security to add.
     */
    public void addSecurity(Security security)
    {
        if (security != null)
        {
            if (!securities.contains(security))
            {
                securities.add(security);
                setChanged();
            }
        }
    }

    /**
     * Adds the securities in the given list to the list of securities
     * related to this headline.
     * <br>Non security objects and duplicates are ignored.
     * 
     * @param list the list of securities to add.
     */
    public void addSecurities(List list)
    {
        Object[] o = list.toArray();
        for (int i = 0; i < o.length; i++)
        {
            if (o[i] instanceof Security)
            {
                Security security = (Security)o[i];
                if (!securities.contains(security))
                {
                    securities.add(security);
                    setChanged();
                }
            }
        }
    }
    
    /**
     * Returns wether this headline is related to the given security.
     * 
     * @param security the security to check.
     * @return true if this headline is related to the security.
     */
    public boolean isSecurity(Security security)
    {
        return securities.contains(security);
    }

    /**
     * Returns the headline's source (news agency).
     * 
     * @return the source name.
     */
    public String getSource()
    {
        return source;
    }

    /**
     * Sets the headline's source (news agency).
     * 
     * @param source the name to set.
     */
    public void setSource(String source)
    {
        if (!this.source.equals(source))
            setChanged();
        this.source = source;
    }

    /**
     * Returns the url of the headline's web page.
     * 
     * @return the headline's url.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Sets the url of the headline's web page.
     * 
     * @param url the url to set.
     */
    public void setUrl(String url)
    {
        if (!this.url.equals(url))
            setChanged();
        this.url = url;
    }

    /**
     * Returns wether this headline was recently added to the database.
     * <br>The means of 'recently added' is specific to the news provider plugin.
     * The UI may display recently added headlines with a different font/color.
     * <br>This field is not persisted across sessions.
     * 
     * @return true if this headline was recently added to the database.
     */
    public boolean isRecent()
    {
        return recent;
    }

    /**
     * Sets the recently added flag.
     * <br>The means of 'recently added' is specific to the news provider plugin.
     * The UI may display recently added headlines with a different font/color.
     * <br>This field is not persisted across sessions.
     * 
     * @param recent the recently flag status.
     */
    public void setRecent(boolean recent)
    {
        if (this.recent != recent)
            setChanged();
        this.recent = recent;
    }

    /**
     * Returns wether this headline was readed by the user.
     * <br>The UI may display recently added headlines with a different font/color.
     * 
     * @return true if this headline was readed by the user.
     */
    public boolean isReaded()
    {
        return readed;
    }

    /**
     * Sets the readed flag.
     * <br>The UI may display readed headlines with a different font/color.
     * 
     * @param readed the readed flag status.
     */
    public void setReaded(boolean readed)
    {
        if (this.readed != readed)
            setChanged();
        this.readed = readed;
    }

    /**
     * Compares two headline objects for equality.
     * <br>News items are considered equals if the headlines dates and titles are equal.
     * 
     * @param obj the object to compare.
     * @return true if the given object is equal to this.
     */
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof NewsItem))
            return false;
        NewsItem that = (NewsItem)obj;
        return this.date.equals(that.date) && this.title.equals(that.title);
    }
}
