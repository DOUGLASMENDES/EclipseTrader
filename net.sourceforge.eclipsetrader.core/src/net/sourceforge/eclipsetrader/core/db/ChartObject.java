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

import java.util.HashMap;
import java.util.Map;


public class ChartObject extends PersistentObject
{
    private ChartTab parent;
    private String pluginId;
    private Map parameters = new HashMap();

    public ChartObject()
    {
    }

    public ChartObject(Integer id)
    {
        super(id);
    }

    public ChartTab getParent()
    {
        return parent;
    }

    public void setParent(ChartTab chartTab)
    {
        this.parent = chartTab;
    }

    public String getPluginId()
    {
        return pluginId;
    }

    public void setPluginId(String pluginId)
    {
        this.pluginId = pluginId;
        setChanged();
    }

    public Map getParameters()
    {
        return parameters;
    }

    public void setParameters(Map parameters)
    {
        this.parameters = parameters;
        setChanged();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.PersistentObject#setChanged()
     */
    public synchronized void setChanged()
    {
        super.setChanged();
        if (getParent() != null)
            getParent().setChanged();
    }
}
