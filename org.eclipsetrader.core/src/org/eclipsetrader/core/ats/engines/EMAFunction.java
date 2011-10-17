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

import java.util.List;

import org.eclipsetrader.core.feed.IBar;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

public class EMAFunction extends MAFunction {

    private static final long serialVersionUID = 1473914377234449997L;

    public EMAFunction() {
    }

    public EMAFunction(List<IBar> bars, int type, int period) {
        super(bars, type, period);
    }

    @SuppressWarnings("unchecked")
    public static Object jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr) {
        List<IBar> bars = (List<IBar>) ScriptableObject.getProperty(getTopLevelScope(ctorObj), JavaScriptEngineInstrument.PROPERTY_BARS);

        if (args.length < 1) {
            return null;
        }

        int period = (int) Context.toNumber(args[0]);

        EMAFunction result = new EMAFunction(bars, Util.TYPE_EMA, period);

        return result;
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.ScriptableObject#getClassName()
     */
    @Override
    public String getClassName() {
        return "EMA";
    }

    public Object jsFunction_crosses(MAFunction other, Object value) throws Exception {
        if (value instanceof IBar) {
            return super.jsFunction_crosses(other, (IBar) value);
        }
        return super.jsFunction_crosses(other, Context.toNumber(value));
    }
}
