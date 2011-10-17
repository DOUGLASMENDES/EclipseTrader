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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipsetrader.ui.internal.UIActivator;

public class ConfigurationElementsViewer {

    private Composite control;
    private TableViewer available;
    private Button right;
    private Button allRight;
    private Button allLeft;
    private Button left;
    private TableViewer selected;
    private Button up;
    private Button down;
    private Image rightImage = UIActivator.getImageDescriptor("icons/etool16/right.gif").createImage(); //$NON-NLS-1$
    private Image allRightImage = UIActivator.getImageDescriptor("icons/etool16/all-right.gif").createImage(); //$NON-NLS-1$
    private Image allLeftImage = UIActivator.getImageDescriptor("icons/etool16/all-left.gif").createImage(); //$NON-NLS-1$
    private Image leftImage = UIActivator.getImageDescriptor("icons/etool16/left.gif").createImage(); //$NON-NLS-1$
    private Image upImage = UIActivator.getImageDescriptor("icons/etool16/up.gif").createImage(); //$NON-NLS-1$
    private Image downImage = UIActivator.getImageDescriptor("icons/etool16/down.gif").createImage(); //$NON-NLS-1$

    private List<IConfigurationElement> availableList = new ArrayList<IConfigurationElement>();
    private List<IConfigurationElement> selectedList = new ArrayList<IConfigurationElement>();

    private DisposeListener disposeListener = new DisposeListener() {

        @Override
        public void widgetDisposed(DisposeEvent e) {
            if (rightImage != null) {
                rightImage.dispose();
            }
            if (allRightImage != null) {
                allRightImage.dispose();
            }
            if (allLeftImage != null) {
                allLeftImage.dispose();
            }
            if (leftImage != null) {
                leftImage.dispose();
            }
            if (upImage != null) {
                upImage.dispose();
            }
            if (downImage != null) {
                downImage.dispose();
            }
        }
    };

    public ConfigurationElementsViewer(Composite parent) {
        control = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, true);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        control.setLayout(gridLayout);

        createLabels(control);
        createInputViewer(control);
        createSelectionViewer(control);

        control.addDisposeListener(disposeListener);
        updateControlsEnablement();
    }

    public Composite getControl() {
        return control;
    }

    protected void createLabels(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Messages.ConfigurationElementsViewer_AvailableColumnsLabel);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        label = new Label(parent, SWT.NONE);
        label.setText(Messages.ConfigurationElementsViewer_ShownColumnsLabel);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    }

    protected void createInputViewer(Composite parent) {
        Composite control = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        control.setLayout(gridLayout);
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ((GridData) control.getLayoutData()).widthHint = 200;

        available = new TableViewer(control, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
        available.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ((GridData) available.getControl().getLayoutData()).heightHint = available.getTable().getItemHeight() * 15 + available.getTable().getBorderWidth() * 2;
        available.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((IConfigurationElement) element).getAttribute("name"); //$NON-NLS-1$
            }
        });
        available.setContentProvider(new ArrayContentProvider());
        available.setSorter(new ViewerSorter());
        available.setInput(availableList);
        available.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateControlsEnablement();
            }
        });

        createInputButtons(control);
    }

    protected void createInputButtons(Composite parent) {
        Composite buttons = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        buttons.setLayout(gridLayout);
        buttons.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

        right = new Button(buttons, SWT.PUSH);
        right.setImage(rightImage);
        right.addSelectionListener(new SelectionAdapter() {

            @Override
            @SuppressWarnings("unchecked")
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) available.getSelection();

                selectedList.addAll(selection.toList());
                selected.refresh();

                availableList.removeAll(selection.toList());
                available.refresh();

                updateControlsEnablement();
            }
        });

        allRight = new Button(buttons, SWT.PUSH);
        allRight.setImage(allRightImage);
        allRight.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedList.addAll(availableList);
                selected.refresh();

                availableList.clear();
                available.refresh();

                updateControlsEnablement();
            }
        });

        allLeft = new Button(buttons, SWT.PUSH);
        allLeft.setImage(allLeftImage);
        allLeft.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                availableList.addAll(selectedList);
                available.refresh();

                selectedList.clear();
                selected.refresh();

                updateControlsEnablement();
            }
        });

        left = new Button(buttons, SWT.PUSH);
        left.setImage(leftImage);
        left.addSelectionListener(new SelectionAdapter() {

            @Override
            @SuppressWarnings("unchecked")
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) selected.getSelection();

                selectedList.removeAll(selection.toList());
                selected.refresh();

                availableList.addAll(selection.toList());
                available.refresh();

                updateControlsEnablement();
            }
        });
    }

    protected void createSelectionViewer(Composite parent) {
        Composite control = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        control.setLayout(gridLayout);
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ((GridData) control.getLayoutData()).widthHint = 200;

        selected = new TableViewer(control, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
        selected.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ((GridData) selected.getControl().getLayoutData()).heightHint = selected.getTable().getItemHeight() * 15 + selected.getTable().getBorderWidth() * 2;
        selected.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((IConfigurationElement) element).getAttribute("name"); //$NON-NLS-1$
            }
        });
        selected.setContentProvider(new ArrayContentProvider());

        selected.setInput(selectedList);
        selected.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateControlsEnablement();
            }
        });

        createSelectionButtons(control);
    }

    protected void moveSelectionUp(Object[] s) {
        List<IConfigurationElement> l = new ArrayList<IConfigurationElement>();
        int index = 999999;
        for (int i = 0; i < s.length; i++) {
            index = Math.min(index, selectedList.indexOf(s[i]));
            l.add((IConfigurationElement) s[i]);
        }

        if (index > 0) {
            index--;
            selectedList.removeAll(l);
            selectedList.addAll(index, l);
            selected.refresh();
        }
    }

    protected void moveSelectionDown(Object[] s) {
        List<IConfigurationElement> l = new ArrayList<IConfigurationElement>();
        int index = -1;
        for (int i = 0; i < s.length; i++) {
            index = Math.max(index, selectedList.indexOf(s[i]));
            l.add((IConfigurationElement) s[i]);
        }

        if (index < selectedList.size() - 1) {
            index++;
            selectedList.removeAll(l);
            index -= l.size() - 1;
            selectedList.addAll(index, l);
            selected.refresh();
        }
    }

    protected void createSelectionButtons(Composite parent) {
        Composite buttons = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        buttons.setLayout(gridLayout);
        buttons.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

        up = new Button(buttons, SWT.PUSH);
        up.setImage(upImage);
        up.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!selected.getSelection().isEmpty()) {
                    Object[] s = ((IStructuredSelection) selected.getSelection()).toArray();
                    moveSelectionUp(s);
                    updateControlsEnablement();
                }
            }
        });

        down = new Button(buttons, SWT.PUSH);
        down.setImage(downImage);
        down.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!selected.getSelection().isEmpty()) {
                    Object[] s = ((IStructuredSelection) selected.getSelection()).toArray();
                    moveSelectionDown(s);
                    updateControlsEnablement();
                }
            }
        });
    }

    public void setAvailableElements(IConfigurationElement[] factories) {
        availableList.clear();
        availableList.addAll(Arrays.asList(factories));
        availableList.removeAll(selectedList);
        available.refresh();
        updateControlsEnablement();
    }

    public void setSelectedElements(IConfigurationElement[] columns) {
        selectedList.clear();
        selectedList.addAll(Arrays.asList(columns));
        selected.refresh();

        availableList.removeAll(selectedList);
        available.refresh();

        updateControlsEnablement();
    }

    public IConfigurationElement[] getSelectedElements() {
        return selectedList.toArray(new IConfigurationElement[selectedList.size()]);
    }

    protected void updateControlsEnablement() {
        right.setEnabled(!available.getSelection().isEmpty());
        allRight.setEnabled(availableList.size() != 0);
        left.setEnabled(!selected.getSelection().isEmpty());
        allLeft.setEnabled(selectedList.size() != 0);

        int upperIndex = -1;
        int lowerIndex = 999999;
        Object[] s = ((IStructuredSelection) selected.getSelection()).toArray();
        for (int i = 0; i < s.length; i++) {
            upperIndex = Math.max(upperIndex, selectedList.indexOf(s[i]));
            lowerIndex = Math.min(lowerIndex, selectedList.indexOf(s[i]));
        }

        up.setEnabled(!selected.getSelection().isEmpty() && upperIndex > 0);
        down.setEnabled(!selected.getSelection().isEmpty() && lowerIndex < selectedList.size() - 1);
    }
}
