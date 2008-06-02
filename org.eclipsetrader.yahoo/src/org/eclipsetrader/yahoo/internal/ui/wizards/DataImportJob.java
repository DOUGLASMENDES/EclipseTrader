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

package org.eclipsetrader.yahoo.internal.ui.wizards;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipsetrader.core.feed.History;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.yahoo.internal.YahooActivator;
import org.eclipsetrader.yahoo.internal.core.Util;
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

	private SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yy", Locale.US); //$NON-NLS-1$
	private SimpleDateFormat dfAlt = new SimpleDateFormat("yy-MM-dd"); //$NON-NLS-1$
	private NumberFormat nf = NumberFormat.getInstance(Locale.US);
	private NumberFormat pf = NumberFormat.getInstance(Locale.US);

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
		monitor.beginTask(getName(), securities.length);
		try {
			IRepositoryService repository = getRepositoryService();

			for (ISecurity security : securities) {
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
								IOHLC[] ohlc = backfill(identifier, beginDate, endDate);
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
						}
					}
				} catch(Exception e) {
					Status status = new Status(Status.ERROR, YahooActivator.PLUGIN_ID, 0, "Error downloading data for " + security, e);
					YahooActivator.log(status);
				}

				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

    protected IOHLC[] backfill(IFeedIdentifier identifier, Date begin, Date end) {
    	List<OHLC> list = new ArrayList<OHLC>();

    	try {
			HttpMethod method = Util.getHistoryFeedMethod(identifier, begin, end);
			method.setFollowRedirects(true);

			HttpClient client = new HttpClient();
			client.executeMethod(method);

			BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));

			// The first line is the header, ignoring
			String inputLine = in.readLine();
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.startsWith("<")) //$NON-NLS-1$
					continue;

		    	try {
					OHLC bar = parseResponseLine(inputLine);
					if (bar != null)
						list.add(bar);
		    	} catch(ParseException e) {
					Status status = new Status(Status.ERROR, YahooActivator.PLUGIN_ID, 0, "Error parsing data: " + inputLine, e);
					YahooActivator.getDefault().getLog().log(status);
		    	}
			}

			in.close();

		} catch (Exception e) {
			Status status = new Status(Status.ERROR, YahooActivator.PLUGIN_ID, 0, "Error reading data", e);
			YahooActivator.getDefault().getLog().log(status);
		}

		return list.toArray(new IOHLC[list.size()]);
    }

    protected OHLC parseResponseLine(String inputLine) throws ParseException {
		String[] item = inputLine.split(","); //$NON-NLS-1$
		if (item.length < 6)
			return null;

		Calendar day = Calendar.getInstance();
		try {
			day.setTime(df.parse(item[0]));
		} catch (ParseException e) {
			try {
				day.setTime(dfAlt.parse(item[0]));
			} catch (ParseException e1) {
				throw e1;
			}
		}
		day.set(Calendar.HOUR, 0);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);
		day.set(Calendar.MILLISECOND, 0);

		OHLC bar = new OHLC(day.getTime(),
				pf.parse(item[1].replace(',', '.')).doubleValue(),
				pf.parse(item[2].replace(',', '.')).doubleValue(),
				pf.parse(item[3].replace(',', '.')).doubleValue(),
				pf.parse(item[4].replace(',', '.')).doubleValue(),
				nf.parse(item[5]).longValue()
			);

		return bar;
    }

	protected IRepositoryService getRepositoryService() {
		IRepositoryService service = null;
		BundleContext context = YahooActivator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
		if (serviceReference != null) {
			service = (IRepositoryService) context.getService(serviceReference);
			context.ungetService(serviceReference);
		}
		return service;
	}
}
