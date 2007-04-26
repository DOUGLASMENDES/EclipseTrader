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

package net.sourceforge.eclipsetrader.ats.core;

import net.sourceforge.eclipsetrader.ats.core.events.IBarListener;
import net.sourceforge.eclipsetrader.ats.core.events.IMarketListener;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Security;

/**
 * Default implementation of the {@link IMarketManager} interface.
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public class MarketManager implements IMarketManager {

	public MarketManager() {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#addBarListener(net.sourceforge.eclipsetrader.ats.core.events.IBarListener)
	 */
	public void addBarListener(IBarListener l) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#addBarListener(net.sourceforge.eclipsetrader.core.db.Security, net.sourceforge.eclipsetrader.ats.core.events.IBarListener)
	 */
	public void addBarListener(Security security, IBarListener l) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#addMarketListener(net.sourceforge.eclipsetrader.ats.core.events.IMarketListener)
	 */
	public void addMarketListener(IMarketListener l) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#addMarketListener(net.sourceforge.eclipsetrader.core.db.Security, net.sourceforge.eclipsetrader.ats.core.events.IMarketListener)
	 */
	public void addMarketListener(Security security, IMarketListener l) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#addSecurity(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public void addSecurity(Security security) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#getBars(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public Bar[] getBars(Security security) {
		return new Bar[0];
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#removeBarListener(net.sourceforge.eclipsetrader.ats.core.events.IBarListener)
	 */
	public void removeBarListener(IBarListener l) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#removeBarListener(net.sourceforge.eclipsetrader.core.db.Security, net.sourceforge.eclipsetrader.ats.core.events.IBarListener)
	 */
	public void removeBarListener(Security security, IBarListener l) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#removeMarketListener(net.sourceforge.eclipsetrader.ats.core.events.IMarketListener)
	 */
	public void removeMarketListener(IMarketListener l) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#removeMarketListener(net.sourceforge.eclipsetrader.core.db.Security, net.sourceforge.eclipsetrader.ats.core.events.IMarketListener)
	 */
	public void removeMarketListener(Security security, IMarketListener l) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#removeSecurity(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public void removeSecurity(Security security) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#start()
	 */
	public void start() {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IMarketManager#stop()
	 */
	public void stop() {
	}
}
