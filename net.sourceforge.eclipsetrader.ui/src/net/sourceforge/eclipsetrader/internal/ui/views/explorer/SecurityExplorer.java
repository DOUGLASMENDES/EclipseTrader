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
import net.sourceforge.eclipsetrader.core.db.PersistentObject;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.SecurityGroup;
import net.sourceforge.eclipsetrader.core.ui.NullSelection;
import net.sourceforge.eclipsetrader.core.ui.SecuritySelection;
import net.sourceforge.eclipsetrader.core.ui.SelectionProvider;
import net.sourceforge.eclipsetrader.core.ui.actions.PropertiesAction;
import net.sourceforge.eclipsetrader.core.ui.preferences.SecurityPropertiesDialog;
import net.sourceforge.eclipsetrader.internal.ui.Activator;
import net.sourceforge.eclipsetrader.internal.ui.dialogs.CreateSecurityGroupDialog;
import net.sourceforge.eclipsetrader.ui.SecurityGroupSelection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
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

	protected static final String PREFS_PRESENTATION = "presentation"; //$NON-NLS-1$

	private TreeViewer viewer;

	private IPropertyChangeListener themeChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getSource() instanceof IThemeManager) {
				if (event.getProperty().equals(IThemeManager.CHANGE_CURRENT_THEME)) {
					if (event.getOldValue() != null)
						((ITheme) event.getOldValue()).removePropertyChangeListener(themeChangeListener);

					ITheme theme = (ITheme) event.getNewValue();
					if (theme != null) {
						viewer.getTree().setBackground(theme.getColorRegistry().get(THEME_EXPLORER_BACKGROUND));
						viewer.getTree().setForeground(theme.getColorRegistry().get(THEME_EXPLORER_FOREGROUND));
						theme.addPropertyChangeListener(themeChangeListener);
					}
				}
			}
			if (event.getSource() instanceof ColorRegistry) {
				ColorRegistry registry = (ColorRegistry) event.getSource();
				if (event.getProperty().equals(THEME_EXPLORER_BACKGROUND))
					viewer.getTree().setBackground(registry.get(event.getProperty()));
				else if (event.getProperty().equals(THEME_EXPLORER_FOREGROUND))
					viewer.getTree().setForeground(registry.get(event.getProperty()));
			}
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
	
	private BulkChangesAction bulkChangesAction = new BulkChangesAction();

	private PropertiesAction propertiesAction = new PropertiesAction() {
		public void run() {
			Object[] selection = ((IStructuredSelection) viewer.getSelection()).toArray();
			if (selection.length == 1 && selection[0] instanceof Security) {
				SecurityPropertiesDialog dlg = new SecurityPropertiesDialog((Security) selection[0], getSite().getShell());
				dlg.open();
			}
		}
	};

	private GroupsPresentationAction groupsPresentationAction = new GroupsPresentationAction() {
		@Override
		protected void selectFlatPresentation() {
			viewer.setContentProvider(new FlatContentProvider());
			viewer.setLabelProvider(new DecoratingLabelProvider(new FlatLabelProvider(), new InstrumentsLabelDecorator()));
			viewer.setInput(new FlatInstrumentsInput());
		}

		@Override
		protected void selectHierarchicalPresentation() {
			viewer.setContentProvider(new HierarchicalContentProvider());
			viewer.setLabelProvider(new DecoratingLabelProvider(new HierarchicalLabelProvider(), new InstrumentsLabelDecorator()));
			viewer.setInput(new InstrumentsInput());
		}
	};

	public SecurityExplorer() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		IMenuManager menuManager = site.getActionBars().getMenuManager();
		menuManager.add(new Separator("top")); //$NON-NLS-1$
		menuManager.add(expandAllAction);
		menuManager.add(collapseAllAction);
		menuManager.add(new Separator());
		menuManager.add(createFolderAction);
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuManager.add(groupsPresentationAction);
		menuManager.add(new Separator("bottom")); //$NON-NLS-1$
		menuManager.add(bulkChangesAction);
		menuManager.add(new Separator());
		menuManager.add(propertiesAction);

		IToolBarManager toolbarManager = site.getActionBars().getToolBarManager();
		menuManager.add(new Separator("beginning")); //$NON-NLS-1$
		toolbarManager.add(expandAllAction);
		toolbarManager.add(collapseAllAction);
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuManager.add(new Separator("end")); //$NON-NLS-1$

		if (memento != null) {
			Integer value = memento.getInteger(PREFS_PRESENTATION);
			if (value != null)
				groupsPresentationAction.setPresentation(value);
		}

		site.getActionBars().setGlobalActionHandler("delete", deleteAction); //$NON-NLS-1$
		site.getActionBars().updateActionBars();

		site.setSelectionProvider(new SelectionProvider());

		super.init(site, memento);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		if (memento != null) {
			memento.putInteger(PREFS_PRESENTATION, groupsPresentationAction.getPresentation());
		}
		super.saveState(memento);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent);

		if (groupsPresentationAction.getPresentation() == GroupsPresentationAction.FLAT) {
			viewer.setContentProvider(new FlatContentProvider());
			viewer.setLabelProvider(new DecoratingLabelProvider(new FlatLabelProvider(), new InstrumentsLabelDecorator()));
		}
		if (groupsPresentationAction.getPresentation() == GroupsPresentationAction.HIERARCHICAL) {
			viewer.setContentProvider(new HierarchicalContentProvider());
			viewer.setLabelProvider(new DecoratingLabelProvider(new HierarchicalLabelProvider(), new InstrumentsLabelDecorator()));
		}

		viewer.setComparator(new InstrumentsViewerComparator());
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

		createContextMenu();

		parent.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (groupsPresentationAction.getPresentation() == GroupsPresentationAction.FLAT)
					viewer.setInput(new FlatInstrumentsInput());
				if (groupsPresentationAction.getPresentation() == GroupsPresentationAction.HIERARCHICAL)
					viewer.setInput(new InstrumentsInput());
				updateSelection((IStructuredSelection) viewer.getSelection());
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

	protected void createContextMenu() {
		MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuManager) {
				menuManager.add(new Separator("top")); //$NON-NLS-1$
				menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				menuManager.add(bulkChangesAction);
				menuManager.add(new Separator());
				menuManager.add(deleteAction);
				menuManager.add(new Separator("bottom")); //$NON-NLS-1$
				menuManager.add(new Separator());
				menuManager.add(propertiesAction);
			}
		});
		viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));
		getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());
	}

	protected void updateSelection(IStructuredSelection selection) {
		ISelection newSelection = new NullSelection();
		if (selection.size() == 1) {
			if (selection.getFirstElement() instanceof Security)
				newSelection = new SecuritySelection((Security) selection.getFirstElement());
			if (selection.getFirstElement() instanceof SecurityGroup)
				newSelection = new SecurityGroupSelection((SecurityGroup) selection.getFirstElement());
		}
		getSite().getSelectionProvider().setSelection(newSelection);

		bulkChangesAction.setSelection(getSelectedSecurities());

		deleteAction.setEnabled(!selection.isEmpty());
		propertiesAction.setEnabled(!selection.isEmpty() && selection.getFirstElement() instanceof Security);
	}

	protected Security[] getSelectedSecurities() {
		List<Security> list = new ArrayList<Security>();

		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		for (Iterator iter = selection.iterator(); iter.hasNext();)
			buildSecuritiesList(list, iter.next());

		return (Security[]) list.toArray(new Security[list.size()]);
	}

	protected void buildSecuritiesList(List<Security> list, Object root) {
		if (root instanceof Security) {
			if (!list.contains(root))
				list.add((Security) root);
		} else if (root instanceof SecurityGroup) {
			for (Object child : ((SecurityGroup) root).getChildrens())
				buildSecuritiesList(list, child);
		} else if (root instanceof Object[]) {
			Object[] array = (Object[]) root;
			for (int i = 0; i < array.length; i++)
				buildSecuritiesList(list, array[i]);
		}
	}
}
