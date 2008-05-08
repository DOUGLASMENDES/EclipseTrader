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
import org.otfeed.event.OTSplit;
import org.otfeed.protocol.CommandEnum;
import org.otfeed.protocol.DataEnum;
import org.otfeed.protocol.ProtocolException;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Request to receive split information.
 */
public class SplitRequest extends AbstractSessionRequest {

	private final String exchangeCode;
	private final String symbolCode;
	private final Date   startDate;
	private final Date   endDate;

	private final IDataDelegate<OTSplit> dataDelegate;

	public String getExchangeCode()  { return exchangeCode; }
	public String getSymbolCode()    { return symbolCode; }

	public SplitRequest(int requestId, 
			String exchangeCode,
			String symbolCode,
			Date   startDate,
			Date   endDate,
			IDataDelegate<OTSplit> dataDelegate,
			ICompletionDelegate completionDelegate) {

		super(CommandEnum.REQUEST_SPLITS, 
				requestId,
				completionDelegate);

		Check.notNull(exchangeCode,  "exchangeCode");
		Check.notNull(symbolCode,  "symbolCode");
		Check.notNull(startDate, "startDate");
		Check.notNull(endDate, "endDate");
		Check.notNull(dataDelegate, "dataDelegate");

		this.exchangeCode  = exchangeCode;
		this.symbolCode    = symbolCode;
		this.startDate     = startDate;
		this.endDate       = endDate;

		this.dataDelegate  = dataDelegate;
	}

	@Override
	public void writeRequest(ByteBuffer out) {
		super.writeRequest(out);

		Util.writeString(out, exchangeCode, 15);
		Util.writeString(out, symbolCode, 15);
		Util.writeDate(out, startDate);
		Util.writeDate(out, endDate);
	}

	@Override
	public JobStatus handleMessage(Header header, ByteBuffer in) {
		super.handleMessage(header, in);

		int typeCode = in.get();
		if(typeCode == DataEnum.EOD.code) {
			return JobStatus.FINISHED;
		}

		if(typeCode != DataEnum.SPLIT.code) {
			throw new ProtocolException("unrecognized type: " + typeCode, in);
		}

		int toFactor         = in.getInt();
		int forFactor        = in.getInt();
		Date declarationDate = Util.readDate(in);
		Date executionDate   = Util.readDate(in);
		Date recordDate      = Util.readDate(in);
		Date paymentDate     = Util.readDate(in);

		OTSplit split = new OTSplit(
			toFactor,
			forFactor,
			declarationDate,
			executionDate,
			recordDate,
			paymentDate);

		dataDelegate.onData(split);

		return JobStatus.ACTIVE;
	}
}
