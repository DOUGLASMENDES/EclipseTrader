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

package net.sourceforge.eclipsetrader.fix.core;

import java.io.InputStream;

import net.sourceforge.eclipsetrader.fix.FixPlugin;

import org.apache.commons.logging.Log;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IStartup;

import quickfix.Acceptor;
import quickfix.DefaultMessageFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.SLF4JLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.SocketInitiator;

public class ExecutorStartup implements IStartup
{
    Acceptor acceptor;
    private static ExecutorStartup instance;
    private Log log = org.apache.commons.logging.LogFactory.getLog(getClass());

    public ExecutorStartup()
    {
    }

    public static ExecutorStartup getInstance()
    {
        if (instance == null)
            instance = new ExecutorStartup();
        return instance;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IStartup#earlyStartup()
     */
    public void earlyStartup()
    {
        instance = this;
        if (FixPlugin.getDefault().getPreferenceStore().getBoolean(FixPlugin.PREFS_ENABLE_EXECUTOR))
            start();
    }
    
    public void start()
    {
        InputStream inputStream = getClass().getResourceAsStream("executor.cfg");
        try {
            SessionSettings settings = new SessionSettings(inputStream);
            settings.setString("FileStorePath", Platform.getLocation().append(settings.getString("FileStorePath")).toOSString());

            quickfix.examples.executor.Application application = new quickfix.examples.executor.Application(settings);
            
            MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
            LogFactory logFactory = new SLF4JLogFactory(settings);
            MessageFactory messageFactory = new DefaultMessageFactory();

            acceptor = new SocketAcceptor(application, messageStoreFactory, settings, logFactory, messageFactory);
            acceptor.start();
        }
        catch(Exception e) {
            log.error("error starting acceptor", e);
        }
        
        inputStream = BanzaiTradingProvider.class.getResourceAsStream("banzai.cfg");
        try {
            BanzaiTradingProvider application = (BanzaiTradingProvider)new BanzaiTradingProvider().create();
            try
            {
                SessionSettings settings = new SessionSettings(inputStream);
                settings.setString("FileStorePath", Platform.getLocation().append(settings.getString("FileStorePath")).toOSString());

                MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
                LogFactory logFactory = new SLF4JLogFactory(settings);
                application.messageFactory = new DefaultMessageFactory();

                application.initiator = new SocketInitiator(application, messageStoreFactory, settings, logFactory, application.messageFactory);
                application.initiator.start();
            }
            catch (Exception e) {
                log.error("error starting initiator", e);
            }
        }
        catch(Exception e) {
            log.error("error starting initiator", e);
        }
    }
    
    public void stop()
    {
        try {
            BanzaiTradingProvider application = (BanzaiTradingProvider)new BanzaiTradingProvider().create();
            if (application.initiator != null)
                application.initiator.stop();
        }
        catch(Exception e) {
            log.error("error stopping initiator", e);
        }
        
        if (acceptor != null)
        {
            acceptor.stop();
            acceptor = null;
        }
    }
}
