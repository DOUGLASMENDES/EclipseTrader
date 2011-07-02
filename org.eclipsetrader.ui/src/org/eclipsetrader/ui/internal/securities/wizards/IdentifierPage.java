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

package org.eclipsetrader.ui.internal.securities.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.securities.FeedPropertiesControl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class IdentifierPage extends WizardPage {

    private Text name;
    private Label propertiesLabel;
    private FeedPropertiesControl properties;

    private IFeedIdentifier feedIdentifier;

    private ModifyListener modifyListener = new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
            updateSymbolSelection();
            if (isCurrentPage()) {
                getContainer().updateButtons();
            }
        }
    };

    public IdentifierPage() {
        super("identifier", "Identifier", null);
        setDescription("Assign a feed identifier to the Security");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        content.setLayout(new GridLayout(2, false));
        setControl(content);
        initializeDialogUnits(content);

        Label label = new Label(content, SWT.NONE);
        label.setText("Identifier name:");
        name = new Text(content, SWT.BORDER);
        name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        name.addModifyListener(modifyListener);

        propertiesLabel = new Label(content, SWT.NONE);
        propertiesLabel.setText("Properties:");
        propertiesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        properties = new FeedPropertiesControl(content);
        properties.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        ((GridData) properties.getControl().getLayoutData()).heightHint = properties.getTree().getItemHeight() * 15;

        updateSymbolSelection();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        name.setFocus();
        super.setVisible(visible);
    }

    protected IMarket[] getMarkets() {
        BundleContext context = UIActivator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IMarketService.class.getName());
        if (serviceReference != null) {
            IMarketService marketService = (IMarketService) context.getService(serviceReference);
            return marketService.getMarkets();
        }
        return new IMarket[0];
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        return true;
    }

    protected void updateSymbolSelection() {
        feedIdentifier = getFeedIdentifierFromSymbol(name.getText());
        if (feedIdentifier != null) {
            properties.setProperties((FeedProperties) feedIdentifier.getAdapter(FeedProperties.class));
        }
        else {
            properties.setProperties(null);
        }

        boolean hasIdentifier = !name.getText().equals("");
        propertiesLabel.setEnabled(hasIdentifier);
        properties.getTree().setEnabled(hasIdentifier);
    }

    protected IFeedIdentifier getFeedIdentifierFromSymbol(String s) {
        return UIActivator.getDefault().getRepositoryService().getFeedIdentifierFromSymbol(s);
    }

    public IFeedIdentifier getFeedIdentifier() {
        if (!name.getText().equals("")) {
            if (feedIdentifier == null) {
                feedIdentifier = new FeedIdentifier(name.getText(), new FeedProperties());
            }
            FeedProperties feedProperties = properties.getProperties();
            if (feedIdentifier instanceof FeedIdentifier) {
                ((FeedIdentifier) feedIdentifier).setProperties(feedProperties);
            }
        }
        return feedIdentifier;
    }
}
