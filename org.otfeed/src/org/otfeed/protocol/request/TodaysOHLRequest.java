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
import org.otfeed.event.OTTodaysOHL;
import org.otfeed.protocol.CommandEnum;
import org.otfeed.protocol.DataEnum;
import org.otfeed.protocol.ProtocolException;

import java.nio.ByteBuffer;

/**
 * Request to receive current OHL (open-high-low) data.
 */
public class TodaysOHLRequest extends AbstractSessionRequest {

	private final String exchangeCode;
	private final String symbolCode;

	private final IDataDelegate<OTTodaysOHL> dataDelegate;

	public String getExchangeCode()  { return exchangeCode; }
	public String getSymbolCode()    { return symbolCode; }

	public TodaysOHLRequest(int requestId, 
			String exchangeCode,
			String symbolCode,
			IDataDelegate<OTTodaysOHL> dataDelegate,
			ICompletionDelegate completionDelegate) {

		super(CommandEnum.REQUEST_HIST_DATA, 
				requestId, completionDelegate);

		Check.notNull(exchangeCode,  "exchangeCode");
		Check.notNull(symbolCode,  "symbolCode");
		Check.notNull(dataDelegate,  "dataDelegate");

		this.exchangeCode  = exchangeCode;
		this.symbolCode    = symbolCode;
		this.dataDelegate  = dataDelegate;
	}

	@Override
	public void writeRequest(ByteBuffer out) {
		super.writeRequest(out);

		Util.writeString(out, exchangeCode, 15);
		Util.writeString(out, symbolCode, 15);
		out.put((byte) 0);
		out.put((byte) 0);
		out.putInt(0);
		out.putInt(0);
		out.put((byte) 9); // special code, meaning "get ohlc"
		out.put((byte) 0);
		out.putShort((short) 0);
	}

	private static OTTodaysOHL readTodaysOHL(ByteBuffer in) {
		double ohl_openPrice = in.getDouble();
		double ohl_highPrice = in.getDouble();
		double ohl_lowPrice  = in.getDouble();

		return new OTTodaysOHL(ohl_openPrice,
			ohl_highPrice, ohl_lowPrice);
	}

	@Override
	public JobStatus handleMessage(Header header, ByteBuffer in) {
		super.handleMessage(header, in);

		int count = in.getInt();

		// to the best of my knowledge, TodaysOHL request returns only
		// one event. Therefore, we have to return end-of data
		// right away... Note that this is exactly what CPP driver from Opentick
		// Corp. does (but not .NET one).
		if(count < 0 || count > 1) {
			throw new ProtocolException("unexpected number of todaysOHL events", in);
		}

		while(count -- > 0) {
			int typeCode = in.get();

			if(typeCode == DataEnum.EOD.code) {
				return JobStatus.FINISHED;
			}
			
			if(typeCode != DataEnum.OHL_TODAY.code) {
				throw new ProtocolException("unexpected OHL data code: " + typeCode, in);
			}

			OTTodaysOHL ohl = readTodaysOHL(in);
			dataDelegate.onData(ohl);
		}

		return JobStatus.FINISHED; // ???
	}
}
