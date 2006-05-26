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

package net.sourceforge.eclipsetrader.trading;

import net.sourceforge.eclipsetrader.core.db.trading.TradingSystem;

public class TradingSystemSelection extends TradingSystemGroupSelection
{
    private TradingSystem system;

    public TradingSystemSelection(TradingSystem system)
    {
        super(system.getGroup());
        this.system = system;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelection#isEmpty()
     */
    public boolean isEmpty()
    {
        return system == null;
    }

    public TradingSystem getSystem()
    {
        return system;
    }
    
    public TradingSystemPlugin getTradingSystemPlugin()
    {
        if (system.getData() == null)
            system.setData(TradingPlugin.createTradingSystemPlugin(system));
        return (TradingSystemPlugin) system.getData();
    }
}
