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

package org.eclipsetrader.core.internal.markets;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.markets.IMarketStatusListener;
import org.eclipsetrader.core.markets.MarketStatusEvent;

public class MarketService extends Observable implements IMarketService, Runnable {

    public static final String REPOSITORY_FILE = "markets.xml"; //$NON-NLS-1$
    private static IMarketService instance;
    private List<Market> marketsList = new ArrayList<Market>();
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    private Thread thread;
    private boolean stopping = false;

    public MarketService() {
        instance = this;
    }

    public static IMarketService getInstance() {
        return instance;
    }

    public void startUp(IProgressMonitor monitor) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(MarketList.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                Status status = new Status(IStatus.WARNING, CoreActivator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                CoreActivator.getDefault().getLog().log(status);
                return true;
            }
        });
        MarketList list = null;
        File file = CoreActivator.getDefault().getStateLocation().append(REPOSITORY_FILE).toFile();
        if (file.exists()) {
            list = (MarketList) unmarshaller.unmarshal(file);
        }
        else {
            list = (MarketList) unmarshaller.unmarshal(FileLocator.openStream(CoreActivator.getDefault().getBundle(), new Path("data").append(REPOSITORY_FILE), false));
        }

        if (list != null) {
            this.marketsList = list.getList();
            for (Market market : this.marketsList) {
                if (market.getTimeZone() != null) {
                    market.setTimeZone(market.getTimeZone());
                }
            }
        }

        if (thread == null) {
            stopping = false;
            thread = new Thread(this);
            thread.start();
        }
    }

    public void shutDown(IProgressMonitor monitor) throws Exception {
        stopping = true;
        if (thread != null) {
            try {
                synchronized (thread) {
                    thread.notify();
                }
                thread.join(30 * 1000);
            } catch (InterruptedException e) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error stopping thread", e);
                CoreActivator.log(status);
            }
            thread = null;
        }

        File file = CoreActivator.getDefault().getStateLocation().append(REPOSITORY_FILE).toFile();
        if (file.exists()) {
            file.delete();
        }
        JAXBContext jaxbContext = JAXBContext.newInstance(MarketList.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                Status status = new Status(IStatus.WARNING, CoreActivator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                CoreActivator.getDefault().getLog().log(status);
                return true;
            }
        });
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$
        marshaller.marshal(new MarketList(marketsList), new FileWriter(file));
    }

    public void addMarket(Market market) {
        marketsList.add(market);
        setChanged();
        notifyObservers();
    }

    public void deleteMarket(Market market) {
        marketsList.remove(market);
        setChanged();
        notifyObservers();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarketService#getMarkets()
     */
    @Override
    public IMarket[] getMarkets() {
        return marketsList.toArray(new IMarket[marketsList.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarketService#getMarket(java.lang.String)
     */
    @Override
    public IMarket getMarket(String name) {
        for (Market market : marketsList) {
            if (market.getName().equals(name)) {
                return market;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarketService#getOpenMarkets()
     */
    @Override
    public IMarket[] getOpenMarkets() {
        List<IMarket> l = new ArrayList<IMarket>();
        for (Market market : marketsList) {
            if (market.isOpen()) {
                l.add(market);
            }
        }
        return l.toArray(new IMarket[l.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarketService#getOpenMarkets(java.util.Date)
     */
    @Override
    public IMarket[] getOpenMarkets(Date time) {
        List<IMarket> l = new ArrayList<IMarket>();
        for (Market market : marketsList) {
            if (market.isOpen(time)) {
                l.add(market);
            }
        }
        return l.toArray(new IMarket[l.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarketService#addMarketStatusListener(org.eclipsetrader.core.markets.IMarketStatusListener)
     */
    @Override
    public void addMarketStatusListener(IMarketStatusListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarketService#removeMarketStatusListener(org.eclipsetrader.core.markets.IMarketStatusListener)
     */
    @Override
    public void removeMarketStatusListener(IMarketStatusListener listener) {
        listeners.remove(listener);
    }

    public boolean isRunning() {
        return thread != null;
    }

    protected boolean isStopping() {
        return stopping;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        Map<IMarket, Boolean> statusMap = new HashMap<IMarket, Boolean>();
        Map<IMarket, String> messageMap = new HashMap<IMarket, String>();

        for (IMarket market : marketsList) {
            statusMap.put(market, market.isOpen());
        }

        synchronized (thread) {
            while (!isStopping()) {
                for (IMarket market : marketsList) {
                    boolean oldStatus = statusMap.get(market);
                    boolean newStatus = market.isOpen();

                    String oldMessage = messageMap.get(market);
                    String newMessage = market.getToday().getMessage();

                    if (oldStatus != newStatus || newMessage != null && !newMessage.equals(oldMessage) || oldMessage != null && !oldMessage.equals(newMessage)) {
                        statusMap.put(market, newStatus);
                        messageMap.put(market, newMessage);
                        fireMarketStatusEvent(market);
                    }
                }

                try {
                    long delay = 60000 - System.currentTimeMillis() % 60000;
                    thread.wait(delay);
                } catch (InterruptedException e) {
                    // Ignore exception, not important at this time
                }
            }
        }
    }

    protected void fireMarketStatusEvent(IMarket market) {
        MarketStatusEvent event = new MarketStatusEvent(market);
        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((IMarketStatusListener) l[i]).marketStatusChanged(event);
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error notifying market status update", e);
                CoreActivator.log(status);
            } catch (LinkageError e) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error notifying market status update", e);
                CoreActivator.log(status);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarketService#getMarketForSecurity(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public IMarket getMarketForSecurity(ISecurity security) {
        for (IMarket market : getMarkets()) {
            if (market.hasMember(security)) {
                return market;
            }
        }
        return null;
    }
}
