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

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.PersistentObject;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.ui.NullSelection;
import net.sourceforge.eclipsetrader.core.ui.SecuritySelection;
import net.sourceforge.eclipsetrader.core.ui.SelectionProvider;
import net.sourceforge.eclipsetrader.core.ui.actions.PropertiesAction;
import net.sourceforge.eclipsetrader.core.ui.preferences.SecurityPropertiesDialog;
import net.sourceforge.eclipsetrader.internal.ui.Activator;
import net.sourceforge.eclipsetrader.internal.ui.InstrumentsInput;
import net.sourceforge.eclipsetrader.internal.ui.SecuritiesLabelProvider;
import net.sourceforge.eclipsetrader.internal.ui.SecuritiesTreeContentProvider;
import net.sourceforge.eclipsetrader.internal.ui.SecuritiesTreeLabelDecorator;
import net.sourceforge.eclipsetrader.internal.ui.SecuritiesTreeViewerComparator;
import net.sourceforge.eclipsetrader.internal.ui.dialogs.CreateSecurityGroupDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

public class SecurityExplorer extends ViewPart {
	public static final String THEME_EXPLORER_BACKGROUND = "EXPLORER_BACKGROUND"; //$NON-NLS-1$

	public static final String THEME_EXPLORER_FOREGROUND = "EXPLORER_FOREGROUND"; //$NON-NLS-1$

	private TreeViewer viewer;
	
	private IPropertyChangeListener themeChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getSource() instanceof IThemeManager) {
				if (event.getProperty().equals(IThemeManager.CHANGE_CURRENT_THEME)) {
					if (event.getOldValue() != null)
						((ITheme)event.getOldValue()).removePropertyChangeListener(themeChangeListener);
					
					ITheme theme = (ITheme) event.getNewValue();
					if (theme != null) {
						viewer.getTree().setBackground(theme.getColorRegistry().get(THEME_EXPLORER_BACKGROUND));
						viewer.getTree().setForeground(theme.getColorRegistry().get(THEME_EXPLORER_FOREGROUND));
						theme.addPropertyChangeListener(themeChangeListener);
					}
				}
			}
			
			ITheme theme = (ITheme) event.getSource();
			if (event.getProperty().equals(THEME_EXPLORER_BACKGROUND))
				viewer.getTree().setBackground(theme.getColorRegistry().get(event.getProperty()));
			else if (event.getProperty().equals(THEME_EXPLORER_FOREGROUND))
				viewer.getTree().setForeground(theme.getColorRegistry().get(event.getProperty()));
		}
	};

	private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			updateSelection((IStructuredSelection) event.getSelection());
		}
	};
	
	private Action createFolderAction = new Action("Create Group...") {
        @Override
        public void run() {
        	CreateSecurityGroupDialog dlg = new CreateSecurityGroupDialog(getViewSite().getShell());
        	dlg.open();
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

	private Action deleteAction = new Action("Delete", PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE)) {
        @Override
        public void run() {
        	Object[] selection = ((IStructuredSelection) viewer.getSelection()).toArray();
        	for (int i = 0; i < selection.length; i++)
        		CorePlugin.getRepository().delete((PersistentObject) selection[i]);
        }
	};
	
	private PropertiesAction propertiesAction = new PropertiesAction() {
		public void run() {
        	Object[] selection = ((IStructuredSelection) viewer.getSelection()).toArray();
			if (selection.length == 1 && selection[0] instanceof Security) {
				SecurityPropertiesDialog dlg = new SecurityPropertiesDialog((Security) selection[0], getSite().getShell());
				dlg.open();
			}
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
        menuManager.add(expandAllAction);
        menuManager.add(collapseAllAction);
        menuManager.add(new Separator());
        menuManager.add(createFolderAction);
        menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menuManager.add(new Separator("bottom")); //$NON-NLS-1$
        menuManager.add(propertiesAction);
        
        IToolBarManager toolbarManager = site.getActionBars().getToolBarManager();
        menuManager.add(new Separator("beginning")); //$NON-NLS-1$
        toolbarManager.add(expandAllAction);
        toolbarManager.add(collapseAllAction);
        menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menuManager.add(new Separator("end")); //$NON-NLS-1$
        
        site.getActionBars().setGlobalActionHandler("delete", deleteAction); //$NON-NLS-1$
        site.getActionBars().updateActionBars();

		site.setSelectionProvider(new SelectionProvider());

    	super.init(site);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent);
		viewer.setContentProvider(new SecuritiesTreeContentProvider());
		viewer.setLabelProvider(new DecoratingLabelProvider(new SecuritiesLabelProvider(), new SecuritiesTreeLabelDecorator()));
		viewer.setComparator(new SecuritiesTreeViewerComparator());
		viewer.addSelectionChangedListener(selectionChangedListener);

		new ExplorerDragAdapter(viewer);
		new ExplorerDropAdapter(viewer);

		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		if (themeManager != null) {
			themeManager.addPropertyChangeListener(themeChangeListener);
			ITheme theme = themeManager.getCurrentTheme();
			if (theme != null) {
				viewer.getTree().setBackground(theme.getColorRegistry().get(THEME_EXPLORER_BACKGROUND));
				viewer.getTree().setForeground(theme.getColorRegistry().get(THEME_EXPLORER_FOREGROUND));
				theme.addPropertyChangeListener(themeChangeListener);
			}
		}
		
        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager menuManager)
            {
                menuManager.add(new Separator("top")); //$NON-NLS-1$
                menuManager.add(createFolderAction);
                menuManager.add(new Separator());
                menuManager.add(expandAllAction);
                menuManager.add(collapseAllAction);
                menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
                menuManager.add(deleteAction);
                menuManager.add(new Separator("bottom")); //$NON-NLS-1$
            }
        });
        viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));
        getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());

		parent.getDisplay().asyncExec(new Runnable() {
			public void run() {
				viewer.setInput(new InstrumentsInput());
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
			themeManager.removePropertyChangeListener(themeChangeListener);
			ITheme theme = themeManager.getCurrentTheme();
			if (theme != null)
				theme.removePropertyChangeListener(themeChangeListener);
		}

		super.dispose();
	}

	protected void updateSelection(IStructuredSelection selection) {
		if (selection.size() == 1 && selection.getFirstElement() instanceof Security)
			getSite().getSelectionProvider().setSelection(new SecuritySelection((Security) selection.getFirstElement()));
		else
			getSite().getSelectionProvider().setSelection(new NullSelection());
		deleteAction.setEnabled(!selection.isEmpty());
		propertiesAction.setEnabled(!selection.isEmpty() && selection.getFirstElement() instanceof Security);
	}
}
