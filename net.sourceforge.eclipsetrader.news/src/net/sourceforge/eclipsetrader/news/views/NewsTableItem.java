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

package net.sourceforge.eclipsetrader.news.views;

import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.db.NewsItem;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

public class NewsTableItem extends TableItem implements DisposeListener, Observer
{
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm"); //$NON-NLS-1$
    private NewsItem newsItem;
    private ITheme theme;
    private IPropertyChangeListener themeChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event)
        {
            if (newsItem.isReaded())
            {
                if (event.getProperty().equals(NewsView.READED_ITEM_BACKGROUND))
                    setBackground(theme.getColorRegistry().get(NewsView.READED_ITEM_BACKGROUND));
                else if (event.getProperty().equals(NewsView.READED_ITEM_FOREGROUND))
                    setForeground(theme.getColorRegistry().get(NewsView.READED_ITEM_FOREGROUND));
                else if (event.getProperty().equals(NewsView.READED_ITEM_FONT))
                    setFont(theme.getFontRegistry().get(NewsView.READED_ITEM_FONT));
            }
            else if (newsItem.isRecent())
            {
                if (event.getProperty().equals(NewsView.NEW_ITEM_BACKGROUND))
                    setBackground(theme.getColorRegistry().get(NewsView.NEW_ITEM_BACKGROUND));
                else if (event.getProperty().equals(NewsView.NEW_ITEM_FOREGROUND))
                    setForeground(theme.getColorRegistry().get(NewsView.NEW_ITEM_FOREGROUND));
                else if (event.getProperty().equals(NewsView.NEW_ITEM_FONT))
                    setFont(theme.getFontRegistry().get(NewsView.NEW_ITEM_FONT));
            }
        }
    };

    public NewsTableItem(NewsItem newsItem, Table parent, int style, int index)
    {
        super(parent, style, index);
        init(newsItem);
    }

    public NewsTableItem(NewsItem newsItem, Table parent, int style)
    {
        super(parent, style);
        init(newsItem);
    }

    protected void init(NewsItem newsItem)
    {
        this.newsItem = newsItem;
        this.newsItem.addObserver(this);

        IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
        if (themeManager != null)
        {
            theme = themeManager.getCurrentTheme();
            if (theme != null)
                theme.addPropertyChangeListener(themeChangeListener);
        }
        
        update();
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
     */
    public void widgetDisposed(DisposeEvent e)
    {
        if (theme != null)
            theme.removePropertyChangeListener(themeChangeListener);
        if (newsItem != null)
            newsItem.deleteObserver(this);
    }

    public void update()
    {
        setText(0, dateTimeFormat.format(newsItem.getDate()));
        setText(1, newsItem.getTitle());
        if (newsItem.getSecurity() != null)
            setText(2, newsItem.getSecurity().getDescription());
        setText(3, newsItem.getSource());

        if (newsItem.isReaded())
        {
            setBackground(theme.getColorRegistry().get(NewsView.READED_ITEM_BACKGROUND));
            setForeground(theme.getColorRegistry().get(NewsView.READED_ITEM_FOREGROUND));
            setFont(theme.getFontRegistry().get(NewsView.READED_ITEM_FONT));
        }
        else if (newsItem.isRecent())
        {
            setBackground(theme.getColorRegistry().get(NewsView.NEW_ITEM_BACKGROUND));
            setForeground(theme.getColorRegistry().get(NewsView.NEW_ITEM_FOREGROUND));
            setFont(theme.getFontRegistry().get(NewsView.NEW_ITEM_FONT));
        }
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg)
    {
        if (!isDisposed())
        {
            getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    if (!isDisposed())
                        update();
                }
            });
        }
    }
    
    public NewsItem getNewsItem()
    {
        return newsItem;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.TableItem#checkSubclass()
     */
    protected void checkSubclass()
    {
    }
}
