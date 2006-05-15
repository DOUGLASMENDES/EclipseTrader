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

package net.sourceforge.eclipsetrader.core.ui;

import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.PortfolioPosition;

public class PortfolioPositionSelection extends AccountSelection
{
    private PortfolioPosition position;

    public PortfolioPositionSelection(Account account, PortfolioPosition position)
    {
        super(account);
        this.position = position;
    }

    public PortfolioPosition getPosition()
    {
        return position;
    }
}
