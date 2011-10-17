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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewItemVisitor;

public class AccountElement extends PlatformObject implements IViewItem, IWorkbenchAdapter {

    IViewItem parent;
    IAccount account;
    List<PositionElement> childs;

    public AccountElement(IViewItem parent, IAccount account) {
        this.parent = parent;
        this.account = account;

        childs = new ArrayList<PositionElement>();
        for (IPosition position : account.getPositions()) {
            childs.add(new PositionElement(this, position));
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#accept(org.eclipsetrader.core.views.IViewItemVisitor)
     */
    @Override
    public void accept(IViewItemVisitor visitor) {
        if (visitor.visit(this)) {
            for (PositionElement element : childs) {
                element.accept(visitor);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#getItemCount()
     */
    @Override
    public int getItemCount() {
        return childs.size();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#getItems()
     */
    @Override
    public IViewItem[] getItems() {
        return childs.toArray(new IViewItem[childs.size()]);
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
        if (adapter.isAssignableFrom(account.getClass())) {
            return account;
        }

        if (adapter.isAssignableFrom(IBroker.class)) {
            IBroker broker = (IBroker) parent.getAdapter(IBroker.class);
            if (broker != null) {
                return broker;
            }
        }

        return super.getAdapter(adapter);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AccountElement)) {
            return false;
        }
        return account.equals(((AccountElement) obj).account);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 11 * account.hashCode();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object o) {
        return childs.toArray(new IViewItem[childs.size()]);
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
        return account.getDescription();
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
        return account.getDescription();
    }
}
