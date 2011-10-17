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

package org.eclipsetrader.core.ats.engines;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IPosition;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

public class HasPositionFunction extends ScriptableObject {

    private static final long serialVersionUID = 237641674017402922L;

    public HasPositionFunction() {
    }

    public static Object jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr) {
        IAccount account = (IAccount) ScriptableObject.getProperty(getTopLevelScope(ctorObj), BaseOrderFunction.PROPERTY_ACCOUNT);

        ISecurity security;
        if (args.length >= 1) {
            security = (ISecurity) args[0];
        }
        else {
            Object property = ScriptableObject.getProperty(getTopLevelScope(ctorObj), BaseOrderFunction.PROPERTY_INSTRUMENT);
            if (!(property instanceof ISecurity)) {
                return new Boolean(false);
            }
            security = (ISecurity) property;
        }

        for (IPosition position : account.getPositions()) {
            if (position.getSecurity() == security) {
                return new Boolean(true);
            }
        }

        return new Boolean(false);
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.ScriptableObject#getClassName()
     */
    @Override
    public String getClassName() {
        return "hasPosition";
    }
}
