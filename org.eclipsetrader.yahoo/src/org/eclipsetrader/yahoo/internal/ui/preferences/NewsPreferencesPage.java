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

package org.eclipsetrader.yahoo.internal.ui.preferences;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipsetrader.yahoo.internal.YahooActivator;
import org.eclipsetrader.yahoo.internal.news.Category;
import org.eclipsetrader.yahoo.internal.news.Page;

public class NewsPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

    private Spinner interval;
    private Spinner hoursAsRecent;
    private Button updateNews;
    private CheckboxTreeViewer providers;

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
        gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
        Label label = new Label(group, SWT.NONE);
        label.setText("Update every");
        interval = new Spinner(group, SWT.BORDER);
        interval.setMinimum(1);
        interval.setMaximum(9999);
        label = new Label(group, SWT.NONE);
        label.setText("minute(s)");

        group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
        label = new Label(group, SWT.NONE);
        label.setText("Consider a news as recent for");
        hoursAsRecent = new Spinner(group, SWT.BORDER);
        hoursAsRecent.setMinimum(0);
        hoursAsRecent.setMaximum(9999);
        label = new Label(group, SWT.NONE);
        label.setText("hour(s)");

        updateNews = new Button(content, SWT.CHECK);
        updateNews.setText("Update security news");

        label = new Label(content, SWT.NONE);
        label.setText("Subscriptions:");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));

        providers = new CheckboxTreeViewer(content, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        gridData.heightHint = providers.getTree().getItemHeight() * 15 + providers.getTree().getBorderWidth() * 2;
        providers.getControl().setLayoutData(gridData);
        providers.setContentProvider(new NewsContentProvider());
        providers.setLabelProvider(new LabelProvider());
        providers.setSorter(new ViewerSorter());
        providers.setUseHashlookup(true);

        providers.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (event.getElement() instanceof Category) {
                    Page[] pages = ((Category) event.getElement()).getPages();
                    for (int i = 0; i < pages.length; i++) {
                        providers.setChecked(pages[i], event.getChecked());
                    }
                }
                else if (event.getElement() instanceof Page) {
                    Category category = ((Page) event.getElement()).getParent();
                    boolean allChecked = true;
                    boolean someChecked = false;
                    for (Page page : category.getPages()) {
                        boolean checked = providers.getChecked(page);
                        someChecked = someChecked || checked;
                        allChecked = allChecked && checked;
                    }
                    providers.setChecked(category, someChecked);
                    providers.setGrayed(category, !allChecked);
                }
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
        IPreferenceStore store = YahooActivator.getDefault().getPreferenceStore();

        interval.setSelection(store.getInt(YahooActivator.PREFS_NEWS_UPDATE_INTERVAL));
        hoursAsRecent.setSelection(store.getInt(YahooActivator.PREFS_HOURS_AS_RECENT));
        updateNews.setSelection(store.getBoolean(YahooActivator.PREFS_UPDATE_SECURITIES_NEWS));

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Category[].class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            InputStream inputStream = FileLocator.openStream(YahooActivator.getDefault().getBundle(), new Path("data/news_feeds.xml"), false);
            JAXBElement<Category[]> element = unmarshaller.unmarshal(new StreamSource(inputStream), Category[].class);

            providers.setInput(element.getValue());
            providers.expandAll();

            for (Category category : element.getValue()) {
                boolean allChecked = true;
                boolean someChecked = false;
                for (Page page : category.getPages()) {
                    boolean checked = store.getBoolean(YahooActivator.PREFS_SUBSCRIBE_PREFIX + page.getId());
                    someChecked = someChecked || checked;
                    allChecked = allChecked && checked;
                    providers.setChecked(page, checked);
                }
                providers.setChecked(category, someChecked);
                providers.setGrayed(category, !allChecked);
            }

        } catch (Exception e) {
            Status status = new Status(IStatus.WARNING, YahooActivator.PLUGIN_ID, 0, "Error reading feed sources", e); //$NON-NLS-1$
            YahooActivator.getDefault().getLog().log(status);
        }

        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        IPreferenceStore store = YahooActivator.getDefault().getPreferenceStore();

        store.setValue(YahooActivator.PREFS_NEWS_UPDATE_INTERVAL, interval.getSelection());
        store.setValue(YahooActivator.PREFS_HOURS_AS_RECENT, hoursAsRecent.getSelection());
        store.setValue(YahooActivator.PREFS_UPDATE_SECURITIES_NEWS, updateNews.getSelection());

        Category[] input = (Category[]) providers.getInput();
        for (Category category : input) {
            for (Page page : category.getPages()) {
                store.setValue(YahooActivator.PREFS_SUBSCRIBE_PREFIX + page.getId(), providers.getChecked(page));
            }
        }

        return super.performOk();
    }
}
