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

package net.sourceforge.eclipsetrader.ats.ui.tradingsystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.ats.ATSPlugin;
import net.sourceforge.eclipsetrader.ats.TradingSystemManager;
import net.sourceforge.eclipsetrader.ats.core.db.Strategy;
import net.sourceforge.eclipsetrader.ats.core.db.TradingSystem;
import net.sourceforge.eclipsetrader.ats.core.internal.Backtest;
import net.sourceforge.eclipsetrader.ats.core.runnables.ComponentRunnable;
import net.sourceforge.eclipsetrader.ats.core.runnables.StrategyRunnable;
import net.sourceforge.eclipsetrader.ats.core.runnables.TradingSystemRunnable;
import net.sourceforge.eclipsetrader.ats.ui.StrategySecuritySelection;
import net.sourceforge.eclipsetrader.ats.ui.StrategySelection;
import net.sourceforge.eclipsetrader.ats.ui.TradingSystemSelection;
import net.sourceforge.eclipsetrader.ats.ui.tradingsystem.properties.TradingSystemPropertiesDialog;
import net.sourceforge.eclipsetrader.ats.ui.tradingsystem.providers.PositionColumn;
import net.sourceforge.eclipsetrader.ats.ui.tradingsystem.providers.StatusColumn;
import net.sourceforge.eclipsetrader.ats.ui.tradingsystem.wizards.StrategyWizard;
import net.sourceforge.eclipsetrader.ats.ui.tradingsystem.wizards.TradingSystemWizard;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.transfers.SecurityTransfer;
import net.sourceforge.eclipsetrader.core.ui.NullSelection;
import net.sourceforge.eclipsetrader.core.ui.SelectionProvider;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class TradingSystemView extends ViewPart {
	public static final String VIEW_ID = "net.sourceforge.eclipsetrader.ats.viewer";

	PreferenceStore preferences;

	Tree tree;

	TreeViewer viewer;

	RunnablesLabelProvider labelProvider = new RunnablesLabelProvider();

	ContentProvider contentProvider = new ContentProvider();

	Action createSystemAction;

	Action createStrategyAction;

	Action deleteAction;

	Action startAction;

	Action backtestAction;

	Action stopAction;

	Action columnsAction;

	Action propertiesAction;

	ICollectionObserver systemsObserver = new ICollectionObserver() {
		public void itemAdded(final Object o) {
			try {
				viewer.getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						TradingSystemRunnable element = (TradingSystemRunnable) o;
						contentProvider.addObservers(element);
						viewer.add(viewer.getInput(), element);
						viewer.expandToLevel(element, 2);
					}
				});
			} catch (SWTException e) {
				if (e.code != SWT.ERROR_WIDGET_DISPOSED)
					throw e;
			}
		}

		public void itemRemoved(Object o) {
			try {
				TradingSystemRunnable element = (TradingSystemRunnable) o;
				contentProvider.removeObservers(element);
				viewer.remove(element);
			} catch (SWTException e) {
				if (e.code != SWT.ERROR_WIDGET_DISPOSED)
					throw e;
			}
		}
	};

	Transfer[] dragTransferTypes = new Transfer[] { ComponentRunnableTransfer.getInstance(), };

	DragSourceAdapter dragSourceListener = new DragSourceAdapter() {
		public void dragStart(DragSourceEvent event) {
			event.doit = !viewer.getSelection().isEmpty();
		}

		public void dragSetData(DragSourceEvent event) {
			List objects = new ArrayList();
			for (Iterator iter = ((IStructuredSelection) viewer.getSelection()).iterator(); iter.hasNext();) {
				Object element = iter.next();
				if (element instanceof ComponentRunnable)
					objects.add(element);
			}
			event.data = (ComponentRunnable[]) objects.toArray(new ComponentRunnable[objects.size()]);
		}

		public void dragFinished(DragSourceEvent event) {
			for (Iterator iter = ((IStructuredSelection) viewer.getSelection()).iterator(); iter.hasNext();) {
				Object element = iter.next();
				if (element instanceof ComponentRunnable)
					((ComponentRunnable) element).getParent().getStrategy().removeSecurity(((ComponentRunnable) element).getSecurity());
			}
		}
	};

	Transfer[] dropTransferTypes = new Transfer[] { SecurityTransfer.getInstance(), ComponentRunnableTransfer.getInstance(), };

	DropTargetAdapter dropTargetListener = new DropTargetAdapter() {
		public void dragOver(DropTargetEvent event) {
			event.detail = DND.DROP_NONE;

			int action = DND.DROP_COPY;
			if (ComponentRunnableTransfer.getInstance().isSupportedType(event.currentDataType))
				action = DND.DROP_MOVE;

			TreeItem treeItem = tree.getItem(tree.toControl(event.x, event.y));
			if (treeItem != null) {
				if (treeItem.getData() instanceof StrategyRunnable)
					event.detail = action;
				if (treeItem.getData() instanceof ComponentRunnable)
					event.detail = action;
			}
		}

		public void drop(DropTargetEvent event) {
			if (ComponentRunnableTransfer.getInstance().isSupportedType(event.currentDataType)) {
				Security[] securities = (Security[]) event.data;
				TreeItem treeItem = tree.getItem(tree.toControl(event.x, event.y));
				if (treeItem != null) {
					StrategyRunnable strategyRunnable = null;

					if (treeItem.getData() instanceof StrategyRunnable)
						strategyRunnable = (StrategyRunnable) treeItem.getData();
					if (treeItem.getData() instanceof ComponentRunnable)
						strategyRunnable = ((ComponentRunnable) treeItem.getData()).getParent();

					if (strategyRunnable != null) {
						for (int i = 0; i < securities.length; i++)
							strategyRunnable.getStrategy().addSecurity(securities[i]);

					}
				}
			}
			if (SecurityTransfer.getInstance().isSupportedType(event.currentDataType)) {
				Security[] securities = (Security[]) event.data;
				TreeItem treeItem = tree.getItem(tree.toControl(event.x, event.y));
				if (treeItem != null) {
					StrategyRunnable strategyRunnable = null;

					if (treeItem.getData() instanceof StrategyRunnable)
						strategyRunnable = (StrategyRunnable) treeItem.getData();
					if (treeItem.getData() instanceof ComponentRunnable)
						strategyRunnable = ((ComponentRunnable) treeItem.getData()).getParent();

					if (strategyRunnable != null) {
						for (int i = 0; i < securities.length; i++)
							strategyRunnable.getStrategy().addSecurity(securities[i]);

					}
				}
			}
		}
	};

	ControlAdapter columnControlListener = new ControlAdapter() {
		public void controlResized(ControlEvent e) {
			int index = tree.indexOf((TreeColumn) e.widget);
			if (index != -1) {
				String className = "COLUMN()";
				if (index > 0)
					className = "COLUMN(" + labelProvider.getColumnLabelProviders()[index - 1].getClass().getName() + ")";
				preferences.setValue(className, ((TreeColumn) e.widget).getWidth());
			}
		}
	};

	public TradingSystemView() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
	 */
	public void init(IViewSite site) throws PartInitException {
		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();

		IPath path = ATSPlugin.getDefault().getStateLocation().append(site.getId() + ".prefs");
		preferences = new PreferenceStore(path.toOSString());
		try {
			preferences.load();
		} catch (Exception e) {
		}

		createSystemAction = new Action("Trading System") {
			public void run() {
				TradingSystemWizard wizard = new TradingSystemWizard();
				WizardDialog dlg = new WizardDialog(getViewSite().getShell(), wizard);
				dlg.open();
			}
		};

		createStrategyAction = new Action("Strategy") {
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection != null && !selection.isEmpty() && selection.getFirstElement() instanceof TradingSystemRunnable) {
					TradingSystem system = (TradingSystem) ((TradingSystemRunnable) selection.getFirstElement()).getTradingSystem();
					StrategyWizard wizard = new StrategyWizard(system);
					WizardDialog dlg = new WizardDialog(getViewSite().getShell(), wizard);
					dlg.open();
				}
			}
		};
		createStrategyAction.setEnabled(false);

		deleteAction = new Action("Delete") {
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (!selection.isEmpty()) {
					if (selection.getFirstElement() instanceof TradingSystemRunnable) {
						TradingSystem system = (TradingSystem) ((TradingSystemRunnable) selection.getFirstElement()).getTradingSystem();
						if (MessageDialog.openConfirm(getViewSite().getShell(), "Delete", "Do you really want to delete the selected trading system ?"))
							ATSPlugin.getRepository().delete(system);
					} else if (selection.getFirstElement() instanceof StrategyRunnable) {
						StrategyRunnable runnable = (StrategyRunnable) selection.getFirstElement();
						TradingSystem system = runnable.getParent().getTradingSystem();
						if (MessageDialog.openConfirm(getViewSite().getShell(), "Delete", "Do you really want to delete the selected strategy ?")) {
							system.removeStrategy(runnable.getStrategy());
							ATSPlugin.getRepository().save(system);
						}
					} else if (selection.getFirstElement() instanceof ComponentRunnable) {
						ComponentRunnable runnable = (ComponentRunnable) selection.getFirstElement();
						TradingSystem system = runnable.getParent().getParent().getTradingSystem();
						if (MessageDialog.openConfirm(getViewSite().getShell(), "Delete", "Do you really want to delete the selected component ?")) {
							runnable.getParent().getStrategy().removeSecurity(runnable.getSecurity());
							ATSPlugin.getRepository().save(system);
						}
					}
				}
			}
		};
		deleteAction.setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
		deleteAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		deleteAction.setEnabled(false);

		startAction = new Action("Start") {
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection != null && !selection.isEmpty()) {
					if (selection.getFirstElement() instanceof TradingSystemRunnable) {
						TradingSystem system = (TradingSystem) ((TradingSystemRunnable) selection.getFirstElement()).getTradingSystem();
						Object[] s = system.getStrategies().toArray();
						for (int i = 0; i < s.length; i++)
							((Strategy) s[i]).setAutoStart(true);
						ATSPlugin.getRepository().save(system);
					} else if (selection.getFirstElement() instanceof StrategyRunnable) {
						StrategyRunnable runnable = (StrategyRunnable) selection.getFirstElement();
						runnable.getStrategy().setAutoStart(true);
						TradingSystem system = runnable.getParent().getTradingSystem();
						ATSPlugin.getRepository().save(system);
					}
				}
			}
		};
		startAction.setImageDescriptor(ATSPlugin.getImageDescriptor("icons/full/elcl16/run.gif"));
		startAction.setDisabledImageDescriptor(ATSPlugin.getImageDescriptor("icons/full/dlcl16/run.gif"));
		startAction.setEnabled(false);

		backtestAction = new Action("Backtest") {
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection != null && !selection.isEmpty()) {
					if (selection.getFirstElement() instanceof TradingSystemRunnable) {
						TradingSystemRunnable runnable = (TradingSystemRunnable) selection.getFirstElement();

						TestPeriodDialog dlg = new TestPeriodDialog(getViewSite().getShell());
						if (dlg.open() == TestPeriodDialog.OK) {
							Backtest test = new Backtest(runnable.getTradingSystem(), dlg.getBeginDate(), dlg.getEndDate());
							test.setUser(true);
							test.schedule();
						}
					} else if (selection.getFirstElement() instanceof StrategyRunnable) {
						StrategyRunnable runnable = (StrategyRunnable) selection.getFirstElement();

						TestPeriodDialog dlg = new TestPeriodDialog(getViewSite().getShell());
						if (dlg.open() == TestPeriodDialog.OK) {
							Backtest test = new Backtest(runnable.getStrategy(), dlg.getBeginDate(), dlg.getEndDate());
							test.setUser(true);
							test.schedule();
						}
					}
				}
			}
		};
		backtestAction.setImageDescriptor(ATSPlugin.getImageDescriptor("icons/full/elcl16/time_go.png"));
		backtestAction.setEnabled(false);

		stopAction = new Action("Stop") {
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection != null && !selection.isEmpty()) {
					if (selection.getFirstElement() instanceof TradingSystemRunnable) {
						TradingSystem system = (TradingSystem) ((TradingSystemRunnable) selection.getFirstElement()).getTradingSystem();
						Object[] s = system.getStrategies().toArray();
						for (int i = 0; i < s.length; i++)
							((Strategy) s[i]).setAutoStart(false);
						ATSPlugin.getRepository().save(system);
					} else if (selection.getFirstElement() instanceof StrategyRunnable) {
						StrategyRunnable runnable = (StrategyRunnable) selection.getFirstElement();
						runnable.getStrategy().setAutoStart(false);
						TradingSystem system = runnable.getParent().getTradingSystem();
						ATSPlugin.getRepository().save(system);
					}
				}
			}
		};
		stopAction.setImageDescriptor(ATSPlugin.getImageDescriptor("icons/full/elcl16/stop.gif"));
		stopAction.setDisabledImageDescriptor(ATSPlugin.getImageDescriptor("icons/full/dlcl16/stop.gif"));
		stopAction.setEnabled(false);

		propertiesAction = new Action("Properties") {
			public void run() {
				ISelection selection = getSite().getSelectionProvider().getSelection();
				if (selection instanceof TradingSystemSelection) {
					TradingSystem system = ((TradingSystemSelection) selection).getTradingSystem();
					TradingSystemPropertiesDialog dlg = new TradingSystemPropertiesDialog(getViewSite().getShell(), system);
					if (dlg.open() == TradingSystemPropertiesDialog.OK)
						ATSPlugin.getRepository().save(system);
				}
			}
		};

		columnsAction = new Action("Columns") {
			public void run() {
				ViewColumnsDialog dlg = new ViewColumnsDialog(getViewSite().getShell());
				dlg.open();
			}
		};

		IToolBarManager toolbarManager = site.getActionBars().getToolBarManager();
		toolbarManager.add(startAction);
		toolbarManager.add(backtestAction);
		toolbarManager.add(stopAction);

		IMenuManager menuManager = site.getActionBars().getMenuManager();
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillMenu(manager);
			}
		});
		menuManager.setRemoveAllWhenShown(true);
		fillMenu(menuManager);

		site.getActionBars().updateActionBars();

		site.setSelectionProvider(new SelectionProvider());
		site.getSelectionProvider().setSelection(new NullSelection());

		super.init(site);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		tree = new Tree(parent, SWT.FULL_SELECTION | SWT.MULTI);
		tree.setHeaderVisible(false);
		tree.setLinesVisible(false);

		labelProvider.setColumnLabelProviders(new ILabelProvider[] { new StatusColumn(), new PositionColumn(), });

		/*        TreeColumn treeColumn = new TreeColumn(tree, SWT.LEFT);
		 treeColumn.setWidth(preferences.getInt("COLUMN()") == 0 ? 150 : preferences.getInt("COLUMN()"));
		 treeColumn.addControlListener(columnControlListener);

		 treeColumn = new TreeColumn(tree, SWT.LEFT);
		 treeColumn.setText("Status");
		 String className = "COLUMN(" + labelProvider.getColumnLabelProviders()[tree.indexOf(treeColumn) - 1].getClass().getName() + ")";
		 treeColumn.setWidth(preferences.getInt(className) == 0 ? 80 : preferences.getInt(className));
		 treeColumn.addControlListener(columnControlListener);
		 
		 treeColumn = new TreeColumn(tree, SWT.RIGHT);
		 treeColumn.setText("Position");
		 className = "COLUMN(" + labelProvider.getColumnLabelProviders()[tree.indexOf(treeColumn) - 1].getClass().getName() + ")";
		 treeColumn.setWidth(preferences.getInt(className) == 0 ? 80 : preferences.getInt(className));
		 treeColumn.addControlListener(columnControlListener);*/

		viewer = new TreeViewer(tree);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider);
		viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, dragTransferTypes, dragSourceListener);
		viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, dropTransferTypes, dropTargetListener);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateSelection();
			}
		});
		viewer.setComparator(new ViewerComparator() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof ComponentRunnable && e2 instanceof ComponentRunnable)
					return ((ComponentRunnable) e1).getSecurity().getDescription().compareTo(((ComponentRunnable) e2).getSecurity().getDescription());
				return 0;
			}
		});

		Font font = tree.getFont();
		FontData fontData = font.getFontData()[0];
		labelProvider.boldFont = new Font(tree.getDisplay(), fontData.getName(), fontData.getHeight(), SWT.BOLD);

		MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuManager) {
				fillMenu(menuManager);
			}
		});
		tree.setMenu(menuMgr.createContextMenu(tree));
		getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());

		viewer.setInput(TradingSystemManager.getInstance().getRunnables());
		viewer.expandToLevel(3);

		TradingSystemManager.getInstance().addRunnablesObserver(systemsObserver);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		TradingSystemManager.getInstance().removeRunnablesObserver(systemsObserver);
		labelProvider.boldFont.dispose();

		try {
			preferences.save();
		} catch (Exception e) {
			IStatus status = new Status(IStatus.WARNING, ATSPlugin.PLUGIN_ID, -1, "Unexpected error while saving preferences", e);
			ATSPlugin.getDefault().getLog().log(status);
		}

		super.dispose();
	}

	protected void fillMenu(IMenuManager menuManager) {
		IStructuredSelection selection = null;
		if (viewer != null)
			selection = (IStructuredSelection) viewer.getSelection();

		MenuManager newMenu = new MenuManager("New", "new");
		newMenu.add(createSystemAction);
		newMenu.add(new Separator());
		if (selection == null || selection.getFirstElement() instanceof TradingSystemRunnable)
			newMenu.add(createStrategyAction);
		newMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuManager.add(newMenu);

		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		menuManager.add(deleteAction);
		menuManager.add(new Separator());
		menuManager.add(startAction);
		menuManager.add(backtestAction);
		menuManager.add(stopAction);
		menuManager.add(new Separator());
		menuManager.add(columnsAction);
		menuManager.add(new Separator());
		menuManager.add(propertiesAction);
	}

	void updateSelection() {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (selection != null && !selection.isEmpty()) {
			if (selection.getFirstElement() instanceof TradingSystemRunnable)
				getSite().getSelectionProvider().setSelection(new TradingSystemSelection((TradingSystemRunnable) selection.getFirstElement()));
			else if (selection.getFirstElement() instanceof StrategyRunnable)
				getSite().getSelectionProvider().setSelection(new StrategySelection((StrategyRunnable) selection.getFirstElement()));
			else if (selection.getFirstElement() instanceof ComponentRunnable)
				getSite().getSelectionProvider().setSelection(new StrategySecuritySelection((ComponentRunnable) selection.getFirstElement()));
			else
				getSite().getSelectionProvider().setSelection(new NullSelection());

			createStrategyAction.setEnabled(selection.getFirstElement() instanceof TradingSystemRunnable);
			deleteAction.setEnabled(true);
			startAction.setEnabled(false); // TODO
			backtestAction.setEnabled(!(getSite().getSelectionProvider().getSelection() instanceof NullSelection));
			stopAction.setEnabled(false); // TODO
		} else {
			getSite().getSelectionProvider().setSelection(new NullSelection());
			createStrategyAction.setEnabled(false);
			deleteAction.setEnabled(false);
			startAction.setEnabled(false);
			backtestAction.setEnabled(false);
			stopAction.setEnabled(false);
		}
	}
}
