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

package org.eclipsetrader.ui;

import org.eclipse.core.databinding.observable.Realm;

public class NullRealm extends Realm {

    public static final Realm instance = new NullRealm();

    public static Realm getInstance() {
        return instance;
    }

    private NullRealm() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.Realm#isCurrent()
     */
    @Override
    public boolean isCurrent() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.Realm#exec(java.lang.Runnable)
     */
    @Override
    public void exec(Runnable runnable) {
        runnable.run();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.Realm#asyncExec(java.lang.Runnable)
     */
    @Override
    public void asyncExec(Runnable runnable) {
        runnable.run();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.Realm#syncExec(java.lang.Runnable)
     */
    @Override
    protected void syncExec(Runnable runnable) {
        runnable.run();
    }
}
