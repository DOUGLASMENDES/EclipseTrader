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

package org.eclipsetrader.ui.internal.charts;

import java.util.Observable;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipsetrader.ui.charts.OHLCField;

public class OHLCFieldInput extends Observable {
	private Label label;
	private ComboViewer combo;

	public OHLCFieldInput(Composite parent) {
		this(parent, (String) null);
	}

	public OHLCFieldInput(Composite parent, String text) {
		if (text != null) {
			label = new Label(parent, SWT.NONE);
			label.setText(text);
		}
		initializeCombo(parent);
	}

	public OHLCFieldInput(Composite parent, Label label) {
		this.label = label;
		initializeCombo(parent);
	}

	protected void initializeCombo(Composite parent) {
		combo = new ComboViewer(parent, SWT.READ_ONLY);
		combo.setLabelProvider(new LabelProvider());
		combo.setContentProvider(new ArrayContentProvider());
		combo.setSorter(new ViewerSorter());
		combo.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
            	setChanged();
            	notifyObservers(event.getSelection());
            }
		});
		combo.setInput(OHLCField.values());
	}

	public void setSelection(OHLCField selection) {
		combo.setSelection(selection != null ? new StructuredSelection(selection) : StructuredSelection.EMPTY);
	}

	public OHLCField getSelection() {
		IStructuredSelection selection = (IStructuredSelection) combo.getSelection();
		return selection.isEmpty() ? null : (OHLCField) selection.getFirstElement();
	}

	public String getText() {
		return label != null ? label.getText() : "";
	}

	public void setText(String text) {
		if (label != null)
			label.setText(text);
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
		if (label != null)
			label.setEnabled(enabled);
		combo.getControl().setEnabled(enabled);
	}

	public boolean getEnabled() {
		return combo.getControl().getEnabled();
	}

	public boolean isEnabled() {
		return combo.getControl().isEnabled();
	}
}
