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

package net.sourceforge.eclipsetrader.trading.internal;

import java.util.List;

import net.sourceforge.eclipsetrader.core.IAccountProvider;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.PersistentPreferenceStore;

public class SimpleAccountProvider implements IAccountProvider
{

    public SimpleAccountProvider()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IAccountProvider#createAccount(net.sourceforge.eclipsetrader.core.db.PersistentPreferenceStore, java.util.List)
     */
    public Account createAccount(PersistentPreferenceStore preferenceStore, List transactions)
    {
        Account account = new SimpleAccount();
        account.setPreferenceStore(preferenceStore);
        account.setTransactions(transactions);
        return account;
    }
}
