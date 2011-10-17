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

package org.eclipsetrader.ui.internal.trading.portfolio;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewItemVisitor;

public class PositionElement extends PlatformObject implements IViewItem, IWorkbenchAdapter {

    IViewItem parent;
    IPosition position;
    ITrade trade;

    public PositionElement(IViewItem parent, IPosition position) {
        this.parent = parent;
        this.position = position;
    }

    public void setTrade(ITrade trade) {
        this.trade = trade;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#accept(org.eclipsetrader.core.views.IViewItemVisitor)
     */
    @Override
    public void accept(IViewItemVisitor visitor) {
        visitor.visit(this);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#getItemCount()
     */
    @Override
    public int getItemCount() {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#getItems()
     */
    @Override
    public IViewItem[] getItems() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#getParent()
     */
    @Override
    public IViewItem getParent() {
        return parent;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#getValues()
     */
    @Override
    public IAdaptable[] getValues() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(position.getClass())) {
            return position;
        }

        if (adapter.isAssignableFrom(position.getSecurity().getClass())) {
            return position.getSecurity();
        }

        if (adapter.isAssignableFrom(ITrade.class)) {
            return trade;
        }
        if (trade != null && adapter.isAssignableFrom(trade.getClass())) {
            return trade;
        }

        if (adapter.isAssignableFrom(IBroker.class)) {
            IBroker broker = (IBroker) parent.getAdapter(IBroker.class);
            if (broker != null) {
                return broker;
            }
        }

        if (adapter.isAssignableFrom(IAccount.class)) {
            IAccount account = (IAccount) parent.getAdapter(IAccount.class);
            if (account != null) {
                return account;
            }
        }

        return super.getAdapter(adapter);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PositionElement)) {
            return false;
        }
        return position.equals(((PositionElement) obj).position);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 11 * position.hashCode();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object o) {
        return new Object[0];
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    @Override
    public ImageDescriptor getImageDescriptor(Object object) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    @Override
    public String getLabel(Object o) {
        return position.getSecurity().getName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object o) {
        return parent;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return position.getSecurity().getName();
    }
}
