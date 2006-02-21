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


public class Level2Ask extends Level2
{

    public Level2Ask()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.Level2#add(double, int, int, java.lang.String)
     */
    public void add(double price, int quantity, int number, String id)
    {
        for (int i = 0; i < list.size(); i++)
        {
            if (((Item)list.get(i)).price > price)
            {
                list.add(i, new Item(price, quantity, number, id));
                return;
            }
        }
        list.add(new Item(price, quantity, number, id));
    }
}
