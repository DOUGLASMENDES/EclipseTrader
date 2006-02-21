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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 */
public class HistoryFeedPage extends WizardPage
{
    private Combo feed;
    private Text symbol;
    private Security security;

    public HistoryFeedPage()
    {
        this(null);
    }

    public HistoryFeedPage(Security security)
    {
        super("");
        setTitle("History Feed");
        setDescription("Set the security feed for history data");
        setPageComplete(true);
        this.security = security;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        setControl(composite);

        Label label = new Label(composite, SWT.NONE);
        label.setText("Feed");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        feed = new Combo(composite, SWT.SINGLE | SWT.READ_ONLY);
        feed.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        feed.setVisibleItemCount(10);
        feed.add("");

        label = new Label(composite, SWT.NONE);
        label.setText("Symbol");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        symbol = new Text(composite, SWT.BORDER);
        symbol.setLayoutData(new GridData(100, SWT.DEFAULT));
        if (security != null && security.getHistoryFeed() != null)
            symbol.setText(security.getHistoryFeed().getSymbol());

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.FEED_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
            java.util.List plugins = Arrays.asList(elements);
            Collections.sort(plugins, new Comparator() {
                public int compare(Object arg0, Object arg1)
                {
                    if ((arg0 instanceof IConfigurationElement) && (arg1 instanceof IConfigurationElement))
                    {
                        String s0 = ((IConfigurationElement) arg0).getAttribute("name"); //$NON-NLS-1$
                        String s1 = ((IConfigurationElement) arg1).getAttribute("name"); //$NON-NLS-1$
                        return s0.compareTo(s1);
                    }
                    return 0;
                }
            });

            for (Iterator iter = plugins.iterator(); iter.hasNext(); )
            {
                IConfigurationElement element = (IConfigurationElement)iter.next();
                String id = element.getAttribute("id"); //$NON-NLS-1$
                String name = element.getAttribute("name"); //$NON-NLS-1$
                
                IConfigurationElement[] children = element.getChildren();
                for (int i = 0; i < children.length; i++)
                {
                    if (children[i].getName().equals("history"))
                    {
                        feed.setData(String.valueOf(feed.getItemCount()), id);
                        feed.add(name);
                        if (security != null && security.getHistoryFeed() != null)
                        {
                            if (id.equals(security.getHistoryFeed().getId()))
                                feed.select(feed.getItemCount() - 1);
                        }
                        break;
                    }
                }
            }
        }
    }
    
    public String getId()
    {
        return (String)feed.getData(String.valueOf(feed.getSelectionIndex()));
    }
    
    public String getSymbol()
    {
        return symbol.getText();
    }
}
