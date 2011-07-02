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

package org.eclipsetrader.ui.internal.markets;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.markets.Market;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.ui.internal.UIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class MembersPage extends PropertyPage {

    private Button showMembers;
    private Button showUnlisted;
    private CheckboxTableViewer members;

    public MembersPage() {
        setTitle("Members");
        noDefaultAndApplyButton();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        content.setLayout(gridLayout);
        initializeDialogUnits(content);

        showMembers = new Button(content, SWT.RADIO);
        showMembers.setText("Show members only");
        showMembers.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        showMembers.setSelection(true);
        showMembers.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshSelection();
            }
        });

        showUnlisted = new Button(content, SWT.RADIO);
        showUnlisted.setText("Show securities that are not members of other markets");
        showUnlisted.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        showUnlisted.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshSelection();
            }
        });

        members = CheckboxTableViewer.newCheckList(content, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        members.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        ((GridData) members.getControl().getLayoutData()).heightHint = members.getTable().getItemHeight() * 15 + members.getTable().getBorderWidth() * 2;
        ((GridData) members.getControl().getLayoutData()).verticalIndent = 5;
        members.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((ISecurity) element).getName();
            }
        });
        members.setContentProvider(new ArrayContentProvider());
        members.setSorter(new ViewerSorter());
        members.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (showMembers.getSelection()) {
                    Market market = (Market) getElement().getAdapter(Market.class);
                    return market.hasMember((ISecurity) element);
                }
                else {
                    for (IMarket market : getMarkets()) {
                        if (market != getElement()) {
                            if (market.hasMember((ISecurity) element)) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
        });
        members.setInput(UIActivator.getDefault().getRepositoryService().getSecurities());

        if (getElement() != null) {
            Market market = (Market) getElement().getAdapter(Market.class);
            if (market != null) {
                members.setCheckedElements(market.getMembers());
            }
        }

        return content;
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
        if (isControlCreated() && getElement() != null) {
            Market market = (Market) getElement().getAdapter(Market.class);
            if (market != null) {
                Object[] checked = members.getCheckedElements();
                ISecurity[] securities = new ISecurity[checked.length];
                System.arraycopy(checked, 0, securities, 0, checked.length);
                market.removeMembers(market.getMembers());
                market.addMembers(securities);
            }
        }
        return super.performOk();
    }

    protected IMarket[] getMarkets() {
        BundleContext context = UIActivator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IMarketService.class.getName());
        if (serviceReference != null) {
            IMarketService marketService = (IMarketService) context.getService(serviceReference);
            context.ungetService(serviceReference);
            return marketService.getMarkets();
        }
        return new IMarket[0];
    }

    protected void refreshSelection() {
        members.refresh();
        if (getElement() != null) {
            Market market = (Market) getElement().getAdapter(Market.class);
            if (market != null) {
                members.setCheckedElements(market.getMembers());
            }
        }
    }
}
