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

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.ui.QueuedRealm;
import org.eclipsetrader.ui.internal.ats.ViewerObservableMap;

public class WatchListViewTickDecorator {

    private final Realm realm = QueuedRealm.getInstance();
    private final IObservableSet knownElements;

    private Color positiveBackgroundColor;
    private Color negativeBackgroundColor;
    private Color evenRowsColor;
    private Color oddRowsColor;

    private WatchListViewCellAttribute positiveTickAttribute;
    private WatchListViewCellAttribute[] positiveFadeAttributes = new WatchListViewCellAttribute[3];
    private WatchListViewCellAttribute negativeTickAttribute;
    private WatchListViewCellAttribute[] negativeFadeAttributes = new WatchListViewCellAttribute[3];

    private final Map<String, Map<Object, TickData>> decoratorMap = new HashMap<String, Map<Object, TickData>>();

    private Timer timer;

    private class TickData {

        public Object element;
        public Object value;
        public double diff;
        public int counter;

        public WritableMap attributeMap;

        public TickData(Object element, Object value) {
            this.element = element;
            this.value = value;
            this.counter = -1;
        }

        public void setValue(Object value) {
            Object oldValue = this.value;
            if (timer != null) {
                if ((oldValue instanceof IAdaptable) && (value instanceof IAdaptable)) {
                    Number oldNumber = (Number) ((IAdaptable) oldValue).getAdapter(Number.class);
                    Number newNumber = (Number) ((IAdaptable) value).getAdapter(Number.class);
                    if (oldNumber != null && newNumber != null) {
                        diff = newNumber.doubleValue() - oldNumber.doubleValue();
                        if (diff > 0) {
                            attributeMap.put(element, positiveTickAttribute);
                            counter = 5;
                        }
                        else if (diff < 0) {
                            attributeMap.put(element, negativeTickAttribute);
                            counter = 5;
                        }
                    }
                }
            }
            this.value = value;
        }

        public void reset() {
            if (counter < 0) {
                return;
            }

            counter--;

            realm.exec(new Runnable() {

                @Override
                public void run() {
                    if (counter < 0) {
                        attributeMap.put(element, null);
                    }
                    else if (diff > 0) {
                        if (counter < positiveFadeAttributes.length) {
                            attributeMap.put(element, positiveFadeAttributes[counter]);
                        }
                    }
                    else if (diff < 0) {
                        if (counter < negativeFadeAttributes.length) {
                            attributeMap.put(element, negativeFadeAttributes[counter]);
                        }
                    }
                }
            });
        }
    }

    private TimerTask tickTimerTask = new TimerTask() {

        @Override
        public void run() {
            for (Map<Object, TickData> tickMap : decoratorMap.values()) {
                for (TickData data : tickMap.values()) {
                    data.reset();
                }
            }
        }
    };

    public WatchListViewTickDecorator(IObservableSet knownElements) {
        this.knownElements = knownElements;
    }

    public void setEnabled(boolean enable) {
        if (enable) {
            if (timer == null) {
                timer = new Timer();
                timer.schedule(tickTimerTask, 100, 500);
            }
        }
        else {
            if (timer != null) {
                timer.cancel();
                realm.exec(new Runnable() {

                    @Override
                    public void run() {
                        for (Map<Object, TickData> tickMap : decoratorMap.values()) {
                            for (TickData data : tickMap.values()) {
                                data.attributeMap.clear();
                            }
                        }
                    }
                });
                timer = null;
            }
        }
    }

    public void setRowColors(Color evenRowsColor, Color oddRowsColor) {
        this.evenRowsColor = evenRowsColor;
        this.oddRowsColor = oddRowsColor;
        buildColors();
    }

    public void setTickColors(Color positiveColor, Color negativeColor) {
        positiveBackgroundColor = positiveColor;
        negativeBackgroundColor = negativeColor;

        positiveTickAttribute = new WatchListViewCellAttribute();
        positiveTickAttribute.evenBackground = positiveColor;
        positiveTickAttribute.oddBackground = positiveColor;

        negativeTickAttribute = new WatchListViewCellAttribute();
        negativeTickAttribute.evenBackground = negativeColor;
        negativeTickAttribute.oddBackground = negativeColor;

        buildColors();
    }

    public CellLabelProvider createCellLabelProvider(final String key) {
        ViewerObservableMap valueMap = new ViewerObservableMap(realm, knownElements, key);

        final WritableMap attributeMap = new WritableMap(realm);

        Map<Object, TickData> tickMap = new HashMap<Object, WatchListViewTickDecorator.TickData>();
        decoratorMap.put(key, tickMap);

        valueMap.addMapChangeListener(new IMapChangeListener() {

            @Override
            public void handleMapChange(MapChangeEvent event) {
                Map<Object, TickData> tickMap = decoratorMap.get(key);
                for (Object elementKey : event.diff.getChangedKeys()) {
                    if (tickMap.get(elementKey) == null) {
                        TickData data = new TickData(elementKey, event.getObservableMap().get(elementKey));
                        data.attributeMap = attributeMap;
                        tickMap.put(elementKey, data);
                    }
                    else {
                        TickData data = tickMap.get(elementKey);
                        data.setValue(event.getObservableMap().get(elementKey));
                    }
                }
            }
        });

        WatchListViewCellLabelProvider labelProvider = new WatchListViewCellLabelProvider(new IObservableMap[] {
            valueMap, attributeMap
        });

        return labelProvider;
    }

    private void buildColors() {
        if (positiveBackgroundColor == null || negativeBackgroundColor == null || evenRowsColor == null || oddRowsColor == null) {
            return;
        }

        for (int i = 0; i < positiveFadeAttributes.length; i++) {
            if (positiveFadeAttributes[i] != null) {
                positiveFadeAttributes[i].evenBackground.dispose();
                positiveFadeAttributes[i].oddBackground.dispose();
            }
            positiveFadeAttributes[i] = new WatchListViewCellAttribute();
        }

        RGB backgroundColor = positiveBackgroundColor.getRGB();
        int steps = 100 / (positiveFadeAttributes.length + 1);
        for (int i = 0, ratio = steps; i < positiveFadeAttributes.length; i++, ratio += steps) {
            RGB rgb = blend(backgroundColor, evenRowsColor.getRGB(), ratio);
            positiveFadeAttributes[i].evenBackground = new Color(Display.getDefault(), rgb);
        }
        steps = 100 / (positiveFadeAttributes.length + 1);
        for (int i = 0, ratio = steps; i < positiveFadeAttributes.length; i++, ratio += steps) {
            RGB rgb = blend(backgroundColor, oddRowsColor.getRGB(), ratio);
            positiveFadeAttributes[i].oddBackground = new Color(Display.getDefault(), rgb);
        }

        for (int i = 0; i < negativeFadeAttributes.length; i++) {
            if (negativeFadeAttributes[i] != null) {
                negativeFadeAttributes[i].evenBackground.dispose();
                negativeFadeAttributes[i].oddBackground.dispose();
            }
            negativeFadeAttributes[i] = new WatchListViewCellAttribute();
        }
        backgroundColor = negativeBackgroundColor.getRGB();
        steps = 100 / (negativeFadeAttributes.length + 1);
        for (int i = 0, ratio = steps; i < negativeFadeAttributes.length; i++, ratio += steps) {
            RGB rgb = blend(backgroundColor, evenRowsColor.getRGB(), ratio);
            negativeFadeAttributes[i].evenBackground = new Color(Display.getDefault(), rgb);
        }
        steps = 100 / (negativeFadeAttributes.length + 1);
        for (int i = 0, ratio = steps; i < negativeFadeAttributes.length; i++, ratio += steps) {
            RGB rgb = blend(backgroundColor, oddRowsColor.getRGB(), ratio);
            negativeFadeAttributes[i].oddBackground = new Color(Display.getDefault(), rgb);
        }
    }

    private RGB blend(RGB c1, RGB c2, int ratio) {
        int r = blend(c1.red, c2.red, ratio);
        int g = blend(c1.green, c2.green, ratio);
        int b = blend(c1.blue, c2.blue, ratio);
        return new RGB(r, g, b);
    }

    private int blend(int v1, int v2, int ratio) {
        return (ratio * v1 + (100 - ratio) * v2) / 100;
    }

    public void dispose() {
        timer.cancel();
        disposeColors();
    }

    private void disposeColors() {
        for (int i = 0; i < positiveFadeAttributes.length; i++) {
            if (positiveFadeAttributes[i] != null) {
                positiveFadeAttributes[i].evenBackground.dispose();
                positiveFadeAttributes[i].oddBackground.dispose();
                positiveFadeAttributes[i] = null;
            }
        }
        for (int i = 0; i < negativeFadeAttributes.length; i++) {
            if (negativeFadeAttributes[i] != null) {
                negativeFadeAttributes[i].evenBackground.dispose();
                negativeFadeAttributes[i].oddBackground.dispose();
                negativeFadeAttributes[i] = null;
            }
        }
    }
}
