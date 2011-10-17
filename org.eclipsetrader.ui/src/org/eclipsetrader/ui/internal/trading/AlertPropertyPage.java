/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.trading;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.PropertyPageManager;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.IAlert;
import org.eclipsetrader.core.trading.IAlertService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("restriction")
public class AlertPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

    TableViewer viewer;
    Button add;
    Button delete;
    CTabFolder folder;

    IAlertService alertService;
    List<IAlert> list;
    PropertyPage[] propertyPages = new PropertyPage[0];

    ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            delete.setEnabled(!event.getSelection().isEmpty());
            doSelectionChanged((IStructuredSelection) event.getSelection());
        }
    };

    public AlertPropertyPage() {
        this(Activator.getDefault().getBundle().getBundleContext());
    }

    protected AlertPropertyPage(BundleContext context) {
        setTitle(Messages.AlertPropertyPage_Title);

        ServiceReference serviceReference = context.getServiceReference(IAlertService.class.getName());
        if (serviceReference != null) {
            alertService = (IAlertService) context.getService(serviceReference);
            context.ungetService(serviceReference);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);

        initializeDialogUnits(content);

        viewer = new TableViewer(content, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        viewer.getTable().setHeaderVisible(false);
        viewer.getTable().setLinesVisible(false);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        gridData.heightHint = viewer.getTable().getItemHeight() * 5 + viewer.getTable().getBorderWidth() * 2;
        viewer.getControl().setLayoutData(gridData);
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new AlertLabelProvider());
        viewer.addSelectionChangedListener(selectionChangedListener);

        createButtonsBar(content);

        folder = new CTabFolder(content, SWT.TOP | SWT.BORDER);
        folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        performDefaults();

        return content;
    }

    protected void createButtonsBar(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

        add = new Button(content, SWT.NONE);
        add.setImage(getImageRegistry().get(Activator.ALERT_ADD_IMAGE));
        add.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                doAddNewAlert();
            }
        });

        delete = new Button(content, SWT.NONE);
        delete.setImage(getImageRegistry().get(Activator.ALERT_DELETE_IMAGE));
        delete.setEnabled(false);
        delete.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                list.removeAll(selection.toList());
                viewer.refresh();
            }
        });
    }

    ImageRegistry getImageRegistry() {
        return Activator.getDefault().getImageRegistry();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        ISecurity security = (ISecurity) getElement().getAdapter(ISecurity.class);

        IAlert[] alerts = alertService.getAlerts(security);

        list = new ArrayList<IAlert>(Arrays.asList(alerts));
        viewer.setInput(list);
    }

    @SuppressWarnings("unchecked")
    void doSelectionChanged(IStructuredSelection selection) {
        for (int i = 0; i < propertyPages.length; i++) {
            propertyPages[i].dispose();
        }

        if (selection.isEmpty()) {
            propertyPages = new PropertyPage[0];
            createTabbedPages();
            return;
        }

        final Object element = selection.getFirstElement();

        PropertyPageManager pageManager = new PropertyPageManager();
        PropertyPageContributorManager.getManager().contribute(pageManager, element);

        IAdaptable adaptableElement = new IAdaptable() {

            @Override
            public Object getAdapter(Class adapter) {
                if (adapter.isAssignableFrom(element.getClass())) {
                    return element;
                }
                return null;
            }
        };

        List<PropertyPage> list = new ArrayList<PropertyPage>();
        for (Object nodeObj : pageManager.getElements(PreferenceManager.PRE_ORDER)) {
            IPreferenceNode node = (IPreferenceNode) nodeObj;
            node.createPage();
            if (node.getPage() instanceof PropertyPage) {
                PropertyPage page = (PropertyPage) node.getPage();
                page.setElement(adaptableElement);
                list.add(page);
            }
        }
        propertyPages = list.toArray(new PropertyPage[list.size()]);

        createTabbedPages();
    }

    void createTabbedPages() {
        CTabItem[] existingItems = folder.getItems();
        for (int i = 0; i < existingItems.length; i++) {
            existingItems[i].dispose();
        }

        if (propertyPages != null) {
            for (int i = 0; i < propertyPages.length; i++) {
                propertyPages[i].dispose();
            }
        }

        for (int i = 0; i < propertyPages.length; i++) {
            CTabItem tabItem = new CTabItem(folder, SWT.NONE);

            Composite content = new Composite(folder, SWT.NONE);
            FillLayout layout = new FillLayout();
            layout.marginWidth = layout.marginHeight = 5;
            content.setLayout(layout);

            propertyPages[i].createControl(content);

            tabItem.setText(propertyPages[i].getTitle());
            tabItem.setControl(content);
        }

        if (folder.getItemCount() != 0) {
            folder.setSelection(folder.getItem(0));
        }

        folder.getParent().layout();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        super.performApply();
        viewer.refresh();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        ISecurity security = (ISecurity) getElement().getAdapter(ISecurity.class);

        if (isControlCreated()) {
            for (int i = 0; i < propertyPages.length; i++) {
                propertyPages[i].performOk();
            }

            alertService.setAlerts(security, list.toArray(new IAlert[list.size()]));
        }

        return super.performOk();
    }

    void doAddNewAlert() {
        ISecurity security = (ISecurity) getElement().getAdapter(ISecurity.class);
        NewAlertWizard wizard = new NewAlertWizard(PlatformUI.getWorkbench(), new StructuredSelection(security));

        WizardDialog dlg = new WizardDialog(getShell(), wizard);
        if (dlg.open() == Window.OK) {
            performDefaults();
        }
    }
}
