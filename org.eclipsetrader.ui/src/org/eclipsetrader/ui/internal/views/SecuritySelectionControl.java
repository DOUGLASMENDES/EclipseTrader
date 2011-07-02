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

package org.eclipsetrader.ui.internal.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.ui.internal.UIActivator;

public class SecuritySelectionControl {

    private Composite control;
    private TableViewer available;
    private Button right;
    private Button allRight;
    private Button allLeft;
    private Button left;
    private TableViewer selected;
    private Button up;
    private Button down;
    private Image rightImage = UIActivator.getImageDescriptor("icons/etool16/right.gif").createImage();
    private Image allRightImage = UIActivator.getImageDescriptor("icons/etool16/all-right.gif").createImage();
    private Image allLeftImage = UIActivator.getImageDescriptor("icons/etool16/all-left.gif").createImage();
    private Image leftImage = UIActivator.getImageDescriptor("icons/etool16/left.gif").createImage();
    private Image upImage = UIActivator.getImageDescriptor("icons/etool16/up.gif").createImage();
    private Image downImage = UIActivator.getImageDescriptor("icons/etool16/down.gif").createImage();

    private List<ISecurity> input = new ArrayList<ISecurity>();
    private List<ISecurity> selection = new ArrayList<ISecurity>();

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

    public SecuritySelectionControl(Composite parent) {
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
        label.setText("Available columns");
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        label = new Label(parent, SWT.NONE);
        label.setText("Shown columns");
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    }

    protected void createInputViewer(Composite parent) {
        Composite control = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        control.setLayout(gridLayout);
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        available = new TableViewer(control, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
        available.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ((GridData) available.getControl().getLayoutData()).heightHint = available.getTable().getItemHeight() * 15 + available.getTable().getBorderWidth() * 2;
        available.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((ISecurity) element).getName();
            }
        });
        available.setContentProvider(new ArrayContentProvider());
        available.setSorter(new ViewerSorter());
        available.setInput(input);
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
            public void widgetSelected(SelectionEvent e) {
                if (available.getSelection() instanceof IStructuredSelection && !available.getSelection().isEmpty()) {
                    Object[] s = ((IStructuredSelection) available.getSelection()).toArray();
                    for (int i = 0; i < s.length; i++) {
                        selection.add((ISecurity) s[i]);
                        input.remove(s[i]);
                    }
                    available.refresh();
                    selected.refresh();
                    updateControlsEnablement();
                }
            }
        });

        allRight = new Button(buttons, SWT.PUSH);
        allRight.setImage(allRightImage);
        allRight.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                selection.clear();
                for (ISecurity factory : input) {
                    selection.add(factory);
                }
                input.clear();
                available.refresh();
                selected.refresh();
                updateControlsEnablement();
            }
        });

        allLeft = new Button(buttons, SWT.PUSH);
        allLeft.setImage(allLeftImage);
        allLeft.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                selected.remove(selection.toArray());
                selection.clear();
                updateControlsEnablement();
            }
        });

        left = new Button(buttons, SWT.PUSH);
        left.setImage(leftImage);
        left.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (selected.getSelection() instanceof IStructuredSelection && !selected.getSelection().isEmpty()) {
                    List<?> s = ((IStructuredSelection) selected.getSelection()).toList();
                    selected.remove(s.toArray());
                    selection.removeAll(s);
                    updateControlsEnablement();
                }
            }
        });
    }

    protected void createSelectionViewer(Composite parent) {
        Composite control = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        control.setLayout(gridLayout);
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        selected = new TableViewer(control, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
        selected.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ((GridData) selected.getControl().getLayoutData()).heightHint = selected.getTable().getItemHeight() * 15 + selected.getTable().getBorderWidth() * 2;
        selected.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((ISecurity) element).getName();
            }
        });
        selected.setContentProvider(new ArrayContentProvider());

        selected.setInput(selection);
        selected.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateControlsEnablement();
            }
        });

        createSelectionButtons(control);
    }

    protected void moveSelectionUp(Object[] s) {
        List<ISecurity> l = new ArrayList<ISecurity>();
        int index = 999999;
        for (int i = 0; i < s.length; i++) {
            index = Math.min(index, selection.indexOf(s[i]));
            l.add((ISecurity) s[i]);
        }

        if (index > 0) {
            index--;
            selection.removeAll(l);
            selection.addAll(index, l);
            selected.refresh();
        }
    }

    protected void moveSelectionDown(Object[] s) {
        List<ISecurity> l = new ArrayList<ISecurity>();
        int index = -1;
        for (int i = 0; i < s.length; i++) {
            index = Math.max(index, selection.indexOf(s[i]));
            l.add((ISecurity) s[i]);
        }

        if (index < selection.size() - 1) {
            index++;
            selection.removeAll(l);
            index -= l.size() - 1;
            selection.addAll(index, l);
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

    public void setInput(ISecurity[] factories) {
        input.clear();
        input.addAll(Arrays.asList(factories));
        available.refresh();
        updateControlsEnablement();
    }

    public void setSelectedColumns(ISecurity[] columns) {
        selection.clear();
        for (int i = 0; i < columns.length; i++) {
            selection.add(columns[i]);
        }
        selected.refresh();

        input.removeAll(Arrays.asList(columns));
        available.refresh();

        updateControlsEnablement();
    }

    public ISecurity[] getSelection() {
        return selection.toArray(new ISecurity[selection.size()]);
    }

    protected void updateControlsEnablement() {
        right.setEnabled(!available.getSelection().isEmpty());
        allRight.setEnabled(input.size() != 0);
        left.setEnabled(!selected.getSelection().isEmpty());
        allLeft.setEnabled(selection.size() != 0);

        int upperIndex = -1;
        int lowerIndex = 999999;
        Object[] s = ((IStructuredSelection) selected.getSelection()).toArray();
        for (int i = 0; i < s.length; i++) {
            upperIndex = Math.max(upperIndex, selection.indexOf(s[i]));
            lowerIndex = Math.min(lowerIndex, selection.indexOf(s[i]));
        }

        up.setEnabled(!selected.getSelection().isEmpty() && upperIndex > 0);
        down.setEnabled(!selected.getSelection().isEmpty() && lowerIndex < selection.size() - 1);
    }
}
