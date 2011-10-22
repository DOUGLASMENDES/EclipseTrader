/*
 * Copyright (c) 2005-2011 Real Time Risk Systems LLC
 * All Rights Reserved
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
