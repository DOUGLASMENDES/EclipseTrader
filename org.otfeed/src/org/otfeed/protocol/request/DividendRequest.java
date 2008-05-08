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
import org.otfeed.event.OTDividend;
import org.otfeed.event.DividendPropertyEnum;
import org.otfeed.protocol.CommandEnum;
import org.otfeed.protocol.DataEnum;
import org.otfeed.protocol.ProtocolException;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Set;


/**
 * Request to receive dividend info.
 * 
 * @see OTDividend
 */
public final class DividendRequest extends AbstractSessionRequest {

	private final String exchangeCode;
	private final String symbolCode;
	private final Date   startDate;
	private final Date   endDate;

	private final IDataDelegate<OTDividend> dataDelegate;

	public String getExchangeCode()  { return exchangeCode; }
	public String getSymbolCode()    { return symbolCode; }

	public DividendRequest(int requestId, 
			String exchangeCode,
			String symbolCode,
			Date   startDate,
			Date   endDate,
			IDataDelegate<OTDividend> dataDelegate,
			ICompletionDelegate completionDelegate) {

		super(CommandEnum.REQUEST_DIVIDENDS, 
				requestId,
				completionDelegate);

		Check.notNull(exchangeCode,  "exchangeCode");
		Check.notNull(symbolCode,  "symbolCode");
		Check.notNull(startDate, "startDate");
		Check.notNull(endDate, "endDate");
		Check.notNull(dataDelegate,  "dataDelegate");
		
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

		if(typeCode != DataEnum.DIVIDEND.code) {
			throw new ProtocolException("unrecognized type: " + typeCode, in);
		}

		double price         = in.getDouble();
		Date declarationDate = Util.readDate(in);
		Date executionDate   = Util.readDate(in);
		Date recordDate      = Util.readDate(in);
		Date paymentDate     = Util.readDate(in);
		Set<DividendPropertyEnum> props = Util.readDividendProperrtyEnumSet(in);

		OTDividend divident = new OTDividend(
			price,
			declarationDate,
			executionDate,
			recordDate,
			paymentDate,
			props);

		dataDelegate.onData(divident);

		return JobStatus.FINISHED;
	}
}
