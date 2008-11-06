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

package org.eclipsetrader.ui.internal.charts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipsetrader.core.feed.History;
import org.eclipsetrader.core.feed.HistoryDay;
import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IConnectorOverride;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedService;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class DataImportJob extends Job {
	public static final int FULL = 0;
	public static final int INCREMENTAL = 1;
	public static final int FULL_INCREMENTAL = 2;

	private ISecurity[] securities;
	private int mode;
	private TimeSpan[] timeSpan;
	private Date fromDate;
	private Date toDate;

	public DataImportJob(ISecurity[] securities, int mode, Date fromDate, Date toDate, TimeSpan[] timeSpan) {
		super("Historical Data Update");
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

		IBackfillConnector defaultBackfillConnector = CoreActivator.getDefault().getDefaultBackfillConnector();
		IBackfillConnector defaultIntradayBackfillConnector = CoreActivator.getDefault().getDefaultBackfillConnector();

		try {
			IRepositoryService repositoryService = ChartsUIActivator.getDefault().getRepositoryService();
			IMarketService marketService = ChartsUIActivator.getDefault().getMarketService();

			for (ISecurity security : filteredList) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;

				monitor.subTask(security.getName().replace("&", "&&"));

				try {
					IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);

					IBackfillConnector backfillConnector = defaultBackfillConnector;
					IBackfillConnector intradayBackfillConnector = defaultIntradayBackfillConnector;

					IMarket market = marketService.getMarketForSecurity(security);
					if (market != null && market.getBackfillConnector() != null) {
						backfillConnector = market.getBackfillConnector();
						intradayBackfillConnector = market.getIntradayBackfillConnector() != null ? market.getIntradayBackfillConnector() : market.getBackfillConnector();
					}

					IConnectorOverride override = (IConnectorOverride) AdapterManager.getDefault().getAdapter(security, IConnectorOverride.class);
					if (override != null) {
						if (override.getBackfillConnector() != null) {
							backfillConnector = override.getBackfillConnector();
							intradayBackfillConnector = override.getBackfillConnector();
						}
						if (override.getIntradayBackfillConnector() != null)
							intradayBackfillConnector = override.getIntradayBackfillConnector();
					}

					Date beginDate = fromDate;
					Date endDate = toDate;

					IHistory history = repositoryService.getHistoryFor(security);
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

							IOHLC[] ohlc = backfillConnector.backfillHistory(identifier, beginDate, endDate, currentTimeSpan);
							if (ohlc != null && ohlc.length != 0) {
								for (IOHLC d : ohlc)
									map.put(d.getDate(), d);
								ohlc = map.values().toArray(new IOHLC[map.values().size()]);

								if (history == null)
									history = new History(security, ohlc);
								else if (history instanceof History)
									((History) history).setOHLC(ohlc);

								repositoryService.saveAdaptable(new IHistory[] { history });
							}
						}
						else if (intradayBackfillConnector.canBackfill(identifier, currentTimeSpan)) {
							monitor.subTask(NLS.bind("{0} ({1})", new Object[] { security.getName().replace("&", "&&"), currentTimeSpan.toString() }));

							IOHLC[] ohlc = intradayBackfillConnector.backfillHistory(identifier, beginDate, endDate, currentTimeSpan);
							if (ohlc != null && ohlc.length != 0) {
								IHistory intradayHistory = history.getSubset(beginDate, endDate, currentTimeSpan);
								if (intradayHistory instanceof HistoryDay)
									((HistoryDay) intradayHistory).setOHLC(ohlc);

								repositoryService.saveAdaptable(new IHistory[] { intradayHistory });
							}
						}

						monitor.worked(1);
					}
				} catch (Exception e) {
					Status status = new Status(Status.ERROR, ChartsUIActivator.PLUGIN_ID, 0, "Error downloading data for " + security, e);
					ChartsUIActivator.log(status);
				}
			}
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, ChartsUIActivator.PLUGIN_ID, 0, "Error downloading data", e);
			ChartsUIActivator.log(status);
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	protected ISecurity[] getFilteredSecurities(ISecurity[] list) {
		List<ISecurity> l = new ArrayList<ISecurity>();

		for (ISecurity security : list) {
			IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);
			if (identifier != null)
				l.add(security);
		}

		Collections.sort(l, new Comparator<ISecurity>() {
            public int compare(ISecurity o1, ISecurity o2) {
	            return o1.getName().compareToIgnoreCase(o2.getName());
            }
		});

		return l.toArray(new ISecurity[l.size()]);
	}
}
