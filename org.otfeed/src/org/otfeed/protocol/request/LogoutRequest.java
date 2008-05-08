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

package org.otfeed.protocol.request;

import org.otfeed.event.ICompletionDelegate;
import org.otfeed.protocol.CommandEnum;

import java.nio.ByteBuffer;

/**
 * Logout request.
 */
public class LogoutRequest extends AbstractSessionRequest {

	public LogoutRequest(int requestId,
			ICompletionDelegate completionDelegate) {
		super(CommandEnum.LOGOUT, requestId, completionDelegate);
	}

	@Override
	public JobStatus handleMessage(Header header, ByteBuffer in) {
		super.handleMessage(header, in);

		return JobStatus.FINISHED;
	}
}
