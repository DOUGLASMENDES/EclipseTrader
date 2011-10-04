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

package org.eclipsetrader.ui.internal.ats;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipsetrader.core.feed.TimeSpan;

public class BarsPage extends WizardPage {

    private ListViewer viewer;
    private Button add;
    private Button remove;

    private List<TimeSpan> list = new ArrayList<TimeSpan>();

    public BarsPage() {
        super("bars", "Bars", null);
        setDescription("Sets the bars timespan");
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

        list.add(TimeSpan.days(1));

        viewer = new ListViewer(content, SWT.BORDER | SWT.MULTI);
        viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new LabelProvider());
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
        GridLayout gridLayout = new GridLayout(1, false);
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
    }

    public TimeSpan[] getValues() {
        return list.toArray(new TimeSpan[list.size()]);
    }
}
