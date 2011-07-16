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

package org.eclipsetrader.directa.internal.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.directa.internal.core.WebConnector;

public class WebBrowserView extends ViewPart {

    public static final String VIEW_ID = "org.eclipsetrader.directa.browser"; //$NON-NLS-1$

    private Action stopAction;
    private Action refreshAction;
    private Browser browser;

    private String url;

    public WebBrowserView() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);

        if (memento != null) {
            url = memento.getString("url"); //$NON-NLS-1$
        }

        stopAction = new Action(Messages.WebBrowserView_Stop) {

            @Override
            public void run() {
                browser.stop();
            }
        };
        stopAction.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_NAV_STOP));
        stopAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_NAV_STOP));
        stopAction.setEnabled(false);

        refreshAction = new Action(Messages.WebBrowserView_Refresh) {

            @Override
            public void run() {
                stopAction.setEnabled(true);
                refreshAction.setEnabled(false);
                browser.refresh();
            }
        };
        refreshAction.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_NAV_REFRESH));
        refreshAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_NAV_REFRESH));
        refreshAction.setEnabled(false);

        IToolBarManager toolbarManager = site.getActionBars().getToolBarManager();
        toolbarManager.add(refreshAction);
        toolbarManager.add(stopAction);
        toolbarManager.add(new Separator("additions")); //$NON-NLS-1$

        site.getActionBars().updateActionBars();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
     */
    @Override
    public void saveState(IMemento memento) {
        memento.putString("url", url); //$NON-NLS-1$
        super.saveState(memento);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
        content.setLayout(gridLayout);

        browser = new Browser(content, SWT.NONE);
        browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        browser.addTitleListener(new TitleListener() {

            @Override
            public void changed(TitleEvent event) {
                setPartName(event.title);
                setTitleToolTip(event.title);
            }
        });
        browser.addProgressListener(new ProgressListener() {

            @Override
            public void changed(ProgressEvent event) {
            }

            @Override
            public void completed(ProgressEvent event) {
                stopAction.setEnabled(false);
                refreshAction.setEnabled(true);
            }
        });
        browser.addLocationListener(new LocationListener() {

            @Override
            public void changed(LocationEvent event) {
            }

            @Override
            public void changing(LocationEvent event) {
                stopAction.setEnabled(true);
                refreshAction.setEnabled(false);
            }
        });
        browser.addOpenWindowListener(new OpenWindowListener() {

            @Override
            public void open(WindowEvent event) {
                event.browser = browser;
            }
        });

        setTitleToolTip(getPartName());

        if (url != null) {
            parent.getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    if (!browser.isDisposed()) {
                        setUrl(url);
                    }
                }
            });
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        browser.setFocus();
    }

    public void setUrl(String url) {
        WebConnector connector = WebConnector.getInstance();
        if ("".equals(connector.getUser())) { //$NON-NLS-1$
            connector.login();
        }

        this.url = url;

        String currentUrl = NLS.bind(url, new Object[] {
                new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()), //$NON-NLS-1$
                connector.getUser(),
        });

        browser.setUrl(currentUrl);
    }
}
