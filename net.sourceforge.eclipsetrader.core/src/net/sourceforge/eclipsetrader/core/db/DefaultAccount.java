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


/**
 * Default account implementation.
 * <p>Used as a fallback when it is not possible to restore a plugin-created account</p>
 */
public class DefaultAccount extends Account
{

    public DefaultAccount()
    {
    }

    public DefaultAccount(Integer id)
    {
        super(id);
    }

    public DefaultAccount(Account account)
    {
        super(account);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.Account#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        return new DefaultAccount(this);
    }
}
