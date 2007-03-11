/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.opentick;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.eclipsetrader.core.ILevel2Feed;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.opentick.internal.Book;
import net.sourceforge.eclipsetrader.opentick.internal.Client;
import net.sourceforge.eclipsetrader.opentick.internal.ClientAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opentick.OTBookCancel;
import com.opentick.OTBookChange;
import com.opentick.OTBookDelete;
import com.opentick.OTBookExecute;
import com.opentick.OTBookOrder;
import com.opentick.OTBookPurge;
import com.opentick.OTBookReplace;
import com.opentick.OTConstants;
import com.opentick.OTDataEntity;
import com.opentick.OTError;
import com.opentick.OTException;

public class Level2Feed implements ILevel2Feed
{
    boolean running = false;
    Set map = new HashSet();
    Map streams = new HashMap();
    Set pendingBookStreams = new HashSet();
    Map books = new HashMap();
    Client client = Client.getInstance();
    private Log log = LogFactory.getLog(getClass());
    ClientAdapter clientListener = new ClientAdapter() {
        public void onRealtimeBookCancel(OTBookCancel msg)
        {
            if (streams.get(String.valueOf(msg.getRequestID())) != null)
            {
                Book book = (Book)books.get(String.valueOf(msg.getRequestID()));
                if (book != null)
                {
                    book.remove(msg.getOrderRef(), msg.getSize());
                    Security security = (Security)streams.get(String.valueOf(msg.getRequestID()));
                    security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
                }
            }
        }

        public void onRealtimeBookChange(OTBookChange msg)
        {
            if (streams.get(String.valueOf(msg.getRequestID())) != null)
            {
                Book book = (Book)books.get(String.valueOf(msg.getRequestID()));
                if (book != null)
                {
                    book.remove(msg.getOrderRef(), msg.getSize());
                    Security security = (Security)streams.get(String.valueOf(msg.getRequestID()));
                    security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
                }
            }
        }

        public void onRealtimeBookDelete(OTBookDelete msg)
        {
            if (streams.get(String.valueOf(msg.getRequestID())) != null)
            {
                Book book = (Book)books.get(String.valueOf(msg.getRequestID()));
                if (book != null)
                {
                    book.delete(msg.getOrderRef(), msg.getSide(), msg.getDeleteType());
                    Security security = (Security)streams.get(String.valueOf(msg.getRequestID()));
                    security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
                }
            }
        }

        public void onRealtimeBookExecute(OTBookExecute msg)
        {
            if (streams.get(String.valueOf(msg.getRequestID())) != null)
            {
                Book book = (Book)books.get(String.valueOf(msg.getRequestID()));
                if (book != null)
                {
                    book.remove(msg.getOrderRef(), msg.getSize());
                    Security security = (Security)streams.get(String.valueOf(msg.getRequestID()));
                    security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
                }
            }
        }

        public void onRealtimeBookOrder(OTBookOrder msg)
        {
            if (streams.get(String.valueOf(msg.getRequestID())) != null)
            {
                Book book = (Book)books.get(String.valueOf(msg.getRequestID()));
                if (book != null)
                {
                    book.add(msg.getTimestamp(), msg.getOrderRef(), msg.getPrice(), msg.getSize(), msg.getSide());
                    Security security = (Security)streams.get(String.valueOf(msg.getRequestID()));
                    security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
                }
            }
        }

        public void onRealtimeBookPurge(OTBookPurge msg)
        {
            if (streams.get(String.valueOf(msg.getRequestID())) != null)
            {
                Book book = (Book)books.get(String.valueOf(msg.getRequestID()));
                if (book != null)
                {
                    book.clear();
                    Security security = (Security)streams.get(String.valueOf(msg.getRequestID()));
                    security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
                }
            }
        }

        public void onRealtimeBookReplace(OTBookReplace msg)
        {
            if (streams.get(String.valueOf(msg.getRequestID())) != null)
            {
                Book book = (Book)books.get(String.valueOf(msg.getRequestID()));
                if (book == null)
                {
                    book = new Book();
                    books.put(String.valueOf(msg.getRequestID()), book);
                }
                book.replace(msg.getTimestamp(), msg.getOrderRef(), msg.getPrice(), msg.getSize(), msg.getSide());
                Security security = (Security)streams.get(String.valueOf(msg.getRequestID()));
                security.setLevel2(book.getLevel2Bid(), book.getLevel2Ask());
            }
        }

        public void onError(OTError msg)
        {
            Security security = (Security)streams.get(String.valueOf(msg.getRequestId()));
            if (security != null)
            {
                log.error(msg.getRequestId() + " / " + msg.getDescription() + " (book) - " + security);
                streams.remove(String.valueOf(msg.getRequestId()));
                books.remove(String.valueOf(msg.getRequestId()));
            }
        }
    };

    public Level2Feed()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#subscribeLevel2(net.sourceforge.eclipsetrader.core.db.Security)
     */
    public void subscribeLevel2(Security security)
    {
        if (!map.contains(security))
        {
            map.add(security);

            try {
                if (running)
                    requestBookStream(security);
            } catch(Exception e) {
                LogFactory.getLog(getClass()).error(e, e);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#unSubscribeLevel2(net.sourceforge.eclipsetrader.core.db.Security)
     */
    public void unSubscribeLevel2(Security security)
    {
        if (map.contains(security))
        {
            map.remove(security);

            try {
                if (running)
                    cancelBookStream(security);
            } catch(Exception e) {
                LogFactory.getLog(getClass()).error(e, e);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#startLevel2()
     */
    public void startLevel2()
    {
        if (!running)
        {
            client.addListener(clientListener);
            try {
                client.login(15 * 1000);
                for (Iterator iter = map.iterator(); iter.hasNext(); )
                    requestBookStream((Security)iter.next());
            } catch(Exception e) {
                LogFactory.getLog(getClass()).error(e, e);
            }
            
            running = true;
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#stopLevel2()
     */
    public void stopLevel2()
    {
        if (running)
        {
            client.removeListener(clientListener);
            try {
                for (Iterator iter = map.iterator(); iter.hasNext(); )
                    cancelBookStream((Security)iter.next());
            } catch(Exception e) {
                LogFactory.getLog(getClass()).error(e, e);
            }
            running = false;
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#snapshotLevel2()
     */
    public void snapshotLevel2()
    {
    }
    
    public void requestBookStream(Security security) throws OTException
    {
        if (client.getStatus() != OTConstants.OT_STATUS_LOGGED_IN)
            pendingBookStreams.add(security);
        else
        {
            String symbol = security.getLevel2Feed().getSymbol();
            if (symbol == null || symbol.length() == 0)
                symbol = security.getCode();
            String exchange = security.getLevel2Feed().getExchange();
            if (exchange == null || exchange.length() == 0)
                exchange = "Q";
            
            int id = client.requestBookStream(new OTDataEntity(exchange, symbol));
            streams.put(String.valueOf(id), security);
            books.put(String.valueOf(id), new Book());

            log.debug(String.valueOf(id) + " / Request Book stream " + security);
        }
    }

    public void cancelBookStream(Security security) throws OTException
    {
        for (Iterator iter = streams.keySet().iterator(); iter.hasNext(); )
        {
            String id = (String)iter.next();
            if (security.equals(streams.get(id)))
            {
                client.cancelBookStream(Integer.parseInt(id));
                streams.remove(id);
                books.remove(id);
                log.debug(String.valueOf(id) + " / Request cancel Book stream " + security);
                break;
            }
        }
        pendingBookStreams.remove(security);
    }
}
