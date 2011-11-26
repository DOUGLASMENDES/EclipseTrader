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
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrderType;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class MarketOrderFunction extends BaseOrderFunction {

    private static final long serialVersionUID = -3066394785133355696L;

    public MarketOrderFunction() {
    }

    public MarketOrderFunction(IBroker broker, IAccount account, ISecurity instrument) {
        super(broker, account, instrument);
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr) throws Exception {
        Object property = ScriptableObject.getProperty(getTopLevelScope(ctorObj), PROPERTY_BROKER);
        IBroker broker = (IBroker) Context.jsToJava(property, IBroker.class);

        property = ScriptableObject.getProperty(getTopLevelScope(ctorObj), PROPERTY_ACCOUNT);
        IAccount account = (IAccount) Context.jsToJava(property, IAccount.class);

        property = ScriptableObject.getProperty(getTopLevelScope(ctorObj), PROPERTY_INSTRUMENT);
        ISecurity instrument = (ISecurity) Context.jsToJava(property, ISecurity.class);

        MarketOrderFunction result = new MarketOrderFunction(broker, account, instrument);
        result.type = IOrderType.Market;

        int index = 0;
        if (args.length >= index + 1) {
            result.jsSet_side(args[index]);
            index++;
        }
        if (args.length >= index + 1) {
            result.jsSet_quantity(args[index]);
            index++;
        }
        if (args.length >= index + 1) {
            result.jsSet_text(args[index]);
            index++;
        }

        if (!inNewExpr) {
            result.jsFunction_send();
            return null;
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.ScriptableObject#getClassName()
     */
    @Override
    public String getClassName() {
        return "MarketOrder"; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.engines.AbstractOrderFunction#jsFunction_send()
     */
    @Override
    public Object jsFunction_send() throws Exception {
        return super.jsFunction_send();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.engines.BaseOrderFunction#jsFunction_cancel()
     */
    @Override
    public Object jsFunction_cancel() throws Exception {
        return super.jsFunction_cancel();
    }
}
