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

package org.eclipsetrader.ui.internal.views;

import java.util.Set;

import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

public abstract class ObservableMapOwnerDrawCellLabelProvider extends OwnerDrawLabelProvider {

    protected IObservableMap[] attributeMaps;

    private IMapChangeListener mapChangeListener = new IMapChangeListener() {

        @Override
        @SuppressWarnings("rawtypes")
        public void handleMapChange(MapChangeEvent event) {
            Set affectedElements = event.diff.getChangedKeys();
            LabelProviderChangedEvent newEvent = new LabelProviderChangedEvent(
                ObservableMapOwnerDrawCellLabelProvider.this,
                affectedElements.toArray());
            fireLabelProviderChanged(newEvent);
        }
    };

    public ObservableMapOwnerDrawCellLabelProvider(IObservableMap attributeMap) {
        this(new IObservableMap[] {
            attributeMap
        });
    }

    public ObservableMapOwnerDrawCellLabelProvider(IObservableMap[] attributeMaps) {
        System.arraycopy(attributeMaps, 0, this.attributeMaps = new IObservableMap[attributeMaps.length], 0, attributeMaps.length);
        for (int i = 0; i < attributeMaps.length; i++) {
            attributeMaps[i].addMapChangeListener(mapChangeListener);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
     */
    @Override
    public void dispose() {
        for (int i = 0; i < attributeMaps.length; i++) {
            attributeMaps[i].removeMapChangeListener(mapChangeListener);
        }
        super.dispose();
        this.attributeMaps = null;
        this.mapChangeListener = null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
     */
    @Override
    public void update(ViewerCell cell) {
        Object element = cell.getElement();
        Object value = attributeMaps[0].get(element);
        cell.setText(value == null ? "" : value.toString()); //$NON-NLS-1$
    }
}
