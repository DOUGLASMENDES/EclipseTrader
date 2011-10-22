/*
 * Copyright (c) 2005-2011 Real Time Risk Systems LLC
 * All Rights Reserved
 */

package org.eclipsetrader.ui;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.swt.widgets.Display;

public class QueuedRealm extends Realm {

    static final int GROW_SIZE = 4;
    static final int MESSAGE_LIMIT = 64;
    static final int DELAY = 100;
    static final Realm instance = new QueuedRealm();

    private Display display;

    int messageCount;
    Runnable[] messages = new Runnable[4];
    private Object messageLock = new Object();

    private Runnable delayedRunnable = new Runnable() {

        @Override
        public void run() {
            Runnable runnable;
            while ((runnable = removeFirst()) != null) {
                runnable.run();
            }
        }
    };

    public static Realm getInstance() {
        return instance;
    }

    private QueuedRealm() {
        this.display = Display.getDefault();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.Realm#isCurrent()
     */
    @Override
    public boolean isCurrent() {
        return Display.getCurrent() == display;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.Realm#exec(java.lang.Runnable)
     */
    @Override
    public void exec(Runnable runnable) {
        addLast(runnable);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.Realm#asyncExec(java.lang.Runnable)
     */
    @Override
    public void asyncExec(Runnable runnable) {
        addLast(runnable);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.Realm#syncExec(java.lang.Runnable)
     */
    @Override
    protected void syncExec(Runnable runnable) {
        display.syncExec(runnable);
    }

    void addLast(Runnable runnable) {
        boolean wake = false;
        synchronized (messageLock) {
            if (messages == null) {
                messages = new Runnable[GROW_SIZE];
            }
            if (messageCount == messages.length) {
                Runnable[] newMessages = new Runnable[messageCount + GROW_SIZE];
                System.arraycopy(messages, 0, newMessages, 0, messageCount);
                messages = newMessages;
            }
            messages[messageCount++] = runnable;
            wake = messageCount == 1;
        }
        if (wake) {
            display.timerExec(DELAY, delayedRunnable);
        }
    }

    Runnable removeFirst() {
        synchronized (messageLock) {
            if (messageCount == 0) {
                return null;
            }
            Runnable lock = messages[0];
            System.arraycopy(messages, 1, messages, 0, --messageCount);
            messages[messageCount] = null;
            if (messageCount == 0) {
                if (messages.length > MESSAGE_LIMIT) {
                    messages = null;
                }
            }
            return lock;
        }
    }
}
