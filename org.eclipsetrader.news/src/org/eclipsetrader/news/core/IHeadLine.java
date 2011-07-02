package org.eclipsetrader.news.core;

import java.util.Date;

import org.eclipsetrader.core.instruments.ISecurity;

public interface IHeadLine {

    public Date getDate();

    public String getText();

    public String getSource();

    public boolean contains(ISecurity security);

    public ISecurity[] getMembers();

    public boolean isRecent();

    public boolean isReaded();

    public void setReaded(boolean readed);

    public String getLink();
}
