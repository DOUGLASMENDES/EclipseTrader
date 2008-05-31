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

package org.eclipsetrader.opentick.internal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipsetrader.core.feed.History;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.TimeSpan.Units;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.opentick.internal.core.repository.IdentifiersList;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.otfeed.IConnection;
import org.otfeed.IRequest;
import org.otfeed.command.AggregationSpan;
import org.otfeed.command.HistDataCommand;
import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTOHLC;

public class DataImportJob extends Job {
	public static final int FULL = 0;
	public static final int INCREMENTAL = 1;
	public static final int FULL_INCREMENTAL = 2;

	private ISecurity[] securities;
	private int mode;
	private TimeSpan[] timeSpan;
	private Date fromDate;
	private Date toDate;

	private int marketOpen = 9 * 60 + 30;
	private int marketClose = 16 * 60 + 0;

	public DataImportJob(ISecurity[] securities, int mode, Date fromDate, Date toDate, TimeSpan[] timeSpan) {
		super("Import Data");
		this.securities = securities;
		this.mode = mode;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.timeSpan = timeSpan;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		ISecurity[] filteredList = getFilteredSecurities(securities);
		monitor.beginTask(getName(), filteredList.length * timeSpan.length);

		try {
			IRepositoryService repository = getRepositoryService();

			monitor.subTask("Connecting");

			Connector.getInstance().connect();
			IConnection connection = Connector.getInstance().getConnection();
			if (connection == null)
				return Status.OK_STATUS;

			for (ISecurity security : filteredList) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;

				monitor.subTask(security.getName().replace("&", "&&"));

				try {
					IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);
					if (identifier != null) {
						Date beginDate = fromDate;
						Date endDate = toDate;

						IHistory history = repository.getHistoryFor(security);
						Map<Date, IOHLC> map = new HashMap<Date, IOHLC>(2048);

						if (history != null && mode != FULL) {
							for (IOHLC d : history.getOHLC())
								map.put(d.getDate(), d);
							if (mode == INCREMENTAL) {
								if (history.getLast() != null)
									beginDate = history.getLast().getDate();
								endDate = Calendar.getInstance().getTime();
							}
						}

						for (TimeSpan currentTimeSpan : timeSpan) {
							if (monitor.isCanceled())
								return Status.CANCEL_STATUS;

							if (currentTimeSpan.equals(TimeSpan.days(1))) {
								monitor.subTask(security.getName().replace("&", "&&"));

								IOHLC[] ohlc = backfill(connection, identifier, beginDate, endDate, currentTimeSpan);
								if (ohlc != null && ohlc.length != 0) {
									for (IOHLC d : ohlc)
										map.put(d.getDate(), d);
									ohlc = map.values().toArray(new IOHLC[map.values().size()]);

									if (history == null)
										history = new History(security, ohlc);
									else if (history instanceof History)
										((History) history).setOHLC(ohlc);

									repository.saveAdaptable(new IHistory[] { history });
								}
							}
							else {
								monitor.subTask(NLS.bind("{0} ({1})", new Object[] { security.getName().replace("&", "&&"), currentTimeSpan.toString() }));

								IOHLC[] ohlc = backfill(connection, identifier, beginDate, endDate, currentTimeSpan);
								if (ohlc != null && ohlc.length != 0) {
									Calendar c = Calendar.getInstance();
									int dayOfYear = -1;

									List<IOHLC> list = new ArrayList<IOHLC>(2048);
									for (IOHLC d : ohlc) {
										c.setTime(d.getDate());
										if (c.get(Calendar.DAY_OF_YEAR) != dayOfYear) {
											if (list.size() != 0) {
												IHistory intradayHistory = new History(history.getSecurity(), list.toArray(new IOHLC[list.size()]), currentTimeSpan);
												repository.saveAdaptable(new IHistory[] { intradayHistory });
												list = new ArrayList<IOHLC>(2048);
											}
											dayOfYear = c.get(Calendar.DAY_OF_YEAR);
										}
										list.add(d);
									}
									if (list.size() != 0) {
										IHistory intradayHistory = new History(history.getSecurity(), list.toArray(new IOHLC[list.size()]), currentTimeSpan);
										repository.saveAdaptable(new IHistory[] { intradayHistory });
									}
								}
							}

							monitor.worked(1);
						}
					}
				} catch (Exception e) {
					Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error downloading data for " + security, e);
					OTActivator.log(status);
				}
			}
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error downloading data", e);
			OTActivator.log(status);
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	protected ISecurity[] getFilteredSecurities(ISecurity[] list) {
		List<ISecurity> l = new ArrayList<ISecurity>();

		for (ISecurity security : list) {
			IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);
			if (identifier != null) {
				String symbol = identifier.getSymbol();
				String exchange = null;

				IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
				if (properties != null) {
					if (properties.getProperty(IdentifiersList.SYMBOL_PROPERTY) != null)
						symbol = properties.getProperty(IdentifiersList.SYMBOL_PROPERTY);
					if (properties.getProperty(IdentifiersList.EXCHANGE_PROPERTY) != null)
						exchange = properties.getProperty(IdentifiersList.EXCHANGE_PROPERTY);
				}

				if (symbol != null && exchange != null)
					l.add(security);
			}
		}

		Collections.sort(l, new Comparator<ISecurity>() {
            public int compare(ISecurity o1, ISecurity o2) {
	            return o1.getName().compareToIgnoreCase(o2.getName());
            }
		});

		return l.toArray(new ISecurity[l.size()]);
	}

	protected IOHLC[] backfill(IConnection connection, IFeedIdentifier identifier, Date begin, Date end, TimeSpan timeSpan) {
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

	protected IRepositoryService getRepositoryService() {
		IRepositoryService service = null;
		BundleContext context = OTActivator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
		if (serviceReference != null) {
			service = (IRepositoryService) context.getService(serviceReference);
			context.ungetService(serviceReference);
		}
		return service;
	}
}
