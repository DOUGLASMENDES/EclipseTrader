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

package org.eclipsetrader.core.ats;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of the <code>ITradeStrategyParameters</code> interface.
 *
 * @since 1.0
 */
public class TradeStrategyParameters implements ITradeStrategyParameters {
	private Map<String, String> map = new HashMap<String, String>();
	private NumberFormat nf = NumberFormat.getInstance(Locale.US);
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public TradeStrategyParameters() {
		nf.setMinimumFractionDigits(0);
	}

	public TradeStrategyParameters(ITradeSystemParameter[] parameters) {
		this();

		for (ITradeSystemParameter param : parameters)
			setParameter(param.getName(), param.getValue());
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.ats.ITradeStrategyParameters#getParameterNames()
	 */
	public String[] getParameterNames() {
		Set<String> c = map.keySet();
		return c.toArray(new String[c.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.ats.ITradeStrategyParameters#getDate(java.lang.String)
	 */
	public Date getDate(String name) {
    	String s = map.get(name);
    	if (s != null) {
    		try {
    			return dateFormat.parse(s);
    		} catch(Exception e) {
    			// Do nothing
    		}
    	}
	    return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.ats.ITradeStrategyParameters#getDouble(java.lang.String)
	 */
	public Double getDouble(String name) {
		try {
	        return map.containsKey(name) ? new Double(nf.parse(map.get(name)).doubleValue()) : null;
        } catch (ParseException e) {
	        return null;
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.ats.ITradeStrategyParameters#getInteger(java.lang.String)
	 */
	public Integer getInteger(String name) {
		try {
	        return map.containsKey(name) ? new Integer(nf.parse(map.get(name)).intValue()) : null;
        } catch (ParseException e) {
	        return null;
        }
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeStrategyParameters#getFloat(java.lang.String)
     */
    public Float getFloat(String name) {
		try {
	        return map.containsKey(name) ? new Float(nf.parse(map.get(name)).floatValue()) : null;
        } catch (ParseException e) {
	        return null;
        }
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeStrategyParameters#getLong(java.lang.String)
     */
    public Long getLong(String name) {
		try {
	        return map.containsKey(name) ? new Long(nf.parse(map.get(name)).longValue()) : null;
        } catch (ParseException e) {
	        return null;
        }
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.ats.ITradeStrategyParameters#getString(java.lang.String)
	 */
	public String getString(String name) {
		return map.get(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.ats.ITradeStrategyParameters#hasParameter(java.lang.String)
	 */
	public boolean hasParameter(String name) {
		return map.containsKey(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.ats.ITradeStrategyParameters#setParameter(java.lang.String, java.lang.String)
	 */
	public void setParameter(String name, String value) {
		if (value != null)
			map.put(name, value);
		else
			map.remove(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.ats.ITradeStrategyParameters#setParameter(java.lang.String, java.lang.Number)
	 */
	public void setParameter(String name, Number value) {
		if (value != null)
			map.put(name, nf.format(value));
		else
			map.remove(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.ats.ITradeStrategyParameters#setParameter(java.lang.String, java.util.Date)
	 */
	public void setParameter(String name, Date value) {
		if (value != null)
			map.put(name, dateFormat.format(value));
		else
			map.remove(name);
	}
}
