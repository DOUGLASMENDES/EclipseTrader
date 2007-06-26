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

package net.sourceforge.eclipsetrader.internal.ui.views.explorer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.SecurityGroup;
import net.sourceforge.eclipsetrader.core.ui.NullSelection;
import net.sourceforge.eclipsetrader.core.ui.SecuritySelection;
import net.sourceforge.eclipsetrader.core.ui.SelectionProvider;
import net.sourceforge.eclipsetrader.internal.ui.Activator;
import net.sourceforge.eclipsetrader.internal.ui.SecuritiesLabelProvider;
import net.sourceforge.eclipsetrader.internal.ui.SecuritiesTreeContentProvider;
import net.sourceforge.eclipsetrader.internal.ui.SecuritiesTreeViewerComparator;
import net.sourceforge.eclipsetrader.internal.ui.dialogs.CreateSecurityGroupDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

public class SecurityExplorer extends ViewPart {
	public static final String TABLE_BACKGROUND = "TABLE_BACKGROUND"; //$NON-NLS-1$

	public static final String TABLE_FOREGROUND = "TABLE_FOREGROUND"; //$NON-NLS-1$

	private TreeViewer viewer;
	
	private List input;

	private IPropertyChangeListener themeChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			ITheme theme = (ITheme) event.getSource();
			if (event.getProperty().equals(TABLE_BACKGROUND))
				viewer.getTree().setBackground(theme.getColorRegistry().get(event.getProperty()));
			else if (event.getProperty().equals(TABLE_FOREGROUND))
				viewer.getTree().setForeground(theme.getColorRegistry().get(event.getProperty()));
		}
	};

	private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			if (selection.size() == 1 && selection.getFirstElement() instanceof Security)
				getSite().getSelectionProvider().setSelection(new SecuritySelection((Security) selection.getFirstElement()));
			else
				getSite().getSelectionProvider().setSelection(new NullSelection());
		}
	};
	
	private Action createFolderAction = new Action("Create Group...") {
        @Override
        public void run() {
        	CreateSecurityGroupDialog dlg = new CreateSecurityGroupDialog(getViewSite().getShell());
        	if (dlg.open() == CreateSecurityGroupDialog.OK) {
        		buildInput();
				viewer.setInput(input);
        	}
        }
	};
	
	private Action expandAllAction = new Action("Expand all", Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/elcl16/expandall.gif")) {
        @Override
        public void run() {
        	viewer.expandAll();
        }
	};
	
	private Action collapseAllAction = new Action("Collapse all", Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/elcl16/collapseall.gif")) {
        @Override
        public void run() {
        	viewer.collapseAll();
        }
	};

	public SecurityExplorer() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
     */
    @Override
    public void init(IViewSite site) throws PartInitException {
        IMenuManager menuManager = site.getActionBars().getMenuManager();
        menuManager.add(new Separator("top")); //$NON-NLS-1$
        menuManager.add(createFolderAction);
        menuManager.add(new Separator());
        menuManager.add(expandAllAction);
        menuManager.add(collapseAllAction);
        menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menuManager.add(new Separator("bottom")); //$NON-NLS-1$
        
        IToolBarManager toolbarManager = site.getActionBars().getToolBarManager();
        menuManager.add(new Separator("beginning")); //$NON-NLS-1$
        toolbarManager.add(expandAllAction);
        toolbarManager.add(collapseAllAction);
        menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menuManager.add(new Separator("end")); //$NON-NLS-1$
        
        site.getActionBars().updateActionBars();

    	super.init(site);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent);
		viewer.setContentProvider(new SecuritiesTreeContentProvider());
		viewer.setLabelProvider(new DecoratingLabelProvider(new SecuritiesLabelProvider(), null));
		viewer.setComparator(new SecuritiesTreeViewerComparator());
		viewer.addSelectionChangedListener(selectionChangedListener);

		new ExplorerDragAdapter(viewer);
		new ExplorerDropAdapter(viewer);

		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		if (themeManager != null) {
			ITheme theme = themeManager.getCurrentTheme();
			if (theme != null) {
//				viewer.getTree().setBackground(theme.getColorRegistry().get(TABLE_BACKGROUND));
//				viewer.getTree().setForeground(theme.getColorRegistry().get(TABLE_FOREGROUND));
				theme.addPropertyChangeListener(themeChangeListener);
			}
		}

		getSite().setSelectionProvider(new SelectionProvider());

		parent.getDisplay().asyncExec(new Runnable() {
			public void run() {
        		buildInput();
				viewer.setInput(input);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		if (themeManager != null) {
			ITheme theme = themeManager.getCurrentTheme();
			if (theme != null)
				theme.removePropertyChangeListener(themeChangeListener);
		}
		super.dispose();
	}

	protected void buildInput() {
		input = new ArrayList();

		for (Iterator<SecurityGroup> iter = CorePlugin.getRepository().allSecurityGroups().iterator(); iter.hasNext();) {
			SecurityGroup g = iter.next();
			if (g.getParentGroup() == null)
				input.add(g);
		}

		for (Iterator<Security> iter = CorePlugin.getRepository().allSecurities().iterator(); iter.hasNext();) {
			Security s = iter.next();
			if (s.getGroup() == null)
				input.add(s);
		}
	}
}
