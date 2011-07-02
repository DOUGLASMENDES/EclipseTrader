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

package org.eclipsetrader.news.internal.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipsetrader.news.core.IHeadLine;
import org.eclipsetrader.news.internal.repository.HeadLine;

public class NewsViewer extends ViewPart {

    public static final String VIEW_ID = "org.eclipsetrader.news.browser";

    private Action stopAction;
    private Action refreshAction;
    private Browser browser;

    private IHeadLine headLine;

    public NewsViewer() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);

        if (memento != null) {
            String title = memento.getString("title");
            String url = memento.getString("url");
            if (url != null) {
                headLine = new HeadLine(null, null, title != null ? title : "", null, url);
            }
        }

        stopAction = new Action("Stop") {

            @Override
            public void run() {
                browser.stop();
            }
        };
        stopAction.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_NAV_STOP));
        stopAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_NAV_STOP));
        stopAction.setEnabled(false);

        refreshAction = new Action("Refresh") {

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
        toolbarManager.add(new Separator("additions"));

        site.getActionBars().updateActionBars();

        site.setSelectionProvider(new SelectionProvider());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
     */
    @Override
    public void saveState(IMemento memento) {
        super.saveState(memento);

        if (headLine != null) {
            memento.putString("title", headLine.getText());
            memento.putString("url", headLine.getLink());
        }
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

        if (headLine != null) {
            setHeadLine(headLine);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        browser.setFocus();
    }

    public IHeadLine getHeadLine() {
        return headLine;
    }

    public void setHeadLine(IHeadLine headLine) {
        this.headLine = headLine;
        browser.setUrl(headLine.getLink());
        setPartName(headLine.getText());
        getSite().getSelectionProvider().setSelection(new StructuredSelection(headLine));
    }
}
