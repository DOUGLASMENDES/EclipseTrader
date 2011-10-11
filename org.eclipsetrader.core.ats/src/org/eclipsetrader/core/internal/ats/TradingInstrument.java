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

package org.eclipsetrader.core.internal.ats;

import org.eclipsetrader.core.ats.ITradingInstrument;
import org.eclipsetrader.core.instruments.ISecurity;

public class TradingInstrument implements ITradingInstrument {

    private final ISecurity instrument;

    public TradingInstrument(ISecurity instrument) {
        this.instrument = instrument;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingInstrument#getInstrument()
     */
    @Override
    public ISecurity getInstrument() {
        return instrument;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(instrument.getClass())) {
            return instrument;
        }
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }
        return null;
    }
}
