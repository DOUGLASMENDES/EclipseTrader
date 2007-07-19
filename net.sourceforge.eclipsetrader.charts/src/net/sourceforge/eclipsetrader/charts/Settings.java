/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.charts;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 */
public class Settings
{
    private Map map = new HashMap();
    private DateFormat dateParser = new SimpleDateFormat("dd/MM/yy HH:mm"); //$NON-NLS-1$
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm"); //$NON-NLS-1$

    public Settings()
    {
    }

    public void set(String key, String value)
    {
        map.put(key, value);
    }

    public void set(String key, int value)
    {
        map.put(key, String.valueOf(value));
    }

    public void set(String key, double value)
    {
        map.put(key, String.valueOf(value));
    }

    public void set(String key, Double value)
    {
        map.put(key, String.valueOf(value));
    }
    
    public void set(String key, Color color)
    {
        map.put(key, String.valueOf(color.getRed()) + "," + String.valueOf(color.getGreen()) + "," + String.valueOf(color.getBlue())); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public void set(String key, RGB color)
    {
        map.put(key, String.valueOf(color.red) + "," + String.valueOf(color.green) + "," + String.valueOf(color.blue)); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public void set(String key, Date date)
    {
        map.put(key, dateFormat.format(date));
    }
    
    public void set(String key, int r, int g, int b)
    {
        map.put(key, String.valueOf(r) + "," + String.valueOf(g) + "," + String.valueOf(b)); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public void set(String key, boolean value)
    {
        map.put(key, value ? "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public void remove(String key)
    {
        map.remove(key);
    }
    
    public String getString(String key, String defaultValue)
    {
        String result = (String)map.get(key);
        if (result == null)
            result = defaultValue;
        return result;
    }
    
    public String getString(String key, int defaultValue)
    {
        String result = (String)map.get(key);
        if (result == null)
            result = String.valueOf(defaultValue);
        return result;
    }
    
    public Integer getInteger(String key, int defaultValue)
    {
        String value = (String)map.get(key);
        if (value == null)
            return new Integer(defaultValue);
        return new Integer(value);
    }
    
    public Double getDouble(String key, double defaultValue)
    {
        String value = (String)map.get(key);
        if (value == null)
            return new Double(defaultValue);
        return new Double(value);
    }
    
    public Double getDouble(String key, Double defaultValue)
    {
        String value = (String)map.get(key);
        if (value == null)
            return defaultValue;
        return new Double(value);
    }
    
    public boolean getBoolean(String key, boolean defaultValue)
    {
        String value = (String)map.get(key);
        if (value == null)
            return defaultValue;
        return value.equals("1"); //$NON-NLS-1$
    }
    
    public void setColor(String key, String value)
    {
        map.put(key, value);
    }
    
    public Color getColor(String key, int r, int g, int b)
    {
        String value = (String)map.get(key);
        if (value == null)
            return new Color(null, r, g, b);

        String[] ar = value.split(","); //$NON-NLS-1$
        return new Color(null, Integer.parseInt(ar[0]), Integer.parseInt(ar[1]), Integer.parseInt(ar[2]));
    }
    
    public Color getColor(String key, RGB rgb)
    {
        String value = (String)map.get(key);
        if (value == null)
            return new Color(null, rgb);

        String[] ar = value.split(","); //$NON-NLS-1$
        return new Color(null, Integer.parseInt(ar[0]), Integer.parseInt(ar[1]), Integer.parseInt(ar[2]));
    }
    
    public Color getColor(String key, Color defaultValue)
    {
        String value = (String)map.get(key);
        if (value == null)
            return defaultValue;

        String[] ar = value.split(","); //$NON-NLS-1$
        return new Color(null, Integer.parseInt(ar[0]), Integer.parseInt(ar[1]), Integer.parseInt(ar[2]));
    }
    
    public Date getDate(String key, Date defaultValue)
    {
        String value = (String)map.get(key);
        if (value == null)
            return defaultValue;
        try {
            return dateParser.parse(value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void setDate(String key, String value)
    {
        map.put(key, value);
    }

    public Set keySet()
    {
        return map.keySet();
    }
    
    public Map getMap()
    {
        return new HashMap(map);
    }
}
