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

package org.eclipsetrader.directa.internal.ui.wizards;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.TimeSpan.Units;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.directa.internal.Activator;
import org.eclipsetrader.ui.Util;
import org.eclipsetrader.ui.internal.UIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ImportDataPage extends WizardPage {

    private static final String K_SELECTION = "SELECTION"; //$NON-NLS-1$
    private static final String K_SECURITIES = "SECURITIES"; //$NON-NLS-1$
    private static final String K_MODE = "MODE"; //$NON-NLS-1$
    private static final String K_FROM_DATE = "FROM_DATE"; //$NON-NLS-1$
    private static final String K_AGGREGATION = "AGGREGATION"; //$NON-NLS-1$

    private Combo type;
    private CDateTime from;
    private CDateTime to;
    private CheckboxTableViewer aggregation;
    private Combo combo;
    private CheckboxTableViewer members;

    public ImportDataPage() {
        super("data", Messages.ImportDataPage_Title, null); //$NON-NLS-1$
        setDescription(Messages.ImportDataPage_Description);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        setControl(content);

        initializeDialogUnits(parent);

        Label label = new Label(content, SWT.NONE);
        label.setText(Messages.ImportDataPage_Type);
        type = new Combo(content, SWT.DROP_DOWN | SWT.READ_ONLY);
        type.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        type.add(Messages.ImportDataPage_Full);
        type.add(Messages.ImportDataPage_Incremental);
        type.add(Messages.ImportDataPage_FullIncremental);
        type.select(1);
        type.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int typeIndex = type.getSelectionIndex();
                from.setEnabled(typeIndex == 0 || typeIndex == 2);
                to.setEnabled(typeIndex == 0);
                if (typeIndex != 0) {
                    to.setSelection(Calendar.getInstance().getTime());
                }
                setPageComplete(isPageComplete());
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText(Messages.ImportDataPage_Period);
        Composite group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        group.setLayout(gridLayout);
        from = new CDateTime(group, CDT.BORDER | CDT.DATE_SHORT | CDT.DROP_DOWN | CDT.TAB_FIELDS);
        from.setPattern(Util.getDateFormatPattern());
        label = new Label(group, SWT.NONE);
        label.setText(Messages.ImportDataPage_To);
        to = new CDateTime(group, CDT.BORDER | CDT.DATE_SHORT | CDT.DROP_DOWN | CDT.TAB_FIELDS);
        to.setPattern(Util.getDateFormatPattern());

        Calendar today = Calendar.getInstance();
        to.setSelection(today.getTime());
        today.add(Calendar.YEAR, -10);
        from.setSelection(today.getTime());

        label = new Label(content, SWT.NONE);
        label.setText(Messages.ImportDataPage_Aggregation);
        label.setLayoutData(new GridData(SWT.TOP, SWT.RIGHT, false, false));
        ((GridData) label.getLayoutData()).verticalIndent = convertVerticalDLUsToPixels(2);
        aggregation = CheckboxTableViewer.newCheckList(content, SWT.BORDER);
        aggregation.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ((GridData) aggregation.getControl().getLayoutData()).heightHint = aggregation.getTable().getItemHeight() * 3;
        aggregation.setContentProvider(new ArrayContentProvider());
        aggregation.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                TimeSpan timeSpan = (TimeSpan) element;
                return NLS.bind("{0} {1}", new Object[] { //$NON-NLS-1$
                    String.valueOf(timeSpan.getLength()),
                    timeSpan.getUnits() == Units.Minutes ? Messages.ImportDataPage_Minutes : Messages.ImportDataPage_Days,
                });
            }
        });
        TimeSpan[] availableSizes = getAvailableAggregations();
        aggregation.setInput(availableSizes);
        aggregation.setChecked(availableSizes[0], true);

        label = new Label(content, SWT.NONE);
        label.setText(Messages.ImportDataPage_Import);
        combo = new Combo(content, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        combo.add(Messages.ImportDataPage_AllSecurities);
        combo.add(Messages.ImportDataPage_SecuritiesSelectedBelow);
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
        ((GridData) members.getControl().getLayoutData()).heightHint = members.getTable().getItemHeight() * 10 + members.getTable().getBorderWidth() * 2;
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

        restoreState();

        int typeIndex = type.getSelectionIndex();
        from.setEnabled(typeIndex == 0 || typeIndex == 2);
        to.setEnabled(typeIndex == 0);

        members.getControl().setEnabled(combo.getSelectionIndex() != 0);

        aggregation.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                setPageComplete(isPageComplete());
            }
        });

        members.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                setPageComplete(isPageComplete());
            }
        });
    }

    protected void restoreState() {
        IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings().getSection(getClass().getName());
        if (dialogSettings != null) {
            if (dialogSettings.get(K_MODE) != null) {
                type.select(dialogSettings.getInt(K_MODE));
            }

            if (dialogSettings.get(K_FROM_DATE) != null) {
                try {
                    from.setSelection(new SimpleDateFormat("yyyyMMdd").parse(dialogSettings.get(K_FROM_DATE))); //$NON-NLS-1$
                } catch (Exception e) {
                    // Do nothing
                }
            }

            String[] s = dialogSettings.getArray(K_AGGREGATION);
            if (s != null) {
                aggregation.setAllChecked(false);
                TimeSpan[] ts = new TimeSpan[s.length];
                for (int i = 0; i < ts.length; i++) {
                    ts[i] = TimeSpan.fromString(s[i]);
                }
                aggregation.setCheckedElements(ts);
            }

            if (dialogSettings.get(K_SECURITIES) != null) {
                combo.select(dialogSettings.getInt(K_SECURITIES));
            }

            String[] selection = dialogSettings.getArray(K_SELECTION);
            if (selection != null) {
                IRepositoryService repository = getRepositoryService();
                for (int i = 0; i < selection.length; i++) {
                    try {
                        ISecurity security = repository.getSecurityFromURI(new URI(selection[i]));
                        if (security != null) {
                            members.setChecked(security, true);
                        }
                    } catch (URISyntaxException e) {
                        // Ignore URI exception, not really important here
                    }
                }
            }
        }
    }

    public void saveState() {
        IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings().getSection(getClass().getName());
        if (dialogSettings == null) {
            dialogSettings = Activator.getDefault().getDialogSettings().addNewSection(getClass().getName());
        }

        dialogSettings.put(K_MODE, type.getSelectionIndex());

        dialogSettings.put(K_FROM_DATE, new SimpleDateFormat("yyyyMMdd").format(from.getSelection())); //$NON-NLS-1$

        TimeSpan[] ts = getAggregation();
        String[] s = new String[ts.length];
        for (int i = 0; i < s.length; i++) {
            s[i] = ts[i].toString();
        }
        dialogSettings.put(K_AGGREGATION, s);

        dialogSettings.put(K_SECURITIES, combo.getSelectionIndex());

        Object[] o = members.getCheckedElements();
        String[] selection = new String[o.length];
        for (int i = 0; i < o.length; i++) {
            IStoreObject storeObject = (IStoreObject) ((IAdaptable) o[i]).getAdapter(IStoreObject.class);
            selection[i] = storeObject.getStore().toURI().toString();
        }
        dialogSettings.put(K_SELECTION, selection);
    }

    protected IRepositoryService getRepositoryService() {
        IRepositoryService service = null;
        BundleContext context = Activator.getDefault().getBundle().getBundleContext();
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
        if (aggregation.getCheckedElements().length == 0) {
            return false;
        }
        if (from.getEnabled() && from.getSelection() == null) {
            return false;
        }
        if (to.getEnabled() && to.getSelection() == null) {
            return false;
        }
        if (combo.getSelectionIndex() == 0) {
            return true;
        }
        return members.getCheckedElements().length != 0;
    }

    public ISecurity[] getCheckedSecurities() {
        Object[] o = combo.getSelectionIndex() == 0 ? (Object[]) members.getInput() : members.getCheckedElements();
        ISecurity[] securities = new ISecurity[o.length];
        System.arraycopy(o, 0, securities, 0, securities.length);
        return securities;
    }

    public TimeSpan[] getAggregation() {
        Object[] ar = aggregation.getCheckedElements();
        TimeSpan[] r = new TimeSpan[ar.length];
        System.arraycopy(ar, 0, r, 0, r.length);
        return r;
    }

    public Date getFromDate() {
        return from.getSelection();
    }

    public Date getToDate() {
        return to.getSelection();
    }

    public int getImportType() {
        switch (type.getSelectionIndex()) {
            case 0:
                return DataImportJob.FULL;
            case 1:
                return DataImportJob.INCREMENTAL;
            case 2:
                return DataImportJob.FULL_INCREMENTAL;
        }
        return DataImportJob.FULL;
    }

    protected TimeSpan[] getAvailableAggregations() {
        List<TimeSpan> list = new ArrayList<TimeSpan>();

        String value = UIActivator.getDefault().getPreferenceStore().getString(UIActivator.PREFS_CHART_BARS);
        String[] s = value.split(",");
        for (int i = 0; i < s.length; i++) {
            TimeSpan timeSpan = TimeSpan.fromString(s[i]);
            if (timeSpan != null) {
                list.add(timeSpan);
            }
        }

        Collections.sort(list, new Comparator<TimeSpan>() {

            @Override
            public int compare(TimeSpan o1, TimeSpan o2) {
                if (o1.getUnits() != o2.getUnits()) {
                    return o2.getUnits().ordinal() - o1.getUnits().ordinal();
                }
                return o1.getLength() - o2.getLength();
            }
        });

        return list.toArray(new TimeSpan[list.size()]);
    }
}
