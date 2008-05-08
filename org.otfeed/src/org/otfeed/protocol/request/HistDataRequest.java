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

import org.otfeed.command.AggregationSpan;
import org.otfeed.event.ICompletionDelegate;
import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTOHLC;
import org.otfeed.protocol.CommandEnum;
import org.otfeed.protocol.DataEnum;
import org.otfeed.protocol.ProtocolException;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Request to receive aggregated historical data.
 */
public final class HistDataRequest extends AbstractSessionRequest {

	private final String           exchangeCode;
	private final String           symbolCode;
	private final Date             startDate;
	private final Date             endDate;
	private final AggregationSpan  aggregationSpan;

	private final IDataDelegate<OTOHLC> dataDelegate;

	public String            getExchangeCode()  { return exchangeCode; }
	public String            getSymbolCode()    { return symbolCode; }
	public Date              getStartDate()     { return startDate; }
	public Date              getEndDate()       { return endDate; }
	public AggregationSpan   getAggregationSpan() { return aggregationSpan; }

	public HistDataRequest(int requestId, 
			String exchangeCode,
			String symbolCode,
			Date startDate,
			Date endDate,
			AggregationSpan aggregationSpan,
			final IDataDelegate<OTOHLC> dataDelegate,
			ICompletionDelegate completionDelegate) {

		super(CommandEnum.REQUEST_HIST_DATA, 
				requestId, completionDelegate);

		Check.notNull(exchangeCode,  "exchangeCode");
		Check.notNull(symbolCode,  "symbolCode");
		Check.notNull(startDate, "startDate");
		Check.notNull(endDate, "endDate");
		Check.notNull(aggregationSpan, "aggreagationSpan");

		Check.notNull(dataDelegate,  "dataDelegate");

		this.exchangeCode    = exchangeCode;
		this.symbolCode      = symbolCode;
		this.startDate       = startDate;
		this.endDate         = endDate;
		this.aggregationSpan = aggregationSpan;
		this.dataDelegate    = dataDelegate;

	}

	@Override
	public void writeRequest(ByteBuffer out) {
		super.writeRequest(out);

		Util.writeString(out, exchangeCode, 15);
		Util.writeString(out, symbolCode, 15);
		out.put((byte) 0);
		out.put((byte) 0);
		Util.writeDate(out, startDate);
		Util.writeDate(out, endDate);
		Util.writeAggreagationSpan(out, aggregationSpan);
	}

	private final static OTOHLC readOHLC(ByteBuffer in) {
		Date ohlc_timestamp    = Util.readDate(in);
		double ohlc_openPrice  = in.getDouble();
		double ohlc_highPrice  = in.getDouble();
		double ohlc_lowPrice   = in.getDouble();
		double ohlc_closePrice = in.getDouble();
		long ohlc_volume       = in.getLong();

		return new OTOHLC(ohlc_timestamp,
			ohlc_openPrice, ohlc_highPrice, ohlc_lowPrice,
			ohlc_closePrice, ohlc_volume);
	}

	@Override
	public JobStatus handleMessage(Header header, ByteBuffer in) {
		super.handleMessage(header, in);

		int count = in.getInt();

		while( count-- > 0) {

			int typeCode = in.get();

			if(typeCode == DataEnum.EOD.code) {
				return JobStatus.FINISHED;
			}
			
			if(typeCode != DataEnum.OHLC.code) {
				throw new ProtocolException("unexpected historical data code: " + typeCode, in);
			}

			OTOHLC ohlc = readOHLC(in);
			dataDelegate.onData(ohlc);
		}

		return JobStatus.ACTIVE;
	}

	@Override
	public final CommandEnum getCancelCommand() {
		return CommandEnum.CANCEL_HIST_DATA; // fixme??
	}
}
