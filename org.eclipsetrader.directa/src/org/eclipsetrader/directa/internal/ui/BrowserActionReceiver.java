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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipsetrader.directa.internal.Activator;

/**
 */
public class BrowserActionReceiver implements IWorkbenchWindowActionDelegate, IViewActionDelegate {

    private static String ID_PREFIX = "directa.browsePage."; //$NON-NLS-1$
    private static String[] pages = {
            "http://www.directaworld.it/calendar1.html", //$NON-NLS-1$
            "http://directatrading.com/trading/toptic?USER={1}", // Titoli piu' trattati //$NON-NLS-1$
            "http://directatrading.com/trading/toptic?USER={1}&Tipo=W", // Warrant piu' trattati //$NON-NLS-1$
            "http://directatrading.com/trading/scotic?USER={1}", // Situazione scoperti //$NON-NLS-1$
            "http://directatrading.com/trading/db2www/marder2/input?DATE={0}&USER={1}", //$NON-NLS-1$
            "http://directatrading.com/trading/cambioc?USER={1}", //$NON-NLS-1$
            "http://directatrading.com/trading/estconc7?USER={1}&DATE={0}&TPGE=A00", //$NON-NLS-1$
            "http://directatrading.com/trading/estconc7?DATE=13112004&TPGE=D00&DATE={0}&USER={1}", //$NON-NLS-1$
            "http://directatrading.com/trading/db2www/ectitoli2/input?Tito=XXXXXX&TPGE=A00&DATE={0}&USER={1}", //$NON-NLS-1$
            "http://directatrading.com/trading/db2www/usectitoli/input?Tito=XXXXXX&TPGE=A00&DATE={0}&USER={1}", //$NON-NLS-1$
            "http://directatrading.com/trading/estitc?USER={1}&Tito=X&TPGE=D00&Merca=I", // Titoli Derivati EU //$NON-NLS-1$
            "http://directatrading.com/trading/estitc?USER={1}&Tito=X&TPGE=D00&Merca=U", // Titoli Derivati US //$NON-NLS-1$
            "http://directatrading.com/trading/db2www/messaggi2/input?TipDett=E&DATE={0}&USER={1}", //$NON-NLS-1$
            "http://directatrading.com/trading/pergic?USER={1}", // Performance Giornaliera //$NON-NLS-1$
            "http://directatrading.com/trading/capgac2?DATE={0}&USER={1}", //$NON-NLS-1$
            "http://directatrading.com/trading/db2www/perforder/input?DATE={0}&USER={1}", //$NON-NLS-1$
            "http://directatrading.com/trading/db2www/divesteri/input?DATE={0}&USER={1}", //$NON-NLS-1$
            "http://directatrading.com/trading/db2www/Sitprest/report?DATE={0}&USER={1}", //$NON-NLS-1$
            "http://notizie.directatrading.com/notizie/asca-ansa.php", //$NON-NLS-1$
            "http://www.affaritaliani.it/directa_oraperora.htm", //$NON-NLS-1$
            "http://www.tradingweek.net/index.php?c=dati_macro", //$NON-NLS-1$
            "http://directatrading.com/trading/permec?USER={1}", // Performance Mensile //$NON-NLS-1$
    };

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    @Override
    public void dispose() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    public void init(IWorkbenchWindow window) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        try {
            IWorkbenchPage pg = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            if (pg != null) {
                IViewPart browser = pg.showView(WebBrowserView.VIEW_ID);
                if (browser != null) {
                    int index = Integer.parseInt(action.getId().substring(ID_PREFIX.length()));
                    ((WebBrowserView) browser).setUrl(pages[index]);
                }
            }
        } catch (PartInitException e) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error opening browser", e); //$NON-NLS-1$
            Activator.log(status);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }
}
