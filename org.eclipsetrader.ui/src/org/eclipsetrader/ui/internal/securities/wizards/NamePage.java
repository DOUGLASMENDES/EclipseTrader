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

package org.eclipsetrader.ui.internal.securities.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.ui.internal.UIActivator;

public class NamePage extends WizardPage {

    private Text name;
    private ComboViewer currency;
    private ComboViewer repository;

    private ModifyListener modifyListener = new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
            if (isCurrentPage()) {
                getContainer().updateButtons();
            }
        }
    };

    public NamePage() {
        super("name", "Common Stock", null);
        setDescription("Create a New Common Stock Security");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        content.setLayout(new GridLayout(2, false));
        setControl(content);
        initializeDialogUnits(content);

        Label label = new Label(content, SWT.NONE);
        label.setText("Security name:");
        name = new Text(content, SWT.BORDER);
        name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        name.addModifyListener(modifyListener);
        name.setFocus();

        label = new Label(content, SWT.NONE);
        label.setText("Currency:");
        currency = new ComboViewer(content, SWT.READ_ONLY);
        currency.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                Locale locale = (Locale) element;
                return NLS.bind("{0} ({1})", new Object[] {
                        locale.getDisplayCountry(),
                        Currency.getInstance(locale).getCurrencyCode(),
                });
            }
        });
        currency.setContentProvider(new ArrayContentProvider());
        currency.setSorter(new ViewerSorter());
        currency.setInput(getAvailableCurrencies());
        currency.setSelection(new StructuredSelection(Locale.getDefault()));

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
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        name.setFocus();
        super.setVisible(visible);
    }

    protected IRepositoryService getRepositoryService() {
        return UIActivator.getDefault().getRepositoryService();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        if (name.getText().equals("")) {
            return false;
        }
        if (getRepositoryService().getSecurityFromName(name.getText()) != null) {
            setErrorMessage("A security with the same name already exists. Choose a different name.");
            return false;
        }
        if (getErrorMessage() != null) {
            setErrorMessage(null);
        }
        return true;
    }

    public String getSecurityName() {
        return name.getText();
    }

    public IRepository getRepository() {
        return (IRepository) ((IStructuredSelection) repository.getSelection()).getFirstElement();
    }

    public Currency getCurrency() {
        Locale locale = (Locale) ((IStructuredSelection) currency.getSelection()).getFirstElement();
        return Currency.getInstance(locale);
    }

    protected List<Locale> getAvailableCurrencies() {
        List<Locale> locale = new ArrayList<Locale>(Arrays.asList(Locale.getAvailableLocales()));
        Collections.sort(locale, new Comparator<Locale>() {

            @Override
            public int compare(Locale arg0, Locale arg1) {
                return arg0.getDisplayCountry().compareTo(arg1.getDisplayCountry());
            }
        });
        for (int i = locale.size() - 1; i >= 1; i--) {
            if (locale.get(i).getDisplayCountry().equals(locale.get(i - 1).getDisplayCountry()) == true) {
                locale.remove(i);
            }
        }
        for (Iterator<Locale> iter = locale.iterator(); iter.hasNext();) {
            try {
                if (Currency.getInstance(iter.next()) == null) {
                    iter.remove();
                }
            } catch (Exception e) {
                iter.remove();
            }
        }
        return locale;
    }
}
