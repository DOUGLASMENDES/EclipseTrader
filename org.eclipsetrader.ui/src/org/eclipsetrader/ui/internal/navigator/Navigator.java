/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.navigator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.views.IView;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewItemVisitor;
import org.eclipsetrader.core.views.IViewVisitor;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.ui.UIConstants;
import org.eclipsetrader.ui.internal.SelectionProvider;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.repositories.Messages;
import org.eclipsetrader.ui.internal.securities.SecurityObjectTransfer;
import org.eclipsetrader.ui.navigator.INavigatorContentGroup;

@SuppressWarnings("restriction")
public class Navigator extends ViewPart {
	private TreeViewer viewer;
	private IMemento memento;

	private Action collapseAllAction;
	private Action expandAllAction;

	private Action deleteAction;

	public Navigator() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.memento = memento;

		ImageRegistry imageRegistry = UIActivator.getDefault().getImageRegistry();

		site.setSelectionProvider(new SelectionProvider());

		collapseAllAction = new Action("Collapse All", imageRegistry.getDescriptor(UIConstants.COLLAPSEALL_ICON)) {
			@Override
			public void run() {
				viewer.collapseAll();
			}
		};

		expandAllAction = new Action("Expand All", imageRegistry.getDescriptor(UIConstants.EXPANDALL_ICON)) {
			@Override
			public void run() {
				viewer.expandAll();
			}
		};

		deleteAction = new Action("Delete") {
			@Override
			public void run() {
				final IAdaptable[] objects = getSelectedObject(viewer.getSelection());
				if (objects.length != 0) {
					if (!MessageDialog.openConfirm(getViewSite().getShell(), getPartName(), Messages.RepositoryExplorer_DeleteConfirmMessage))
						return;
					final IRepositoryService service = UIActivator.getDefault().getRepositoryService();
					service.runInService(new IRepositoryRunnable() {
						public IStatus run(IProgressMonitor monitor) throws Exception {
							service.deleteAdaptable(objects);
							return Status.OK_STATUS;
						}
					}, null);
				}
			}
		};
		deleteAction.setImageDescriptor(imageRegistry.getDescriptor(UIConstants.DELETE_EDIT_ICON));
		deleteAction.setDisabledImageDescriptor(imageRegistry.getDescriptor(UIConstants.DELETE_EDIT_DISABLED_ICON));
		deleteAction.setId(ActionFactory.DELETE.getId());
		deleteAction.setActionDefinitionId("org.eclipse.ui.edit.delete"); //$NON-NLS-1$
		deleteAction.setEnabled(false);

		IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
		toolBarManager.add(expandAllAction);
		toolBarManager.add(collapseAllAction);

		site.getActionBars().setGlobalActionHandler(deleteAction.getId(), deleteAction);

		site.getActionBars().updateActionBars();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.FULL_SELECTION);
		viewer.setUseHashlookup(true);
		viewer.setLabelProvider(new DecoratingLabelProvider(new NavigatorLabelProvider(), WorkbenchPlugin.getDefault().getDecoratorManager().getLabelDecorator()));
		viewer.setContentProvider(new NavigatorContentProvider());
		viewer.setSorter(new ViewerSorter() {
			@Override
			public int category(Object element) {
				if (element instanceof IAdaptable) {
					if (((IAdaptable) element).getAdapter(ISecurity.class) != null)
						return 1;
					if (((IAdaptable) element).getAdapter(IWatchList.class) != null)
						return 2;
					if (((IAdaptable) element).getAdapter(IRepository.class) != null)
						return 3;
				}
				return 0;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if ((e1 instanceof IAdaptable) && (e2 instanceof IAdaptable)) {
					if (((IAdaptable) e1).getAdapter(String.class) != null && ((IAdaptable) e2).getAdapter(String.class) != null)
						return 0;
				}
				return super.compare(viewer, e1, e2);
			}
		});

		DragSource dragSource = new DragSource(viewer.getControl(), DND.DROP_COPY | DND.DROP_MOVE);
		dragSource.setTransfer(new Transfer[] {
			SecurityObjectTransfer.getInstance()
		});
		dragSource.addDragListener(new DragSourceListener() {
			public void dragStart(DragSourceEvent event) {
				event.doit = getSelectedObject(viewer.getSelection()).length != 0;
			}

			public void dragSetData(DragSourceEvent event) {
				if (SecurityObjectTransfer.getInstance().isSupportedType(event.dataType))
					event.data = getSelectedObject(viewer.getSelection());
			}

			public void dragFinished(DragSourceEvent event) {
			}
		});

		MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuManager) {
				menuManager.add(new Separator("group.new"));
				menuManager.add(new GroupMarker("group.goto"));
				menuManager.add(new Separator("group.open"));
				menuManager.add(new GroupMarker("group.openWith"));
				menuManager.add(new Separator("group.trade"));
				menuManager.add(new GroupMarker("group.tradeWith"));
				menuManager.add(new Separator("group.show"));
				menuManager.add(new Separator("group.edit"));
				menuManager.add(new GroupMarker("group.reorganize"));
				menuManager.add(new GroupMarker("group.port"));
				menuManager.add(new Separator("group.generate"));
				menuManager.add(new Separator("group.search"));
				menuManager.add(new Separator("group.build"));
				menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				menuManager.add(new Separator("group.properties"));

				menuManager.appendToGroup("group.show", new Action("Expand All") {
					@Override
					public void run() {
						IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
						for (Iterator<?> iter = selection.iterator(); iter.hasNext();)
							viewer.expandToLevel(iter.next(), TreeViewer.ALL_LEVELS);
					}
				});
				menuManager.appendToGroup("group.reorganize", deleteAction);
			}
		});
		viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));
		getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());

		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				try {
					IHandlerService service = (IHandlerService) getSite().getService(IHandlerService.class);
					service.executeCommand("org.eclipse.ui.file.open", null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IAdaptable[] objects = getSelectedObject(event.getSelection());
				deleteAction.setEnabled(objects.length != 0);
				getViewSite().getSelectionProvider().setSelection(event.getSelection());
			}
		});

		NavigatorView view = new NavigatorView();
		view.setContentProviders(new IStructuredContentProvider[] {
		    new SecuritiesContentProvider(), new WatchListsContentProvider(),
		});
		view.setGroups(new INavigatorContentGroup[] {
		    new InstrumentTypeGroup(), new MarketGroup(),
		});
		view.update();
		viewer.setInput(view);

		if (memento != null) {
			String s = memento.getString("expanded");
			if (s != null) {
				String[] sr = s.split(";");
				final Set<Integer> itemHash = new HashSet<Integer>();
				for (int i = 0; i < sr.length; i++) {
					try {
						itemHash.add(Integer.parseInt(sr[i]));
					} catch (Exception e) {
						// Do nothing
					}
				}
				view.accept(new IViewVisitor() {
					public boolean visit(IView view) {
						return true;
					}

					public boolean visit(IViewItem viewItem) {
						if (itemHash.contains(viewItem.hashCode()))
							viewer.setExpandedState(viewItem, true);
						return true;
					}
				});
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (!viewer.getControl().isDisposed())
			viewer.getControl().setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		Object[] o = viewer.getExpandedElements();
		if (o != null && o.length != 0) {
			StringBuffer s = new StringBuffer();
			for (int i = 0; i < o.length; i++) {
				if (i != 0)
					s.append(";");
				s.append(o[i].hashCode());
			}
			memento.putString("expanded", s.toString());
		}

		super.saveState(memento);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		NavigatorView view = (NavigatorView) viewer.getInput();
		if (view != null)
			view.dispose();

		super.dispose();
	}

	protected IAdaptable[] getSelectedObject(ISelection selection) {
		final Set<ISecurity> list = new HashSet<ISecurity>();

		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			for (Object o : ((IStructuredSelection) selection).toArray()) {
				if (o instanceof NavigatorViewItem) {
					((NavigatorViewItem) o).accept(new IViewItemVisitor() {
						public boolean visit(IViewItem viewItem) {
							ISecurity reference = (ISecurity) viewItem.getAdapter(ISecurity.class);
							if (reference != null)
								list.add(reference);
							return true;
						}
					});
				}
			}
		}

		return list.toArray(new IAdaptable[list.size()]);
	}
}
