/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.ats.internal;

import net.sourceforge.eclipsetrader.ats.ATSPlugin;
import net.sourceforge.eclipsetrader.ats.TradingSystemManager;
import net.sourceforge.eclipsetrader.ats.core.db.TradingSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IStartup;

/**
 * Initializes the trading system runnables at startup.
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public class EarlyStartup implements IStartup {
	/**
	 * Logger instance.
	 */
	private Log log = LogFactory.getLog(getClass());

	public EarlyStartup() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public void earlyStartup() {
		Job job = new Job("ATS") {
			protected IStatus run(IProgressMonitor monitor) {
				Object[] system = ATSPlugin.getRepository().allTradingSystems().toArray();
				monitor.beginTask("Initializing Automated Trading Systems", system.length);

				log.info("Initializing Automated Trading Systems");

				TradingSystemManager tradingSystemManager = TradingSystemManager.getInstance();
				for (int i = 0; i < system.length; i++) {
					tradingSystemManager.addTradingSystem((TradingSystem) system[i]);
					monitor.worked(1);
				}

				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setUser(false);
		job.schedule();
	}
}
