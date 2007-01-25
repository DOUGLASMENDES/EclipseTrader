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

import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.ObservableList;
import net.sourceforge.eclipsetrader.core.db.NewsItem;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.ui.views.WebBrowser;
import net.sourceforge.eclipsetrader.news.NewsPlugin;
import net.sourceforge.eclipsetrader.news.internal.Messages;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

public class NewsView extends ViewPart implements ICollectionObserver
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.newslist"; //$NON-NLS-1$
    public static final String TABLE_BACKGROUND = "NEWS_TABLE_BACKGROUND"; //$NON-NLS-1$
    public static final String TABLE_FOREGROUND = "NEWS_TABLE_FOREGROUND"; //$NON-NLS-1$
    public static final String TABLE_FONT = "NEWS_TABLE_FONT"; //$NON-NLS-1$
    public static final String READED_ITEM_BACKGROUND = "NEWS_READED_ITEM_BACKGROUND"; //$NON-NLS-1$
    public static final String READED_ITEM_FOREGROUND = "NEWS_READED_ITEM_FOREGROUND"; //$NON-NLS-1$
    public static final String READED_ITEM_FONT = "NEWS_READED_ITEM_FONT"; //$NON-NLS-1$
    public static final String NEW_ITEM_BACKGROUND = "NEWS_NEW_ITEM_BACKGROUND"; //$NON-NLS-1$
    public static final String NEW_ITEM_FOREGROUND = "NEWS_NEW_ITEM_FOREGROUND"; //$NON-NLS-1$
    public static final String NEW_ITEM_FONT = "NEWS_NEW_ITEM_FONT"; //$NON-NLS-1$
    private Security security;
    private Table table;
    private ObservableList newsList = new ObservableList();
    private ITheme theme;
    private IPropertyChangeListener themeChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event)
        {
            if (event.getProperty().equals(TABLE_BACKGROUND))
                table.setBackground(theme.getColorRegistry().get(TABLE_BACKGROUND));
            else if (event.getProperty().equals(TABLE_FOREGROUND))
                table.setForeground(theme.getColorRegistry().get(TABLE_FOREGROUND));
            else if (event.getProperty().equals(TABLE_FONT))
                table.setFont(theme.getFontRegistry().get(TABLE_FONT));
        }
    };

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
     */
    public void init(IViewSite site) throws PartInitException
    {
        IMenuManager menuManager = site.getActionBars().getMenuManager();
        menuManager.add(new Separator("top")); //$NON-NLS-1$
        menuManager.add(new Separator("group1")); //$NON-NLS-1$
        menuManager.add(new Separator("group2")); //$NON-NLS-1$
        menuManager.add(new Separator("group3")); //$NON-NLS-1$
        menuManager.add(new Separator("group4")); //$NON-NLS-1$
        menuManager.add(new Separator("group5")); //$NON-NLS-1$
        menuManager.add(new Separator("group6")); //$NON-NLS-1$
        menuManager.add(new Separator("additions")); //$NON-NLS-1$
        menuManager.add(new Separator("bottom")); //$NON-NLS-1$
        
        IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
        toolBarManager.add(new Separator("begin")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group1")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group2")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group3")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group4")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group5")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group6")); //$NON-NLS-1$
        toolBarManager.add(new Separator("additions")); //$NON-NLS-1$
        toolBarManager.add(new Separator("end")); //$NON-NLS-1$
        
        IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
        if (themeManager != null)
        {
            theme = themeManager.getCurrentTheme();
            if (theme != null)
            {
                if (!theme.getFontRegistry().hasValueFor(NewsView.NEW_ITEM_FONT))
                {
                    Font font = theme.getFontRegistry().getBold(NewsView.NEW_ITEM_FONT);
                    theme.getFontRegistry().put(NewsView.NEW_ITEM_FONT, font.getFontData());
                }
                theme.addPropertyChangeListener(themeChangeListener);
            }
        }
        super.init(site);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
        content.setLayout(gridLayout);
        
        table = new Table(content, SWT.MULTI|SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        if (theme != null)
        {
            table.setForeground(theme.getColorRegistry().get(TABLE_FOREGROUND));
            table.setBackground(theme.getColorRegistry().get(TABLE_BACKGROUND));
            table.setFont(theme.getFontRegistry().get(TABLE_FONT));
        }
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(Messages.NewsView_Date);
        column.setWidth(120);
        column = new TableColumn(table, SWT.NONE);
        column.setText(Messages.NewsView_Title);
        column.setWidth(60);
        column = new TableColumn(table, SWT.NONE);
        column.setText(Messages.NewsView_Security);
        column.setWidth(120);
        column = new TableColumn(table, SWT.NONE);
        column.setText(Messages.NewsView_Source);
        column.setWidth(100);
        table.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e)
            {
                int width = table.getClientArea().width - table.getColumn(0).getWidth() - table.getColumn(2).getWidth() - 110;
                if (table.getVerticalBar() != null)
                    width -= table.getVerticalBar().getSize().x;
                if (width < 100) width = 100;
                table.getColumn(1).setWidth(width);
                if ("gtk".equals(SWT.getPlatform())) //$NON-NLS-1$
                    table.getColumn(table.getColumnCount() - 1).pack();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent e)
            {
                showSelected();
            }
        });
        
        parent.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                if (table.isDisposed())
                    return;
                
                if (getViewSite().getSecondaryId() != null)
                {
                    security = (Security) CorePlugin.getRepository().getSecurity(getViewSite().getSecondaryId());
                    if (security != null)
                    {
                        setPartName(security.getDescription() + " " + getPartName()); //$NON-NLS-1$
                        newsList = CorePlugin.getRepository().allNews(security);
                    }
                }
                else
                    newsList = CorePlugin.getRepository().allNews();

                table.setRedraw(false);
                for (Iterator iter = newsList.iterator(); iter.hasNext(); )
                {
                    NewsItem news = (NewsItem) iter.next();
                    TableItem tableItem = null;

                    TableItem[] items = table.getItems();
                    for (int i = 0; i < items.length; i++)
                    {
                        if (news.getDate().compareTo(((NewsTableItem)items[i]).getNewsItem().getDate()) > 0)
                        {
                            tableItem = new NewsTableItem(news, table, SWT.NONE, i);
                            break;
                        }
                    }

                    if (tableItem == null)
                        tableItem = new NewsTableItem(news, table, SWT.NONE);
                }
                table.setRedraw(true);
                newsList.addCollectionObserver(NewsView.this);
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus()
    {
        table.getParent().getParent().setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    public void dispose()
    {
        newsList.removeCollectionObserver(this);
        if (theme != null)
            theme.removePropertyChangeListener(themeChangeListener);
        super.dispose();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemAdded(java.lang.Object)
     */
    public void itemAdded(final Object o)
    {
        table.getDisplay().syncExec(new Runnable() {
            public void run()
            {
                NewsItem news = (NewsItem) o;
                TableItem tableItem = null;

                TableItem[] items = table.getItems();
                for (int i = 0; i < items.length; i++)
                {
                    if (news.getDate().compareTo(((NewsTableItem)items[i]).getNewsItem().getDate()) > 0)
                    {
                        tableItem = new NewsTableItem(news, table, SWT.NONE, i);
                        break;
                    }
                }

                if (tableItem == null)
                    tableItem = new NewsTableItem(news, table, SWT.NONE);
            }
        });
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemRemoved(java.lang.Object)
     */
    public void itemRemoved(final Object o)
    {
        table.getDisplay().syncExec(new Runnable() {
            public void run()
            {
                TableItem[] items = table.getItems();
                for (int i = 0; i < items.length; i++)
                {
                    if (o.equals(((NewsTableItem)items[i]).getNewsItem()))
                        items[i].dispose();
                }
            }
        });
    }
    
    public void showNext()
    {
        int index = table.getSelectionIndex() + 1;
        if (index >= table.getItemCount())
            index = 0;
        table.setSelection(index);
        showSelected();
    }

    public void showPrevious()
    {
        int index = table.getSelectionIndex();
        if (index <= 0)
            index = table.getItemCount() - 1;
        else
            index--;
        table.setSelection(index);
        showSelected();
    }
    
    private void showSelected()
    {
        if (table.getSelectionCount() != 0)
        {
            NewsTableItem tableItem = (NewsTableItem) table.getSelection()[0];
            NewsItem news = tableItem.getNewsItem();
            try {
                WebBrowser browser = (WebBrowser) getSite().getPage().showView(WebBrowser.VIEW_ID, NewsPlugin.PLUGIN_ID, IWorkbenchPage.VIEW_ACTIVATE);
                if (browser != null)
                {
                    browser.setUrl(news.getUrl());
                    news.setRecent(false);
                    news.setReaded(true);
                    CorePlugin.getRepository().save(news);
                }
            } catch(PartInitException e1) {
                CorePlugin.logException(e1);
            };
        }
    }
    
    public Security getSecurity()
    {
        return security;
    }
}
