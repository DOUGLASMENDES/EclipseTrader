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

import java.util.Arrays;
import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.feed.FeedSource;
import net.sourceforge.eclipsetrader.core.ui.preferences.FeedOptions;
import net.sourceforge.eclipsetrader.core.ui.preferences.IntradayDataOptions;
import net.sourceforge.eclipsetrader.core.ui.preferences.TradeSourceOptions;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 */
public class SecurityWizard extends Wizard
{
    public static String WINDOW_TITLE = Messages.SecurityWizard_Title;
    private SecurityPage securityPage;
    private FeedOptions quoteFeedOptions = new FeedOptions("quote"); //$NON-NLS-1$
    private FeedOptions level2FeedOptions = new FeedOptions("level2"); //$NON-NLS-1$
    private FeedOptions historyFeedOptions = new FeedOptions("history"); //$NON-NLS-1$
    private TradeSourceOptions tradeSourceOptions = new TradeSourceOptions();
    private IntradayDataOptions intradayOptions = new IntradayDataOptions();

    public SecurityWizard()
    {
    }

    public void open()
    {
        setWindowTitle(WINDOW_TITLE);
        WizardDialog dlg = create();
        dlg.open();
    }
    
    public WizardDialog create()
    {
        securityPage = new SecurityPage();
        addPage(securityPage);

        WizardPage page = new WizardPage("") { //$NON-NLS-1$
            public void createControl(Composite parent)
            {
                setControl(quoteFeedOptions.createControls(parent));
            }
        };
        page.setTitle(Messages.SecurityWizard_QuoteFeedTitle);
        page.setDescription(Messages.SecurityWizard_QuoteFeedDescription);
        page.setPageComplete(true);
        addPage(page);
        
        page = new WizardPage("") { //$NON-NLS-1$
            public void createControl(Composite parent)
            {
                setControl(level2FeedOptions.createControls(parent));
            }
        };
        page.setTitle(Messages.SecurityWizard_Level2FeedTitle);
        page.setDescription(Messages.SecurityWizard_Level2FeedDescription);
        page.setPageComplete(true);
        addPage(page);
        
        page = new WizardPage("") { //$NON-NLS-1$
            public void createControl(Composite parent)
            {
                setControl(historyFeedOptions.createControls(parent));
            }
        };
        page.setTitle(Messages.SecurityWizard_HistoryFeedTitle);
        page.setDescription(Messages.SecurityWizard_HistoryFeedDescription);
        page.setPageComplete(true);
        addPage(page);
        
        page = new WizardPage("") { //$NON-NLS-1$
            public void createControl(Composite parent)
            {
                setControl(tradeSourceOptions.createControls(parent, null));
            }
        };
        page.setTitle(Messages.SecurityWizard_TradeSourceTitle);
        page.setDescription(Messages.SecurityWizard_TradeSourceDescription);
        addPage(page);
        
        page = new WizardPage("") { //$NON-NLS-1$
            public void createControl(Composite parent)
            {
                setControl(intradayOptions.createControls(parent, null));
            }
        };
        page.setTitle(Messages.SecurityWizard_IntradayChartsTitle);
        page.setDescription(Messages.SecurityWizard_IntradayChartsDescription);
        addPage(page);

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
        Security security = new Security();
        
        security.setCode(securityPage.getCode());
        security.setDescription(securityPage.getSecurityDescription());
        security.setCurrency(securityPage.getCurrency());

        security.setQuoteFeed(quoteFeedOptions.getFeed());
        security.setLevel2Feed(level2FeedOptions.getFeed());
        security.setHistoryFeed(historyFeedOptions.getFeed());
        
        security.setTradeSource(tradeSourceOptions.getSource());
        intradayOptions.saveSettings(security);
        
        CorePlugin.getRepository().save(security);
        return true;
    }

    static void updateFeedExchanges(String type, Combo combo, FeedSource feed)
    {
        combo.removeAll();
        combo.add(""); //$NON-NLS-1$

        if (feed != null)
        {
            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.FEED_EXTENSION_POINT);
            if (extensionPoint != null)
            {
                java.util.List plugins = Arrays.asList(extensionPoint.getConfigurationElements());
                for (Iterator iter = plugins.iterator(); iter.hasNext(); )
                {
                    IConfigurationElement element = (IConfigurationElement)iter.next();
                    if (!element.getAttribute("id").equals(feed.getId())) //$NON-NLS-1$
                        continue;
                    
                    IConfigurationElement[] children = element.getChildren();
                    for (int i = 0; i < children.length; i++)
                    {
                        if (children[i].getName().equals(type)) //$NON-NLS-1$
                        {
                            IConfigurationElement[] exchanges = children[i].getChildren();
                            for (int x = 0; x < exchanges.length; x++)
                            {
                                if (exchanges[x].getName().equals("exchange")) //$NON-NLS-1$
                                {
                                    String id = exchanges[x].getAttribute("id"); //$NON-NLS-1$
                                    combo.setData(String.valueOf(combo.getItemCount()), id); //$NON-NLS-1$
                                    combo.add(exchanges[x].getAttribute("name")); //$NON-NLS-1$
                                    if (id.equals(feed.getExchange()))
                                        combo.select(combo.getItemCount() - 1);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        combo.setEnabled(combo.getItemCount() > 1);
    }

    static void updateFeedExchanges(String type, Combo combo, String feed)
    {
        combo.removeAll();
        combo.add(""); //$NON-NLS-1$

        if (feed != null)
        {
            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.FEED_EXTENSION_POINT);
            if (extensionPoint != null)
            {
                java.util.List plugins = Arrays.asList(extensionPoint.getConfigurationElements());
                for (Iterator iter = plugins.iterator(); iter.hasNext(); )
                {
                    IConfigurationElement element = (IConfigurationElement)iter.next();
                    if (!element.getAttribute("id").equals(feed)) //$NON-NLS-1$
                        continue;
                    
                    IConfigurationElement[] children = element.getChildren();
                    for (int i = 0; i < children.length; i++)
                    {
                        if (children[i].getName().equals(type)) //$NON-NLS-1$
                        {
                            IConfigurationElement[] exchanges = children[i].getChildren();
                            for (int x = 0; x < exchanges.length; x++)
                            {
                                if (exchanges[x].getName().equals("exchange")) //$NON-NLS-1$
                                {
                                    String id = exchanges[x].getAttribute("id"); //$NON-NLS-1$
                                    combo.setData(String.valueOf(combo.getItemCount()), id); //$NON-NLS-1$
                                    combo.add(exchanges[x].getAttribute("name")); //$NON-NLS-1$
                                }
                            }
                        }
                    }
                }
            }
        }
        
        combo.setEnabled(combo.getItemCount() > 1);
    }
}
