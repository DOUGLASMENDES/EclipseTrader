/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.yahoo.internal.core;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;

public class UtilTest extends TestCase {

	public void testGetYahooSymbol() throws Exception {
		FeedProperties properties = new FeedProperties();
		properties.setProperty("org.eclipsetrader.yahoo.symbol", "F.MI");
		FeedIdentifier identifier = new FeedIdentifier("F", properties);
		assertEquals("F.MI", Util.getSymbol(identifier));
	}

	public void testGetDefaultSymbolIfYahooIsMissing() throws Exception {
		FeedProperties properties = new FeedProperties();
		FeedIdentifier identifier = new FeedIdentifier("F.MI", properties);
		assertEquals("F.MI", Util.getSymbol(identifier));
	}
}
