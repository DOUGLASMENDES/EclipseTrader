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

import org.otfeed.command.MonthAndYear;
import org.otfeed.command.OptionTypeEnum;
import org.otfeed.command.PriceRange;
import org.otfeed.event.ICompletionDelegate;
import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTOptionInit;

import org.otfeed.protocol.CommandEnum;
import org.otfeed.protocol.DataEnum;
import org.otfeed.protocol.ParamTypeEnum;
import org.otfeed.protocol.ProtocolException;

import java.nio.ByteBuffer;

/**
 * Request to receive information on option.
 */
public final class OptionInitRequest extends AbstractSessionRequest {

	private final String exchangeCode;
	private final String symbolCode;
	private final MonthAndYear expiration;
	private final PriceRange   strikeRange;

	private final IDataDelegate<OTOptionInit> dataDelegate;

	public String getExchangeCode()  { return exchangeCode; }
	public String getSymbolCode()    { return symbolCode; }

	public OptionInitRequest(
			int requestId, 
			String exchangeCode,
			String symbolCode,
			MonthAndYear expiration,
			PriceRange strikeRange,
			IDataDelegate<OTOptionInit> dataDelegate,
			ICompletionDelegate completionDelegate) {

		super(CommandEnum.REQUEST_OPTION_INIT, 
				requestId, completionDelegate);

		Check.notNull(exchangeCode,  "exchangeCode");
		Check.notNull(symbolCode,  "symbolCode");
		Check.notNull(dataDelegate,  "dataDelegate");

		this.exchangeCode    = exchangeCode;
		this.symbolCode      = symbolCode;
		this.expiration      = expiration; // optional
		this.strikeRange     = strikeRange; // optional

		this.dataDelegate    = dataDelegate;
	}

	@Override
	public void writeRequest(ByteBuffer out) {
		super.writeRequest(out);

		int paramMask = 0;
		if(expiration != null) {
			paramMask |= ParamTypeEnum.BY_DATE.code;
		}

		if(strikeRange != null) {
			paramMask |= ParamTypeEnum.BY_PRICE.code;
		}

		Util.writeString(out, exchangeCode, 15);
		Util.writeString(out, symbolCode, 15);
		if(expiration != null) {
			out.putShort((short) expiration.month);
			out.putInt(expiration.year);
		} else {
			out.putShort((short) 0);
			out.putInt(0);
		}

		if(strikeRange != null) {
			out.putDouble(strikeRange.min); // ??? endianess
			out.putDouble(strikeRange.max); // ??? endianess
		} else {
			out.putDouble(0.0); // ??? endianess
			out.putDouble(0.0); // ??? endianess
		}

		out.putInt(paramMask);         // ??? endianess
	}

	@Override
	public JobStatus handleMessage(Header header, ByteBuffer in) {
		super.handleMessage(header, in);

		int typeCode = in.get();
		if(typeCode == DataEnum.EOD.code) {
			return JobStatus.FINISHED;
		}

		if(typeCode != DataEnum.OPTION_INIT.code) {
			throw new ProtocolException("unexpected data code: " + typeCode, in);
		}

		String underlyerSymbol = Util.readString(in, 12);
		String symbol          = Util.readString(in, 12);
		double strikePrice     = in.getDouble();
		int contractSize       = in.getInt();

		// some confusion exists between ot drivers as
		// to whether its binary or textual.
		int expirationYear     = Integer.parseInt(
				Util.readString(in, 4));
		short expirationMonth  = Short.parseShort(
				Util.readString(in, 2)); 
		short expirationDay    = Short.parseShort(
				Util.readString(in, 2));

		OptionTypeEnum exersizeStyle = Util.readOptionTypeEnum(in);
		String underlyerCusip        = Util.readString(in, 9);
		String currency              = Util.readString(in, 3);
		char optionMarker            = (char) in.get();

		OTOptionInit optionInit = new OTOptionInit(
			underlyerSymbol,
			symbol,
			strikePrice,
			contractSize,
			expirationYear,
			expirationMonth,
			expirationDay,
			exersizeStyle,
			underlyerCusip,
			currency,
			optionMarker);

		dataDelegate.onData(optionInit);

		return JobStatus.ACTIVE;
	}
}
