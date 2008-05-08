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
import org.otfeed.event.InstrumentEnum;
import org.otfeed.event.OTEquityInit;
import org.otfeed.protocol.CommandEnum;
import org.otfeed.protocol.DataEnum;
import org.otfeed.protocol.ProtocolException;

import java.nio.ByteBuffer;

/**
 * request to receive equity info.
 */
public final class EquityInitRequest extends AbstractSessionRequest {

	private final String exchangeCode;
	private final String symbolCode;

	private final IDataDelegate<OTEquityInit> dataDelegate;

	public String getExchangeCode()  { return exchangeCode; }
	public String getSymbolCode()    { return symbolCode; }

	public EquityInitRequest(int requestId, 
			String exchangeCode,
			String symbolCode,
			IDataDelegate<OTEquityInit> dataDelegate,
			ICompletionDelegate completionDelegate) {

		super(CommandEnum.REQUEST_EQUITY_INIT, 
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
	}

	@Override
	public JobStatus handleMessage(Header header, ByteBuffer in) {
		super.handleMessage(header, in);

		int typeCode = in.get();
		if(typeCode != DataEnum.EQUITY_INIT.code) {
			throw new ProtocolException("unrecognized type: " + typeCode, in);
		}

		String curency         = Util.readString(in, 3);
		InstrumentEnum type    = Util.readInstrumentEnum(in);
		String company         = Util.readString(in, 80);
		double prevClosePrice  = in.getDouble();
		String prevCloseDate   = Util.readString(in, 8);
		double annualHighPrice = in.getDouble();
		String annualHighDate  = Util.readString(in, 8);
		double annualLowPrice  = in.getDouble();
		String annualLowDate   = Util.readString(in, 8);
		double earningsPrice   = in.getDouble();
		String earningsDate    = Util.readString(in, 8);
		long totalShares       = in.getLong();
		long averageVolume     = in.getLong();
		String CUSIP           = Util.readString(in, 9);
		String ISIN            = Util.readString(in, 12);
		boolean isUPC11830     = Util.readBoolean(in);
		boolean isSmallCap     = Util.readBoolean(in);
		boolean isTestlssue    = Util.readBoolean(in);

		OTEquityInit equity = new OTEquityInit(
			curency, 
			type, 
			company,
			prevClosePrice, 
			prevCloseDate, 
			annualHighPrice, 
			annualHighDate,
			annualLowPrice, 
			annualLowDate, 
			earningsPrice, 
			earningsDate,
			totalShares, 
			averageVolume, 
			CUSIP, 
			ISIN, 
			isUPC11830,
			isSmallCap,
			isTestlssue);

		dataDelegate.onData(equity);

		return JobStatus.FINISHED;
	}
}
