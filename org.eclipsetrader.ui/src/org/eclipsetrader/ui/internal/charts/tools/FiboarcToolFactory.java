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

package org.eclipsetrader.ui.internal.charts.tools;

import java.text.NumberFormat;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipsetrader.core.charts.DataSeries;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.ui.charts.ChartObjectFocusEvent;
import org.eclipsetrader.ui.charts.ChartParameters;
import org.eclipsetrader.ui.charts.ChartToolEditor.ChartObjectEditorEvent;
import org.eclipsetrader.ui.charts.DataBounds;
import org.eclipsetrader.ui.charts.Graphics;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IChartObjectVisitor;
import org.eclipsetrader.ui.charts.IChartParameters;
import org.eclipsetrader.ui.charts.IEditableChartObject;
import org.eclipsetrader.ui.charts.IGraphics;
import org.eclipsetrader.ui.charts.PixelTools;

public class FiboarcToolFactory implements IChartObjectFactory, IExecutableExtension {

    private String id;
    private String factoryName;
    private String name;

    private Value value1;
    private Value value2;
    private Double factors[] = {
            0.618, 0.5, 0.382, 0.236
    };

    private RGB color = new RGB(0, 0, 0);

    private FiboarcToolObject object;

    public static class Value implements IAdaptable {

        private Date date;
        private Number value;

        public Value(Date date, Number value) {
            this.date = date;
            this.value = value;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public Number getValue() {
            return value;
        }

        public void setValue(Number value) {
            this.value = value;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
            if (adapter.isAssignableFrom(Date.class)) {
                return date;
            }
            if (adapter.isAssignableFrom(Number.class)) {
                return value;
            }
            return null;
        }
    }

    public class FiboarcToolObject implements IChartObject, IEditableChartObject {

        private Point p1;
        private Point p2;
        private int[] factorLine;

        private NumberFormat numberFormat = NumberFormat.getInstance();
        private NumberFormat percentageFormat = NumberFormat.getPercentInstance();

        private boolean focus;
        private boolean editorActive;
        private Value currentValue;

        private Point p1start;
        private Point p2start;
        private int lastX = -1, lastY = -1;

        public FiboarcToolObject() {
            numberFormat.setGroupingUsed(true);
            numberFormat.setMinimumFractionDigits(0);
            numberFormat.setMaximumFractionDigits(4);

            percentageFormat.setGroupingUsed(false);
            percentageFormat.setMinimumFractionDigits(0);
            percentageFormat.setMaximumFractionDigits(2);
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#setDataBounds(org.eclipsetrader.ui.charts.DataBounds)
         */
        @Override
        public void setDataBounds(DataBounds bounds) {
            p1 = p2 = null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#getDataSeries()
         */
        @Override
        public IDataSeries getDataSeries() {
            return new DataSeries(name, new IAdaptable[] {
                    value1, value2
            });
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#containsPoint(int, int)
         */
        @Override
        public boolean containsPoint(int x, int y) {
            if (p1 != null && p2 != null) {
                if (PixelTools.isPointOnLine(x, y, p1.x, p1.y, p2.x, p2.y)) {
                    return true;
                }
                if (factorLine != null) {
                    for (int i = 0; i < factorLine.length; i++) {
                        if (PixelTools.isPointOnLine(x, y, p1.x, p1.y, p2.x, factorLine[i])) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip()
         */
        @Override
        public String getToolTip() {
            return Messages.FiboarcToolFactory_Tooltip;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip(int, int)
         */
        @Override
        public String getToolTip(int x, int y) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#handleFocusGained(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
         */
        @Override
        public void handleFocusGained(ChartObjectFocusEvent event) {
            focus = true;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#handleFocusLost(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
         */
        @Override
        public void handleFocusLost(ChartObjectFocusEvent event) {
            focus = false;
        }

        protected boolean hasFocus() {
            return focus;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#paint(org.eclipsetrader.ui.charts.IGraphics)
         */
        @Override
        public void paint(IGraphics graphics) {
            if (editorActive) {
                return;
            }

            if (p1 == null && value1 != null) {
                p1 = graphics.mapToPoint(value1.getDate(), value1.getValue());
            }
            if (p2 == null && value2 != null) {
                p2 = graphics.mapToPoint(value2.getDate(), value2.getValue());
            }

            paintShape(graphics);
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#paintScale(org.eclipsetrader.ui.charts.Graphics)
         */
        @Override
        public void paintScale(Graphics graphics) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IEditableChartObject#isOnDragHandle(int, int)
         */
        @Override
        public boolean isOnDragHandle(int x, int y) {
            if (p1 != null && p2 != null) {
                if (PixelTools.isPointOnLine(x, y, p1.x, p1.y, p2.x, p2.y)) {
                    return true;
                }
                if (factorLine != null) {
                    for (int i = 0; i < factorLine.length; i++) {
                        if (PixelTools.isPointOnLine(x, y, p1.x, p1.y, p2.x, factorLine[i])) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IEditableChartObject#isOnEditHandle(int, int)
         */
        @Override
        public boolean isOnEditHandle(int x, int y) {
            if (p1 != null && p2 != null) {
                if (Math.abs(x - p1.x) <= 2 && Math.abs(y - p1.y) <= 2) {
                    return true;
                }
                else if (Math.abs(x - p2.x) <= 2 && Math.abs(y - p2.y) <= 2) {
                    return true;
                }
            }
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IEditableChartObject#handleMouseDown(org.eclipsetrader.ui.charts.ChartToolEditor.ChartObjectEditorEvent)
         */
        @Override
        public void handleMouseDown(ChartObjectEditorEvent e) {
            if (value1 == null && value2 == null) {
                value1 = new Value(e.date, e.value);
                value2 = new Value(e.date, e.value);
                currentValue = value2;
            }
            else {
                if (p1 == null || p2 == null) {
                    p1 = e.graphics.mapToPoint(value1.getDate(), value1.getValue());
                    p2 = e.graphics.mapToPoint(value2.getDate(), value2.getValue());
                }
                if (Math.abs(e.x - p1.x) <= 2 && Math.abs(e.y - p1.y) <= 2) {
                    currentValue = value1;
                }
                else if (Math.abs(e.x - p2.x) <= 2 && Math.abs(e.y - p2.y) <= 2) {
                    currentValue = value2;
                }
                else {
                    p1start = p1;
                    p2start = p2;
                    lastX = e.x;
                    lastY = e.y;
                }
            }
            editorActive = true;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IEditableChartObject#handleMouseUp(org.eclipsetrader.ui.charts.ChartToolEditor.ChartObjectEditorEvent)
         */
        @Override
        public void handleMouseUp(ChartObjectEditorEvent e) {
            currentValue = null;
            lastX = -1;
            lastY = -1;
            editorActive = false;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IEditableChartObject#handleMouseMove(org.eclipsetrader.ui.charts.ChartToolEditor.ChartObjectEditorEvent)
         */
        @Override
        public void handleMouseMove(ChartObjectEditorEvent e) {
            if (editorActive && value1 != null && value2 != null) {
                if (p1 != null && p2 != null) {
                    e.canvas.redraw();
                    e.canvas.update();
                }

                if (currentValue != null) {
                    currentValue.setDate(e.date);
                    currentValue.setValue(e.value);
                }
                else {
                    value1.setDate((Date) e.graphics.mapToHorizontalValue(p1start.x + e.x - lastX));
                    value1.setValue((Number) e.graphics.mapToVerticalValue(p1start.y + e.y - lastY));

                    value2.setDate((Date) e.graphics.mapToHorizontalValue(p2start.x + e.x - lastX));
                    value2.setValue((Number) e.graphics.mapToVerticalValue(p2start.y + e.y - lastY));
                }

                p1 = e.graphics.mapToPoint(value1.getDate(), value1.getValue());
                p2 = e.graphics.mapToPoint(value2.getDate(), value2.getValue());
                paintShape(e.graphics);
            }
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#invalidate()
         */
        @Override
        public void invalidate() {
        }

        protected void paintShape(IGraphics graphics) {
            if (p1 != null && p2 != null) {
                graphics.pushState();
                graphics.setForegroundColor(color);

                graphics.drawLine(p1.x, p1.y, p2.x, p2.y);

                int size = p2.y - p1.y;
                graphics.drawArc(p1.x - size, p1.y - size, size * 2, size * 2, 0, 180);

                String s = NLS.bind("{0} - {1}", new Object[] { //$NON-NLS-1$
                        percentageFormat.format(1.0),
                        numberFormat.format(value2.getValue()),
                });
                Point p = graphics.stringExtent(s);
                graphics.drawString(s, p1.x, p2.y - p.y);

                factorLine = new int[factors.length];
                double range = value1.getValue().doubleValue() - value2.getValue().doubleValue();
                for (int i = 0; i < factors.length; i++) {
                    double value = value2.getValue().doubleValue() + factors[i] * range;
                    factorLine[i] = graphics.mapToVerticalAxis(value);

                    size = factorLine[i] - p1.y;
                    graphics.drawArc(p1.x - size, p1.y - size, size * 2, size * 2, 0, 180);

                    s = NLS.bind("{0} - {1}", new Object[] { //$NON-NLS-1$
                            percentageFormat.format(factors[i]),
                            numberFormat.format(value),
                    });
                    p = graphics.stringExtent(s);
                    graphics.drawString(s, p1.x, factorLine[i] - p.y);
                }

                if (hasFocus()) {
                    graphics.setBackgroundColor(color);
                    graphics.fillRectangle(p1.x - 2, p1.y - 2, 5, 5);
                    graphics.fillRectangle(p2.x - 2, p2.y - 2, 5, 5);
                }

                graphics.popState();
            }
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#accept(org.eclipsetrader.ui.charts.IChartObjectVisitor)
         */
        @Override
        public void accept(IChartObjectVisitor visitor) {
            visitor.visit(this);
        }
    }

    public FiboarcToolFactory() {
        object = new FiboarcToolObject();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id"); //$NON-NLS-1$
        factoryName = config.getAttribute("name"); //$NON-NLS-1$
        name = config.getAttribute("name"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#createObject(org.eclipsetrader.core.charts.IDataSeries)
     */
    @Override
    public IChartObject createObject(IDataSeries source) {
        return object;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getParameters()
     */
    @Override
    public IChartParameters getParameters() {
        ChartParameters parameters = new ChartParameters();

        if (!factoryName.equals(name)) {
            parameters.setParameter("name", name); //$NON-NLS-1$
        }

        if (value1 != null) {
            parameters.setParameter("d1", value1.getDate()); //$NON-NLS-1$
            parameters.setParameter("v1", value1.getValue()); //$NON-NLS-1$
        }
        if (value2 != null) {
            parameters.setParameter("d2", value2.getDate()); //$NON-NLS-1$
            parameters.setParameter("v2", value2.getValue()); //$NON-NLS-1$
        }

        if (color != null) {
            parameters.setParameter("color", color); //$NON-NLS-1$
        }

        return parameters;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
     */
    @Override
    public void setParameters(IChartParameters parameters) {
        name = parameters.hasParameter("name") ? parameters.getString("name") : name; //$NON-NLS-1$ //$NON-NLS-2$

        Date d1 = parameters.hasParameter("d1") ? parameters.getDate("d1") : null; //$NON-NLS-1$ //$NON-NLS-2$
        Number v1 = parameters.hasParameter("v1") ? parameters.getDouble("v1") : null; //$NON-NLS-1$ //$NON-NLS-2$
        if (d1 != null && v1 != null) {
            value1 = new Value(d1, v1);
        }

        Date d2 = parameters.hasParameter("d2") ? parameters.getDate("d2") : null; //$NON-NLS-1$ //$NON-NLS-2$
        Number v2 = parameters.hasParameter("v2") ? parameters.getDouble("v2") : null; //$NON-NLS-1$ //$NON-NLS-2$
        if (d2 != null && v2 != null) {
            value2 = new Value(d2, v2);
        }

        color = parameters.hasParameter("color") ? parameters.getColor("color") : null; //$NON-NLS-1$ //$NON-NLS-2$

        object.invalidate();
    }
}
