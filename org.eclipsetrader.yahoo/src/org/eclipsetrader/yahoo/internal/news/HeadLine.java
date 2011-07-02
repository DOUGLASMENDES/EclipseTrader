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

package org.eclipsetrader.yahoo.internal.news;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.news.core.IHeadLine;

@XmlRootElement(name = "headline")
@XmlType(name = "org.eclipsetrader.yahoo.Headline")
public class HeadLine implements IHeadLine {

    @XmlAttribute(name = "date")
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private Date date;

    private boolean recent;

    @XmlAttribute(name = "readed")
    private boolean readed;

    @XmlElement(name = "text")
    private String text;

    @XmlElement(name = "link")
    private String link;

    @XmlElement(name = "source")
    private String source;

    @XmlElementWrapper(name = "members")
    @XmlElement(name = "security")
    @XmlJavaTypeAdapter(SecurityAdapter.class)
    private List<ISecurity> members;

    public HeadLine() {
    }

    public HeadLine(Date date, String source, String text, ISecurity[] members, String link) {
        this.date = date;
        this.source = source;
        this.text = text;
        this.members = members != null ? new ArrayList<ISecurity>(Arrays.asList(members)) : null;
        this.link = link;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.IHeadLine#getDate()
     */
    @Override
    @XmlTransient
    public Date getDate() {
        return date;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.IHeadLine#getSource()
     */
    @Override
    @XmlTransient
    public String getSource() {
        return source;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.IHeadLine#getText()
     */
    @Override
    @XmlTransient
    public String getText() {
        return text;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.IHeadLine#contains(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public boolean contains(ISecurity security) {
        if (members == null) {
            return false;
        }
        return members.contains(security);
    }

    /**
     * Adds a member to the receiver.
     *
     * @param security the member to add.
     */
    public void addMember(ISecurity security) {
        if (members == null) {
            members = new ArrayList<ISecurity>();
        }
        if (!members.contains(security)) {
            members.add(security);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.IHeadLine#getMembers()
     */
    @Override
    @XmlTransient
    public ISecurity[] getMembers() {
        return members != null ? members.toArray(new ISecurity[members.size()]) : new ISecurity[0];
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.IHeadLine#isReaded()
     */
    @Override
    @XmlTransient
    public boolean isReaded() {
        return readed;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.IHeadLine#setReaded(boolean)
     */
    @Override
    public void setReaded(boolean readed) {
        this.readed = readed;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.IHeadLine#isRecent()
     */
    @Override
    @XmlTransient
    public boolean isRecent() {
        return recent;
    }

    public void setRecent(boolean recent) {
        this.recent = recent;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.IHeadLine#getLink()
     */
    @Override
    @XmlTransient
    public String getLink() {
        return link;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HeadLine)) {
            return false;
        }
        HeadLine other = (HeadLine) obj;
        if (link.equals(other.getLink())) {
            return true;
        }
        return text.equals(other.getText()) && (source == other.getSource() || source != null && source.equals(other.getSource()));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 3 * text.hashCode() + 7 * (source != null ? source.hashCode() : 0) + 11;
    }
}
