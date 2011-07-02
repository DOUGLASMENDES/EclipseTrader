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

package org.eclipsetrader.ui.charts;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.RGB;
import org.eclipsetrader.core.charts.repository.IParameter;
import org.eclipsetrader.core.internal.charts.repository.Parameter;

public class ChartParameters implements IChartParameters {

    private Map<String, String> map = new HashMap<String, String>();
    private NumberFormat nf = NumberFormat.getInstance(Locale.US);
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$

    public ChartParameters() {
        nf.setMinimumFractionDigits(0);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.model.IChartParameters#getParameterNames()
     */
    @Override
    public String[] getParameterNames() {
        Set<String> c = map.keySet();
        return c.toArray(new String[c.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.model.IChartParameters#getString(java.lang.String)
     */
    @Override
    public String getString(String name) {
        return map.get(name);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.model.IChartParameters#getInteger(java.lang.String)
     */
    @Override
    public Integer getInteger(String name) {
        try {
            return map.containsKey(name) ? new Integer(nf.parse(map.get(name)).intValue()) : null;
        } catch (ParseException e) {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.model.IChartParameters#getDouble(java.lang.String)
     */
    @Override
    public Double getDouble(String name) {
        try {
            return map.containsKey(name) ? new Double(nf.parse(map.get(name)).doubleValue()) : null;
        } catch (ParseException e) {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.model.IChartParameters#getColor(java.lang.String)
     */
    @Override
    public RGB getColor(String name) {
        String value = map.get(name);
        if (value != null) {
            String[] ar = value.split(","); //$NON-NLS-1$
            if (ar.length == 3) {
                return new RGB(Integer.parseInt(ar[0]), Integer.parseInt(ar[1]), Integer.parseInt(ar[2]));
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.model.IChartParameters#hasParameter(java.lang.String)
     */
    @Override
    public boolean hasParameter(String name) {
        return map.containsKey(name);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.model.IChartParameters#setParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void setParameter(String name, String value) {
        if (value != null) {
            map.put(name, value);
        }
        else {
            map.remove(name);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.model.IChartParameters#setParameter(java.lang.String, java.lang.Number)
     */
    @Override
    public void setParameter(String name, Number value) {
        if (value != null) {
            map.put(name, nf.format(value));
        }
        else {
            map.remove(name);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.model.IChartParameters#setParameter(java.lang.String, org.eclipse.swt.graphics.RGB)
     */
    @Override
    public void setParameter(String name, RGB value) {
        if (value != null) {
            map.put(name, value.red + "," + value.green + "," + value.blue); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else {
            map.remove(name);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartParameters#getDate(java.lang.String)
     */
    @Override
    public Date getDate(String name) {
        String s = map.get(name);
        if (s != null) {
            try {
                return dateFormat.parse(s);
            } catch (Exception e) {
                // Do nothing
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartParameters#setParameter(java.lang.String, java.util.Date)
     */
    @Override
    public void setParameter(String name, Date value) {
        if (value != null) {
            map.put(name, dateFormat.format(value));
        }
        else {
            map.remove(name);
        }
    }

    public IParameter[] toParametersArray() {
        List<Parameter> params = new ArrayList<Parameter>();
        for (String name : map.keySet()) {
            params.add(new Parameter(name, map.get(name)));
        }
        return params.toArray(new IParameter[params.size()]);
    }
}
