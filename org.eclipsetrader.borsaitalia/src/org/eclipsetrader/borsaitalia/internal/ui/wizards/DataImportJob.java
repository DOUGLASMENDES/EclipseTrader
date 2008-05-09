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

package org.eclipsetrader.borsaitalia.internal.ui.wizards;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.NumberFormat;
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
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipsetrader.borsaitalia.internal.Activator;
import org.eclipsetrader.core.feed.History;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class DataImportJob extends Job {
	public static final int FULL_UPDATE = 0;
	public static final int INCREMENTAL_UPDATE = 1;
	public static final int FULL_DOWNLOAD = 2;
	private ISecurity[] securities;
	private int mode = FULL_UPDATE;

	private String host = "grafici.borsaitalia.it";
	private NumberFormat nf = NumberFormat.getInstance(Locale.US);
	private SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss"); //$NON-NLS-1$

	public DataImportJob(ISecurity[] securities) {
		super("Import Data");
		this.securities = securities;
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
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;

				monitor.subTask(security.getName().replace("&", "&&"));

				try {
					IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);
					if (identifier != null) {
						Calendar today = Calendar.getInstance();
						Date endDate = today.getTime();
						today.add(Calendar.YEAR, -10);
						Date beginDate = today.getTime();

						IHistory history = repository.getHistoryFor(security);
						Map<Date, IOHLC> map = new HashMap<Date, IOHLC>(2048);

						if (history != null && mode != FULL_DOWNLOAD) {
							if (mode == FULL_UPDATE) {
								if (history.getFirst() != null)
									beginDate = history.getFirst().getDate();
							}
							else if (mode == INCREMENTAL_UPDATE) {
								for (IOHLC d : history.getOHLC())
									map.put(d.getDate(), d);
								if (history.getLast() != null)
									beginDate = history.getLast().getDate();
							}
						}

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
				} catch (Exception e) {
					Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error downloading data for " + security, e);
					Activator.log(status);
				}

				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	protected IOHLC[] backfill(IFeedIdentifier identifier, Date begin, Date end) {
		String code = identifier.getSymbol();
		String symbol = identifier.getSymbol();

		IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
		if (properties != null) {
			if (properties.getProperty("org.eclipsetrader.borsaitalia.isin") != null)
				symbol = properties.getProperty("org.eclipsetrader.borsaitalia.isin");
			if (properties.getProperty("org.eclipsetrader.borsaitalia.code") != null)
				code = properties.getProperty("org.eclipsetrader.borsaitalia.code");
		}

		List<OHLC> list = new ArrayList<OHLC>();

		try {
			HttpMethod method = new GetMethod("http://" + host + "/scripts/cligipsw.dll");
			method.setQueryString(new NameValuePair[] {
					new NameValuePair("app", "tic_d"),
					new NameValuePair("action", "dwnld4push"),
					new NameValuePair("cod", code),
					new NameValuePair("codneb", symbol),
					new NameValuePair("req_type", "GRAF_DS"),
					new NameValuePair("ascii", "1"),
					new NameValuePair("form_id", ""),
					new NameValuePair("period", "1DAY"),
					new NameValuePair("From", df.format(begin.getTime())),
				});
			method.setFollowRedirects(true);

			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			client.executeMethod(method);

			BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));

			// The first line is the header, ignoring
			String inputLine = in.readLine();
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.startsWith("@") == true || inputLine.length() == 0) //$NON-NLS-1$
					continue;

				try {
					String[] item = inputLine.split("\\|"); //$NON-NLS-1$
					OHLC bar = new OHLC(
							df.parse(item[0]),
							nf.parse(item[1]).doubleValue(),
							nf.parse(item[2]).doubleValue(),
							nf.parse(item[3]).doubleValue(),
							nf.parse(item[4]).doubleValue(),
							nf.parse(item[5]).longValue()
						);
					list.add(bar);
				} catch (Exception e) {
					Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error parsing data: " + inputLine, e);
					Activator.getDefault().getLog().log(status);
				}
			}

			in.close();

		} catch (Exception e) {
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error reading data", e);
			Activator.getDefault().getLog().log(status);
		}

		return list.toArray(new IOHLC[list.size()]);
	}

	protected IRepositoryService getRepositoryService() {
		IRepositoryService service = null;
		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
		if (serviceReference != null) {
			service = (IRepositoryService) context.getService(serviceReference);
			context.ungetService(serviceReference);
		}
		return service;
	}
}
