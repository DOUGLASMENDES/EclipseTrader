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
import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTExchange;
import org.otfeed.protocol.CommandEnum;

import java.nio.ByteBuffer;

/**
 * Request to get list of exchanges.
 */
public class ListExchangesRequest extends AbstractSessionRequest {

	private final IDataDelegate<OTExchange> dataDelegate;

	public ListExchangesRequest(int requestId, 
		IDataDelegate<OTExchange> dataDelegate,
		ICompletionDelegate completionDelegate) {

		super(CommandEnum.REQUEST_LIST_EXCHANGES, 
				requestId, completionDelegate);

		Check.notNull(dataDelegate,  "dataDelegate");
		
		this.dataDelegate = dataDelegate;

	}

	@Override
	public JobStatus handleMessage(Header header, ByteBuffer in) {
		super.handleMessage(header, in);

		String subscriptionURL = Util.readString(in);
		int exchangesNum = in.getShort();

		for (int i = 0; i < exchangesNum; i++) {

			String  code        = Util.readString(in, 15);
			boolean isAvailable = Util.readBoolean(in);
			String  title       = Util.readString(in);
			String  desc        = Util.readString(in);

			OTExchange exchange = new OTExchange(
					code,
					title,
                        		desc, 
					isAvailable,
					subscriptionURL);

			dataDelegate.onData(exchange);
		}

		return JobStatus.FINISHED;
	}
}
