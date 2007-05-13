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

package net.sourceforge.eclipsetrader.news.views;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class NewsStartupJob extends Job {
	Security security;
	
	List list = new ArrayList();

	public NewsStartupJob() {
		this(null);
	}

	public NewsStartupJob(Security security) {
		super(security != null ? security + ": News Startup" : "News Startup");
		this.security = security;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
		if (security != null)
			list = new ArrayList(CorePlugin.getRepository().allNews(security));
		else
			list = new ArrayList(CorePlugin.getRepository().allNews());
		return Status.OK_STATUS;
	}

	public List getList() {
    	return list;
    }
}
