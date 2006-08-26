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

package net.sourceforge.eclipsetrader.core;

import net.sourceforge.eclipsetrader.core.db.Order;

public interface ITradingProvider
{
    
    public abstract void setName(String name);
    
    public abstract String getName();

    public abstract void sendNew(Order order);

    public abstract void sendCancelRequest(Order order);

    public abstract void sendReplaceRequest(Order order);

}