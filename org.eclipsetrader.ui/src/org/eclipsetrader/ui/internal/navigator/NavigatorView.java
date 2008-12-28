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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.views.IView;
import org.eclipsetrader.core.views.IViewChangeListener;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewVisitor;
import org.eclipsetrader.core.views.ViewEvent;
import org.eclipsetrader.core.views.ViewItemDelta;
import org.eclipsetrader.ui.navigator.INavigatorContentGroup;

public class NavigatorView implements IView {
	private IStructuredContentProvider[] contentProviders;
	private INavigatorContentGroup[] groups;
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	private NavigatorViewItem root = new NavigatorViewItem();

	public NavigatorView() {
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IView#dispose()
     */
    public void dispose() {
    	listeners.clear();
		for (int i = 0; i < contentProviders.length; i++)
			contentProviders[i].dispose();
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IView#getItems()
     */
    public IViewItem[] getItems() {
	    return root.getItems();
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IView#addViewChangeListener(org.eclipsetrader.core.views.IViewChangeListener)
     */
    public void addViewChangeListener(IViewChangeListener listener) {
		listeners.add(listener);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IView#removeViewChangeListener(org.eclipsetrader.core.views.IViewChangeListener)
     */
    public void removeViewChangeListener(IViewChangeListener listener) {
		listeners.remove(listener);
    }

	protected void notifyListeners(ViewEvent event) {
		Object[] l = listeners.getListeners();
		for (int i = 0; i < l.length; i++) {
			try {
				((IViewChangeListener) l[i]).viewChanged(event);
			} catch(Exception e) {
	    		Status status = new Status(Status.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running view listener", e);
	    		CoreActivator.log(status);
			} catch(LinkageError e) {
	    		Status status = new Status(Status.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running view listener", e);
	    		CoreActivator.log(status);
			}
		}
	}

    public IStructuredContentProvider[] getContentProviders() {
    	return contentProviders;
    }

	public void setContentProviders(IStructuredContentProvider[] contentProviders) {
    	this.contentProviders = contentProviders;
    }

	public void setGroups(INavigatorContentGroup[] groups) {
    	this.groups = groups;
    }

    public INavigatorContentGroup[] getGroups() {
    	return groups;
    }

	public void update() {
		List<Object> instruments = new ArrayList<Object>();
		if (contentProviders != null) {
			for (int i = 0; i < contentProviders.length; i++)
				instruments.addAll(Arrays.asList(contentProviders[i].getElements(this)));
		}

		NavigatorViewItem newRoot = new NavigatorViewItem();
    	if (groups != null && groups.length != 0)
    		groupElements(newRoot, instruments.toArray(new IAdaptable[instruments.size()]), groups);
    	else {
    		for (Object element : instruments)
    			newRoot.createChild(element);
    	}

    	ViewItemDelta delta = new ViewItemDelta(ViewItemDelta.NO_CHANGE, root);
    	updateTree(delta, root, newRoot);
    	if (delta.getChildCount() != 0)
    		notifyListeners(new ViewEvent(this, delta.getChilds()));
    }

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
    	if (adapter.isAssignableFrom(getClass()))
    		return this;
	    return null;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IView#accept(org.eclipsetrader.core.views.IViewVisitor)
     */
    public void accept(IViewVisitor visitor) {
    	if (visitor.visit(this)) {
    		for (IViewItem viewItem : getItems())
    			viewItem.accept(visitor);
    	}
    }

	protected void updateTree(ViewItemDelta parentDelta, NavigatorViewItem oldItem, NavigatorViewItem newItem) {
    	for (IViewItem viewItem : oldItem.getItems()) {
    		Object reference = ((NavigatorViewItem) viewItem).getReference();
    		if (!newItem.hasChild(reference)) {
    			oldItem.removeChild(reference);
    			parentDelta.createChild(viewItem, ViewItemDelta.REMOVED);
    		}
    	}
    	for (IViewItem viewItem : newItem.getItems()) {
    		Object reference = ((NavigatorViewItem) viewItem).getReference();
    		if (!oldItem.hasChild(reference)) {
    			NavigatorViewItem addedItem = oldItem.createChild(reference);
    			ViewItemDelta delta = parentDelta.createChild(addedItem, ViewItemDelta.ADDED);
        		updateTree(delta, addedItem, (NavigatorViewItem) viewItem);
    		}
    		else {
    			NavigatorViewItem existingViewItem = oldItem.getChild(reference);
    			ViewItemDelta delta = parentDelta.createChild(existingViewItem, ViewItemDelta.NO_CHANGE);
        		updateTree(delta, existingViewItem, (NavigatorViewItem) viewItem);
    		}
    	}
    }

	protected void groupElements(NavigatorViewItem parent, IAdaptable[] elements, INavigatorContentGroup[] groups) {
		List<IAdaptable> set = new ArrayList<IAdaptable>();
		set.addAll(Arrays.asList(elements));

		if (groups != null && groups.length != 0) {
			INavigatorContentGroup group = groups[0];

			INavigatorContentGroup[] downGroups = null;
			if (groups.length > 1) {
				downGroups = new INavigatorContentGroup[groups.length - 1];
				System.arraycopy(groups, 1, downGroups, 0, downGroups.length);
			}

			IViewItem[] groupedElements = group.getGroupedContent(elements);

			if (groupedElements.length != 0) {
				for (IViewItem viewItem : groupedElements) {
					for (IViewItem childItem : viewItem.getItems()) {
						Object reference = childItem.getAdapter(Object.class);
						for (Iterator<IAdaptable> iter = set.iterator(); iter.hasNext(); ) {
							Object obj = iter.next().getAdapter(reference.getClass());
							if (obj == reference)
								iter.remove();
						}
					}

					Object reference = viewItem.getAdapter(Object.class);
					NavigatorViewItem instrumentViewItem = parent.createChild(reference);
					groupElements(instrumentViewItem, viewItem.getItems(), downGroups);
				}
			}
			if (set.size() != 0)
				groupElements(parent, set.toArray(new IAdaptable[set.size()]), downGroups);
		}
		else {
			for (int i = 0; i < elements.length; i++) {
				Object reference = elements[i].getAdapter(Object.class);
				parent.createChild(reference);
			}
		}
	}
}
