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

package net.sourceforge.eclipsetrader.core.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;

/**
 * A concrete preference store implementation based on an internal
 * <code>java.util.Map</code> object.
 * <p>
 * This class was not designed to be subclassed.
 * </p>
 * 
 * @see IPreferenceStore
 */
public class PersistentPreferenceStore implements IPreferenceStore
{
    /**
     * List of registered listeners (element type:
     * <code>IPropertyChangeListener</code>). These listeners are to be
     * informed when the current value of a preference changes.
     */
    private ListenerList listeners = new ListenerList();

    /**
     * The mapping from preference name to preference value (represented as
     * strings).
     */
    private Map properties = new HashMap();

    /**
     * The mapping from preference name to default preference value (represented
     * as strings).
     */
    private Map defaultProperties = new HashMap();

    /**
     * Indicates whether a value as been changed by <code>setToDefault</code>
     * or <code>setValue</code>; initially <code>false</code>.
     */
    private boolean dirty = false;

    /**
     * Creates an empty preference store.
     */
    public PersistentPreferenceStore()
    {
    }

    /**
     * Creates an preference store that is the exact copy of the given preference store.
     */
    public PersistentPreferenceStore(PersistentPreferenceStore preferenceStore)
    {
        properties.putAll(preferenceStore.properties);
        defaultProperties.putAll(preferenceStore.defaultProperties);
    }
    
    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener)
    {
        listeners.add(listener);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public boolean contains(String name)
    {
        return (properties.containsKey(name) || defaultProperties.containsKey(name));
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void firePropertyChangeEvent(String name, Object oldValue, Object newValue)
    {
        final Object[] finalListeners = this.listeners.getListeners();
        // Do we need to fire an event.
        if (finalListeners.length > 0 && (oldValue == null || !oldValue.equals(newValue)))
        {
            final PropertyChangeEvent pe = new PropertyChangeEvent(this, name, oldValue, newValue);
            for (int i = 0; i < finalListeners.length; ++i)
            {
                final IPropertyChangeListener l = (IPropertyChangeListener) finalListeners[i];
                SafeRunnable.run(new SafeRunnable(JFaceResources.getString("PreferenceStore.changeError")) { //$NON-NLS-1$
                    public void run()
                    {
                        l.propertyChange(pe);
                    }
                });
            }
        }
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public boolean getBoolean(String name)
    {
        return getBoolean(properties, name);
    }

    /**
     * Helper function: gets boolean for a given name.
     * 
     * @param p
     * @param name
     * @return boolean
     */
    private boolean getBoolean(Map p, String name)
    {
        String value = (String)p.get(name);
        if (value == null)
            return BOOLEAN_DEFAULT_DEFAULT;
        if (value.equals(IPreferenceStore.TRUE))
            return true;
        return false;
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public boolean getDefaultBoolean(String name)
    {
        return getBoolean(defaultProperties, name);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public double getDefaultDouble(String name)
    {
        return getDouble(defaultProperties, name);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public float getDefaultFloat(String name)
    {
        return getFloat(defaultProperties, name);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public int getDefaultInt(String name)
    {
        return getInt(defaultProperties, name);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public long getDefaultLong(String name)
    {
        return getLong(defaultProperties, name);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public String getDefaultString(String name)
    {
        return getString(defaultProperties, name);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public double getDouble(String name)
    {
        return getDouble(properties, name);
    }

    /**
     * Helper function: gets double for a given name.
     * 
     * @param p
     * @param name
     * @return double
     */
    private double getDouble(Map p, String name)
    {
        String value = (String)p.get(name);
        if (value == null)
            return DOUBLE_DEFAULT_DEFAULT;
        double ival = DOUBLE_DEFAULT_DEFAULT;
        try
        {
            ival = new Double(value).doubleValue();
        }
        catch (NumberFormatException e)
        {
        }
        return ival;
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public float getFloat(String name)
    {
        return getFloat(properties, name);
    }

    /**
     * Helper function: gets float for a given name.
     * @param p
     * @param name
     * @return float
     */
    private float getFloat(Map p, String name)
    {
        String value = (String)p.get(name);
        if (value == null)
            return FLOAT_DEFAULT_DEFAULT;
        float ival = FLOAT_DEFAULT_DEFAULT;
        try
        {
            ival = new Float(value).floatValue();
        }
        catch (NumberFormatException e)
        {
        }
        return ival;
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public int getInt(String name)
    {
        return getInt(properties, name);
    }

    /**
     * Helper function: gets int for a given name.
     * @param p
     * @param name
     * @return int
     */
    private int getInt(Map p, String name)
    {
        String value = (String)p.get(name);
        if (value == null)
            return INT_DEFAULT_DEFAULT;
        int ival = 0;
        try
        {
            ival = Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
        }
        return ival;
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public long getLong(String name)
    {
        return getLong(properties, name);
    }

    /**
     * Helper function: gets long for a given name.
     * @param p
     * @param name
     * @return
     */
    private long getLong(Map p, String name)
    {
        String value = (String)p.get(name);
        if (value == null)
            return LONG_DEFAULT_DEFAULT;
        long ival = LONG_DEFAULT_DEFAULT;
        try
        {
            ival = Long.parseLong(value);
        }
        catch (NumberFormatException e)
        {
        }
        return ival;
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public String getString(String name)
    {
        return getString(properties, name);
    }

    /**
     * Helper function: gets string for a given name.
     * @param p
     * @param name
     * @return
     */
    private String getString(Map p, String name)
    {
        String value = (String)p.get(name);
        if (value == null)
            return STRING_DEFAULT_DEFAULT;
        return value;
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public boolean isDefault(String name)
    {
        return (!properties.containsKey(name) && defaultProperties.containsKey(name));
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public boolean needsSaving()
    {
        return dirty;
    }

    /**
     * Returns an enumeration of all preferences known to this store which have
     * current values other than their default value.
     * 
     * @return an array of preference names
     */
    public String[] preferenceNames()
    {
        ArrayList list = new ArrayList();
        Iterator it = properties.keySet().iterator();
        while (it.hasNext())
        {
            list.add(it.next());
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void putValue(String name, String value)
    {
        String oldValue = getString(name);
        if (oldValue == null || !oldValue.equals(value))
        {
            setValue(properties, name, value);
            dirty = true;
        }
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener)
    {
        listeners.remove(listener);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setDefault(String name, double value)
    {
        setValue(defaultProperties, name, value);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setDefault(String name, float value)
    {
        setValue(defaultProperties, name, value);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setDefault(String name, int value)
    {
        setValue(defaultProperties, name, value);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setDefault(String name, long value)
    {
        setValue(defaultProperties, name, value);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setDefault(String name, String value)
    {
        setValue(defaultProperties, name, value);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setDefault(String name, boolean value)
    {
        setValue(defaultProperties, name, value);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setToDefault(String name)
    {
        Object oldValue = properties.get(name);
        properties.remove(name);
        dirty = true;
        Object newValue = null;
        if (defaultProperties != null)
            newValue = defaultProperties.get(name);
        firePropertyChangeEvent(name, oldValue, newValue);
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setValue(String name, double value)
    {
        double oldValue = getDouble(name);
        if (oldValue != value)
        {
            setValue(properties, name, value);
            dirty = true;
            firePropertyChangeEvent(name, new Double(oldValue), new Double(value));
        }
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setValue(String name, float value)
    {
        float oldValue = getFloat(name);
        if (oldValue != value)
        {
            setValue(properties, name, value);
            dirty = true;
            firePropertyChangeEvent(name, new Float(oldValue), new Float(value));
        }
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setValue(String name, int value)
    {
        int oldValue = getInt(name);
        if (oldValue != value)
        {
            setValue(properties, name, value);
            dirty = true;
            firePropertyChangeEvent(name, new Integer(oldValue), new Integer(value));
        }
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setValue(String name, long value)
    {
        long oldValue = getLong(name);
        if (oldValue != value)
        {
            setValue(properties, name, value);
            dirty = true;
            firePropertyChangeEvent(name, new Long(oldValue), new Long(value));
        }
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setValue(String name, String value)
    {
        String oldValue = getString(name);
        if (oldValue == null || !oldValue.equals(value))
        {
            setValue(properties, name, value);
            dirty = true;
            firePropertyChangeEvent(name, oldValue, value);
        }
    }

    /*
     * (non-Javadoc) Method declared on IPreferenceStore.
     */
    public void setValue(String name, boolean value)
    {
        boolean oldValue = getBoolean(name);
        if (oldValue != value)
        {
            setValue(properties, name, value);
            dirty = true;
            firePropertyChangeEvent(name, new Boolean(oldValue), new Boolean(value));
        }
    }

    /**
     * Helper method: sets value for a given name.
     * @param p
     * @param name
     * @param value
     */
    private void setValue(Map p, String name, double value)
    {
        p.put(name, Double.toString(value));
    }

    /**
     * Helper method: sets value for a given name.
     * @param p
     * @param name
     * @param value
     */
    private void setValue(Map p, String name, float value)
    {
        p.put(name, Float.toString(value));
    }

    /**
     * Helper method: sets value for a given name.
     * @param p
     * @param name
     * @param value
     */
    private void setValue(Map p, String name, int value)
    {
        p.put(name, Integer.toString(value));
    }

    /**
     * Helper method: sets the value for a given name.
     * @param p
     * @param name
     * @param value
     */
    private void setValue(Map p, String name, long value)
    {
        p.put(name, Long.toString(value));
    }

    /**
     * Helper method: sets the value for a given name.
     * @param p
     * @param name
     * @param value
     */
    private void setValue(Map p, String name, String value)
    {
        Assert.isTrue(value != null);
        p.put(name, value);
    }

    /**
     * Helper method: sets the value for a given name.
     * @param p
     * @param name
     * @param value
     */
    private void setValue(Map p, String name, boolean value)
    {
        p.put(name, value == true ? IPreferenceStore.TRUE : IPreferenceStore.FALSE);
    }
}
