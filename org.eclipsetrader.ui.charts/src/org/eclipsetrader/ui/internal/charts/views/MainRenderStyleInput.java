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

package org.eclipsetrader.ui.internal.charts.views;

import java.util.Observable;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class MainRenderStyleInput extends Observable {

    private Label label;
    private ComboViewer combo;

    public MainRenderStyleInput(Composite parent) {
        this(parent, (String) null);
    }

    public MainRenderStyleInput(Composite parent, String text) {
        if (text != null) {
            label = new Label(parent, SWT.NONE);
            label.setText(text);
        }
        initializeCombo(parent);
    }

    protected void initializeCombo(Composite parent) {
        combo = new ComboViewer(parent, SWT.READ_ONLY);
        combo.setLabelProvider(new LabelProvider());
        combo.setContentProvider(new ArrayContentProvider());
        combo.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                setChanged();
                notifyObservers(event.getSelection());
            }
        });
        combo.setInput(MainRenderStyle.values());
    }

    public void setSelection(MainRenderStyle selection) {
        combo.setSelection(selection != null ? new StructuredSelection(selection) : StructuredSelection.EMPTY);
    }

    public MainRenderStyle getSelection() {
        IStructuredSelection selection = (IStructuredSelection) combo.getSelection();
        return selection.isEmpty() ? null : (MainRenderStyle) selection.getFirstElement();
    }

    public String getText() {
        return label != null ? label.getText() : "";
    }

    public void setText(String text) {
        if (label != null) {
            label.setText(text);
        }
    }

    public Label getLabel() {
        return label;
    }

    public ComboViewer getViewer() {
        return combo;
    }

    public Combo getCombo() {
        return combo.getCombo();
    }

    public void setEnabled(boolean enabled) {
        if (label != null) {
            label.setEnabled(enabled);
        }
        combo.getControl().setEnabled(enabled);
    }

    public boolean getEnabled() {
        return combo.getControl().getEnabled();
    }

    public boolean isEnabled() {
        return combo.getControl().isEnabled();
    }
}
