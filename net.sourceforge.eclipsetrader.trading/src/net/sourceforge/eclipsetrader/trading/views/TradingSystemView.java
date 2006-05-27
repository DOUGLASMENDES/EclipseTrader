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

package net.sourceforge.eclipsetrader.trading.views;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.trading.TradingSystem;
import net.sourceforge.eclipsetrader.core.db.trading.TradingSystemGroup;
import net.sourceforge.eclipsetrader.core.ui.NullSelection;
import net.sourceforge.eclipsetrader.core.ui.SelectionProvider;
import net.sourceforge.eclipsetrader.trading.TradingPlugin;
import net.sourceforge.eclipsetrader.trading.TradingSystemGroupSelection;
import net.sourceforge.eclipsetrader.trading.TradingSystemPlugin;
import net.sourceforge.eclipsetrader.trading.TradingSystemSelection;
import net.sourceforge.eclipsetrader.trading.dialogs.TestPeriodDialog;
import net.sourceforge.eclipsetrader.trading.internal.DeleteAction;
import net.sourceforge.eclipsetrader.trading.internal.PropertiesAction;
import net.sourceforge.eclipsetrader.trading.internal.TreeLayout;
import net.sourceforge.eclipsetrader.trading.wizards.systems.TradingSystemSettingsDialog;
import net.sourceforge.eclipsetrader.trading.wizards.systems.TradingSystemWizard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class TradingSystemView extends ViewPart implements ICollectionObserver
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.views.system";
    private Tree tree;
    private NumberFormat amountFormat = NumberFormat.getInstance();
    private NumberFormat priceFormat = NumberFormat.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
    private Color evenForeground = new Color(null, 0, 0, 0);
    private Color evenBackground = new Color(null, 255, 255, 255);
    private Color oddForeground = new Color(null, 0, 0, 0);
    private Color oddBackground = new Color(null, 224, 224, 255);
    private Action createGroupAction;
    private Action createSystemAction;
    private Action deleteAction;
    private Action propertiesAction;
    private Action runTestAction;
    private Map expandMap = new HashMap();
    private TradingSystemTreeItemTransfer _tradingSystemTreeItemTransfer = new TradingSystemTreeItemTransfer();

    public TradingSystemView()
    {
        amountFormat.setGroupingUsed(true);
        amountFormat.setMinimumIntegerDigits(1);
        amountFormat.setMinimumFractionDigits(2);
        amountFormat.setMaximumFractionDigits(2);

        priceFormat.setGroupingUsed(true);
        priceFormat.setMinimumIntegerDigits(1);
        priceFormat.setMinimumFractionDigits(4);
        priceFormat.setMaximumFractionDigits(4);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
     */
    public void init(IViewSite site) throws PartInitException
    {
        createGroupAction = new Action() {
            public void run()
            {
                InputDialog dlg = new InputDialog(getViewSite().getShell(), getText(), "Enter the name of the group to create:", null, null);
                if (dlg.open() == InputDialog.OK && dlg.getValue() != null)
                {
                    TradingSystemGroup group = new TradingSystemGroup();
                    group.setDescription(dlg.getValue());

                    ISelection selection = getViewSite().getSelectionProvider().getSelection();
                    if (selection instanceof TradingSystemGroupSelection)
                        group.setParent(((TradingSystemGroupSelection)selection).getGroup());
                    
                    CorePlugin.getRepository().save(group);
                }
            }
        };
        createGroupAction.setText("Create Group");

        createSystemAction = new Action() {
            public void run()
            {
                TradingSystemWizard wizard = new TradingSystemWizard();
                ISelection selection = getViewSite().getSelectionProvider().getSelection();
                if (selection instanceof TradingSystemGroupSelection)
                    wizard.setGroup(((TradingSystemGroupSelection)selection).getGroup());
                wizard.open();
            }
        };
        createSystemAction.setText("Create Trading System");
        createSystemAction.setImageDescriptor(TradingPlugin.getImageDescriptor("icons/eview16/server_database.png"));
        
        deleteAction = new DeleteAction() {
            public void run()
            {
                TreeItem[] selection = tree.getSelection();
                if (selection.length != 0)
                {
                    if (MessageDialog.openConfirm(getViewSite().getShell(), getPartName(), "Do you really want to delete the selected item(s) ?"))
                    {
                        for (int i = 0; i < selection.length; i++)
                        {
                            if (selection[i] instanceof TradingSystemItem)
                                CorePlugin.getRepository().delete(((TradingSystemItem)selection[i]).getSystem());
                            else if (selection[i] instanceof GroupTreeItem)
                                CorePlugin.getRepository().delete(((GroupTreeItem)selection[i]).getGroup());
                        }
                    }
                }
            }
        };

        propertiesAction = new PropertiesAction() {
            public void run()
            {
                TreeItem[] selection = tree.getSelection();
                if (selection.length == 1)
                {
                    if (selection[0] instanceof TradingSystemItem)
                    {
                        TradingSystem system = ((TradingSystemItem)selection[0]).getSystem();
                        TradingSystemSettingsDialog dlg = new TradingSystemSettingsDialog(system, getViewSite().getShell());
                        dlg.open();
                    }
                    else if (selection[0] instanceof GroupTreeItem)
                    {
                        TradingSystemGroup group = ((GroupTreeItem)selection[0]).getGroup();

                        InputDialog dlg = new InputDialog(getViewSite().getShell(), "Edit Group", "Enter the name of the group to edit:", group.getDescription(), null);
                        if (dlg.open() == InputDialog.OK && dlg.getValue() != null)
                        {
                            group.setDescription(dlg.getValue());
                            CorePlugin.getRepository().save(group);
                        }
                    }
                }
            }
        };
        
        runTestAction = new Action() {
            public void run()
            {
                TreeItem[] selection = tree.getSelection();
                if (selection.length == 1 && selection[0] instanceof TradingSystemItem)
                {
                    TradingSystem system = ((TradingSystemItem)selection[0]).getSystem();
                    
                    TestPeriodDialog dlg = new TestPeriodDialog(getViewSite().getShell(), system.getSecurity().getHistory());
                    if (dlg.open() == TestPeriodDialog.OK)
                    {
                        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        try {
                            TestRunnerView view = (TestRunnerView)page.showView(TestRunnerView.VIEW_ID);
                            view.runTradingSystem(system, dlg.getBeginDate(), dlg.getEndDate());
                        }
                        catch (PartInitException e1) {
                            CorePlugin.logException(e1);
                        }
                    }
                }
            }
        };
        runTestAction.setText("Run Test");

        IMenuManager menuManager = site.getActionBars().getMenuManager();
        menuManager.add(new Separator("top")); //$NON-NLS-1$
        menuManager.add(new Separator("internal.top")); //$NON-NLS-1$
        menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS)); //$NON-NLS-1$
        menuManager.add(new Separator("clipboard.top")); //$NON-NLS-1$
        menuManager.add(new Separator("clipboard.bottom")); //$NON-NLS-1$
        menuManager.add(new Separator("bottom")); //$NON-NLS-1$
        menuManager.add(new Separator("internal.bottom")); //$NON-NLS-1$

        menuManager.appendToGroup("internal.top", createGroupAction);
        menuManager.appendToGroup("internal.top", createSystemAction);
        menuManager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, runTestAction);
        menuManager.appendToGroup("clipboard.bottom", deleteAction);
        menuManager.appendToGroup("internal.bottom", propertiesAction);
        
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
        
        tree = new Tree(content, SWT.FULL_SELECTION|SWT.MULTI);
        tree.setHeaderVisible(true);
        tree.setLinesVisible(false);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        tree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                updateSelection();
            }
        });
        tree.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e)
            {
                if (tree.getItem(new Point(e.x, e.y)) == null)
                {
                    tree.deselectAll();
                    updateSelection();
                }
            }

            public void mouseDoubleClick(MouseEvent e)
            {
            }
        });
        tree.addTreeListener(new TreeListener() {
            public void treeCollapsed(TreeEvent e)
            {
                if (e.item instanceof GroupTreeItem)
                {
                    TradingSystemGroup group = ((GroupTreeItem)e.item).getGroup();
                    expandMap.remove(String.valueOf(group.getId()));
                }
                updateItemColors();
            }

            public void treeExpanded(TreeEvent e)
            {
                if (e.item instanceof GroupTreeItem)
                {
                    TradingSystemGroup group = ((GroupTreeItem)e.item).getGroup();
                    expandMap.put(String.valueOf(group.getId()), new Boolean(true));
                }
                updateItemColors();
            }
        });
        TreeLayout layout = new TreeLayout();
        tree.setLayout(layout);

        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        layout.addColumnData(new ColumnPixelData(200, true, false));
        column = new TreeColumn(tree, SWT.LEFT);
        column.setText("Account");
        layout.addColumnData(new ColumnPixelData(100, true, false));
        column = new TreeColumn(tree, SWT.RIGHT);
        column.setText("Position");
        layout.addColumnData(new ColumnPixelData(60, true, false));
        column = new TreeColumn(tree, SWT.LEFT);
        column.setText("Signal");
        layout.addColumnData(new ColumnPixelData(60, true, false));
        column = new TreeColumn(tree, SWT.RIGHT);
        column.setText("Date");
        layout.addColumnData(new ColumnPixelData(60, true, true));

        getSite().setSelectionProvider(new SelectionProvider());

        DragSource dragSource = new DragSource(tree, DND.DROP_COPY|DND.DROP_MOVE);
        dragSource.setTransfer(new Transfer[] { _tradingSystemTreeItemTransfer });
        dragSource.addDragListener(new DragSourceListener() {
            private TreeItem[] selection;

            public void dragStart(DragSourceEvent event)
            {
                selection = tree.getSelection();
                if (selection.length == 0)
                    event.doit = false;
            }

            public void dragSetData(DragSourceEvent event)
            {
                int count = 0;

                for (int i = 0; i < selection.length; i++)
                {
                    if (selection[i] instanceof TradingSystemItem)
                        count++;
                }
                
                TradingSystemItem[] items = new TradingSystemItem[count];
                count = 0;
                for (int i = 0; i < selection.length; i++)
                {
                    if (selection[i] instanceof TradingSystemItem)
                        items[count++] = (TradingSystemItem)selection[i]; 
                }
                event.data = items;
            }

            public void dragFinished(DragSourceEvent event)
            {
            }
        });
        DropTarget target = new DropTarget(parent, DND.DROP_COPY|DND.DROP_MOVE);
        target.setTransfer(new Transfer[] { _tradingSystemTreeItemTransfer });
        target.addDropListener(new DropTargetListener() {
            public void dragEnter(DropTargetEvent event)
            {
                if (event.detail == DND.DROP_DEFAULT)
                    event.detail = DND.DROP_COPY;
            
                event.currentDataType = null;
                
                TransferData[] data = event.dataTypes;
                for (int i = 0; i < data.length; i++)
                {
                    if (_tradingSystemTreeItemTransfer.isSupportedType(data[i]))
                    {
                        event.currentDataType = data[i];
                        break;
                    }
                }
            }

            public void dragOver(DropTargetEvent event)
            {
                event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;

                TreeItem treeItem = tree.getItem(tree.toControl(event.x, event.y));
                if (treeItem != null)
                {
                    TreeItem[] selection = { treeItem };
                    tree.setSelection(selection);
                }
            }

            public void dragOperationChanged(DropTargetEvent event)
            {
            }

            public void dragLeave(DropTargetEvent event)
            {
            }

            public void dropAccept(DropTargetEvent event)
            {
            }

            public void drop(DropTargetEvent event)
            {
                if (_tradingSystemTreeItemTransfer.isSupportedType(event.currentDataType))
                {
                    Integer[] items = (Integer[]) event.data;
                    if (items != null)
                    {
                        TradingSystemGroup group = null;
                        TreeItem treeItem = tree.getItem(tree.toControl(event.x, event.y));
                        if (treeItem != null)
                        {
                            if (treeItem instanceof GroupTreeItem)
                                group = ((GroupTreeItem)treeItem).getGroup();
                            else if (treeItem instanceof TradingSystemItem)
                                group = ((TradingSystemItem)treeItem).getSystem().getGroup();
                        }
                        for (int i = 0; i < items.length; i++)
                        {
                            TradingSystem system = (TradingSystem) CorePlugin.getRepository().load(TradingSystem.class, items[i]);
                            CorePlugin.getRepository().delete(system);
                            system.setGroup(group);
                            CorePlugin.getRepository().save(system);
                        }
                    }
                }
            }
        });

        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager menuManager)
            {
                menuManager.add(new Separator("top")); //$NON-NLS-1$
                menuManager.add(new Separator("internal.top")); //$NON-NLS-1$
                menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS)); //$NON-NLS-1$
                menuManager.add(new Separator("clipboard.top")); //$NON-NLS-1$
                menuManager.add(new Separator("clipboard.bottom")); //$NON-NLS-1$
                menuManager.add(new Separator("bottom")); //$NON-NLS-1$
                menuManager.add(new Separator("internal.bottom")); //$NON-NLS-1$

                menuManager.appendToGroup("internal.top", createGroupAction);
                menuManager.appendToGroup("internal.top", createSystemAction);
                menuManager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, runTestAction);
                menuManager.appendToGroup("clipboard.bottom", deleteAction);
                menuManager.appendToGroup("internal.bottom", propertiesAction);
            }
        });
        tree.setMenu(menuMgr.createContextMenu(tree));
        getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());

        IActionBars actionBars = getViewSite().getActionBars();
        actionBars.setGlobalActionHandler("delete", deleteAction);
        actionBars.setGlobalActionHandler("properties", propertiesAction);
        
        IPreferenceStore preferenceStore = TradingPlugin.getDefault().getPreferenceStore();
        String[] values = preferenceStore.getString("TS_VIEW_EXPANDED_ITEMS").split(";");
        for (int i = 0; i < values.length; i++)
            expandMap.put(values[i], new Boolean(true));

        parent.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                updateView();
                updateSelection();
                CorePlugin.getRepository().getTradingSystemGroups().addCollectionObserver(TradingSystemView.this);
                CorePlugin.getRepository().getTradingSystems().addCollectionObserver(TradingSystemView.this);
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus()
    {
        tree.getParent().setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    public void dispose()
    {
        StringBuffer sb = new StringBuffer();
        for (Iterator iter = expandMap.keySet().iterator(); iter.hasNext(); )
        {
            if (sb.length() != 0)
                sb.append(";");
            sb.append((String)iter.next());
        }
        TradingPlugin.getDefault().getPreferenceStore().setValue("TS_VIEW_EXPANDED_ITEMS", sb.toString());

        CorePlugin.getRepository().getTradingSystemGroups().removeCollectionObserver(this);
        CorePlugin.getRepository().getTradingSystems().removeCollectionObserver(this);
        
        super.dispose();
    }

    private void updateView()
    {
        tree.setRedraw(false);
        tree.removeAll();
        
        for (Iterator iter = CorePlugin.getRepository().getTradingSystemGroups().iterator(); iter.hasNext(); )
        {
            TradingSystemGroup group = (TradingSystemGroup) iter.next();
            if (group.getParent() == null)
                new GroupTreeItem(group, tree, SWT.NONE);
        }
        
        for (Iterator iter = CorePlugin.getRepository().getTradingSystems().iterator(); iter.hasNext(); )
        {
            TradingSystem system = (TradingSystem) iter.next();
            if (system.getGroup() == null)
                new TradingSystemItem(system, tree, SWT.NONE);
        }
        
        tree.setRedraw(true);

        int index = 0;
        TreeItem[] items = tree.getItems();
        for (int i = 0; i < items.length; i++)
            index = updateItemColors(items[i], index);
    }
    
    private void updateSelection()
    {
        TreeItem[] selection = tree.getSelection();
        if (selection != null && selection.length == 1)
        {
            if (selection[0] instanceof TradingSystemItem)
                getSite().getSelectionProvider().setSelection(new TradingSystemSelection(((TradingSystemItem) selection[0]).getSystem()));
            else if (selection[0] instanceof GroupTreeItem)
                getSite().getSelectionProvider().setSelection(new TradingSystemGroupSelection(((GroupTreeItem) selection[0]).getGroup()));
            else
                getSite().getSelectionProvider().setSelection(new NullSelection());
        }
        else
            getSite().getSelectionProvider().setSelection(new NullSelection());
        
        deleteAction.setEnabled(selection.length != 0);
        runTestAction.setEnabled(selection.length == 1);
        propertiesAction.setEnabled(selection.length == 1);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemAdded(java.lang.Object)
     */
    public void itemAdded(Object o)
    {
        if (o instanceof TradingSystem)
        {
            TradingSystem system = (TradingSystem) o;
            if (system.getGroup() == null)
                new TradingSystemItem(system, tree, SWT.NONE);
        }
        else if (o instanceof TradingSystemGroup)
        {
            TradingSystemGroup group = (TradingSystemGroup) o;
            if (group.getParent() == null)
                new GroupTreeItem(group, tree, SWT.NONE);
        }
        updateItemColors();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemRemoved(java.lang.Object)
     */
    public void itemRemoved(Object o)
    {
        if (o instanceof TradingSystem)
        {
            TradingSystem object = (TradingSystem) o;
            TreeItem[] items = tree.getItems();
            for (int i = 0; i < items.length; i++)
            {
                if (!(items[i] instanceof TradingSystemItem))
                    continue;
                if (((TradingSystemItem) items[i]).getSystem().equals(object))
                    items[i].dispose();
            }
        }
        else if (o instanceof TradingSystemGroup)
        {
            TradingSystemGroup object = (TradingSystemGroup) o;
            TreeItem[] items = tree.getItems();
            for (int i = 0; i < items.length; i++)
            {
                if (!(items[i] instanceof GroupTreeItem))
                    continue;
                if (((GroupTreeItem) items[i]).getGroup().equals(object))
                    items[i].dispose();
            }
        }
        updateItemColors();
    }
    
    private void updateItemColors()
    {
        tree.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                if (!tree.isDisposed())
                {
                    int index = 0;
                    TreeItem[] items = tree.getItems();
                    for (int i = 0; i < items.length; i++)
                        index = updateItemColors(items[i], index);
                }
            }
        });
    }
    
    private int updateItemColors(TreeItem treeItem, int index)
    {
        treeItem.setBackground((index & 1) == 0 ? evenBackground : oddBackground);
        treeItem.setForeground((index & 1) == 0 ? evenForeground : oddForeground);
        index++;

        if (treeItem instanceof GroupTreeItem)
        {
            if (expandMap.get(String.valueOf(((GroupTreeItem)treeItem).getGroup().getId())) != null)
                treeItem.setExpanded(true);
        }
        
        if (treeItem.getExpanded())
        {
            TreeItem[] items = treeItem.getItems();
            for (int i = 0; i < items.length; i++)
                index = updateItemColors(items[i], index);
        }
        
        return index;
    }

    public class GroupTreeItem extends TreeItem implements ICollectionObserver, Observer
    {
        TradingSystemGroup group;

        public GroupTreeItem(TradingSystemGroup group, Tree parent, int style, int index)
        {
            super(parent, style, index);
            init(group);
        }

        public GroupTreeItem(TradingSystemGroup group, Tree parent, int style)
        {
            super(parent, style);
            init(group);
        }

        public GroupTreeItem(TradingSystemGroup group, TreeItem parentItem, int style, int index)
        {
            super(parentItem, style, index);
            init(group);
        }

        public GroupTreeItem(TradingSystemGroup group, TreeItem parentItem, int style)
        {
            super(parentItem, style);
            init(group);
        }
        
        protected void init(TradingSystemGroup group)
        {
            this.group = group;
            this.group.addObserver(this);
            this.group.getGroups().addCollectionObserver(this);
            this.group.getTradingSystems().addCollectionObserver(this);
            
            addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    GroupTreeItem.this.group.deleteObserver(GroupTreeItem.this);
                    GroupTreeItem.this.group.getGroups().removeCollectionObserver(GroupTreeItem.this);
                    GroupTreeItem.this.group.getTradingSystems().removeCollectionObserver(GroupTreeItem.this);
                }
            });
            
            FontData fd = getFont().getFontData()[0];
            Font font = new Font(null, fd.getName(), fd.getHeight(), SWT.BOLD);
            setFont(font);

            setText(0, group.getDescription());

            for (Iterator iter = this.group.getGroups().iterator(); iter.hasNext(); )
                new GroupTreeItem((TradingSystemGroup) iter.next(), this, SWT.NONE);

            for (Iterator iter = this.group.getTradingSystems().iterator(); iter.hasNext(); )
                new TradingSystemItem((TradingSystem) iter.next(), this, SWT.NONE);
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.TreeItem#checkSubclass()
         */
        protected void checkSubclass()
        {
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemAdded(java.lang.Object)
         */
        public void itemAdded(Object o)
        {
            if (o instanceof TradingSystem)
                new TradingSystemItem((TradingSystem) o, this, SWT.NONE);
            else if (o instanceof TradingSystemGroup)
                new GroupTreeItem((TradingSystemGroup) o, this, SWT.NONE);
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemRemoved(java.lang.Object)
         */
        public void itemRemoved(Object o)
        {
            if (o instanceof TradingSystem)
            {
                TradingSystem object = (TradingSystem) o;
                TreeItem[] items = getItems();
                for (int i = 0; i < items.length; i++)
                {
                    if (!(items[i] instanceof TradingSystemItem))
                        continue;
                    if (((TradingSystemItem) items[i]).getSystem().equals(object))
                        items[i].dispose();
                }
            }
            else if (o instanceof TradingSystemGroup)
            {
                TradingSystemGroup object = (TradingSystemGroup) o;
                TreeItem[] items = getItems();
                for (int i = 0; i < items.length; i++)
                {
                    if (!(items[i] instanceof GroupTreeItem))
                        continue;
                    if (((GroupTreeItem) items[i]).getGroup().equals(object))
                        items[i].dispose();
                }
            }
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
                        setText(0, group.getDescription());
                }
            });
        }

        public TradingSystemGroup getGroup()
        {
            return group;
        }
    }
    
    public class TradingSystemItem extends TreeItem implements Observer
    {
        TradingSystem system;
        TradingSystemPlugin plugin;

        public TradingSystemItem(TradingSystem system, Tree parent, int style, int index)
        {
            super(parent, style, index);
            init(system);
        }

        public TradingSystemItem(TradingSystem system, Tree parent, int style)
        {
            super(parent, style);
            init(system);
        }

        public TradingSystemItem(TradingSystem system, TreeItem parentItem, int style, int index)
        {
            super(parentItem, style, index);
            init(system);
        }

        public TradingSystemItem(TradingSystem system, TreeItem parentItem, int style)
        {
            super(parentItem, style);
            init(system);
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.TreeItem#checkSubclass()
         */
        protected void checkSubclass()
        {
        }
        
        private void init(TradingSystem system)
        {
            this.system = system;
            this.system.addObserver(this);

            plugin = TradingPlugin.createTradingSystemPlugin(system);
            plugin.addObserver(this);
            
            addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    TradingSystemItem.this.system.deleteObserver(TradingSystemItem.this);
                    TradingSystemItem.this.plugin.deleteObserver(TradingSystemItem.this);
                }
            });

            setText(0, system.getSecurity().getDescription());
            setText(1, system.getAccount().getDescription());
            setText(2, String.valueOf(system.getAccount().getPosition(system.getSecurity())));
            if (system.getSignal() == TradingSystem.SIGNAL_BUY)
                setText(3, "Buy");
            else if (system.getSignal() == TradingSystem.SIGNAL_SELL)
                setText(3, "Sell");
            else if (system.getSignal() == TradingSystem.SIGNAL_HOLD)
                setText(3, "Hold");
            else
                setText(3, "");
            setText(4, system.getDate() != null ? dateFormat.format(system.getDate()) : "");
        }

        /* (non-Javadoc)
         * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
         */
        public void update(Observable o, Object arg)
        {
            if (o == plugin)
            {
                system.setDate(Calendar.getInstance().getTime());
                system.setSignal(plugin.getSignal());
            }

            getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    if (!isDisposed())
                    {
                        setText(0, system.getSecurity().getDescription());
                        setText(1, system.getAccount().getDescription());
                        setText(2, String.valueOf(system.getAccount().getPosition(system.getSecurity())));
                        if (system.getSignal() == TradingSystem.SIGNAL_BUY)
                            setText(3, "Buy");
                        else if (system.getSignal() == TradingSystem.SIGNAL_SELL)
                            setText(3, "Sell");
                        else if (system.getSignal() == TradingSystem.SIGNAL_HOLD)
                            setText(3, "Hold");
                        else
                            setText(3, "");
                        setText(4, system.getDate() != null ? dateFormat.format(system.getDate()) : "");
                    }
                }
            });
        }

        public TradingSystem getSystem()
        {
            return system;
        }
    }

    public class TradingSystemTreeItemTransfer extends ByteArrayTransfer
    {
        private final String TYPENAME = TradingSystemItem.class.getName();
        private final int TYPEID = registerType(TYPENAME);

        private TradingSystemTreeItemTransfer()
        {
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
         */
        protected String[] getTypeNames()
        {
            return new String[] { TYPENAME };
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
         */
        protected int[] getTypeIds()
        {
            return new int[] { TYPEID };
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(java.lang.Object, org.eclipse.swt.dnd.TransferData)
         */
        protected void javaToNative(Object object, TransferData transferData)
        {
            if (!checkMyType(object) || !isSupportedType(transferData))
                DND.error(DND.ERROR_INVALID_DATA);

            try
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream writeOut = new ObjectOutputStream(out);
                
                if (object instanceof TradingSystemItem)
                {
                    writeOut.writeInt(1);
                    writeOut.writeObject(((TradingSystemItem)object).getSystem().getId());
                }
                else if (object instanceof TradingSystemItem[])
                {
                    TradingSystemItem[] array = (TradingSystemItem[]) object;
                    writeOut.writeInt(array.length);
                    for (int i = 0; i < array.length; i++)
                        writeOut.writeObject(array[i].getSystem().getId());
                }
                
                byte[] buffer = out.toByteArray();
                writeOut.close();
                super.javaToNative(buffer, transferData);
            }
            catch (IOException e) {
                CorePlugin.logException(e);
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.dnd.ByteArrayTransfer#nativeToJava(org.eclipse.swt.dnd.TransferData)
         */
        protected Object nativeToJava(TransferData transferData)
        {
            if (isSupportedType(transferData))
            {
                byte[] buffer = (byte[]) super.nativeToJava(transferData);
                if (buffer == null)
                    return null;

                try
                {
                    ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                    ObjectInputStream readIn = new ObjectInputStream(in);

                    int length = readIn.readInt();
                    Integer[] security = new Integer[length];
                    for (int i = 0; i < length; i++)
                        security[i] = (Integer)readIn.readObject();
                    
                    readIn.close();
                    return security;
                }
                catch (Exception e) {
                    CorePlugin.logException(e);
                }
            }

            return null;
        }

        private boolean checkMyType(Object object)
        {
            return (object instanceof TradingSystemItem || object instanceof TradingSystemItem[]);
        }
    }
}
