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

package org.eclipsetrader.core.internal.views;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewItemVisitor;

public class WatchListViewItem extends PlatformObject implements IViewItem {
	private Object reference;
	private IAdaptable[] values = new IAdaptable[0];
	private WatchListViewItem parent;
	private Map<Object, WatchListViewItem> childs = new HashMap<Object, WatchListViewItem>();

	public WatchListViewItem() {
	}

	public WatchListViewItem(Object reference) {
	    this.reference = reference;
    }

	protected WatchListViewItem(WatchListViewItem parent, Object reference) {
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
		Collection<WatchListViewItem> c = childs.values();
		return c.toArray(new IViewItem[c.size()]);
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
	public WatchListViewItem getChild(Object reference) {
		return childs.get(reference);
	}

	/**
	 * Creates a <code>WatchListViewItem</code> that is a child of the receiver.
	 *
	 * @param reference the object to associate to the new item.
	 * @return the new <code>WatchListViewItem</code>.
	 */
	public WatchListViewItem createChild(Object reference) {
		WatchListViewItem viewItem = new WatchListViewItem(this, reference);
		childs.put(reference, viewItem);
		return viewItem;
	}

	public void removeChild(Object reference) {
		childs.remove(reference);
	}

	public Iterator<WatchListViewItem> iterator() {
		return childs.values().iterator();
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IViewItem#getValues()
	 */
	public IAdaptable[] getValues() {
		return values;
	}

	public void setValues(IAdaptable[] values) {
    	this.values = values;
    }

	public Object getReference() {
    	return reference;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (reference instanceof IAdaptable) {
			Object obj = ((IAdaptable) reference).getAdapter(adapter);
			if (obj != null)
				return obj;
		}

		if (reference != null && adapter.isAssignableFrom(reference.getClass()))
			return reference;

		if (adapter.isAssignableFrom(getClass()))
			return this;

		return super.getAdapter(adapter);
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
}
