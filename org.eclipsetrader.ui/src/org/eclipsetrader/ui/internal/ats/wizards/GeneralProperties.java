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

package org.eclipsetrader.ui.internal.ats.wizards;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.core.ats.IStrategy;
import org.eclipsetrader.core.ats.ScriptStrategy;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.ats.SaveAdaptableHelper;

public class GeneralProperties extends PropertyPage implements IWorkbenchPropertyPage {

    private Text name;
    private ComboViewer repository;

    private IRepository targetRepository;

    private ModifyListener modifyListener = new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
            setValid(isValid());
        }
    };

    public GeneralProperties() {
        setTitle("General");
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

        Label label = new Label(content, SWT.NONE);
        label.setText("Name:");
        name = new Text(content, SWT.BORDER);
        name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        label = new Label(content, SWT.NONE);
        label.setText("Target repository:");
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        repository = new ComboViewer(content, SWT.READ_ONLY);
        repository.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        repository.setLabelProvider(new LabelProvider());
        repository.setContentProvider(new ArrayContentProvider());
        repository.setSorter(new ViewerSorter());
        repository.setInput(getRepositoryService().getRepositories());
        repository.setSelection(new StructuredSelection(getRepositoryService().getRepository("local")));

        performDefaults();
        name.addModifyListener(modifyListener);

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        IStrategy strategy = (IStrategy) getElement().getAdapter(IStrategy.class);
        name.setText(strategy.getName());

        IStoreObject storeObject = (IStoreObject) strategy.getAdapter(IStoreObject.class);
        repository.setSelection(new StructuredSelection(storeObject.getStore().getRepository()));

        super.performDefaults();
    }

    protected void applyChanges() {
        ScriptStrategy strategy = (ScriptStrategy) getElement().getAdapter(ScriptStrategy.class);
        if (strategy != null) {
            strategy.setName(name.getText());
        }

        targetRepository = (IRepository) ((IStructuredSelection) repository.getSelection()).getFirstElement();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#isValid()
     */
    @Override
    public boolean isValid() {
        if (name.getText().equals("")) {
            setErrorMessage("The strategy must have a name.");
            return false;
        }
        if (getErrorMessage() != null) {
            setErrorMessage(null);
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        if (getControl() != null) {
            applyChanges();
        }
        return super.performOk();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        applyChanges();
        SaveAdaptableHelper.save(getElement(), targetRepository);
        super.performApply();
    }

    public IRepository getRepository() {
        return targetRepository;
    }

    protected IRepositoryService getRepositoryService() {
        return UIActivator.getDefault().getRepositoryService();
    }
}
