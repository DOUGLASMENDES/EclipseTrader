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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.Event;
import net.sourceforge.eclipsetrader.core.db.PopupEvent;
import net.sourceforge.eclipsetrader.core.ui.dialogs.EventDetailsDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class EventsView extends ViewPart implements ICollectionObserver
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.views.events";
    Table table;
    Action removeSelectedAction;
    Action removeAllAction;
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
    ViewEventDetailsDialog dialog;
    private Comparator comparator = new Comparator() {
        public int compare(Object arg0, Object arg1)
        {
            return ((Event)arg1).getDate().compareTo(((Event)arg0).getDate());
        }
    };
    private DisposeListener dialogDisposeListener = new DisposeListener() {
        public void widgetDisposed(DisposeEvent e)
        {
            dialog = null;
        }
    };

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
     */
    public void init(IViewSite site) throws PartInitException
    {
        IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
        toolBarManager.add(new Separator("begin")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group5")); //$NON-NLS-1$
        toolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        toolBarManager.add(new Separator("end")); //$NON-NLS-1$

        removeSelectedAction = new Action() {
            public void run()
            {
                TableItem[] selection = table.getSelection();
                for (int i = 0; i < selection.length; i++)
                    CorePlugin.getRepository().delete((Event)selection[i].getData());
            }
        };
        removeSelectedAction.setToolTipText("Remove Selected Event");
        removeSelectedAction.setImageDescriptor(CorePlugin.getImageDescriptor("icons/elcl16/search_rem.gif"));
        removeSelectedAction.setDisabledImageDescriptor(CorePlugin.getImageDescriptor("icons/dlcl16/search_rem.gif"));
        removeSelectedAction.setEnabled(false);
        toolBarManager.appendToGroup("group5", removeSelectedAction);
        removeAllAction = new Action() {
            public void run()
            {
                CorePlugin.getRepository().allEvents().clear();
                updateView();
            }
        };
        removeAllAction.setToolTipText("Remove All Events");
        removeAllAction.setImageDescriptor(CorePlugin.getImageDescriptor("icons/elcl16/search_remall.gif"));
        removeAllAction.setDisabledImageDescriptor(CorePlugin.getImageDescriptor("icons/dlcl16/search_remall.gif"));
        removeAllAction.setEnabled(false);
        toolBarManager.appendToGroup("group5", removeAllAction);

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
        
        table = new Table(content, SWT.SINGLE|SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        table.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent e)
            {
                if (table.getSelectionIndex() != -1)
                {
                    if (dialog == null)
                    {
                        Event event = (Event) table.getItem(table.getSelectionIndex()).getData();
                        dialog = new ViewEventDetailsDialog(event, getViewSite().getShell());
                        dialog.open();
                    }
                }
            }
        });
        table.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                removeSelectedAction.setEnabled(table.getSelectionIndex() != -1);
            }
        });
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText("Date");
        column = new TableColumn(table, SWT.NONE);
        column.setText("Time");
        column = new TableColumn(table, SWT.NONE);
        column.setText("Security");
        column = new TableColumn(table, SWT.NONE);
        column.setText("Message");
        
        parent.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                updateView();
                CorePlugin.getRepository().allEvents().addCollectionObserver(EventsView.this);
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
        CorePlugin.getRepository().allEvents().removeCollectionObserver(this);
        super.dispose();
    }

    private void updateView()
    {
        table.removeAll();
        
        List list = new ArrayList(CorePlugin.getRepository().allEvents());
        Collections.sort(list, comparator);
        
        for (Iterator iter = list.iterator(); iter.hasNext(); )
        {
            Event event = (Event)iter.next();
            TableItem tableItem = new TableItem(table, SWT.NONE);
            tableItem.setText(0, dateFormatter.format(event.getDate()));
            tableItem.setText(1, timeFormatter.format(event.getDate()));
            tableItem.setText(2, event.getSecurity() != null ? event.getSecurity().getDescription() : "");
            tableItem.setText(3, event.getMessage());
            tableItem.setData(event);
        }
        
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumn(i).pack();
        
        removeSelectedAction.setEnabled(table.getSelectionIndex() != -1);
        removeAllAction.setEnabled(table.getItemCount() != 0);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemAdded(java.lang.Object)
     */
    public void itemAdded(Object o)
    {
        final Event event = (Event)o;
        
        table.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                TableItem tableItem = new TableItem(table, SWT.NONE, 0);
                tableItem.setText(0, dateFormatter.format(event.getDate()));
                tableItem.setText(1, timeFormatter.format(event.getDate()));
                tableItem.setText(2, event.getSecurity() != null ? event.getSecurity().getDescription() : "");
                tableItem.setText(3, event.getMessage());
                tableItem.setData(event);

                for (int i = 0; i < table.getColumnCount(); i++)
                    table.getColumn(i).pack();

                removeSelectedAction.setEnabled(table.getSelectionIndex() != -1);
                removeAllAction.setEnabled(table.getItemCount() != 0);
                
                if (event instanceof PopupEvent)
                {
                    if (dialog == null)
                    {
                        dialog = new ViewEventDetailsDialog(event, getViewSite().getShell());
                        dialog.open();
                    }
                }
                
                if (dialog != null)
                    dialog.updateButtonStatus();
            }
        });
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemRemoved(java.lang.Object)
     */
    public void itemRemoved(final Object o)
    {
        table.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                TableItem[] items = table.getItems();
                for (int i = 0; i < items.length; i++)
                {
                    if (items[i].getData().equals(o))
                        items[i].dispose();
                }
                
                for (int i = 0; i < table.getColumnCount(); i++)
                    table.getColumn(i).pack();

                removeSelectedAction.setEnabled(table.getSelectionIndex() != -1);
                removeAllAction.setEnabled(table.getItemCount() != 0);
                if (dialog != null)
                    dialog.updateButtonStatus();
            }
        });
    }
    
    public Table getTable()
    {
        return table;
    }

    /**
     * View-aware event details dialog.
     * <p>This object subclasses the standard event details dialog to hilight the currently
     * viewed event.</p>
     */
    class ViewEventDetailsDialog extends EventDetailsDialog
    {

        public ViewEventDetailsDialog(Event event, Shell parentShell)
        {
            super(event, parentShell);
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ui.dialogs.EventDetailsDialog#updateEvent()
         */
        protected void updateEvent()
        {
            TableItem[] items = table.getItems();
            for (int i = 0; i < items.length; i++)
            {
                if (getEvent().equals(items[i].getData()))
                    table.select(i);
            }
            super.updateEvent();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.window.Window#open()
         */
        public int open()
        {
            dialog.setBlockOnOpen(false);
            int result = super.open();
            dialog.getShell().addDisposeListener(dialogDisposeListener);
            return result;
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ui.dialogs.EventDetailsDialog#updateButtonStatus()
         */
        public void updateButtonStatus()
        {
            super.updateButtonStatus();
        }
    }
}
