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

package org.eclipsetrader.core.trading;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

public interface ITradingService {

	public IBrokerConnector[] getBrokerConnectors();

	public IBrokerConnector getBrokerConnector(String id);

	public void addOrders(IOrderMonitor[] order);

	public void removeOrders(IOrderMonitor[] order);

	public void updateOrders(IOrderMonitor[] order);

	public IOrderMonitor[] getOrders();

	public void addOrderChangeListener(IOrderChangeListener listener);

	public void removeOrderChangeListener(IOrderChangeListener listener);

	public IStatus runInService(ITradingServiceRunnable runnable, IProgressMonitor monitor);

	public IStatus runInService(ITradingServiceRunnable runnable, ISchedulingRule rule, IProgressMonitor monitor);
}
