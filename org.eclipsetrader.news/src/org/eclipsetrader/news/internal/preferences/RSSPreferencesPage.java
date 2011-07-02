/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.news.internal.preferences;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipsetrader.news.internal.Activator;
import org.eclipsetrader.news.internal.connectors.FeedSource;
import org.eclipsetrader.news.internal.connectors.RSSNewsProvider;

public class RSSPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

    private Spinner interval;
    private CheckboxTableViewer table;
    private Button editButton;
    private Button removeButton;

    private List<FeedSource> list = new ArrayList<FeedSource>();

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
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

        Composite group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
        Label label = new Label(group, SWT.NONE);
        label.setText(Messages.RSSPreferencesPage_AutoUpdate);
        interval = new Spinner(group, SWT.BORDER);
        interval.setMinimum(1);
        interval.setMaximum(9999);
        label = new Label(group, SWT.NONE);
        label.setText(Messages.RSSPreferencesPage_Minutes);

        label = new Label(group, SWT.NONE);
        label.setText(Messages.RSSPreferencesPage_Subscriptions);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
        createViewer(content);

        group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

        Button button = new Button(group, SWT.PUSH);
        button.setText(Messages.RSSPreferencesPage_Add);
        button.setLayoutData(new GridData(80, SWT.DEFAULT));
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                RSSFeedDialog dlg = new RSSFeedDialog(getShell());
                if (dlg.open() == Window.OK) {
                    FeedSource feedSource = dlg.getFeedSource();
                    feedSource.setEnabled(true);
                    list.add(feedSource);
                    table.refresh();
                    table.setChecked(feedSource, feedSource.isEnabled());
                    table.setSelection(new StructuredSelection(feedSource), true);
                }
            }
        });

        editButton = new Button(group, SWT.PUSH);
        editButton.setText(Messages.RSSPreferencesPage_Edit);
        editButton.setEnabled(false);
        editButton.setLayoutData(new GridData(80, SWT.DEFAULT));
        editButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) table.getSelection();
                FeedSource feedSource = (FeedSource) selection.getFirstElement();

                RSSFeedDialog dlg = new RSSFeedDialog(getShell());
                dlg.setFeedSource(feedSource);
                if (dlg.open() == Window.OK) {
                    table.refresh();
                }
            }
        });

        removeButton = new Button(group, SWT.PUSH);
        removeButton.setText(Messages.RSSPreferencesPage_Remove);
        removeButton.setEnabled(false);
        removeButton.setLayoutData(new GridData(80, SWT.DEFAULT));
        removeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) table.getSelection();
                list.removeAll(selection.toList());
                table.remove(selection.toArray());
            }
        });

        performDefaults();

        return content;
    }

    protected void createViewer(Composite parent) {
        table = CheckboxTableViewer.newCheckList(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        table.getTable().setHeaderVisible(false);
        table.getTable().setLinesVisible(false);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = table.getTable().getItemHeight() * 15 + table.getTable().getBorderWidth() * 2;
        table.getControl().setLayoutData(gridData);

        table.setContentProvider(new ArrayContentProvider());
        table.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((FeedSource) element).getName();
            }
        });
        table.setSorter(new ViewerSorter());
        table.setInput(list);

        table.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                editButton.setEnabled(((IStructuredSelection) table.getSelection()).size() == 1);
                removeButton.setEnabled(!table.getSelection().isEmpty());
            }
        });
        table.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                ((FeedSource) event.getElement()).setEnabled(event.getChecked());
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        store.setValue(Activator.PREFS_UPDATE_INTERVAL, interval.getSelection());

        try {
            File file = Activator.getDefault().getStateLocation().append(RSSNewsProvider.HEADLINES_FILE).toFile();
            if (file.exists()) {
                file.delete();
            }

            JAXBContext jaxbContext = JAXBContext.newInstance(FeedSource[].class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setEventHandler(new ValidationEventHandler() {

                @Override
                public boolean handleEvent(ValidationEvent event) {
                    Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                    Activator.getDefault().getLog().log(status);
                    return true;
                }
            });
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$

            JAXBElement<FeedSource[]> element = new JAXBElement<FeedSource[]>(new QName("list"), FeedSource[].class, list.toArray(new FeedSource[list.size()]));
            marshaller.marshal(element, new FileWriter(file));
        } catch (Exception e) {
            Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error saving RSS subscriptions", null); //$NON-NLS-1$
            Activator.getDefault().getLog().log(status);
        }

        return super.performOk();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        interval.setSelection(store.getInt(Activator.PREFS_UPDATE_INTERVAL));

        FeedSource[] sources = getSources();
        list.clear();
        list.addAll(Arrays.asList(sources));
        table.refresh();
        for (int i = 0; i < sources.length; i++) {
            table.setChecked(sources[i], sources[i].isEnabled());
        }

        super.performDefaults();
    }

    protected FeedSource[] getSources() {
        try {
            File file = Activator.getDefault().getStateLocation().append(RSSNewsProvider.HEADLINES_FILE).toFile();
            if (file.exists()) {
                JAXBContext jaxbContext = JAXBContext.newInstance(FeedSource[].class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                unmarshaller.setEventHandler(new ValidationEventHandler() {

                    @Override
                    public boolean handleEvent(ValidationEvent event) {
                        Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                        Activator.getDefault().getLog().log(status);
                        return true;
                    }
                });
                JAXBElement<FeedSource[]> element = unmarshaller.unmarshal(new StreamSource(file), FeedSource[].class);
                return element.getValue();
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error reading RSS subscriptions", null); //$NON-NLS-1$
            Activator.getDefault().getLog().log(status);
        }
        return new FeedSource[0];
    }
}
