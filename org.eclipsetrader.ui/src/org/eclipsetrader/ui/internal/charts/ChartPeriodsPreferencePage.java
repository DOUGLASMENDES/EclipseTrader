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

package org.eclipsetrader.ui.internal.charts;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.ats.TimeSpanDialog;

public class ChartPeriodsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private ListViewer viewer;
    private Button add;
    private Button edit;
    private Button remove;

    private PeriodList list = new PeriodList();

    public ChartPeriodsPreferencePage() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(UIActivator.getDefault().getPreferenceStore());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.verticalSpacing = convertVerticalDLUsToPixels(2);
        content.setLayout(gridLayout);

        viewer = new ListViewer(content, SWT.BORDER | SWT.MULTI);
        viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((Period) element).getDescription();
            }

        });
        viewer.setComparator(new ViewerComparator() {

            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                Period p1 = (Period) e1;
                Period p2 = (Period) e2;
                if (p1.getPeriod().higherThan(p2.getPeriod())) {
                    return -1;
                }
                if (p2.getPeriod().higherThan(p1.getPeriod())) {
                    return 1;
                }
                return 0;
            }
        });
        viewer.setInput(list);

        Composite buttons = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        buttons.setLayout(gridLayout);
        buttons.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));

        add = new Button(buttons, SWT.PUSH);
        add.setText("Add");
        add.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        add.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                PeriodDialog dlg = new PeriodDialog(getShell());
                if (dlg.open() == TimeSpanDialog.OK) {
                    list.add(dlg.getSelection());
                    viewer.refresh();
                }
            }
        });

        edit = new Button(buttons, SWT.PUSH);
        edit.setText("Edit");
        edit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        edit.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                if (selection.size() != 1) {
                    return;
                }
                Period period = (Period) selection.getFirstElement();
                PeriodDialog dlg = new PeriodDialog(getShell(), period);
                if (dlg.open() == TimeSpanDialog.OK) {
                    viewer.refresh();
                }
            }
        });
        edit.setEnabled(false);

        remove = new Button(buttons, SWT.PUSH);
        remove.setText("Remove");
        remove.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        remove.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                list.removeAll(selection.toList());
                viewer.refresh();
            }
        });
        remove.setEnabled(false);

        performDefaults();

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                edit.setEnabled(((IStructuredSelection) event.getSelection()).size() == 1);
                remove.setEnabled(!event.getSelection().isEmpty());
            }
        });

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        list.clear();

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(PeriodList.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            StringReader stream = new StringReader(getPreferenceStore().getString(UIActivator.PREFS_CHART_PERIODS));
            list.addAll((PeriodList) unmarshaller.unmarshal(stream));
        } catch (Exception e) {
            e.printStackTrace();
        }

        viewer.refresh();

        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        try {
            StringWriter string = new StringWriter();
            JAXBContext jaxbContext = JAXBContext.newInstance(list.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
            marshaller.marshal(list, string);
            getPreferenceStore().setValue(UIActivator.PREFS_CHART_PERIODS, string.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.performOk();
    }
}
