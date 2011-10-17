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

package org.eclipsetrader.core.trading;

import java.util.Map;

import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITrade;

/**
 * Interface implemented by alerts.
 * 
 * @since 1.0
 */
public interface IAlert {

    public String getId();

    public String getName();

    public String getDescription();

    public void setParameters(Map<String, Object> map);

    public Map<String, Object> getParameters();

    public void setInitialValues(ITrade trade, IQuote quote);

    public void setTrade(ITrade trade);

    public void setQuote(IQuote quote);

    public boolean isTriggered();
}
