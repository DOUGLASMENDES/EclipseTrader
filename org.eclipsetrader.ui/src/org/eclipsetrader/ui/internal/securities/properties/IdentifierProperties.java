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

package org.eclipsetrader.ui.internal.securities.properties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.securities.FeedPropertiesControl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class IdentifierProperties extends PropertyPage implements IWorkbenchPropertyPage {

    private Text name;
    private Label propertiesLabel;
    private FeedPropertiesControl properties;

    private ModifyListener modifyListener = new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
            updateSymbolSelection();
            setValid(isValid());
        }
    };

    public IdentifierProperties() {
        setTitle("Identifier");
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
        initializeDialogUnits(content);

        Label label = new Label(content, SWT.NONE);
        label.setText("Identifier name:");
        name = new Text(content, SWT.BORDER);
        name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        propertiesLabel = new Label(content, SWT.NONE);
        propertiesLabel.setText("Properties:");
        propertiesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        properties = new FeedPropertiesControl(content);
        properties.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        ((GridData) properties.getControl().getLayoutData()).heightHint = properties.getTree().getItemHeight() * 15;

        performDefaults();
        name.addModifyListener(modifyListener);

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        ISecurity security = (ISecurity) getElement().getAdapter(ISecurity.class);

        IFeedIdentifier feedIdentifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);
        if (feedIdentifier != null) {
            name.setText(feedIdentifier.getSymbol());
        }

        FeedProperties feedProperties = (FeedProperties) feedIdentifier.getAdapter(FeedProperties.class);
        properties.setProperties(feedProperties);

        updateSymbolSelection();

        super.performDefaults();
    }

    protected void applyChanges() {
        Security security = (Security) getElement().getAdapter(Security.class);
        if (security != null) {
            if (name.getText().equals("")) {
                security.setIdentifier(null);
            }
            else {
                IFeedIdentifier feedIdentifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);
                if (feedIdentifier == null) {
                    feedIdentifier = getRepositoryService().getFeedIdentifierFromSymbol(name.getText());
                    if (feedIdentifier == null) {
                        feedIdentifier = new FeedIdentifier(name.getText(), new FeedProperties());
                    }
                }
                if (feedIdentifier instanceof FeedIdentifier) {
                    ((FeedIdentifier) feedIdentifier).setSymbol(name.getText());
                    ((FeedIdentifier) feedIdentifier).setProperties(properties.getProperties());
                }
                security.setIdentifier(feedIdentifier);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#isValid()
     */
    @Override
    public boolean isValid() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        if (getControl() != null) {
            applyChanges();
        }
        return super.performOk();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        applyChanges();

        final ISecurity security = (ISecurity) getElement().getAdapter(ISecurity.class);
        final IRepositoryService service = getRepositoryService();
        service.runInService(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                service.saveAdaptable(new IAdaptable[] {
                    security
                });
                return Status.OK_STATUS;
            }
        }, null);
    }

    protected void updateSymbolSelection() {
        boolean hasIdentifier = !name.getText().equals("");
        propertiesLabel.setEnabled(hasIdentifier);
        properties.getTree().setEnabled(hasIdentifier);
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

    protected IRepositoryService getRepositoryService() {
        return UIActivator.getDefault().getRepositoryService();
    }
}
