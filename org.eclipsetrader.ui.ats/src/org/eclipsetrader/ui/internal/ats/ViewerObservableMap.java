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

package org.eclipsetrader.ui.internal.ats;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;

@SuppressWarnings("rawtypes")
public class ViewerObservableMap implements IObservableMap {

    private final Realm realm;
    private final String key;

    private final WritableMap delegate;

    private final ISetChangeListener changeListener = new ISetChangeListener() {

        @Override
        public void handleSetChange(SetChangeEvent event) {
            Map additions = new HashMap();
            for (Object o : event.diff.getAdditions()) {
                ViewItem viewItem = (ViewItem) o;
                delegate.put(o, viewItem.getValue(key));
                viewItem.addPropertyChangeListener(key, propertyChangeListener);
            }
            for (Object o : event.diff.getRemovals()) {
                ViewItem viewItem = (ViewItem) o;
                delegate.remove(viewItem);
                viewItem.removePropertyChangeListener(key, propertyChangeListener);
            }
            delegate.putAll(additions);
        }
    };

    private final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            realm.exec(new Runnable() {

                @Override
                public void run() {
                    delegate.put(evt.getSource(), evt.getNewValue());
                }
            });
        }
    };

    public ViewerObservableMap(IObservableSet knownElements, String key) {
        this.realm = Realm.getDefault();
        this.key = key;
        this.delegate = new WritableMap(realm);

        for (Object o : knownElements) {
            ViewItem viewItem = (ViewItem) o;
            delegate.put(o, viewItem.getValue(key));
            viewItem.addPropertyChangeListener(key, propertyChangeListener);
        }

        knownElements.addSetChangeListener(changeListener);
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map m) {
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.IObservable#getRealm()
     */
    @Override
    public Realm getRealm() {
        return realm;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.IObservable#addChangeListener(org.eclipse.core.databinding.observable.IChangeListener)
     */
    @Override
    public void addChangeListener(IChangeListener listener) {
        delegate.addChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.IObservable#removeChangeListener(org.eclipse.core.databinding.observable.IChangeListener)
     */
    @Override
    public void removeChangeListener(IChangeListener listener) {
        delegate.removeChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.IObservable#addStaleListener(org.eclipse.core.databinding.observable.IStaleListener)
     */
    @Override
    public void addStaleListener(IStaleListener listener) {
        delegate.addStaleListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.IObservable#removeStaleListener(org.eclipse.core.databinding.observable.IStaleListener)
     */
    @Override
    public void removeStaleListener(IStaleListener listener) {
        delegate.removeStaleListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.IObservable#isStale()
     */
    @Override
    public boolean isStale() {
        return delegate.isStale();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.IObservable#addDisposeListener(org.eclipse.core.databinding.observable.IDisposeListener)
     */
    @Override
    public void addDisposeListener(IDisposeListener listener) {
        delegate.addDisposeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.IObservable#removeDisposeListener(org.eclipse.core.databinding.observable.IDisposeListener)
     */
    @Override
    public void removeDisposeListener(IDisposeListener listener) {
        delegate.removeDisposeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.IObservable#isDisposed()
     */
    @Override
    public boolean isDisposed() {
        return delegate.isDisposed();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.IObservable#dispose()
     */
    @Override
    public void dispose() {
        delegate.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.map.IObservableMap#getKeyType()
     */
    @Override
    public Object getKeyType() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.map.IObservableMap#getValueType()
     */
    @Override
    public Object getValueType() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.map.IObservableMap#addMapChangeListener(org.eclipse.core.databinding.observable.map.IMapChangeListener)
     */
    @Override
    public void addMapChangeListener(IMapChangeListener listener) {
        delegate.addMapChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.map.IObservableMap#removeMapChangeListener(org.eclipse.core.databinding.observable.map.IMapChangeListener)
     */
    @Override
    public void removeMapChangeListener(IMapChangeListener listener) {
        delegate.removeMapChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.map.IObservableMap#size()
     */
    @Override
    public int size() {
        return delegate.size();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.map.IObservableMap#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.map.IObservableMap#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.map.IObservableMap#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.map.IObservableMap#get(java.lang.Object)
     */
    @Override
    public Object get(Object key) {
        return delegate.get(key);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.map.IObservableMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(Object key, Object value) {
        return delegate.put(key, value);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.map.IObservableMap#remove(java.lang.Object)
     */
    @Override
    public Object remove(Object key) {
        return delegate.remove(key);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.map.IObservableMap#keySet()
     */
    @Override
    public Set keySet() {
        return delegate.keySet();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.map.IObservableMap#values()
     */
    @Override
    public Collection values() {
        return delegate.values();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.map.IObservableMap#entrySet()
     */
    @Override
    public Set entrySet() {
        return delegate.entrySet();
    }
}
