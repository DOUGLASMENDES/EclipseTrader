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

package org.eclipsetrader.ui.internal.providers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class LastTradeTimeFactory extends LastTradeDateTimeFactory {

    public LastTradeTimeFactory() {
        super(DateFormat.getTimeInstance(SimpleDateFormat.MEDIUM));
    }
}
