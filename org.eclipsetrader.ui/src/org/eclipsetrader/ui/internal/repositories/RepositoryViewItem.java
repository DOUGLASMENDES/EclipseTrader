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

package org.eclipsetrader.ui.internal.repositories;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewItemVisitor;

@SuppressWarnings("restriction")
public class RepositoryViewItem implements IViewItem {
	private Object object;
	private IAdaptable[] values = new IAdaptable[0];
	private RepositoryViewItem parent;
	private Map<Object, RepositoryViewItem> childs = new HashMap<Object, RepositoryViewItem>();

	public RepositoryViewItem() {
	}

	protected RepositoryViewItem(RepositoryViewItem parent, Object object) {
		this.parent = parent;
		this.object = object;
		this.values = new IAdaptable[] {
			new IAdaptable() {
                @SuppressWarnings("unchecked")
                public Object getAdapter(Class adapter) {
            		if (RepositoryViewItem.this.object != null) {
                		if (adapter.isAssignableFrom(RepositoryViewItem.this.object.getClass()))
                			return RepositoryViewItem.this.object;
                		if (adapter.isAssignableFrom(String.class))
                			return RepositoryViewItem.this.object.toString();
            		}
	                return null;
                }
			},
		};
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
		Collection<RepositoryViewItem> c = childs.values();
		return c.toArray(new IViewItem[c.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IViewItem#getParent()
	 */
	public IViewItem getParent() {
		return parent;
	}

	public boolean hasChild(Object object) {
		return childs.containsKey(object);
	}

	public RepositoryViewItem getChild(Object object) {
		return childs.get(object);
	}

	public RepositoryViewItem createChild(Object object) {
		RepositoryViewItem viewItem = new RepositoryViewItem(this, object);
		childs.put(object, viewItem);
		return viewItem;
	}

	public void removeChild(Object object) {
		childs.remove(object);
	}

	public Iterator<RepositoryViewItem> iterator() {
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

	public Object getObject() {
    	return object;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (object != null && adapter.isAssignableFrom(object.getClass()))
			return object;

		if (object instanceof IAdaptable) {
			Object obj = ((IAdaptable) object).getAdapter(adapter);
			if (obj != null)
				return obj;
		}

		if (adapter.isAssignableFrom(getClass()))
			return this;

		if (object instanceof IAdaptable)
			return AdapterManager.getDefault().getAdapter(object, adapter);

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
	    return 7 * (object != null ? object.hashCode() : 0) +
	           11 * (parent != null ? parent.hashCode() : 0);
    }
}
