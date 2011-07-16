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

package org.eclipsetrader.borsaitalia.internal.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipsetrader.borsaitalia.internal.Activator;

public class InstrumentsPage extends WizardPage {

    private CheckboxTableViewer instruments;

    private List<Instrument> instrumentList;

    public InstrumentsPage() {
        super("instrument", Messages.InstrumentsPage_Name, null); //$NON-NLS-1$
        setDescription(Messages.InstrumentsPage_Description);
        setPageComplete(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        content.setLayout(new GridLayout(1, false));
        setControl(content);
        initializeDialogUnits(content);

        Label label = new Label(content, SWT.NONE);
        label.setText(Messages.InstrumentsPage_Instruments);

        instruments = CheckboxTableViewer.newCheckList(content, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        instruments.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ((GridData) instruments.getControl().getLayoutData()).heightHint = instruments.getTable().getItemHeight() * 15 + instruments.getTable().getBorderWidth() * 2;
        instruments.setLabelProvider(new LabelProvider());
        instruments.setContentProvider(new ArrayContentProvider());
        instruments.setSorter(new ViewerSorter());
        instruments.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                setPageComplete(instruments.getCheckedElements().length != 0);
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible && instrumentList == null) {
            Display.getCurrent().asyncExec(new Runnable() {

                @Override
                public void run() {
                    init();
                }
            });
        }
    }

    protected void init() {
        try {
            File file = Activator.getDefault().getStateLocation().append("instruments.xml").toFile(); //$NON-NLS-1$
            if (!file.exists()) {
                file = new File(FileLocator.getBundleFile(Activator.getDefault().getBundle()), "data/instruments.xml"); //$NON-NLS-1$
            }
            if (file.exists() == true) {
                try {
                    JAXBContext jaxbContext = JAXBContext.newInstance(Instrument[].class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    unmarshaller.setEventHandler(new ValidationEventHandler() {

                        @Override
                        public boolean handleEvent(ValidationEvent event) {
                            Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                            Activator.log(status);
                            return true;
                        }
                    });
                    JAXBElement<Instrument[]> element = unmarshaller.unmarshal(new StreamSource(file), Instrument[].class);
                    instrumentList = new ArrayList<Instrument>(Arrays.asList(element.getValue()));
                } catch (Exception e) {
                    Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error loading exchanges from " + file, e); //$NON-NLS-1$
                    Activator.log(status);
                }
            }

            if (instrumentList != null) {
                try {
                    if (!instruments.getControl().isDisposed()) {
                        instruments.getControl().getDisplay().asyncExec(new Runnable() {

                            @Override
                            public void run() {
                                if (!instruments.getControl().isDisposed()) {
                                    instruments.getControl().setRedraw(false);
                                    instruments.setInput(instrumentList);
                                    instruments.getControl().setRedraw(true);
                                    instruments.getControl().setEnabled(true);
                                    setPageComplete(false);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    // Do nothing
                }
                return;
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error loading instruments", e); //$NON-NLS-1$
            Activator.log(status);
        }
    }

    public Instrument[] getInstruments() {
        Object[] o = instruments.getCheckedElements();
        Instrument[] i = new Instrument[o.length];
        System.arraycopy(o, 0, i, 0, i.length);
        return i;
    }
}
