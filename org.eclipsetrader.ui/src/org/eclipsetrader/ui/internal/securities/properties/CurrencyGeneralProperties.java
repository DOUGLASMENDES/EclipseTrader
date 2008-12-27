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

package org.eclipsetrader.ui.internal.securities.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.eclipsetrader.core.instruments.CurrencyExchange;
import org.eclipsetrader.core.instruments.ICurrencyExchange;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.ui.internal.UIActivator;

public class CurrencyGeneralProperties extends PropertyPage implements IWorkbenchPropertyPage {
	private Text name;
	private ComboViewer fromCurrency;
	private ComboViewer toCurrency;
	private ComboViewer repository;

	private ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			setValid(isValid());
		}
	};

	public CurrencyGeneralProperties() {
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
		label.setText("Security name:");
		name = new Text(content, SWT.BORDER);
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		label = new Label(content, SWT.NONE);
		label.setText("From Currency:");
		fromCurrency = new ComboViewer(content, SWT.READ_ONLY);
		fromCurrency.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
            	Currency c = (Currency) element;
            	return c.getCurrencyCode();
            }
		});
		fromCurrency.setContentProvider(new ArrayContentProvider());
		fromCurrency.setSorter(new ViewerSorter());
		fromCurrency.setInput(getAvailableCurrencies());

		label = new Label(content, SWT.NONE);
		label.setText("To Currency:");
		toCurrency = new ComboViewer(content, SWT.READ_ONLY);
		toCurrency.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
            	Currency c = (Currency) element;
            	return c.getCurrencyCode();
            }
		});
		toCurrency.setContentProvider(new ArrayContentProvider());
		toCurrency.setSorter(new ViewerSorter());
		toCurrency.setInput(getAvailableCurrencies());

		label = new Label(content, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		label = new Label(content, SWT.NONE);
		label.setText("Target repository:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		label.setEnabled(false);
		repository = new ComboViewer(content, SWT.READ_ONLY);
		repository.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		repository.setLabelProvider(new LabelProvider());
		repository.setContentProvider(new ArrayContentProvider());
		repository.setSorter(new ViewerSorter());
		repository.setInput(getRepositoryService().getRepositories());
		repository.getControl().setEnabled(false);

		performDefaults();
		name.addModifyListener(modifyListener);

		return content;
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
		ICurrencyExchange security = (ICurrencyExchange) getElement().getAdapter(ICurrencyExchange.class);
		name.setText(security.getName());
		fromCurrency.setSelection(new StructuredSelection(security.getFromCurrency()));
		toCurrency.setSelection(new StructuredSelection(security.getToCurrency()));

		IStoreObject storeObject = (IStoreObject) security.getAdapter(IStoreObject.class);
		repository.setSelection(new StructuredSelection(storeObject.getStore().getRepository()));

		super.performDefaults();
    }

    protected void applyChanges() {
		CurrencyExchange security = (CurrencyExchange) getElement().getAdapter(CurrencyExchange.class);
		if (security != null) {
			security.setName(name.getText());

			IStructuredSelection selection = (IStructuredSelection) fromCurrency.getSelection();
			security.setFromCurrency((Currency) selection.getFirstElement());
			selection = (IStructuredSelection) toCurrency.getSelection();
			security.setToCurrency((Currency) selection.getFirstElement());
		}
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#isValid()
     */
    @Override
    public boolean isValid() {
    	if (name.getText().equals("")) {
    		setErrorMessage("The security must have a name.");
    		return false;
    	}
		ISecurity security = getRepositoryService().getSecurityFromName(name.getText());
    	if (security != null && security != getElement().getAdapter(ISecurity.class)) {
    		setErrorMessage("A security with the same name already exists. Choose a different name.");
    		return false;
    	}
    	if (getErrorMessage() != null)
    		setErrorMessage(null);
	    return true;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
    	if (getControl() != null)
    		applyChanges();
	    return super.performOk();
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
    	applyChanges();

    	final ISecurity security = (ISecurity) getElement().getAdapter(ISecurity.class);
		final IRepositoryService service = getRepositoryService();
		service.runInService(new IRepositoryRunnable() {
            public IStatus run(IProgressMonitor monitor) throws Exception {
            	service.saveAdaptable(new IAdaptable[] { security });
	            return Status.OK_STATUS;
            }
		}, null);

	    super.performApply();
    }

	protected IRepositoryService getRepositoryService() {
		return UIActivator.getDefault().getRepositoryService();
	}

	protected Set<Currency> getAvailableCurrencies() {
	    List<Locale> locale = new ArrayList<Locale>(Arrays.asList(Locale.getAvailableLocales()));
		Collections.sort(locale, new Comparator<Locale>() {
			public int compare(Locale arg0, Locale arg1) {
				return arg0.getDisplayCountry().compareTo(arg1.getDisplayCountry());
			}
		});
		for (int i = locale.size() - 1; i >= 1; i--) {
			if ((locale.get(i)).getDisplayCountry().equals((locale.get(i - 1)).getDisplayCountry()) == true)
				locale.remove(i);
		}
		Set<Currency> result = new HashSet<Currency>();
		for (Iterator<Locale> iter = locale.iterator(); iter.hasNext(); ) {
			try {
				Currency c = Currency.getInstance(iter.next());
				if (c == null)
					iter.remove();
				else
					result.add(c);
			} catch(Exception e) {
				iter.remove();
			}
		}
		return result;
	}
}
