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

package net.sourceforge.eclipsetrader.core.ui.preferences;

import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.CurrencyConverter;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.ui.internal.Messages;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SecurityPropertiesDialog extends PreferenceDialog
{
    private Security security;

    public SecurityPropertiesDialog(Security security, Shell parentShell)
    {
        super(parentShell, new PreferenceManager());
        this.security = security;
        
        getPreferenceManager().addToRoot(new PreferenceNode("general", new GeneralPage())); //$NON-NLS-1$

        PreferenceNode node = new PreferenceNode("feed", new PreferencePage("Feeds") { //$NON-NLS-1$ //$NON-NLS-2$
            protected Control createContents(Composite parent)
            {
                noDefaultAndApplyButton();
                return new Composite(parent, SWT.NONE);
            }
        });
        getPreferenceManager().addToRoot(node);
        node.add(new PreferenceNode("quote", new QuoteFeedPage())); //$NON-NLS-1$
        node.add(new PreferenceNode("level2", new Level2FeedPage())); //$NON-NLS-1$
        node.add(new PreferenceNode("history", new HistoryFeedPage())); //$NON-NLS-1$
        
        getPreferenceManager().addToRoot(new PreferenceNode("intraday", new DataCollectorPage())); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(Messages.SecurityPropertiesDialog_Title + security.getDescription());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#handleSave()
     */
    protected void handleSave()
    {
        super.handleSave();
        CorePlugin.getRepository().save(security);
    }

    private class GeneralPage extends PreferencePage
    {
        private Text code;
        private Text securityDescription;
        private Combo currency;
        private Button clearHistory;
        private Button clearIntraday;

        public GeneralPage()
        {
            super(Messages.SecurityPropertiesDialog_GeneralPage);
            noDefaultAndApplyButton();
            setValid(false);
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
         */
        protected Control createContents(Composite parent)
        {
            Composite content = new Composite(parent, SWT.NONE);
            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.marginWidth = gridLayout.marginHeight = 0;
            content.setLayout(gridLayout);
            content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            Label label = new Label(content, SWT.NONE);
            label.setText(Messages.SecurityPropertiesDialog_Code);
            label.setLayoutData(new GridData(125, SWT.DEFAULT));
            code = new Text(content, SWT.BORDER);
            code.setLayoutData(new GridData(100, SWT.DEFAULT));
            code.setText(security.getCode());
            code.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e)
                {
                }
            });

            label = new Label(content, SWT.NONE);
            label.setText(Messages.SecurityPropertiesDialog_Description);
            label.setLayoutData(new GridData(125, SWT.DEFAULT));
            securityDescription = new Text(content, SWT.BORDER);
            securityDescription.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
            securityDescription.setText(security.getDescription());
            securityDescription.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e)
                {
                }
            });

            label = new Label(content, SWT.NONE);
            label.setText(Messages.SecurityPropertiesDialog_Currency);
            label.setLayoutData(new GridData(125, SWT.DEFAULT));
            currency = new Combo(content, SWT.SINGLE | SWT.READ_ONLY);
            currency.setVisibleItemCount(10);
            currency.add(""); //$NON-NLS-1$
            
            label = new Label(content, SWT.NONE);
            label.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, true, 2, 1));

            clearHistory = new Button(content, SWT.CHECK);
            clearHistory.setText(Messages.SecurityPropertiesDialog_ClearHistory);
            clearHistory.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));

            clearIntraday = new Button(content, SWT.CHECK);
            clearIntraday.setText(Messages.SecurityPropertiesDialog_ClearIntraday);
            clearIntraday.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));

            List list = CurrencyConverter.getInstance().getCurrencies();
            Collections.sort(list, new Comparator() {
                public int compare(Object arg0, Object arg1)
                {
                    return ((String)arg0).compareTo((String)arg1);
                }
            });
            for (Iterator iter = list.iterator(); iter.hasNext(); )
                currency.add((String)iter.next());
            if (security.getCurrency() != null)
                currency.setText(security.getCurrency().getCurrencyCode());
            
            setValid(true);

            return content;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.preference.PreferencePage#okToLeave()
         */
        public boolean okToLeave()
        {
            if (code.getText().length() == 0)
                return false;
            if (securityDescription.getText().length() == 0)
                return false;
            return super.okToLeave();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.preference.PreferencePage#performOk()
         */
        public boolean performOk()
        {
            if (isValid())
            {
                security.setCode(code.getText());
                security.setDescription(securityDescription.getText());
               	security.setCurrency(currency.getText().length() != 0 ? Currency.getInstance(currency.getText()) : null);
                
                if (clearHistory.getSelection())
                {
                    security.getHistory().clear();
                    security.setChanged();
                    CorePlugin.getRepository().saveHistory(security.getId(), security.getHistory());
                }

                if (clearIntraday.getSelection())
                {
                    security.getIntradayHistory().clear();
                    security.setChanged();
                    CorePlugin.getRepository().saveIntradayHistory(security.getId(), security.getIntradayHistory());
                }
            }
            return super.performOk();
        }
    }

    private class QuoteFeedPage extends PreferencePage
    {
        FeedOptions options = new FeedOptions("quote"); //$NON-NLS-1$

        public QuoteFeedPage()
        {
            super(Messages.SecurityPropertiesDialog_QuotePage);
            noDefaultAndApplyButton();
            setValid(false);
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ui.preferences.SecurityPropertiesDialog.FeedPage#createContents(org.eclipse.swt.widgets.Composite)
         */
        protected Control createContents(Composite parent)
        {
            Composite content = options.createControls(parent, security.getQuoteFeed());
            
            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.marginWidth = gridLayout.marginHeight = 0;
            content.setLayout(gridLayout);

            setValid(true);
            
            return content;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.preference.PreferencePage#performOk()
         */
        public boolean performOk()
        {
            if (isValid())
                security.setQuoteFeed(options.getFeed());
            return super.performOk();
        }
    }
    
    private class Level2FeedPage extends PreferencePage
    {
        FeedOptions options = new FeedOptions("level2"); //$NON-NLS-1$

        public Level2FeedPage()
        {
            super(Messages.SecurityPropertiesDialog_Level2Page);
            noDefaultAndApplyButton();
            setValid(false);
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ui.preferences.SecurityPropertiesDialog.FeedPage#createContents(org.eclipse.swt.widgets.Composite)
         */
        protected Control createContents(Composite parent)
        {
            Composite content = options.createControls(parent, security.getLevel2Feed());

            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.marginWidth = gridLayout.marginHeight = 0;
            content.setLayout(gridLayout);
            
            setValid(true);
            
            return content;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.preference.PreferencePage#performOk()
         */
        public boolean performOk()
        {
            if (isValid())
                security.setLevel2Feed(options.getFeed());
            return super.performOk();
        }
    }
    
    private class HistoryFeedPage extends PreferencePage
    {
        FeedOptions options = new FeedOptions("history"); //$NON-NLS-1$

        public HistoryFeedPage()
        {
            super(Messages.SecurityPropertiesDialog_HistoryPage);
            noDefaultAndApplyButton();
            setValid(false);
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ui.preferences.SecurityPropertiesDialog.FeedPage#createContents(org.eclipse.swt.widgets.Composite)
         */
        protected Control createContents(Composite parent)
        {
            Composite content = options.createControls(parent, security.getHistoryFeed());

            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.marginWidth = gridLayout.marginHeight = 0;
            content.setLayout(gridLayout);
            
            setValid(true);
            
            return content;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.preference.PreferencePage#performOk()
         */
        public boolean performOk()
        {
            if (isValid())
                security.setHistoryFeed(options.getFeed());
            return super.performOk();
        }
    }

    private class DataCollectorPage extends PreferencePage
    {
        IntradayDataOptions options = new IntradayDataOptions();

        public DataCollectorPage()
        {
            super(Messages.SecurityPropertiesDialog_IntradayChartsPage);
            noDefaultAndApplyButton();
            setValid(false);
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
         */
        protected Control createContents(Composite parent)
        {
            Composite content = options.createControls(parent, security);

            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.marginWidth = gridLayout.marginHeight = 0;
            content.setLayout(gridLayout);
            
            setValid(true);
            
            return content;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.preference.PreferencePage#performOk()
         */
        public boolean performOk()
        {
            if (isValid())
                return options.saveSettings(security);
            return super.performOk();
        }
    }
}
