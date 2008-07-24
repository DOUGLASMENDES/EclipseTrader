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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.Dividend;
import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IDividend;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.ISplit;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.Split;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.TimeSpan.Units;
import org.eclipsetrader.opentick.internal.OTActivator;
import org.eclipsetrader.opentick.internal.core.repository.IdentifiersList;
import org.otfeed.IConnection;
import org.otfeed.IRequest;
import org.otfeed.command.AggregationSpan;
import org.otfeed.command.DividendsCommand;
import org.otfeed.command.HistDataCommand;
import org.otfeed.command.SplitsCommand;
import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTDividend;
import org.otfeed.event.OTOHLC;
import org.otfeed.event.OTSplit;

public class BackfillConnector implements IBackfillConnector, IExecutableExtension {
	private String id;
	private String name;

	private IConnection connection;
	private int marketOpen = 9 * 60 + 30;
	private int marketClose = 16 * 60 + 0;

	public BackfillConnector() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		id = config.getAttribute("id");
		name = config.getAttribute("name");
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IBackfillConnector#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IBackfillConnector#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IBackfillConnector#canBackfill(org.eclipsetrader.core.feed.IFeedIdentifier, org.eclipsetrader.core.feed.TimeSpan)
     */
    public boolean canBackfill(IFeedIdentifier identifier, TimeSpan timeSpan) {
		String symbol = identifier.getSymbol();
		String exchange = null;

		IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
		if (properties != null) {
			if (properties.getProperty(IdentifiersList.SYMBOL_PROPERTY) != null)
				symbol = properties.getProperty(IdentifiersList.SYMBOL_PROPERTY);
			if (properties.getProperty(IdentifiersList.EXCHANGE_PROPERTY) != null)
				exchange = properties.getProperty(IdentifiersList.EXCHANGE_PROPERTY);
		}

		if (symbol == null || exchange == null)
			return false;

		if (timeSpan.getUnits() == Units.Days && timeSpan.getLength() != 1)
			return false;
		if (timeSpan.getUnits() != Units.Days && timeSpan.getUnits() != Units.Minutes)
			return false;

		return true;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IBackfillConnector#backfillHistory(org.eclipsetrader.core.feed.IFeedIdentifier, java.util.Date, java.util.Date, org.eclipsetrader.core.feed.TimeSpan)
	 */
	public IOHLC[] backfillHistory(IFeedIdentifier identifier, Date begin, Date end, TimeSpan timeSpan) {
		String symbol = identifier.getSymbol();
		String exchange = null;

		IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
		if (properties != null) {
			if (properties.getProperty(IdentifiersList.SYMBOL_PROPERTY) != null)
				symbol = properties.getProperty(IdentifiersList.SYMBOL_PROPERTY);
			if (properties.getProperty(IdentifiersList.EXCHANGE_PROPERTY) != null)
				exchange = properties.getProperty(IdentifiersList.EXCHANGE_PROPERTY);
		}

		if (symbol == null || exchange == null)
			return new IOHLC[0];

		AggregationSpan period = timeSpan.getUnits() == Units.Minutes ? AggregationSpan.minutes(timeSpan.getLength()) : AggregationSpan.days(timeSpan.getLength());

		final List<OHLC> list = new ArrayList<OHLC>(4096);

		try {
			if (timeSpan.getUnits() == Units.Minutes) {
				if (begin != null) {
					Calendar c = Calendar.getInstance(Locale.US);
					c.setTime(begin);
					c.setTimeZone(TimeZone.getTimeZone("America/New_York"));
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					begin = c.getTime();
				}
				if (end != null) {
					Calendar c = Calendar.getInstance(Locale.US);
					c.setTime(end);
					c.setTimeZone(TimeZone.getTimeZone("America/New_York"));
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					end = c.getTime();
				}

				final Calendar c = Calendar.getInstance(Locale.US);
				c.setTimeZone(TimeZone.getTimeZone("America/New_York"));

				HistDataCommand command = new HistDataCommand(exchange, symbol, begin, end, period, new IDataDelegate<OTOHLC>() {
		            public void onData(OTOHLC event) {
						c.setTime(event.getTimestamp());
						if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
							return;
						int minutesOfDay = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
						if (minutesOfDay >= marketOpen && minutesOfDay < marketClose) {
							OHLC bar = new OHLC(
									event.getTimestamp(),
									event.getOpenPrice(),
									event.getHighPrice(),
									event.getLowPrice(),
									event.getClosePrice(),
									event.getVolume());
							list.add(bar);
						}
		            }
				});
				IRequest request = connection.prepareRequest(command);
				request.submit();
				request.waitForCompletion();
			}
			else {
				HistDataCommand command = new HistDataCommand(exchange, symbol, begin, end, period, new IDataDelegate<OTOHLC>() {
		            public void onData(OTOHLC event) {
						OHLC bar = new OHLC(
								event.getTimestamp(),
								event.getOpenPrice(),
								event.getHighPrice(),
								event.getLowPrice(),
								event.getClosePrice(),
								event.getVolume());
						list.add(bar);
		            }
				});
				IRequest request = connection.prepareRequest(command);
				request.submit();
				request.waitForCompletion();
			}
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error reading data", e);
			OTActivator.getDefault().getLog().log(status);
		}

		return list.toArray(new IOHLC[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IBackfillConnector#backfillDividends(org.eclipsetrader.core.feed.IFeedIdentifier, java.util.Date, java.util.Date)
	 */
	public IDividend[] backfillDividends(IFeedIdentifier identifier, Date from, Date to) {
		String symbol = identifier.getSymbol();
		String exchange = null;

		IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
		if (properties != null) {
			if (properties.getProperty(IdentifiersList.SYMBOL_PROPERTY) != null)
				symbol = properties.getProperty(IdentifiersList.SYMBOL_PROPERTY);
			if (properties.getProperty(IdentifiersList.EXCHANGE_PROPERTY) != null)
				exchange = properties.getProperty(IdentifiersList.EXCHANGE_PROPERTY);
		}

		if (symbol == null || exchange == null)
			return null;

		final List<IDividend> list = new ArrayList<IDividend>();

		try {
			DividendsCommand command = new DividendsCommand(exchange, symbol, from, to, new IDataDelegate<OTDividend>() {
	            public void onData(OTDividend event) {
	            	Dividend dividend = new Dividend(event.getPaymentDate(), event.getPrice());
	            	list.add(dividend);
	            }
			});
			IRequest request = connection.prepareRequest(command);
			request.submit();
			request.waitForCompletion();
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error reading data", e);
			OTActivator.getDefault().getLog().log(status);
		}

		return list.toArray(new IDividend[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IBackfillConnector#backfillSplits(org.eclipsetrader.core.feed.IFeedIdentifier, java.util.Date, java.util.Date)
	 */
	public ISplit[] backfillSplits(IFeedIdentifier identifier, Date from, Date to) {
		String symbol = identifier.getSymbol();
		String exchange = null;

		IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
		if (properties != null) {
			if (properties.getProperty(IdentifiersList.SYMBOL_PROPERTY) != null)
				symbol = properties.getProperty(IdentifiersList.SYMBOL_PROPERTY);
			if (properties.getProperty(IdentifiersList.EXCHANGE_PROPERTY) != null)
				exchange = properties.getProperty(IdentifiersList.EXCHANGE_PROPERTY);
		}

		if (symbol == null || exchange == null)
			return null;

		final List<ISplit> list = new ArrayList<ISplit>();

		try {
			SplitsCommand command = new SplitsCommand(exchange, symbol, from, to, new IDataDelegate<OTSplit>() {
	            public void onData(OTSplit event) {
	            	Split split = new Split(event.getPaymentDate(), event.getForFactor(), event.getToFactor());
	            	list.add(split);
	            }
			});
			IRequest request = connection.prepareRequest(command);
			request.submit();
			request.waitForCompletion();
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error reading data", e);
			OTActivator.getDefault().getLog().log(status);
		}

		return list.toArray(new ISplit[list.size()]);
	}
}
