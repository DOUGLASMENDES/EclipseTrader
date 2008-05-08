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

package org.eclipsetrader.yahoo.internal.ui.wizards;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.yahoo.internal.YahooActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ImportDataPage extends WizardPage {
	private Combo combo;
	private CheckboxTableViewer members;

	public ImportDataPage() {
		super("data", "Import", null);
		setDescription("Select the securities to import.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		content.setLayout(gridLayout);
		setControl(content);

		initializeDialogUnits(parent);

		Label label = new Label(content, SWT.NONE);
		label.setText("Import");
		combo = new Combo(content, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		combo.add("all securities");
		combo.add("securities selected below");
		combo.select(0);
		combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
    			members.getControl().setEnabled(combo.getSelectionIndex() != 0);
            	setPageComplete(isPageComplete());
            }
		});

		members = CheckboxTableViewer.newCheckList(content, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		members.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		((GridData) members.getControl().getLayoutData()).heightHint = members.getTable().getItemHeight() * 15 + members.getTable().getBorderWidth() * 2;
		((GridData) members.getControl().getLayoutData()).verticalIndent = 5;
		members.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
	            return ((ISecurity) element).getName();
            }
		});
		members.setContentProvider(new ArrayContentProvider());
		members.setSorter(new ViewerSorter());
		members.setInput(getRepositoryService().getSecurities());
		members.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
            	setPageComplete(isPageComplete());
            }
		});
		members.getControl().setEnabled(combo.getSelectionIndex() != 0);
	}

	protected IRepositoryService getRepositoryService() {
		IRepositoryService service = null;
		BundleContext context = YahooActivator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
		if (serviceReference != null) {
			service = (IRepositoryService) context.getService(serviceReference);
			context.ungetService(serviceReference);
		}
		return service;
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
    	if (combo.getSelectionIndex() == 0)
    		return true;
	    return members.getCheckedElements().length != 0;
    }

    public ISecurity[] getCheckedSecurities() {
    	Object[] o = combo.getSelectionIndex() == 0 ? (Object[])members.getInput() : members.getCheckedElements();
    	ISecurity[] securities = new ISecurity[o.length];
    	System.arraycopy(o, 0, securities, 0, securities.length);
    	return securities;
    }
}
