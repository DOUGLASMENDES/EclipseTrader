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

package org.eclipsetrader.directa.internal.ui.wizards;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipsetrader.core.feed.History;
import org.eclipsetrader.core.feed.HistoryDay;
import org.eclipsetrader.core.feed.IDividend;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Stock;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.directa.internal.Activator;
import org.eclipsetrader.directa.internal.core.connector.BackfillConnector;
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
        super(Messages.DataImportJob_Title);
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
        monitor.beginTask(getName(), securities.length);

        BundleContext context = Activator.getDefault().getBundle().getBundleContext();
        ServiceReference<IRepositoryService> serviceReference = context.getServiceReference(IRepositoryService.class);
        try {
            IRepositoryService repositoryService = context.getService(serviceReference);
            return doImport(monitor, repositoryService);
        } finally {
            context.ungetService(serviceReference);
            monitor.done();
        }
    }

    IStatus doImport(IProgressMonitor monitor, IRepositoryService repositoryService) {
        for (ISecurity security : securities) {
            monitor.subTask(security.getName().replace("&", "&&")); //$NON-NLS-1$ //$NON-NLS-2$
            monitor.worked(0);

            try {
                IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);
                if (identifier != null) {
                    Date beginDate = fromDate;
                    Date endDate = toDate;

                    IHistory history = repositoryService.getHistoryFor(security);
                    Map<Date, IOHLC> dailyDataMap = new HashMap<Date, IOHLC>(2048);

                    if (history != null && mode != FULL) {
                        for (IOHLC d : history.getOHLC()) {
                            dailyDataMap.put(d.getDate(), d);
                        }
                        if (mode == INCREMENTAL) {
                            if (history.getLast() != null) {
                                beginDate = history.getLast().getDate();
                            }
                            endDate = Calendar.getInstance().getTime();
                        }
                    }

                    BackfillConnector connector = new BackfillConnector();
                    Map<TimeSpan, IOHLC[]> dataMap = new HashMap<TimeSpan, IOHLC[]>();

                    for (TimeSpan currentTimeSpan : timeSpan) {
                        if (monitor.isCanceled()) {
                            return Status.CANCEL_STATUS;
                        }

                        IOHLC[] ohlc = connector.backfillHistory(identifier, beginDate, endDate, currentTimeSpan);
                        if (ohlc != null && ohlc.length != 0) {
                            dataMap.put(currentTimeSpan, ohlc);
                        }
                    }

                    if (dataMap.size() == timeSpan.length) {
                        for (TimeSpan currentTimeSpan : dataMap.keySet()) {
                            IOHLC[] ohlc = dataMap.get(currentTimeSpan);
                            if (ohlc == null) {
                                continue;
                            }
                            if (currentTimeSpan.equals(TimeSpan.days(1))) {
                                for (IOHLC d : ohlc) {
                                    dailyDataMap.put(d.getDate(), d);
                                }
                                ohlc = dailyDataMap.values().toArray(new IOHLC[dailyDataMap.values().size()]);

                                if (history == null) {
                                    history = new History(security, ohlc);
                                }
                                else if (history instanceof History) {
                                    ((History) history).setOHLC(ohlc);
                                }

                                repositoryService.saveAdaptable(new IHistory[] {
                                    history
                                });
                            }
                            else {
                                IHistory intradayHistory = history.getSubset(beginDate, endDate, currentTimeSpan);
                                if (intradayHistory instanceof HistoryDay) {
                                    ((HistoryDay) intradayHistory).setOHLC(ohlc);
                                }

                                repositoryService.saveAdaptable(new IHistory[] {
                                    intradayHistory
                                });
                            }
                        }

                        if (security instanceof Stock) {
                            IDividend[] dividends = connector.backfillDividends(identifier, beginDate, endDate);
                            if (dividends != null && dividends.length != 0) {
                                Map<Date, IDividend> dividendsMap = new HashMap<Date, IDividend>();

                                IDividend[] currentDividends = ((Stock) security).getDividends();
                                if (currentDividends != null && mode != FULL) {
                                    for (IDividend d : currentDividends) {
                                        dividendsMap.put(d.getExDate(), d);
                                    }
                                }

                                for (int i = 0; i < dividends.length; i++) {
                                    dividendsMap.put(dividends[i].getExDate(), dividends[i]);
                                }

                                if (dividendsMap.size() != 0) {
                                    ((Stock) security).setDividends(dividendsMap.values().toArray(new IDividend[dividendsMap.values().size()]));
                                    repositoryService.saveAdaptable(new ISecurity[] {
                                        security
                                    });
                                }
                            }
                        }
                    }
                    else {
                        Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Missing data for " + security.getName(), null); //$NON-NLS-1$
                        Activator.log(status);
                    }
                }
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error downloading data for " + security, e); //$NON-NLS-1$
                Activator.log(status);
            }

            monitor.worked(1);
        }
        return Status.OK_STATUS;
    }
}
