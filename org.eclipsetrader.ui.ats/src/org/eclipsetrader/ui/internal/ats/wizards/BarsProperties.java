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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.core.ats.IStrategy;
import org.eclipsetrader.core.ats.ScriptStrategy;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.ui.internal.ats.SaveAdaptableHelper;
import org.eclipsetrader.ui.internal.ats.TimeSpanDialog;

public class BarsProperties extends PropertyPage implements IWorkbenchPropertyPage {

    private ListViewer viewer;
    private Button add;
    private Button remove;

    private List<TimeSpan> list = new ArrayList<TimeSpan>();

    public BarsProperties() {
        setTitle("Bars");
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

        viewer = new ListViewer(content, SWT.BORDER | SWT.MULTI);
        viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                TimeSpan timeSpan = (TimeSpan) element;
                return NLS.bind("{0} {1}", new Object[] {
                    String.valueOf(timeSpan.getLength()), timeSpan.getUnits()
                });
            }

        });
        viewer.setComparator(new ViewerComparator() {

            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                TimeSpan ts1 = (TimeSpan) e1;
                TimeSpan ts2 = (TimeSpan) e2;
                if (ts1.higherThan(ts2)) {
                    return 1;
                }
                if (ts2.higherThan(ts1)) {
                    return -1;
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
                TimeSpanDialog dlg = new TimeSpanDialog(getShell());
                if (dlg.open() == TimeSpanDialog.OK) {
                    list.add(dlg.getSelection());
                    viewer.refresh();
                }
            }
        });

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

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                remove.setEnabled(!event.getSelection().isEmpty());
            }
        });

        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        IStrategy strategy = (IStrategy) getElement().getAdapter(IStrategy.class);

        list.clear();
        list.addAll(Arrays.asList(strategy.getBarsTimeSpan()));
        viewer.refresh();

        super.performDefaults();
    }

    protected void applyChanges() {
        ScriptStrategy strategy = (ScriptStrategy) getElement().getAdapter(ScriptStrategy.class);
        if (strategy != null) {
            strategy.setBarsTimeSpan(list.toArray(new TimeSpan[list.size()]));
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#isValid()
     */
    @Override
    public boolean isValid() {
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
        SaveAdaptableHelper.save(getElement());
        super.performApply();
    }
}
