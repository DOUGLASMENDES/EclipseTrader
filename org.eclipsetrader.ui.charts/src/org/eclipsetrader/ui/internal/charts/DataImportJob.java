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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipsetrader.core.feed.History;
import org.eclipsetrader.core.feed.HistoryDay;
import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IConnectorOverride;
import org.eclipsetrader.core.feed.IDividend;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.ISplit;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Stock;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;

@SuppressWarnings("restriction")
public class DataImportJob extends Job {
	public static final int FULL = 0;
	public static final int INCREMENTAL = 1;
	public static final int FULL_INCREMENTAL = 2;

	private ISecurity[] securities;
	private int mode;
	private TimeSpan[] timeSpan;
	private Date fromDate;
	private Date toDate;

	private List<IStatus> results = new ArrayList<IStatus>();
	IPreferenceStore preferences;

	public DataImportJob(ISecurity security, int mode, Date fromDate, Date toDate, TimeSpan[] timeSpan) {
		super(Messages.DataImportJob_Name);
		this.securities = new ISecurity[] {
			security
		};
		this.mode = mode;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.timeSpan = timeSpan;
	}

	public DataImportJob(ISecurity[] securities, int mode, Date fromDate, Date toDate, TimeSpan[] timeSpan) {
		super(Messages.DataImportJob_Name);
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
		monitor.beginTask(getName(), filteredList.length);

		preferences = ChartsUIActivator.getDefault().getPreferenceStore();

		IBackfillConnector defaultBackfillConnector = CoreActivator.getDefault().getDefaultBackfillConnector();
		IBackfillConnector defaultIntradayBackfillConnector = CoreActivator.getDefault().getDefaultBackfillConnector();

		try {
			IRepositoryService repositoryService = ChartsUIActivator.getDefault().getRepositoryService();
			IMarketService marketService = ChartsUIActivator.getDefault().getMarketService();

			for (ISecurity security : filteredList) {
				if (monitor.isCanceled()) {
					if (results.size() != 0)
						return new MultiStatus(ChartsUIActivator.PLUGIN_ID, 0, results.toArray(new IStatus[results.size()]), Messages.DataImportJob_DownloadErrorMessage, null);
					return Status.CANCEL_STATUS;
				}

				monitor.subTask(security.getName().replace("&", "&&")); //$NON-NLS-1$ //$NON-NLS-2$

				try {
					IStoreObject storeObject = (IStoreObject) security.getAdapter(IStoreObject.class);
					IRepository defaultRepository = storeObject.getStore().getRepository();

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

					if (beginDate == null) {
						beginDate = getDefaultStartDate();
					}
					if (endDate == null)
						endDate = new Date();

					IHistory history = repositoryService.getHistoryFor(security);
					Map<Date, IOHLC> dailyDataMap = new HashMap<Date, IOHLC>(2048);

					if (history != null && mode != FULL) {
						for (IOHLC d : history.getOHLC())
							dailyDataMap.put(d.getDate(), d);
						if (mode == INCREMENTAL) {
							if (history.getLast() != null)
								beginDate = history.getLast().getDate();
							endDate = Calendar.getInstance().getTime();
						}
					}

					Map<TimeSpan, IOHLC[]> dataMap = new HashMap<TimeSpan, IOHLC[]>();

					for (TimeSpan currentTimeSpan : timeSpan) {
						if (monitor.isCanceled()) {
							if (results.size() != 0)
								return new MultiStatus(ChartsUIActivator.PLUGIN_ID, 0, results.toArray(new IStatus[results.size()]), Messages.DataImportJob_DownloadErrorMessage, null);
							return Status.CANCEL_STATUS;
						}

						if (currentTimeSpan.equals(TimeSpan.days(1))) {
							monitor.subTask(security.getName().replace("&", "&&")); //$NON-NLS-1$ //$NON-NLS-2$

							IOHLC[] ohlc = backfillConnector.backfillHistory(identifier, beginDate, endDate, currentTimeSpan);
							if (ohlc != null && ohlc.length != 0)
								dataMap.put(currentTimeSpan, ohlc);
						}
						else if (intradayBackfillConnector.canBackfill(identifier, currentTimeSpan)) {
							monitor.subTask(NLS.bind("{0} ({1})", new Object[] { security.getName().replace("&", "&&"), currentTimeSpan.toString()})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

							IOHLC[] ohlc = intradayBackfillConnector.backfillHistory(identifier, beginDate, endDate, currentTimeSpan);
							if (ohlc != null && ohlc.length != 0)
								dataMap.put(currentTimeSpan, ohlc);
						}
						else
							dataMap.put(currentTimeSpan, null);

						if (!dataMap.containsKey(currentTimeSpan)) {
							String message = NLS.bind(Messages.DataImportJob_DownloadDataErrorMessage, new Object[] {
							    currentTimeSpan.toString(), security.getName()
							});
							Status status = new Status(Status.ERROR, ChartsUIActivator.PLUGIN_ID, 0, message, null);
							results.add(status);
						}
					}

					if (dataMap.size() == timeSpan.length) {
						for (TimeSpan currentTimeSpan : dataMap.keySet()) {
							IOHLC[] ohlc = dataMap.get(currentTimeSpan);
							if (ohlc == null)
								continue;
							if (currentTimeSpan.equals(TimeSpan.days(1))) {
								for (IOHLC d : ohlc)
									dailyDataMap.put(d.getDate(), d);
								ohlc = dailyDataMap.values().toArray(new IOHLC[dailyDataMap.values().size()]);

								if (history == null)
									history = new History(security, ohlc);
								else if (history instanceof History)
									((History) history).setOHLC(ohlc);

								if ((security instanceof Stock) && (history instanceof History)) {
									ISplit[] splits = backfillConnector.backfillSplits(identifier, beginDate, endDate);
									if (splits != null && splits.length != 0) {
										Map<Date, ISplit> splitsMap = new HashMap<Date, ISplit>();

										ISplit[] currentSplits = history.getSplits();
										if (currentSplits != null && mode != FULL) {
											for (ISplit s : currentSplits)
												splitsMap.put(s.getDate(), s);
										}

										for (int i = 0; i < splits.length; i++)
											splitsMap.put(splits[i].getDate(), splits[i]);

										Collection<ISplit> c = splitsMap.values();
										((History) history).setSplits(c.toArray(new ISplit[c.size()]));
									}
								}

								repositoryService.saveAdaptable(new IHistory[] {
									history
								}, defaultRepository);
							}
							else {
								if (history == null) {
									history = new History(security, ohlc);
									IHistory intradayHistory = history.getSubset(beginDate, endDate, currentTimeSpan);
									if (intradayHistory instanceof HistoryDay)
										((HistoryDay) intradayHistory).setOHLC(ohlc);
									repositoryService.saveAdaptable(new IHistory[] {
									    history, intradayHistory
									}, defaultRepository);
								}
								else {
									IHistory intradayHistory = history.getSubset(beginDate, endDate, currentTimeSpan);
									if (intradayHistory instanceof HistoryDay)
										((HistoryDay) intradayHistory).setOHLC(ohlc);
									repositoryService.saveAdaptable(new IHistory[] {
										intradayHistory
									}, defaultRepository);
								}
							}
						}

						if (security instanceof Stock) {
							IDividend[] dividends = backfillConnector.backfillDividends(identifier, beginDate, endDate);
							if (dividends != null && dividends.length != 0) {
								Map<Date, IDividend> dividendsMap = new HashMap<Date, IDividend>();

								IDividend[] currentDividends = ((Stock) security).getDividends();
								if (currentDividends != null && mode != FULL) {
									for (IDividend d : currentDividends)
										dividendsMap.put(d.getExDate(), d);
								}

								for (int i = 0; i < dividends.length; i++)
									dividendsMap.put(dividends[i].getExDate(), dividends[i]);

								if (dividendsMap.size() != 0) {
									((Stock) security).setDividends(dividendsMap.values().toArray(new IDividend[dividendsMap.values().size()]));
									repositoryService.saveAdaptable(new ISecurity[] {
										security
									});
								}
							}
						}
					}
				} catch (Exception e) {
					Status status = new Status(Status.ERROR, ChartsUIActivator.PLUGIN_ID, 0, Messages.DataImportJob_SecurityDownloadErrorMessage + security.getName(), e);
					results.add(status);
				}

				monitor.worked(1);
			}
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, ChartsUIActivator.PLUGIN_ID, 0, Messages.DataImportJob_DataErrorMessage, e);
			results.add(status);
		} finally {
			monitor.done();
		}

		if (results.size() != 0)
			return new MultiStatus(ChartsUIActivator.PLUGIN_ID, 0, results.toArray(new IStatus[results.size()]), Messages.DataImportJob_DownloadErrorMessage, null);
		return Status.OK_STATUS;
	}

	Date getDefaultStartDate() throws ParseException {
		int method = preferences.getInt(ChartsUIActivator.PREFS_INITIAL_BACKFILL_METHOD);

		if (method == 0) {
			String s = preferences.getString(ChartsUIActivator.PREFS_INITIAL_BACKFILL_START_DATE);
			return new SimpleDateFormat("yyyyMMdd").parse(s);
		}
		else if (method == 1) {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.MILLISECOND, 0);
			c.add(Calendar.YEAR, -preferences.getInt(ChartsUIActivator.PREFS_INITIAL_BACKFILL_YEARS));
			return c.getTime();
		}

		throw new IllegalArgumentException("Invalid initial backfill method " + method);
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
