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

import java.util.Date;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.opentick.internal.core.repository.IdentifierType;
import org.otfeed.command.BookDeleteTypeEnum;
import org.otfeed.event.OTBookChange;
import org.otfeed.event.OTBookDelete;
import org.otfeed.event.OTBookExecute;
import org.otfeed.event.OTBookOrder;
import org.otfeed.event.OTBookReplace;
import org.otfeed.event.TradeSideEnum;

public class FeedSubscription2Test extends TestCase {

	public void testOrderDelegateBuyer() throws Exception {
		FeedSubscription2 subscription = new FeedSubscription2(null, new IdentifierType("MSFT", "Q"));
		assertNull(subscription.getBook());
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid", 10.0, 1000, TradeSideEnum.BUYER, true));
		subscription.fireNotification();
		IBook book = subscription.getBook();
		assertEquals(1, book.getBidProposals().length);
		assertEquals(10.0, book.getBidProposals()[0].getPrice());
		assertEquals(new Long(1000), book.getBidProposals()[0].getQuantity());
		assertEquals(0, book.getAskProposals().length);
    }

	public void testOrderDelegateSeller() throws Exception {
		FeedSubscription2 subscription = new FeedSubscription2(null, new IdentifierType("MSFT", "Q"));
		assertNull(subscription.getBook());
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid", 10.0, 1000, TradeSideEnum.SELLER, true));
		subscription.fireNotification();
		IBook book = subscription.getBook();
		assertEquals(0, book.getBidProposals().length);
		assertEquals(1, book.getAskProposals().length);
		assertEquals(10.0, book.getAskProposals()[0].getPrice());
		assertEquals(new Long(1000), book.getAskProposals()[0].getQuantity());
    }

	public void testChangeDelegate() throws Exception {
		FeedSubscription2 subscription = new FeedSubscription2(null, new IdentifierType("MSFT", "Q"));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid", 10.0, 1000, TradeSideEnum.BUYER, true));
		subscription.changeDelegate.onData(new OTBookChange(new Date(), "refid", 15.0, 2000));
		subscription.fireNotification();
		IBook book = subscription.getBook();
		assertEquals(1, book.getBidProposals().length);
		assertEquals(15.0, book.getBidProposals()[0].getPrice());
		assertEquals(new Long(2000), book.getBidProposals()[0].getQuantity());
		assertEquals(0, book.getAskProposals().length);
    }

	public void testReplaceDelegate() throws Exception {
		FeedSubscription2 subscription = new FeedSubscription2(null, new IdentifierType("MSFT", "Q"));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid", 10.0, 1000, TradeSideEnum.BUYER, true));
		subscription.replaceDelegate.onData(new OTBookReplace(new Date(), "refid", 15.0, 2000, TradeSideEnum.BUYER));
		subscription.fireNotification();
		IBook book = subscription.getBook();
		assertEquals(1, book.getBidProposals().length);
		assertEquals(15.0, book.getBidProposals()[0].getPrice());
		assertEquals(new Long(2000), book.getBidProposals()[0].getQuantity());
		assertEquals(0, book.getAskProposals().length);
    }

	public void testReplaceNewDelegate() throws Exception {
		FeedSubscription2 subscription = new FeedSubscription2(null, new IdentifierType("MSFT", "Q"));
		subscription.replaceDelegate.onData(new OTBookReplace(new Date(), "refid", 15.0, 2000, TradeSideEnum.BUYER));
		subscription.fireNotification();
		IBook book = subscription.getBook();
		assertEquals(1, book.getBidProposals().length);
		assertEquals(15.0, book.getBidProposals()[0].getPrice());
		assertEquals(new Long(2000), book.getBidProposals()[0].getQuantity());
		assertEquals(0, book.getAskProposals().length);
    }

	public void testExecuteDelegate() throws Exception {
		FeedSubscription2 subscription = new FeedSubscription2(null, new IdentifierType("MSFT", "Q"));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid", 10.0, 1000, TradeSideEnum.BUYER, true));
		subscription.executeDelegate.onData(new OTBookExecute(new Date(), "refid", 100, 0));
		subscription.fireNotification();
		IBook book = subscription.getBook();
		assertEquals(1, book.getBidProposals().length);
		assertEquals(10.0, book.getBidProposals()[0].getPrice());
		assertEquals(new Long(900), book.getBidProposals()[0].getQuantity());
		assertEquals(0, book.getAskProposals().length);
    }

	public void testExecuteAndRemoveDelegate() throws Exception {
		FeedSubscription2 subscription = new FeedSubscription2(null, new IdentifierType("MSFT", "Q"));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid", 10.0, 1000, TradeSideEnum.BUYER, true));
		subscription.executeDelegate.onData(new OTBookExecute(new Date(), "refid", 1000, 0));
		subscription.fireNotification();
		IBook book = subscription.getBook();
		assertEquals(0, book.getBidProposals().length);
		assertEquals(0, book.getAskProposals().length);
    }

	public void testDeleteOrderDelegate() throws Exception {
		FeedSubscription2 subscription = new FeedSubscription2(null, new IdentifierType("MSFT", "Q"));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid", 10.0, 1000, TradeSideEnum.BUYER, true));
		subscription.deleteDelegate.onData(new OTBookDelete(new Date(), "refid", BookDeleteTypeEnum.ORDER, TradeSideEnum.BUYER));
		subscription.fireNotification();
		IBook book = subscription.getBook();
		assertEquals(0, book.getBidProposals().length);
		assertEquals(0, book.getAskProposals().length);
    }

	public void testDeleteBuyerAfterDelegate() throws Exception {
		FeedSubscription2 subscription = new FeedSubscription2(null, new IdentifierType("MSFT", "Q"));

		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid1", 12.0, 1000, TradeSideEnum.BUYER, true));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid2", 11.0, 1000, TradeSideEnum.BUYER, true));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid3", 10.0, 1000, TradeSideEnum.BUYER, true));

		subscription.deleteDelegate.onData(new OTBookDelete(new Date(), "refid2", BookDeleteTypeEnum.AFTER, TradeSideEnum.BUYER));
		subscription.fireNotification();

		IBook book = subscription.getBook();
		assertEquals(1, book.getBidProposals().length);
		assertEquals(12.0, book.getBidProposals()[0].getPrice());
		assertEquals(0, book.getAskProposals().length);
    }

	public void testDeleteBuyerPreviousDelegate() throws Exception {
		FeedSubscription2 subscription = new FeedSubscription2(null, new IdentifierType("MSFT", "Q"));

		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid1", 12.0, 1000, TradeSideEnum.BUYER, true));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid2", 11.0, 1000, TradeSideEnum.BUYER, true));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid3", 10.0, 1000, TradeSideEnum.BUYER, true));

		subscription.deleteDelegate.onData(new OTBookDelete(new Date(), "refid2", BookDeleteTypeEnum.PREVIOUS, TradeSideEnum.BUYER));
		subscription.fireNotification();

		IBook book = subscription.getBook();
		assertEquals(1, book.getBidProposals().length);
		assertEquals(10.0, book.getBidProposals()[0].getPrice());
		assertEquals(0, book.getAskProposals().length);
    }

	public void testDeleteSellerAfterDelegate() throws Exception {
		FeedSubscription2 subscription = new FeedSubscription2(null, new IdentifierType("MSFT", "Q"));

		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid1", 10.0, 1000, TradeSideEnum.SELLER, true));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid2", 11.0, 1000, TradeSideEnum.SELLER, true));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid3", 12.0, 1000, TradeSideEnum.SELLER, true));

		subscription.deleteDelegate.onData(new OTBookDelete(new Date(), "refid2", BookDeleteTypeEnum.AFTER, TradeSideEnum.SELLER));
		subscription.fireNotification();

		IBook book = subscription.getBook();
		assertEquals(0, book.getBidProposals().length);
		assertEquals(1, book.getAskProposals().length);
		assertEquals(10.0, book.getAskProposals()[0].getPrice());
    }

	public void testDeleteSellerPreviousDelegate() throws Exception {
		FeedSubscription2 subscription = new FeedSubscription2(null, new IdentifierType("MSFT", "Q"));

		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid1", 10.0, 1000, TradeSideEnum.SELLER, true));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid2", 11.0, 1000, TradeSideEnum.SELLER, true));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid3", 12.0, 1000, TradeSideEnum.SELLER, true));

		subscription.deleteDelegate.onData(new OTBookDelete(new Date(), "refid2", BookDeleteTypeEnum.PREVIOUS, TradeSideEnum.SELLER));
		subscription.fireNotification();

		IBook book = subscription.getBook();
		assertEquals(0, book.getBidProposals().length);
		assertEquals(1, book.getAskProposals().length);
		assertEquals(12.0, book.getAskProposals()[0].getPrice());
    }

	public void testDeleteAllDelegate() throws Exception {
		FeedSubscription2 subscription = new FeedSubscription2(null, new IdentifierType("MSFT", "Q"));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid", 10.0, 1000, TradeSideEnum.BUYER, true));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid1", 12.0, 1000, TradeSideEnum.BUYER, true));
		subscription.fireNotification();
		IBook book = subscription.getBook();
		assertEquals(2, book.getBidProposals().length);
		assertEquals(0, book.getAskProposals().length);
		subscription.deleteDelegate.onData(new OTBookDelete(new Date(), "refid", BookDeleteTypeEnum.ALL, TradeSideEnum.BUYER));
		subscription.fireNotification();
		book = subscription.getBook();
		assertEquals(0, book.getBidProposals().length);
		assertEquals(0, book.getAskProposals().length);
    }

	public void testDeleteAllOtherSideDelegate() throws Exception {
		FeedSubscription2 subscription = new FeedSubscription2(null, new IdentifierType("MSFT", "Q"));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid", 10.0, 1000, TradeSideEnum.BUYER, true));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid1", 12.0, 1000, TradeSideEnum.BUYER, true));
		subscription.fireNotification();
		IBook book = subscription.getBook();
		assertEquals(2, book.getBidProposals().length);
		assertEquals(0, book.getAskProposals().length);
		subscription.deleteDelegate.onData(new OTBookDelete(new Date(), "refid", BookDeleteTypeEnum.ALL, TradeSideEnum.SELLER));
		subscription.fireNotification();
		book = subscription.getBook();
		assertEquals(2, book.getBidProposals().length);
		assertEquals(0, book.getAskProposals().length);
    }

	public void testGroupPrices() throws Exception {
		FeedSubscription2 subscription = new FeedSubscription2(null, new IdentifierType("MSFT", "Q"));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid1", 10.0, 1000, TradeSideEnum.BUYER, true));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid2", 12.0, 500, TradeSideEnum.SELLER, true));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid3", 10.0, 500, TradeSideEnum.BUYER, true));
		subscription.orderDelegate.onData(new OTBookOrder(new Date(), "refid4", 11.0, 2000, TradeSideEnum.BUYER, true));
		subscription.fireNotification();
		IBook book = subscription.getBook();
		assertEquals(2, book.getBidProposals().length);
		assertEquals(11.0, book.getBidProposals()[0].getPrice());
		assertEquals(new Long(2000), book.getBidProposals()[0].getQuantity());
		assertEquals(10.0, book.getBidProposals()[1].getPrice());
		assertEquals(new Long(1500), book.getBidProposals()[1].getQuantity());
		assertEquals(new Long(2), book.getBidProposals()[1].getProposals());

		assertEquals(1, book.getAskProposals().length);
		assertEquals(12.0, book.getAskProposals()[0].getPrice());
		assertEquals(new Long(500), book.getAskProposals()[0].getQuantity());
	}
}
