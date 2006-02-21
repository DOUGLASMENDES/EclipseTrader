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

package net.sourceforge.eclipsetrader.core.ui.wizards;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.Security.Feed;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 */
public class SecurityWizard extends Wizard implements INewWizard
{
    public static String WINDOW_TITLE = "New Security Wizard";
    public static String EDIT_WINDOW_TITLE = "Security Edit Wizard";
    private Security security;
    private SecurityPage securityPage;
    private QuoteFeedPage quoteFeedPage;
    private HistoryFeedPage historyFeedPage;

    public SecurityWizard()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection)
    {
        setWindowTitle(WINDOW_TITLE);
        
        securityPage = new SecurityPage(security);
        addPage(securityPage);
        quoteFeedPage = new QuoteFeedPage(security);
        addPage(quoteFeedPage);
        historyFeedPage = new HistoryFeedPage(security);
        addPage(historyFeedPage);
    }

    public Security open()
    {
        setWindowTitle(WINDOW_TITLE);
        WizardDialog dlg = create(null);
        dlg.open();
        return this.security;
    }
    
    public Security open(Security security)
    {
        setWindowTitle(EDIT_WINDOW_TITLE);
        WizardDialog dlg = create(security);
        dlg.open();
        return this.security;
    }
    
    public WizardDialog create(Security security)
    {
        this.security = security;
        
        securityPage = new SecurityPage(security);
        addPage(securityPage);
        quoteFeedPage = new QuoteFeedPage(security);
        addPage(quoteFeedPage);
        historyFeedPage = new HistoryFeedPage(security);
        addPage(historyFeedPage);

        WizardDialog dlg = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), this);
        dlg.create();
        
        return dlg;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#canFinish()
     */
    public boolean canFinish()
    {
        return securityPage.isPageComplete();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish()
    {
        if (security == null)
            security = new Security();
        
        security.setCode(securityPage.getCode());
        security.setDescription(securityPage.getSecurityDescription());
        security.setCurrency(securityPage.getCurrency());
        
        if (quoteFeedPage.getId() != null)
        {
            Feed feed = security.new Feed();
            feed.setId(quoteFeedPage.getId());
            feed.setSymbol(quoteFeedPage.getSymbol());
            security.setQuoteFeed(feed);
        }
        else
            security.setQuoteFeed(null);
        
        if (quoteFeedPage.getLevel2Id() != null)
        {
            Feed feed = security.new Feed();
            feed.setId(quoteFeedPage.getLevel2Id());
            feed.setSymbol(quoteFeedPage.getLevel2Symbol());
            security.setLevel2Feed(feed);
        }
        else
            security.setLevel2Feed(null);
        
        if (historyFeedPage.getId() != null)
        {
            Feed feed = security.new Feed();
            feed.setId(historyFeedPage.getId());
            feed.setSymbol(historyFeedPage.getSymbol());
            security.setHistoryFeed(feed);
        }
        else
            security.setHistoryFeed(null);
        
        CorePlugin.getRepository().save(security);
        return true;
    }
}
