/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.navigator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewItemVisitor;

@SuppressWarnings("restriction")
public class NavigatorViewItem implements IViewItem {
	private Object reference;
	private NavigatorViewItem parent;
	private Map<Object, NavigatorViewItem> childs = new HashMap<Object, NavigatorViewItem>();

	public NavigatorViewItem() {
	}

	public NavigatorViewItem(NavigatorViewItem parent, Object reference) {
		this.parent = parent;
	    this.reference = reference;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#getItemCount()
     */
    public int getItemCount() {
		return childs != null ? childs.size() : 0;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#getItems()
     */
    public IViewItem[] getItems() {
		Collection<NavigatorViewItem> c = childs.values();
		return c.toArray(new IViewItem[c.size()]);
    }

    protected void setItems(NavigatorViewItem[] items) {
    	childs.clear();
    	for (NavigatorViewItem viewItem : items) {
    		childs.put(viewItem.getReference(), viewItem);
    		viewItem.parent = this;
    	}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#getParent()
     */
    public IViewItem getParent() {
    	return parent;
    }

	/**
	 * Checks if the given reference object is a child of the receiver.
	 *
	 * @param reference the reference object.
	 * @return <code>true</code> if it is a child.
	 */
	public boolean hasChild(Object reference) {
		return childs.containsKey(reference);
	}

	/**
	 * Returns the <code>WatchListViewItem</code> that holds the given reference object.
	 *
	 * @param reference the reference object.
	 * @return the <code>WatchListViewItem</code> that holds the object, or <code>null</code>.
	 */
	public NavigatorViewItem getChild(Object reference) {
		return childs.get(reference);
	}

	/**
	 * Creates a <code>WatchListViewItem</code> that is a child of the receiver.
	 *
	 * @param reference the object to associate to the new item.
	 * @return the new <code>WatchListViewItem</code>.
	 */
	public NavigatorViewItem createChild(Object reference) {
		NavigatorViewItem viewItem = new NavigatorViewItem(this, reference);
		childs.put(reference, viewItem);
		return viewItem;
	}

	protected void addChild(NavigatorViewItem viewItem) {
		childs.put(viewItem.getReference(), viewItem);
		viewItem.parent = this;
	}

	public void removeChild(Object reference) {
		childs.remove(reference);
	}

	public Iterator<NavigatorViewItem> iterator() {
		return childs.values().iterator();
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#getValues()
     */
    public IAdaptable[] getValues() {
	    return new IAdaptable[0];
    }

	public Object getReference() {
    	return reference;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
		if (reference != null && adapter.isAssignableFrom(reference.getClass()))
			return reference;

		if (reference instanceof IAdaptable) {
			Object obj = ((IAdaptable) reference).getAdapter(adapter);
			if (obj != null)
				return obj;
		}

		if (adapter.isAssignableFrom(getClass()))
			return this;

		if (reference instanceof IAdaptable)
			return AdapterManager.getDefault().getAdapter(reference, adapter);

		return null;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#accept(org.eclipsetrader.core.views.IViewItemVisitor)
     */
    public void accept(IViewItemVisitor visitor) {
    	if (visitor.visit(this)) {
    		for (IViewItem viewItem : getItems())
    			viewItem.accept(visitor);
    	}
    }

	/* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	    return 7 * (reference != null ? reference.hashCode() : 0) +
	           11 * (parent != null ? parent.hashCode() : 0);
    }

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	    return reference != null ? reference.toString() : super.toString();
    }
}
