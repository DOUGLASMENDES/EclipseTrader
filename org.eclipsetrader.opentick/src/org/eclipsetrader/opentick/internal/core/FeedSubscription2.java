/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.opentick.internal.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.Book;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.IBookEntry;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription2;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ISubscriptionListener;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.QuoteDelta;
import org.eclipsetrader.core.feed.QuoteEvent;
import org.eclipsetrader.opentick.internal.OTActivator;
import org.eclipsetrader.opentick.internal.core.repository.IdentifierType;
import org.otfeed.IRequest;
import org.otfeed.command.BookDeleteTypeEnum;
import org.otfeed.command.BookStreamCommand;
import org.otfeed.event.ICompletionDelegate;
import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTBookCancel;
import org.otfeed.event.OTBookChange;
import org.otfeed.event.OTBookDelete;
import org.otfeed.event.OTBookExecute;
import org.otfeed.event.OTBookOrder;
import org.otfeed.event.OTBookPriceLevel;
import org.otfeed.event.OTBookPurge;
import org.otfeed.event.OTBookReplace;
import org.otfeed.event.OTError;
import org.otfeed.event.TradeSideEnum;

public class FeedSubscription2 implements IFeedSubscription2 {
	private FeedConnector connector;
	private IdentifierType identifierType;

	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	private IBook book;
	private Map<String, BookEntry> entries = new HashMap<String, BookEntry>();
	private TreeSet<BookEntry> bid = new TreeSet<BookEntry>();
	private TreeSet<BookEntry> ask = new TreeSet<BookEntry>();
	private boolean changed;
	private IRequest bookStream;

	private int instanceCount = 0;

	private Log logger = LogFactory.getLog(getClass());

	private static Comparator<BookEntry> priceComparator = new Comparator<BookEntry>() {
        public int compare(BookEntry o1, BookEntry o2) {
        	if (o1.getPrice() < o2.getPrice())
        		return -1;
        	else if (o1.getPrice() > o2.getPrice())
        		return 1;
            return 0;
        }
	};

	IDataDelegate<OTBookOrder> orderDelegate = new IDataDelegate<OTBookOrder>() {
        public void onData(OTBookOrder event) {
        	synchronized(entries) {
            	BookEntry entry = new BookEntry(event.getOrderRef(), event.getTimestamp(), event.getPrice(), event.getSize(), event.getSide());
            	entries.put(event.getOrderRef(), entry);
            	changed = true;
        	}
        	if (connector != null)
        		connector.wakeupNotifyThread();
        }
	};

	IDataDelegate<OTBookChange> changeDelegate = new IDataDelegate<OTBookChange>() {
        public void onData(OTBookChange event) {
        	synchronized(entries) {
            	BookEntry oldEntry = entries.get(event.getOrderRef());
            	if (oldEntry != null) {
                	BookEntry entry = new BookEntry(event.getOrderRef(), event.getTimestamp(), event.getPrice(), event.getSize(), oldEntry.getSide());
                	entries.put(event.getOrderRef(), entry);
                	changed = true;
            	}
        	}
        	if (changed && connector != null)
        		connector.wakeupNotifyThread();
        }
	};

	IDataDelegate<OTBookReplace> replaceDelegate = new IDataDelegate<OTBookReplace>() {
        public void onData(OTBookReplace event) {
        	synchronized(entries) {
            	BookEntry entry = new BookEntry(event.getOrderRef(), event.getTimestamp(), event.getPrice(), event.getSize(), event.getSide());
            	entries.put(event.getOrderRef(), entry);
            	changed = true;
        	}
        	if (connector != null)
        		connector.wakeupNotifyThread();
        }
	};

	private IDataDelegate<OTBookCancel> cancelDelegate = new IDataDelegate<OTBookCancel>() {
        public void onData(OTBookCancel event) {
        	synchronized(entries) {
        		BookEntry entry = entries.get(event.getOrderRef());
        		if (entry != null) {
        			entry.setQuantity(entry.getQuantity() - event.getSize());
        			if (entry.getQuantity() <= 0L)
        				entries.remove(event.getOrderRef());
                	changed = true;
        		}
        	}
        	if (changed && connector != null)
        		connector.wakeupNotifyThread();
        }
	};

	private IDataDelegate<OTBookPurge> purgeDelegate = new IDataDelegate<OTBookPurge>() {
        public void onData(OTBookPurge event) {
    		logger.info(event);
        }
	};

	IDataDelegate<OTBookExecute> executeDelegate = new IDataDelegate<OTBookExecute>() {
        public void onData(OTBookExecute event) {
        	synchronized(entries) {
        		BookEntry entry = entries.get(event.getOrderRef());
        		if (entry != null) {
        			entry.setQuantity(entry.getQuantity() - event.getSize());
        			if (entry.getQuantity() <= 0L)
        				entries.remove(event.getOrderRef());
                	changed = true;
        		}
        	}
        	if (changed && connector != null)
        		connector.wakeupNotifyThread();
        }
	};

	IDataDelegate<OTBookDelete> deleteDelegate = new IDataDelegate<OTBookDelete>() {
        public void onData(OTBookDelete event) {
        	synchronized(entries) {
            	if (event.getDeleteType() == BookDeleteTypeEnum.ALL) {
    				for (Iterator<BookEntry> iter = entries.values().iterator(); iter.hasNext(); ) {
    					BookEntry entry = iter.next();
    					if (entry.getSide() == event.getSide()) {
    						iter.remove();
    	                	changed = true;
    					}
    				}
            	}
            	else {
                	BookEntry refEntry = entries.get(event.getOrderRef());
                	if (refEntry != null) {
                		switch (event.getDeleteType()) {
                			case ORDER:
                				entries.remove(event.getOrderRef());
                            	changed = true;
                				break;
                			case PREVIOUS:
                				if (event.getSide() == TradeSideEnum.SELLER) {
                    				for (Iterator<BookEntry> iter = entries.values().iterator(); iter.hasNext(); ) {
                    					BookEntry entry = iter.next();
                    					if (entry.getSide() == event.getSide() && entry.getPrice() <= refEntry.getPrice()) {
                    						iter.remove();
                    	                	changed = true;
                    					}
                    				}
                				}
                				else {
                    				for (Iterator<BookEntry> iter = entries.values().iterator(); iter.hasNext(); ) {
                    					BookEntry entry = iter.next();
                    					if (entry.getSide() == event.getSide() && entry.getPrice() >= refEntry.getPrice()) {
                    						iter.remove();
                    	                	changed = true;
                    					}
                    				}
                				}
                				break;
                			case AFTER:
                				if (event.getSide() == TradeSideEnum.SELLER) {
                    				for (Iterator<BookEntry> iter = entries.values().iterator(); iter.hasNext(); ) {
                    					BookEntry entry = iter.next();
                    					if (entry.getSide() == event.getSide() && entry.getPrice() >= refEntry.getPrice()) {
                    						iter.remove();
                    	                	changed = true;
                    					}
                    				}
                				}
                				else {
                    				for (Iterator<BookEntry> iter = entries.values().iterator(); iter.hasNext(); ) {
                    					BookEntry entry = iter.next();
                    					if (entry.getSide() == event.getSide() && entry.getPrice() <= refEntry.getPrice()) {
                    						iter.remove();
                    	                	changed = true;
                    					}
                    				}
                				}
                				break;
                		}
                	}
            	}
        	}
        	if (changed && connector != null)
        		connector.wakeupNotifyThread();
        }
	};

	private IDataDelegate<OTBookPriceLevel> priceLevelDelegate = new IDataDelegate<OTBookPriceLevel>() {
        public void onData(OTBookPriceLevel event) {
    		logger.info(event);
        }
	};

	public FeedSubscription2(FeedConnector connector, IdentifierType identifierType) {
		this.connector = connector;
		this.identifierType = identifierType;
	}

	public IdentifierType getIdentifierType() {
    	return identifierType;
    }

	protected void submitRequests(org.otfeed.IConnection connection) {
		logger.info("Adding " + identifierType.getCompoundSymbol());
		BookStreamCommand bookStreamCommand = new BookStreamCommand(identifierType.getExchange(), identifierType.getSymbol());
		bookStreamCommand.setOrderDelegate(orderDelegate);
		bookStreamCommand.setChangeDelegate(changeDelegate);
		bookStreamCommand.setReplaceDelegate(replaceDelegate);
		bookStreamCommand.setCancelDelegate(cancelDelegate);
		bookStreamCommand.setPurgeDelegate(purgeDelegate);
		bookStreamCommand.setExecuteDelegate(executeDelegate);
		bookStreamCommand.setDeleteDelegate(deleteDelegate);
		bookStreamCommand.setPriceLevelDelegate(priceLevelDelegate);
		bookStreamCommand.setCompletionDelegate(new ICompletionDelegate() {
            public void onDataEnd(OTError error) {
        		logger.info(error);
            }
		});
		bookStream = connection.prepareRequest(bookStreamCommand);
		bookStream.submit();

		if (getSymbol().endsWith("MSFT"))
    		System.out.println("Requesting Level II data for " + getSymbol());
	}

	protected void cancelRequests() throws Exception {
		logger.info("Removing " + identifierType.getCompoundSymbol());
		if (bookStream != null && !bookStream.isCompleted()) {
			try {
				bookStream.cancel();
			} catch (IllegalStateException e) {
				// Do nothing
			}
			bookStream = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription2#getBook()
	 */
	public IBook getBook() {
		return book;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#dispose()
	 */
	public void dispose() {
		connector.disposeSubscription2(this);
	}

    protected int incrementInstanceCount() {
    	instanceCount++;
    	return instanceCount;
    }

    protected int decrementInstanceCount() {
    	instanceCount--;
    	return instanceCount;
    }

	protected int getInstanceCount() {
    	return instanceCount;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#getSymbol()
	 */
	public String getSymbol() {
		if (identifierType.getExchange().equals("@"))
			return identifierType.getSymbol();
	    return identifierType.getCompoundSymbol();
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#getIdentifier()
	 */
	public IFeedIdentifier getIdentifier() {
		return identifierType.getIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#getLastClose()
	 */
	public ILastClose getLastClose() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#getQuote()
	 */
	public IQuote getQuote() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#getTodayOHL()
	 */
	public ITodayOHL getTodayOHL() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#getTrade()
	 */
	public ITrade getTrade() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#addSubscriptionListener(org.eclipsetrader.core.feed.ISubscriptionListener)
	 */
	public void addSubscriptionListener(ISubscriptionListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedSubscription#removeSubscriptionListener(org.eclipsetrader.core.feed.ISubscriptionListener)
	 */
	public void removeSubscriptionListener(ISubscriptionListener listener) {
		listeners.remove(listener);
	}

    protected void fireNotification() {
    	IBook oldValue = book;
    	synchronized(entries) {
    		if (!changed)
    			return;

    		Double bidPrice = null, askPrice = null;
    		long bidQuantity = 0L, askQuantity = 0L;
    		long bidNumber = 0L, askNumber = 0L;

    		bid.clear();
    		ask.clear();

    		List<BookEntry> sortedValues = new ArrayList<BookEntry>(entries.values());
    		Collections.sort(sortedValues, priceComparator);

    		for (BookEntry entry : sortedValues) {
    			if (entry.getSide() == TradeSideEnum.SELLER) {
    				if (askPrice != null && !askPrice.equals(entry.getPrice())) {
    					ask.add(new BookEntry(askPrice, askQuantity, askNumber, TradeSideEnum.SELLER));
    		    		askQuantity = 0L;
    		    		askNumber = 0L;
    				}
    				askPrice = entry.getPrice();
    				askQuantity += entry.getQuantity();
    				askNumber++;
    			}
    			else {
    				if (bidPrice != null && !bidPrice.equals(entry.getPrice())) {
    					bid.add(new BookEntry(bidPrice, bidQuantity, bidNumber, TradeSideEnum.BUYER));
    					bidQuantity = 0L;
    					bidNumber = 0L;
    				}
    				bidPrice = entry.getPrice();
    				bidQuantity += entry.getQuantity();
    				bidNumber++;
    			}
    		}
			if (askPrice != null)
				ask.add(new BookEntry(askPrice, askQuantity, askNumber, TradeSideEnum.SELLER));
			if (bidPrice != null)
				bid.add(new BookEntry(bidPrice, bidQuantity, bidNumber, TradeSideEnum.BUYER));

    		book = new Book(
    				bid.toArray(new IBookEntry[bid.size()]),
    				ask.toArray(new IBookEntry[ask.size()]));

    		changed = false;
    	}
    	QuoteEvent event = new QuoteEvent(connector, getIdentifier(), new QuoteDelta[] {
    		new QuoteDelta(identifierType.getIdentifier(), oldValue, book),
    	});
    	Object[] l = listeners.getListeners();
    	for (int i = 0; i < l.length; i++) {
    		try {
        		((ISubscriptionListener) l[i]).quoteUpdate(event);
    		} catch(Exception e) {
				Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error notifying a quote update", e);
				OTActivator.log(status);
    		} catch (LinkageError e) {
				Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error notifying a quote update", e);
				OTActivator.log(status);
    		}
    	}
    }
}
