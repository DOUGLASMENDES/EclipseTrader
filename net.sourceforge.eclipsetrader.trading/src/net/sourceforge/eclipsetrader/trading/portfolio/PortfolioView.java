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

package net.sourceforge.eclipsetrader.trading.portfolio;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.PortfolioPosition;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.transfers.SecurityTransfer;
import net.sourceforge.eclipsetrader.core.ui.AccountGroupSelection;
import net.sourceforge.eclipsetrader.core.ui.AccountSelection;
import net.sourceforge.eclipsetrader.core.ui.NullSelection;
import net.sourceforge.eclipsetrader.core.ui.PortfolioPositionSelection;
import net.sourceforge.eclipsetrader.core.ui.SelectionProvider;
import net.sourceforge.eclipsetrader.trading.TradingPlugin;
import net.sourceforge.eclipsetrader.trading.dialogs.TransactionDialog;
import net.sourceforge.eclipsetrader.trading.internal.ClosePositionAction;
import net.sourceforge.eclipsetrader.trading.internal.TransactionAction;

import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class PortfolioView extends ViewPart
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.views.portfolio";
    public static final String PREFS_COLUMN_SIZE = "COLUMN_SIZE";
    public static final String PREFS_EXPANDED_GROUPS = "EXPANDED_GROUPS";
    public static final String PREFS_EXPANDED_ACCOUNTS = "EXPANDED_ACCOUNTS";
    PreferenceStore preferences;
    Set expandedGroups = new HashSet();
    Set expandedAccounts = new HashSet();
    Tree tree;
    TreeViewer viewer;
    Color negativeForeground = new Color(null, 240, 0, 0);
    Color positiveForeground = new Color(null, 0, 192, 0);
    Font boldFont;
    Action closePositionAction = new ClosePositionAction(this);
    Action newTransactionAction = new TransactionAction(this);
    SelectionProvider selectionProvider = new SelectionProvider();
    ControlListener columnControlListener = new ControlAdapter() {
        public void controlResized(ControlEvent e)
        {
            StringBuffer sizes = new StringBuffer();
            for (int i = 0; i < tree.getColumnCount(); i++)
                sizes.append(String.valueOf(tree.getColumn(i).getWidth()) + ";");
            preferences.setValue(PREFS_COLUMN_SIZE, sizes.toString());
        }
    };
    Job updateJob = new Job("Portfolio Build") {
        protected IStatus run(IProgressMonitor monitor)
        {
            final PortfolioInput input = new PortfolioInput();
            try {
                tree.getDisplay().asyncExec(new Runnable() {
                    public void run()
                    {
                        if (!tree.isDisposed())
                        {
                            viewer.setInput(input);
                            restoreExpandedStatus();
                            updateSelection();
                        }
                    }
                });
            } catch(SWTException e) {
                if (e.code != SWT.ERROR_WIDGET_DISPOSED)
                    throw e;
            }
            return Status.OK_STATUS;
        }
    };
    DropTargetAdapter dropTargetListener = new DropTargetAdapter() {
        public void dragEnter(DropTargetEvent event)
        {
            event.detail = DND.DROP_COPY;
        }

        public void drop(DropTargetEvent event)
        {
            event.detail = DND.DROP_COPY;
            
            Account account = null;
            if (event.item instanceof TreeItem)
            {
                Object node = event.item.getData();
                if (node instanceof PositionTreeNode)
                    account = ((PositionTreeNode)node).getParent().value;
                if (node instanceof AccountTreeNode)
                    account = ((AccountTreeNode)node).value;
            }

            if (account != null && SecurityTransfer.getInstance().isSupportedType(event.currentDataType))
            {
                Security[] securities = (Security[]) event.data;
                for (int i = 0; i < securities.length; i++)
                {
                    TransactionDialog dlg = new TransactionDialog(account, getViewSite().getShell());
                    dlg.open(securities[i]);
                }
            }
        }
    };

    public PortfolioView()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
     */
    public void init(IViewSite site) throws PartInitException
    {
        preferences = new PreferenceStore(TradingPlugin.getDefault().getStateLocation().append("portfolio.prefs").toOSString());
        try {
            preferences.load();
        } catch(Exception e) {
        }

        IMenuManager menuManager = site.getActionBars().getMenuManager();
        menuManager.add(new Separator("top")); //$NON-NLS-1$
        menuManager.add(closePositionAction);
        menuManager.add(newTransactionAction);
        menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menuManager.add(new Separator("bottom")); //$NON-NLS-1$
        
        site.getActionBars().updateActionBars();
        site.setSelectionProvider(selectionProvider);

        super.init(site);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        tree = new Tree(parent, SWT.FULL_SELECTION|SWT.MULTI);
        tree.setHeaderVisible(true);
        tree.setLinesVisible(false);
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        column.addControlListener(columnControlListener);
        column = new TreeColumn(tree, SWT.RIGHT);
        column.setText("Position");
        column.addControlListener(columnControlListener);
        column = new TreeColumn(tree, SWT.RIGHT);
        column.setText("Price");
        column.addControlListener(columnControlListener);
        column = new TreeColumn(tree, SWT.RIGHT);
        column.setText("Last");
        column.addControlListener(columnControlListener);
        column = new TreeColumn(tree, SWT.RIGHT);
        column.setText("Value");
        column.addControlListener(columnControlListener);
        column = new TreeColumn(tree, SWT.RIGHT);
        column.setText("Gain / Loss");
        column.addControlListener(columnControlListener);

        FontData fd = tree.getFont().getFontData()[0];
        boldFont = new Font(null, fd.getName(), fd.getHeight(), SWT.BOLD);
        
        viewer = new TreeViewer(tree);
        viewer.setUseHashlookup(true);
        viewer.setContentProvider(new PortfolioContentProvider());
        PortfolioLabelProvider labelProvider = new PortfolioLabelProvider();
        labelProvider.boldFont = boldFont;
        labelProvider.negativeForeground = negativeForeground;
        labelProvider.positiveForeground = positiveForeground;
        viewer.setLabelProvider(labelProvider);
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                updateSelection();
            }
        });
        viewer.addTreeListener(new ITreeViewerListener() {

            public void treeCollapsed(TreeExpansionEvent event)
            {
                Object element = event.getElement();
                if (element instanceof AccountGroupTreeNode)
                    expandedGroups.remove(((AccountGroupTreeNode)element).value.getId());
                if (element instanceof AccountTreeNode)
                    expandedAccounts.remove(((AccountTreeNode)element).value.getId());
                saveExpandedStatus();
            }

            public void treeExpanded(TreeExpansionEvent event)
            {
                Object element = event.getElement();
                if (element instanceof AccountGroupTreeNode)
                    expandedGroups.add(((AccountGroupTreeNode)element).value.getId());
                if (element instanceof AccountTreeNode)
                    expandedAccounts.add(((AccountTreeNode)element).value.getId());
                saveExpandedStatus();
            }
        });
        viewer.addDropSupport(DND.DROP_COPY, new Transfer[] { SecurityTransfer.getInstance() }, dropTargetListener);

        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager menuManager)
            {
                menuManager.add(new Separator("top")); //$NON-NLS-1$
                menuManager.add(closePositionAction);
                menuManager.add(newTransactionAction);
                menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
                menuManager.add(new Separator("bottom")); //$NON-NLS-1$
            }
        });
        tree.setMenu(menuMgr.createContextMenu(tree));
        getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());

        String[] sizes = preferences.getString(PREFS_COLUMN_SIZE).split(";");
        for (int i = 0; i < tree.getColumnCount(); i++)
        {
            if (i < sizes.length && sizes[i].length() != 0)
                tree.getColumn(i).setWidth(Integer.parseInt(sizes[i]));
            else
                tree.getColumn(i).setWidth(i == 0 ? 100 : 75);
        }
        if ("gtk".equals(SWT.getPlatform()))
            tree.getColumn(tree.getColumnCount() - 1).pack();
        
        updateJob.setUser(false);
        updateJob.schedule();

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus()
    {
        tree.setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    public void dispose()
    {
        boldFont.dispose();

        try {
            preferences.save();
        } catch(Exception e) {
            LogFactory.getLog(getClass()).warn(e);
        }
        
        super.dispose();
    }
    
    void updateSelection()
    {
        IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
        if (selection != null && !selection.isEmpty())
        {
            Object obj = selection.getFirstElement();
            if (obj instanceof AccountTreeNode)
                selectionProvider.setSelection(new AccountSelection(((AccountTreeNode)obj).value));
            else if (obj instanceof AccountGroupTreeNode)
                selectionProvider.setSelection(new AccountGroupSelection(((AccountGroupTreeNode)obj).value));
            else if (obj instanceof PositionTreeNode)
            {
                Account account = ((PositionTreeNode)obj).parent.value;
                PortfolioPosition position = ((PositionTreeNode)obj).value;
                selectionProvider.setSelection(new PortfolioPositionSelection(account, position));
            }
            else
                selectionProvider.setSelection(new NullSelection());
            
            closePositionAction.setEnabled(obj instanceof PositionTreeNode);
            newTransactionAction.setEnabled((obj instanceof AccountTreeNode) || (obj instanceof PositionTreeNode));
        }
        else
        {
            selectionProvider.setSelection(new NullSelection());
            closePositionAction.setEnabled(false);
            newTransactionAction.setEnabled(false);
        }
    }
    
    void saveExpandedStatus()
    {
        StringBuffer sb = new StringBuffer();
        for (Iterator iter = expandedGroups.iterator(); iter.hasNext(); )
        {
            if (sb.length() != 0)
                sb.append(";");
            sb.append(String.valueOf(iter.next()));
        }
        preferences.setValue(PREFS_EXPANDED_GROUPS, sb.toString());

        sb = new StringBuffer();
        for (Iterator iter = expandedAccounts.iterator(); iter.hasNext(); )
        {
            if (sb.length() != 0)
                sb.append(";");
            sb.append(String.valueOf(iter.next()));
        }
        preferences.setValue(PREFS_EXPANDED_ACCOUNTS, sb.toString());
    }
    
    void restoreExpandedStatus()
    {
        PortfolioInput input = (PortfolioInput)viewer.getInput();

        String[] values = preferences.getString(PREFS_EXPANDED_GROUPS).split(";");
        for (int i = 0; i < values.length; i++)
        {
            try {
                Integer id = new Integer(values[i]);
                Object element = input.getAccountGroupNode(id); 
                if (element != null)
                {
                    expandedGroups.add(id);
                    viewer.setExpandedState(element, true);
                }
            } catch(Exception e) {}
        }
        
        values = preferences.getString(PREFS_EXPANDED_ACCOUNTS).split(";");
        for (int i = 0; i < values.length; i++)
        {
            try {
                Integer id = new Integer(values[i]);
                Object element = input.getAccountNode(id); 
                if (element != null)
                {
                    expandedAccounts.add(id);
                    viewer.setExpandedState(element, true);
                }
            } catch(Exception e) {}
        }
    }
}
