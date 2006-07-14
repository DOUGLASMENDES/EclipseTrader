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
import net.sourceforge.eclipsetrader.core.transfers.SecurityTransfer;
import net.sourceforge.eclipsetrader.core.ui.NullSelection;
import net.sourceforge.eclipsetrader.core.ui.SecuritySelection;
import net.sourceforge.eclipsetrader.core.ui.SelectionProvider;
import net.sourceforge.eclipsetrader.core.ui.actions.PropertiesAction;
import net.sourceforge.eclipsetrader.core.ui.internal.DeleteAction;
import net.sourceforge.eclipsetrader.core.ui.internal.Messages;
import net.sourceforge.eclipsetrader.core.ui.preferences.SecurityPropertiesDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
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
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
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
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.views.securities"; //$NON-NLS-1$
    public static final String PREFS_SORT_COLUMN = "SECURITIES_SORT_COLUMN"; //$NON-NLS-1$
    public static final String PREFS_SORT_DIRECTION = "SECURITIES_SORT_DIRECTION"; //$NON-NLS-1$
    public static final String PREFS_COLUMNS_SIZE = "SECURITIES_COLUMNS_SIZE"; //$NON-NLS-1$
    public static final String TABLE_BACKGROUND = "TABLE_BACKGROUND"; //$NON-NLS-1$
    public static final String TABLE_FOREGROUND = "TABLE_FOREGROUND"; //$NON-NLS-1$
    public static final String TABLE_FONT = "TABLE_FONT"; //$NON-NLS-1$
    private Table table;
    private int sortColumn = 0;
    private int sortDirection = 1;
    private ITheme theme;
    private Action deleteAction = new DeleteAction(this);
    private Action propertiesAction;
    private Comparator comparator = new Comparator() {
        public int compare(Object arg0, Object arg1)
        {
            switch(sortColumn)
            {
                case 0:
                    if (sortDirection == 0)
                        return ((Security)arg0).getCode().compareTo(((Security)arg1).getCode());
                    else
                        return ((Security)arg1).getCode().compareTo(((Security)arg0).getCode());
                case 1:
                    if (sortDirection == 0)
                        return ((Security)arg0).getDescription().compareTo(((Security)arg1).getDescription());
                    else
                        return ((Security)arg1).getDescription().compareTo(((Security)arg0).getDescription());
                case 2:
                {
                    String s0 = ""; //$NON-NLS-1$
                    String s1 = ""; //$NON-NLS-1$
                    if (((Security)arg0).getCurrency() != null)
                        s0 = ((Security)arg0).getCurrency().getCurrencyCode();
                    if (((Security)arg1).getCurrency() != null)
                        s1 = ((Security)arg1).getCurrency().getCurrencyCode();
                    if (sortDirection == 0)
                        return s0.compareTo(s1);
                    else
                        return s1.compareTo(s0);
                }
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
    private ControlListener columnControlListener = new ControlAdapter() {
        public void controlResized(ControlEvent e)
        {
            StringBuffer sizes = new StringBuffer();
            for (int i = 0; i < table.getColumnCount(); i++)
                sizes.append(String.valueOf(table.getColumn(i).getWidth()) + ";"); //$NON-NLS-1$
            CorePlugin.getDefault().getPreferenceStore().setValue(PREFS_COLUMNS_SIZE, sizes.toString());
        }
    };
    private SelectionListener columnSelectionListener = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e)
        {
            int index = table.indexOf((TableColumn)e.widget);
            if (sortColumn == index)
                sortDirection = sortDirection == 0 ? 1 : 0;
            else
            {
                sortColumn = index;
                sortDirection = 0;
            }

            IPreferenceStore prefs = CorePlugin.getDefault().getPreferenceStore();
            prefs.setValue(PREFS_SORT_COLUMN, sortColumn);
            prefs.setValue(PREFS_SORT_DIRECTION, sortDirection);
            
            updateView();
        }
    };

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
     */
    public void init(IViewSite site) throws PartInitException
    {
        propertiesAction = new PropertiesAction() {
            public void run()
            {
                Security[] security = getSelection();
                if (security.length == 1)
                {
                    SecurityPropertiesDialog dlg = new SecurityPropertiesDialog(security[0], getSite().getShell());
                    dlg.open();
                }
            }
        };
        
        IMenuManager menuManager = site.getActionBars().getMenuManager();
        menuManager.add(new Separator("top")); //$NON-NLS-1$
        menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menuManager.add(new Separator("bottom")); //$NON-NLS-1$
        
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
        });
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(Messages.SecuritiesView_Code);
        column.addControlListener(columnControlListener);
        column.addSelectionListener(columnSelectionListener);
        column = new TableColumn(table, SWT.NONE);
        column.setText(Messages.SecuritiesView_Description);
        column.addControlListener(columnControlListener);
        column.addSelectionListener(columnSelectionListener);
        column = new TableColumn(table, SWT.NONE);
        column.setText(Messages.SecuritiesView_Currency);
        column.addControlListener(columnControlListener);
        column.addSelectionListener(columnSelectionListener);
        
        // Drag and drop support
        DragSource dragSource = new DragSource(table, DND.DROP_COPY|DND.DROP_MOVE);
        dragSource.setTransfer(new Transfer[] { SecurityTransfer.getInstance() });
        dragSource.addDragListener(new DragSourceListener() {
            public void dragStart(DragSourceEvent event)
            {
                if (table.getSelectionCount() == 0)
                    event.doit = false;
            }

            public void dragSetData(DragSourceEvent event)
            {
                TableItem selection[] = table.getSelection();
                Security[] securities = new Security[selection.length];
                for (int i = 0; i < selection.length; i++)
                {
                    SecurityTableItem item = (SecurityTableItem)selection[i];
                    securities[i] = item.getSecurity();
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
        IActionBars actionBars = getViewSite().getActionBars();
        actionBars.setGlobalActionHandler("delete", deleteAction); //$NON-NLS-1$
        actionBars.setGlobalActionHandler("properties", propertiesAction); //$NON-NLS-1$

        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager menuManager)
            {
                menuManager.add(new Separator("top")); //$NON-NLS-1$
                menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
                menuManager.add(new Separator());
                menuManager.add(deleteAction);
                menuManager.add(new Separator("bottom")); //$NON-NLS-1$
                menuManager.add(new Separator());
                menuManager.add(propertiesAction);
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
        
        String[] sizes = CorePlugin.getDefault().getPreferenceStore().getString(PREFS_COLUMNS_SIZE).split(";"); //$NON-NLS-1$
        for (int i = 0; i < table.getColumnCount(); i++)
        {
            if (i < sizes.length && sizes[i].length() != 0)
                table.getColumn(i).setWidth(Integer.parseInt(sizes[i]));
            else
            {
                table.getColumn(i).pack();
                if (table.getColumn(i).getWidth() == 0)
                    table.getColumn(i).setWidth(100);
            }
        }
        if ("gtk".equals(SWT.getPlatform())) //$NON-NLS-1$
            table.getColumn(table.getColumnCount() - 1).pack();
        
        if (sortColumn >= 0 && sortColumn < table.getColumnCount())
        {
            table.setSortColumn(table.getColumn(sortColumn));
            table.setSortDirection(sortDirection == 0 ? SWT.UP : SWT.DOWN);
        }
        else
        {
            table.setSortColumn(null);
            sortColumn = -1;
            sortDirection = 0;
        }
        
        updateSelection();
    }
    
    public Security[] getSelection()
    {
        TableItem selection[] = table.getSelection();
        Security[] securities = new Security[selection.length];
        for (int i = 0; i < selection.length; i++)
        {
            SecurityTableItem item = (SecurityTableItem)selection[i];
            securities[i] = item.getSecurity();
        }
        return securities;
    }
    
    private void updateSelection()
    {
        Security[] security = getSelection();
        if (security.length == 1)
            getSite().getSelectionProvider().setSelection(new SecuritySelection(security[0]));
        else
            getSite().getSelectionProvider().setSelection(new NullSelection());
        deleteAction.setEnabled(security.length != 0);
        propertiesAction.setEnabled(security.length == 1);
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
            setText(2, security.getCurrency() != null ? security.getCurrency().getCurrencyCode() : ""); //$NON-NLS-1$
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
