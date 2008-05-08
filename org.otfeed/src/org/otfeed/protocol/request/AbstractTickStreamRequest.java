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

import org.otfeed.command.VolumeStyleEnum;
import org.otfeed.event.ICompletionDelegate;
import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTBBO;
import org.otfeed.event.OTMMQuote;
import org.otfeed.event.OTQuote;
import org.otfeed.event.OTTrade;
import org.otfeed.protocol.CommandEnum;
import org.otfeed.protocol.DataEnum;
import org.otfeed.protocol.ProtocolException;

import org.otfeed.protocol.request.quote.QuoteReader;

import java.nio.ByteBuffer;

import java.util.Map;
import java.util.HashMap;

abstract class AbstractTickStreamRequest extends AbstractSessionRequest {

	private final String exchangeCode;
	private final String symbolCode;
	private final VolumeStyleEnum volumeStyle;
	private final int mask;

	private final Map<Integer,QuoteReader> map = new HashMap<Integer,QuoteReader>();

	public String getExchangeCode()  { return exchangeCode; }
	public String getSymbolCode()    { return symbolCode; }
	public VolumeStyleEnum getVolumeStyle() { return volumeStyle; }

	public AbstractTickStreamRequest(CommandEnum command,
			int requestId, 
			String exchangeCode,
			String symbolCode,
			VolumeStyleEnum volumeStyle,
			IDataDelegate<OTQuote>   quoteDelegate,
			IDataDelegate<OTTrade>   tradeDelegate,
			IDataDelegate<OTMMQuote> mmQuoteDelegate,
			IDataDelegate<OTBBO>     bboDelegate,
			ICompletionDelegate completionDelegate) {

		super(command, requestId, completionDelegate);

		Check.notNull(exchangeCode,  "exchangeCode");
		Check.notNull(symbolCode,  "symbolCode");
		Check.notNull(volumeStyle,  "volumeStyle");

		this.exchangeCode  = exchangeCode;
		this.symbolCode    = symbolCode;
		this.volumeStyle   = volumeStyle;

		int mask = 0;
		if(quoteDelegate != null) {
			QuoteReader rdr = QuoteReader.quoteReaderEx(quoteDelegate);
			map.put(rdr.type.code, rdr);
			mask |= rdr.mask;
		}
		if(tradeDelegate != null) {
			QuoteReader rdr = QuoteReader.tradeReaderEx(tradeDelegate);
			map.put(rdr.type.code, rdr);
			mask |= rdr.mask;
		}
		if(mmQuoteDelegate != null) {
			QuoteReader rdr = QuoteReader.mmQuoteReaderEx(mmQuoteDelegate);
			map.put(rdr.type.code, rdr);
			mask |= rdr.mask;
		}
		if(bboDelegate != null) {
			// TODO: bbo events impossible here?
			QuoteReader rdr = QuoteReader.bboReaderEx(bboDelegate);
			map.put(rdr.type.code, rdr);
			mask |= rdr.mask;
		}
		if(mask == 0) {
			throw new IllegalArgumentException("must set at least one quote event delegate");
		}
		
		switch(volumeStyle) {
		case INDIVIDUAL:
			mask |= VolumeStyleEnum.INDIVIDUAL_VOLUME_FLAG;
			break;
		case COMPOUND:
			break;
		default:
			throw new IllegalArgumentException("unrecognized volumeStyle: " + volumeStyle);
		}
		this.mask = mask;
	}

	@Override
	public void writeRequest(ByteBuffer out) {
		super.writeRequest(out);

		Util.writeString(out, exchangeCode, 15);
		Util.writeString(out, symbolCode, 15);
		out.put((byte) 0);
		out.put((byte) 0);
		out.putInt(mask);
	}

	@Override
	public JobStatus handleMessage(Header header, ByteBuffer in) {

		if(header.getCommand() != CommandEnum.REQUEST_TICK_STREAM) {
			throw new ProtocolException("unexpected command: "
				+ header.getCommand(), in);
		}

		int typeCode = in.get();

		if(typeCode == DataEnum.EOD.code) {
			return JobStatus.FINISHED;
		}

		QuoteReader reader = map.get(typeCode);
		if(reader == null) {
			throw new ProtocolException("protocol error: unknown type code " + typeCode, in);
		}

		reader.read(header, in);

		return JobStatus.ACTIVE;
	}
}
