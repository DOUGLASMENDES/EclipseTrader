/**
 * Copyright 2007 Mike Kroutikov.
 *
 * This program is free software; you can redistribute it and/or modify
 *   it under the terms of the Lesser GNU General Public License as 
 *   published by the Free Software Foundation; either version 3 of
 *   the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   Lesser GNU General Public License for more details.
 *
 *   You should have received a copy of the Lesser GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.otfeed.support;

import org.otfeed.event.IConnectionStateListener;
import org.otfeed.event.OTError;
import org.otfeed.event.OTHost;

/**
 * A no-op implementation of
 * {@link IConnectionStateListener}.
 * Useful if one needs to override just one or two methods, and is 
 * not interested in monitoring other connection state changes.
 */
public class ConnectionStateListener implements IConnectionStateListener {

	public void onConnecting(OTHost addr) { }

	public void onConnected() { }

	public void onRedirect(OTHost addr) { }

	public void onLogin() { }

	public void onError(OTError error) { }
}
