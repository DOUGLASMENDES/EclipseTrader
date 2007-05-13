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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.NewsItem;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.ui.views.WebBrowser;
import net.sourceforge.eclipsetrader.news.NewsPlugin;
import net.sourceforge.eclipsetrader.news.internal.Messages;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
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

public class NewsView extends ViewPart {
	public static final String VIEW_ID = "net.sourceforge.eclipsetrader.newslist"; //$NON-NLS-1$

	static final String TABLE_BACKGROUND = "NEWS_TABLE_BACKGROUND"; //$NON-NLS-1$

	static final String TABLE_FOREGROUND = "NEWS_TABLE_FOREGROUND"; //$NON-NLS-1$

	static final String TABLE_FONT = "NEWS_TABLE_FONT"; //$NON-NLS-1$

	static final String READED_ITEM_BACKGROUND = "NEWS_READED_ITEM_BACKGROUND"; //$NON-NLS-1$

	static final String READED_ITEM_FOREGROUND = "NEWS_READED_ITEM_FOREGROUND"; //$NON-NLS-1$

	static final String READED_ITEM_FONT = "NEWS_READED_ITEM_FONT"; //$NON-NLS-1$

	static final String NEW_ITEM_BACKGROUND = "NEWS_NEW_ITEM_BACKGROUND"; //$NON-NLS-1$

	static final String NEW_ITEM_FOREGROUND = "NEWS_NEW_ITEM_FOREGROUND"; //$NON-NLS-1$

	static final String NEW_ITEM_FONT = "NEWS_NEW_ITEM_FONT"; //$NON-NLS-1$
	
	IDialogSettings settings;
	
	Security security;

	TableViewer viewer;
	
	NewsLabelProvider labelProvider = new NewsLabelProvider();

	List newsList = new ArrayList();

	IPropertyChangeListener themeChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			ITheme theme = (ITheme)event.getSource();
            if (event.getProperty().equals(TABLE_BACKGROUND))
                viewer.getControl().setBackground(theme.getColorRegistry().get(TABLE_BACKGROUND));
            else if (event.getProperty().equals(TABLE_FOREGROUND))
            	viewer.getControl().setForeground(theme.getColorRegistry().get(TABLE_FOREGROUND));
            else if (event.getProperty().equals(TABLE_FONT))
            	viewer.getControl().setFont(theme.getFontRegistry().get(TABLE_FONT));
            else if (event.getProperty().equals(NEW_ITEM_BACKGROUND)) {
            	labelProvider.setAddedBackground(theme.getColorRegistry().get(NEW_ITEM_BACKGROUND));
            	viewer.refresh();
            }
            else if (event.getProperty().equals(NEW_ITEM_FOREGROUND)) {
            	labelProvider.setAddedForeground(theme.getColorRegistry().get(NEW_ITEM_FOREGROUND));
            	viewer.refresh();
            }
            else if (event.getProperty().equals(NEW_ITEM_FONT)) {
            	labelProvider.setAddedFont(theme.getFontRegistry().get(NEW_ITEM_FONT));
            	viewer.refresh();
            }
            else if (event.getProperty().equals(READED_ITEM_BACKGROUND)) {
            	labelProvider.setReadedBackground(theme.getColorRegistry().get(READED_ITEM_BACKGROUND));
            	viewer.refresh();
            }
            else if (event.getProperty().equals(READED_ITEM_FOREGROUND)) {
            	labelProvider.setReadedForeground(theme.getColorRegistry().get(READED_ITEM_FOREGROUND));
            	viewer.refresh();
            }
            else if (event.getProperty().equals(READED_ITEM_FONT)) {
            	labelProvider.setReadedFont(theme.getFontRegistry().get(READED_ITEM_FONT));
            	viewer.refresh();
            }
		}
	};
	
	ICollectionObserver collectionObserver = new ICollectionObserver() {
        public void itemAdded(Object o) {
    		final NewsItem newsItem = (NewsItem)o;
        	if (security == null || newsItem.isSecurity(security)) {
        		newsList.add(newsItem);
            	try {
            		viewer.getControl().getDisplay().asyncExec(new Runnable() {
                        public void run() {
            	            if (!viewer.getControl().isDisposed()) {
            	        		viewer.add(newsItem);
            	        		newsItem.addObserver(newsItemObserver);
            	            }
                        }
            		});
            	} catch(SWTException e) {
            		if (e.code != SWT.ERROR_WIDGET_DISPOSED)
            			throw(e);
            	}
        	}
        }

        public void itemRemoved(Object o) {
    		final NewsItem newsItem = (NewsItem)o;
        	if (security == null || newsItem.isSecurity(security)) {
        		newsList.remove(newsItem);
            	try {
            		viewer.getControl().getDisplay().asyncExec(new Runnable() {
                        public void run() {
            	            if (!viewer.getControl().isDisposed()) {
            	        		viewer.remove(newsItem);
            	        		newsItem.deleteObserver(newsItemObserver);
            	            }
                        }
            		});
            	} catch(SWTException e) {
            		if (e.code != SWT.ERROR_WIDGET_DISPOSED)
            			throw(e);
            	}
        	}
        }
	};
	
	Observer newsItemObserver = new Observer() {
        public void update(final Observable o, Object arg) {
        	try {
        		viewer.getControl().getDisplay().asyncExec(new Runnable() {
                    public void run() {
        	            if (!viewer.getControl().isDisposed())
        	            	viewer.update(o, null);
                    }
        		});
        	} catch(SWTException e) {
        		if (e.code != SWT.ERROR_WIDGET_DISPOSED)
        			throw(e);
        	}
        }
	};

	ControlAdapter controlListener = new ControlAdapter() {
        public void controlResized(ControlEvent e) {
	        TableColumn column = (TableColumn)e.widget;
	        int index = viewer.getTable().indexOf(column);
	        settings.put("column" + String.valueOf(index) + ".width", column.getWidth());
	    }
	};
	
	Action refreshAction = new Action("Update") {
        public void run() {
            if (security != null)
                NewsPlugin.getDefault().startFeedSnapshot(security);
            else
                NewsPlugin.getDefault().startFeedSnapshot();
        }
	};
	
	Action showNextAction = new Action("Next") {
        public void run() {
        	showNext();
        }
	};
	
	Action showPreviousAction = new Action("Previous") {
        public void run() {
        	showPrevious();
        }
	};
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
	 */
	public void init(IViewSite site) throws PartInitException {
		refreshAction.setImageDescriptor(NewsPlugin.getImageDescriptor("icons/elcl16/refresh.gif"));
		refreshAction.setDisabledImageDescriptor(NewsPlugin.getImageDescriptor("icons/dlcl16/refresh.gif"));

		showNextAction.setImageDescriptor(NewsPlugin.getImageDescriptor("icons/elcl16/next_nav.gif"));
		showNextAction.setDisabledImageDescriptor(NewsPlugin.getImageDescriptor("icons/dlcl16/next_nav.gif"));

		showPreviousAction.setImageDescriptor(NewsPlugin.getImageDescriptor("icons/elcl16/prev_nav.gif"));
		showPreviousAction.setDisabledImageDescriptor(NewsPlugin.getImageDescriptor("icons/dlcl16/prev_nav.gif"));

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
		toolBarManager.add(showPreviousAction);
		toolBarManager.add(showNextAction);
		toolBarManager.add(new Separator("group2")); //$NON-NLS-1$
		toolBarManager.add(refreshAction);
		toolBarManager.add(new Separator("group3")); //$NON-NLS-1$
		toolBarManager.add(new Separator("group4")); //$NON-NLS-1$
		toolBarManager.add(new Separator("group5")); //$NON-NLS-1$
		toolBarManager.add(new Separator("group6")); //$NON-NLS-1$
		toolBarManager.add(new Separator("additions")); //$NON-NLS-1$
		toolBarManager.add(new Separator("end")); //$NON-NLS-1$

		settings = NewsPlugin.getDefault().getDialogSettings().getSection(VIEW_ID);
		if (settings == null)
			settings = NewsPlugin.getDefault().getDialogSettings().addNewSection(VIEW_ID);
		
		if (site.getSecondaryId() != null) {
			security = (Security) CorePlugin.getRepository().getSecurity(site.getSecondaryId());
			if (security != null)
				setPartName(security.getDescription() + " " + getPartName()); //$NON-NLS-1$

			if (settings.getSection(site.getSecondaryId()) != null)
				settings = settings.getSection(site.getSecondaryId());
			else
				settings = settings.addNewSection(site.getSecondaryId());
		}
		
		super.init(site);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(false);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText(Messages.NewsView_Date);
		column.setWidth(settings.get("column0.width") == null ? 120 : settings.getInt("column0.width"));
		column.addControlListener(controlListener);
		
		column = new TableColumn(table, SWT.NONE);
		column.setText(Messages.NewsView_Title);
		column.setWidth(settings.get("column1.width") == null ? 240 : settings.getInt("column1.width"));
		column.addControlListener(controlListener);
		
		column = new TableColumn(table, SWT.NONE);
		column.setText(Messages.NewsView_Security);
		column.setWidth(settings.get("column2.width") == null ? 120 : settings.getInt("column2.width"));
		column.addControlListener(controlListener);
		
		column = new TableColumn(table, SWT.NONE);
		column.setText(Messages.NewsView_Source);
		column.setWidth(settings.get("column3.width") == null ? 120 : settings.getInt("column3.width"));
		column.addControlListener(controlListener);

		viewer = new TableViewer(table);
		viewer.setLabelProvider(labelProvider);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setComparator(new ViewerComparator() {
            public int compare(Viewer viewer, Object e1, Object e2) {
	            return ((NewsItem)e2).getDate().compareTo(((NewsItem)e1).getDate());
            }
		});
		viewer.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
            	showSelected();
            }
		});

		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		ITheme theme = themeManager.getCurrentTheme();
		if (theme != null) {
			table.setForeground(theme.getColorRegistry().get(TABLE_FOREGROUND));
			table.setBackground(theme.getColorRegistry().get(TABLE_BACKGROUND));
			table.setFont(theme.getFontRegistry().get(TABLE_FONT));

			labelProvider.setAddedBackground(theme.getColorRegistry().get(NEW_ITEM_BACKGROUND));
			labelProvider.setAddedForeground(theme.getColorRegistry().get(NEW_ITEM_FOREGROUND));
			labelProvider.setAddedFont(theme.getFontRegistry().get(NEW_ITEM_FONT));
			
			labelProvider.setReadedBackground(theme.getColorRegistry().get(READED_ITEM_BACKGROUND));
			labelProvider.setReadedForeground(theme.getColorRegistry().get(READED_ITEM_FOREGROUND));
			labelProvider.setReadedFont(theme.getFontRegistry().get(READED_ITEM_FONT));

			theme.addPropertyChangeListener(themeChangeListener);
		}
		
		viewer.setInput(newsList);
		
		if (security != null)
			CorePlugin.getRepository().allNews(security).addCollectionObserver(collectionObserver);
		else
			CorePlugin.getRepository().allNews().addCollectionObserver(collectionObserver);
		
		NewsStartupJob job = new NewsStartupJob(security);
		job.addJobChangeListener(new JobChangeAdapter() {

			/* (non-Javadoc)
             * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
             */
            public void done(IJobChangeEvent event) {
            	final NewsStartupJob job = (NewsStartupJob)event.getJob();
            	try {
            		viewer.getControl().getDisplay().asyncExec(new Runnable() {
                        public void run() {
            	            if (!viewer.getControl().isDisposed()) {
            		            newsList = job.getList();
            	            	viewer.setInput(newsList);
            	            	for (Iterator iter = newsList.iterator(); iter.hasNext(); )
                	        		((NewsItem)iter.next()).addObserver(newsItemObserver);
            	            }
                        }
            		});
            	} catch(SWTException e) {
            		if (e.code != SWT.ERROR_WIDGET_DISPOSED)
            			throw(e);
            	}
            }
		});
		job.setUser(false);
		job.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		if (!viewer.getControl().isDisposed())
			viewer.getControl().setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		if (security != null)
			CorePlugin.getRepository().allNews(security).removeCollectionObserver(collectionObserver);
		else
			CorePlugin.getRepository().allNews().removeCollectionObserver(collectionObserver);

		for (Iterator iter = newsList.iterator(); iter.hasNext(); )
    		((NewsItem)iter.next()).deleteObserver(newsItemObserver);
		
		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		ITheme theme = themeManager.getCurrentTheme();
		if (theme != null)
			theme.removePropertyChangeListener(themeChangeListener);
		
		super.dispose();
	}

	public void showNext() {
		NewsItem news = getSelectedItem();
		TableItem[] items = viewer.getTable().getItems();
		if (news == null && items.length != 0) {
			viewer.setSelection(new StructuredSelection(items[0].getData()));
			viewer.reveal(items[0].getData());
			showSelected();
			return;
		}
		for (int i = 0; i < items.length; i++) {
			if (items[i].getData() == news) {
				i++;
				if (i < items.length) {
					viewer.setSelection(new StructuredSelection(items[i].getData()));
					viewer.reveal(items[i].getData());
					showSelected();
					return;
				}
			}
		}
	}

	public void showPrevious() {
		NewsItem news = getSelectedItem();
		TableItem[] items = viewer.getTable().getItems();
		if (news == null && items.length != 0) {
			viewer.setSelection(new StructuredSelection(items[items.length - 1].getData()));
			viewer.reveal(items[items.length - 1].getData());
			showSelected();
			return;
		}
		for (int i = 0; i < items.length; i++) {
			if (items[i].getData() == news) {
				i--;
				if (i >= 0) {
					viewer.setSelection(new StructuredSelection(items[i].getData()));
					viewer.reveal(items[i].getData());
					showSelected();
					return;
				}
			}
		}
	}

	private void showSelected() {
		NewsItem news = getSelectedItem();
		if (news != null) {
			try {
				WebBrowser browser = (WebBrowser) getSite().getPage().showView(WebBrowser.VIEW_ID, NewsPlugin.PLUGIN_ID, IWorkbenchPage.VIEW_ACTIVATE);
				if (browser != null) {
					browser.setUrl(news.getUrl());
					news.setRecent(false);
					news.setReaded(true);
					CorePlugin.getRepository().save(news);
				}
			} catch (PartInitException e1) {
				CorePlugin.logException(e1);
			}
		}
	}

	protected NewsItem getSelectedItem() {
		if (!viewer.getSelection().isEmpty())
			return (NewsItem) ((IStructuredSelection)viewer.getSelection()).getFirstElement();
		return null;
	}
}
