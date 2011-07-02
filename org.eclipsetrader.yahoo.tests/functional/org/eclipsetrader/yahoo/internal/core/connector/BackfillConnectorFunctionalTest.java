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

package org.eclipsetrader.yahoo.internal.core.connector;

import java.util.Date;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.TimeSpan;

public class BackfillConnectorFunctionalTest extends TestCase {

	public void testBackfill1MinDataForUSStocks() throws Exception {
		FeedIdentifier identifier = new FeedIdentifier("MSFT", null);

		BackfillConnector connector = new BackfillConnector();
		IOHLC[] result = connector.backfillHistory(identifier, new Date(), new Date(), TimeSpan.minutes(1));

		assertFalse(result.length == 0);
	}

	public void testBackfill5MinDataForUSStocks() throws Exception {
		FeedIdentifier identifier = new FeedIdentifier("MSFT", null);

		BackfillConnector connector = new BackfillConnector();
		IOHLC[] result = connector.backfillHistory(identifier, new Date(), new Date(), TimeSpan.minutes(5));

		assertFalse(result.length == 0);
	}

	public void testBackfill1MinDataForItalianStocks() throws Exception {
		FeedIdentifier identifier = new FeedIdentifier("MS.MI", null);

		BackfillConnector connector = new BackfillConnector();
		IOHLC[] result = connector.backfillHistory(identifier, new Date(), new Date(), TimeSpan.minutes(1));

		assertFalse(result.length == 0);
	}

	public void testBackfill5MinDataForItalianStocks() throws Exception {
		FeedIdentifier identifier = new FeedIdentifier("MS.MI", null);

		BackfillConnector connector = new BackfillConnector();
		IOHLC[] result = connector.backfillHistory(identifier, new Date(), new Date(), TimeSpan.minutes(5));

		assertFalse(result.length == 0);
	}

	public void testBackfill1MinDataForGermanStocks() throws Exception {
		FeedIdentifier identifier = new FeedIdentifier("BAS.DE", null);

		BackfillConnector connector = new BackfillConnector();
		IOHLC[] result = connector.backfillHistory(identifier, new Date(), new Date(), TimeSpan.minutes(1));

		assertFalse(result.length == 0);
	}

	public void testBackfill5MinDataForGermanStocks() throws Exception {
		FeedIdentifier identifier = new FeedIdentifier("BAS.DE", null);

		BackfillConnector connector = new BackfillConnector();
		IOHLC[] result = connector.backfillHistory(identifier, new Date(), new Date(), TimeSpan.minutes(5));

		assertFalse(result.length == 0);
	}

	public void testBackfill1MinDataForFranceStocks() throws Exception {
		FeedIdentifier identifier = new FeedIdentifier("AF.PA", null);

		BackfillConnector connector = new BackfillConnector();
		IOHLC[] result = connector.backfillHistory(identifier, new Date(), new Date(), TimeSpan.minutes(1));

		assertFalse(result.length == 0);
	}

	public void testBackfill5MinDataForFranceStocks() throws Exception {
		FeedIdentifier identifier = new FeedIdentifier("AF.PA", null);

		BackfillConnector connector = new BackfillConnector();
		IOHLC[] result = connector.backfillHistory(identifier, new Date(), new Date(), TimeSpan.minutes(5));

		assertFalse(result.length == 0);
	}

	public void testBackfill1MinDataForIndianStocks() throws Exception {
		FeedIdentifier identifier = new FeedIdentifier("UNITECH.BO", null);

		BackfillConnector connector = new BackfillConnector();
		IOHLC[] result = connector.backfillHistory(identifier, new Date(), new Date(), TimeSpan.minutes(1));

		assertFalse(result.length == 0);
	}

	public void testBackfill5MinDataForIndianStocks() throws Exception {
		FeedIdentifier identifier = new FeedIdentifier("UNITECH.BO", null);

		BackfillConnector connector = new BackfillConnector();
		IOHLC[] result = connector.backfillHistory(identifier, new Date(), new Date(), TimeSpan.minutes(5));

		assertFalse(result.length == 0);
	}

	public void testBackfill1MinDataForAustralianStocks() throws Exception {
		FeedIdentifier identifier = new FeedIdentifier("LYC.AX", null);

		BackfillConnector connector = new BackfillConnector();
		IOHLC[] result = connector.backfillHistory(identifier, new Date(), new Date(), TimeSpan.minutes(1));

		assertFalse(result.length == 0);
	}

	public void testBackfill5MinDataForAustralianStocks() throws Exception {
		FeedIdentifier identifier = new FeedIdentifier("LYC.AX", null);

		BackfillConnector connector = new BackfillConnector();
		IOHLC[] result = connector.backfillHistory(identifier, new Date(), new Date(), TimeSpan.minutes(5));

		assertFalse(result.length == 0);
	}

	public void testBackfill5MinDataForUKStocks() throws Exception {
		FeedIdentifier identifier = new FeedIdentifier("BAY.L", null);

		BackfillConnector connector = new BackfillConnector();
		IOHLC[] result = connector.backfillHistory(identifier, new Date(), new Date(), TimeSpan.minutes(5));

		assertFalse("No data for " + identifier.toString(), result.length == 0);
	}
}
