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

package net.sourceforge.eclipsetrader.core.ui.views;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.ui.NullSelection;
import net.sourceforge.eclipsetrader.core.ui.SecuritySelection;
import net.sourceforge.eclipsetrader.core.ui.SelectionProvider;
import net.sourceforge.eclipsetrader.core.ui.wizards.SecurityWizard;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 */
public class SecuritiesView extends ViewPart implements ICollectionObserver
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.views.securities";
    public static final String PREFS_SORT_COLUMN = "SORT_COLUMN";
    public static final String PREFS_SORT_DIRECTION = "SORT_DIRECTION";
    public static final String TABLE_BACKGROUND = "TABLE_BACKGROUND";
    public static final String TABLE_FOREGROUND = "TABLE_FOREGROUND";
    public static final String TABLE_FONT = "TABLE_FONT";
    private Table table;
    private int sortColumn = 0;
    private int sortDirection = 1;
    private ITheme theme;
    private Comparator comparator = new Comparator() {
        public int compare(Object arg0, Object arg1)
        {
            switch(sortColumn)
            {
                case 0:
                    if (sortDirection == 1)
                        return ((Security)arg0).getCode().compareTo(((Security)arg1).getCode());
                    else
                        return ((Security)arg1).getCode().compareTo(((Security)arg0).getCode());
                case 1:
                    if (sortDirection == 1)
                        return ((Security)arg0).getDescription().compareTo(((Security)arg1).getDescription());
                    else
                        return ((Security)arg1).getDescription().compareTo(((Security)arg0).getDescription());
            }
            return 0;
        }
    };
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
        menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menuManager.add(new Separator("bottom")); //$NON-NLS-1$
        
        IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
        toolBarManager.add(new Separator("begin")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group1")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group2")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group3")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group4")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group5")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group6")); //$NON-NLS-1$
        toolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        toolBarManager.add(new Separator("end")); //$NON-NLS-1$
        
        IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
        if (themeManager != null)
        {
            theme = themeManager.getCurrentTheme();
            if (theme != null)
                theme.addPropertyChangeListener(themeChangeListener);
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
        table.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                updateSelection();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e)
            {
                if (table.getItem(new Point(e.x, e.y)) == null)
                {
                    table.deselectAll();
                    getSite().getSelectionProvider().setSelection(new NullSelection());
                }
            }
            public void mouseDoubleClick(MouseEvent e)
            {
                Security security = getSelection();
                if (security != null)
                {
                    SecurityWizard wizard = new SecurityWizard();
                    wizard.open(security);
                }
            }
        });
        table.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e)
            {
                int width = table.getSize().x - table.getColumn(0).getWidth(); // - 60;
                if (table.getVerticalBar() != null)
                    width -= table.getVerticalBar().getSize().x;
                if (width < 100) width = 100;
                table.getColumn(1).setWidth(width);
            }
        });
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText("Code");
        column.setWidth(60);
        column.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                if (sortColumn == 0)
                    sortDirection = (sortDirection == 1) ? 0 : 1;
                else
                {
                    sortColumn = 0;
                    sortDirection = 1;
                }

                IPreferenceStore prefs = CorePlugin.getDefault().getPreferenceStore();
                prefs.setValue(PREFS_SORT_COLUMN, sortColumn);
                prefs.setValue(PREFS_SORT_DIRECTION, sortDirection);
                
                updateView();
            }
        });
        column = new TableColumn(table, SWT.NONE);
        column.setText("Description");
        column.setWidth(50);
        column.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                if (sortColumn == 1)
                    sortDirection = (sortDirection == 1) ? 0 : 1;
                else
                {
                    sortColumn = 1;
                    sortDirection = 1;
                }

                IPreferenceStore prefs = CorePlugin.getDefault().getPreferenceStore();
                prefs.setValue(PREFS_SORT_COLUMN, sortColumn);
                prefs.setValue(PREFS_SORT_DIRECTION, sortDirection);
                
                updateView();
            }
        });
/*        column = new TableColumn(table, SWT.NONE);
        column.setText("Currency");
        column.setWidth(60);*/
        
        // Drag and drop support
        DragSource dragSource = new DragSource(table, DND.DROP_COPY);
        Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
        dragSource.setTransfer(types);
        dragSource.addDragListener(new DragSourceListener() {
            public void dragStart(DragSourceEvent event)
            {
                if (table.getSelectionCount() == 0)
                    event.doit = false;
            }

            public void dragSetData(DragSourceEvent event)
            {
                TableItem selection[] = table.getSelection();
                String securities = "";
                for (int i = 0; i < selection.length; i++)
                {
                    SecurityTableItem item = (SecurityTableItem)selection[i];
                    securities += String.valueOf(item.getSecurity().getId()) + ";";
                }
                event.data = securities;
            }

            public void dragFinished(DragSourceEvent event)
            {
            }
        });
        
        IPreferenceStore prefs = CorePlugin.getDefault().getPreferenceStore();
        prefs.setDefault(PREFS_SORT_COLUMN, sortColumn);
        prefs.setDefault(PREFS_SORT_DIRECTION, sortDirection);
        sortColumn = prefs.getInt(PREFS_SORT_COLUMN);
        sortDirection = prefs.getInt(PREFS_SORT_DIRECTION);

        getSite().setSelectionProvider(new SelectionProvider());

        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager menuManager)
            {
                menuManager.add(new Separator("top")); //$NON-NLS-1$
                menuManager.add(new Separator("group1")); //$NON-NLS-1$
                menuManager.add(new Separator("group2")); //$NON-NLS-1$
                menuManager.add(new Separator("group3")); //$NON-NLS-1$
                menuManager.add(new Separator("group4")); //$NON-NLS-1$
                menuManager.add(new Separator("group5")); //$NON-NLS-1$
                menuManager.add(new Separator("group6")); //$NON-NLS-1$
                menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
                menuManager.add(new Separator("bottom")); //$NON-NLS-1$
            }
        });
        table.setMenu(menuMgr.createContextMenu(table));
        getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());

        parent.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                updateView();
                CorePlugin.getRepository().allSecurities().addCollectionObserver(SecuritiesView.this);
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus()
    {
        table.getParent().setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    public void dispose()
    {
        if (theme != null)
            theme.removePropertyChangeListener(themeChangeListener);
        CorePlugin.getRepository().allSecurities().removeCollectionObserver(this);
        super.dispose();
    }
    
    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemAdded(java.lang.Object)
     */
    public void itemAdded(Object o)
    {
        Security security = (Security)o;

        TableItem items[] = table.getItems();
        for (int i = 0; i < items.length; i++)
        {
            Security arg1 = ((SecurityTableItem)items[i]).getSecurity();
            if (comparator.compare(security, arg1) < 0)
            {
                new SecurityTableItem(table, SWT.NONE, i, security);
                updateSelection();
                return;
            }
        }

        new SecurityTableItem(table, SWT.NONE, security);
        updateSelection();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemRemoved(java.lang.Object)
     */
    public void itemRemoved(Object o)
    {
        TableItem items[] = table.getItems();
        for (int i = 0; i < items.length; i++)
        {
            if (o.equals(((SecurityTableItem) items[i]).getSecurity()))
                items[i].dispose();
        }
        updateSelection();
    }

    public void updateView()
    {
        List list = CorePlugin.getRepository().allSecurities();
        Collections.sort(list, comparator);
        
        int index = 0;
        SecurityTableItem tableItem = null;
        for (Iterator iter = list.iterator(); iter.hasNext(); )
        {
            Security security = (Security)iter.next();
            if (index < table.getItemCount())
            {
                tableItem = (SecurityTableItem) table.getItem(index);
                tableItem.setSecurity(security);
            }
            else
                tableItem = new SecurityTableItem(table, SWT.NONE, security);
            index++;
        }
        table.setItemCount(index);
        
        updateSelection();
    }
    
    public Security getSelection()
    {
        Security selection = null;
        if (table.getSelectionCount() == 1)
            selection = ((SecurityTableItem)table.getSelection()[0]).getSecurity();
        return selection;
    }
    
    private void updateSelection()
    {
        Security security = getSelection();
        if (security != null)
            getSite().getSelectionProvider().setSelection(new SecuritySelection(security));
        else
            getSite().getSelectionProvider().setSelection(new NullSelection());
    }
    
    private class SecurityTableItem extends TableItem implements DisposeListener, Observer
    {
        private Security security;

        public SecurityTableItem(Table parent, int style, int index, Security security)
        {
            super(parent, style, index);
            setSecurity(security);
            addDisposeListener(this);
        }

        public SecurityTableItem(Table parent, int style, Security security)
        {
            super(parent, style);
            setSecurity(security);
            addDisposeListener(this);
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.TableItem#checkSubclass()
         */
        protected void checkSubclass()
        {
        }

        public Security getSecurity()
        {
            return security;
        }

        private void setSecurity(Security security)
        {
            if (this.security != null)
                this.security.deleteObserver(this);
            this.security = security;

            update();
            
            this.security.addObserver(this);
        }

        private void update()
        {
            setText(0, security.getCode());
            setText(1, security.getDescription());
//            setText(2, security.getCurrency() != null ? security.getCurrency().getCurrencyCode() : "");
        }

        /* (non-Javadoc)
         * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
         */
        public void update(Observable o, Object arg)
        {
            getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    if (!isDisposed())
                        update();
                }
            });
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
         */
        public void widgetDisposed(DisposeEvent e)
        {
            if (this.security != null)
                this.security.deleteObserver(this);
        }
    }
}
