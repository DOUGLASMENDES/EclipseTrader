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
import org.otfeed.command.PriceRange;
import org.otfeed.command.VolumeStyleEnum;
import org.otfeed.event.ICompletionDelegate;
import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTBBO;
import org.otfeed.event.OTMMQuote;
import org.otfeed.event.OTQuote;
import org.otfeed.event.OTTrade;
import org.otfeed.protocol.CommandEnum;
import org.otfeed.protocol.DataEnum;
import org.otfeed.protocol.ParamTypeEnum;
import org.otfeed.protocol.ProtocolException;

import org.otfeed.protocol.request.quote.QuoteReader;

import java.nio.ByteBuffer;

import java.util.Map;
import java.util.HashMap;

abstract class AbstractOptionChainRequest 
			extends AbstractTickStreamRequest {

	private final MonthAndYear expiration;
	private final PriceRange   strikeRange;
	private final int mask;

	private final Map<Integer,QuoteReader> map = new HashMap<Integer,QuoteReader>();

	public AbstractOptionChainRequest(CommandEnum command,
			int requestId, 
			String exchangeCode,
			String symbolCode,
			MonthAndYear expiration,
			PriceRange strikeRange,
			VolumeStyleEnum volumeStyle,
			
			IDataDelegate<OTQuote>   quoteDelegate,
			IDataDelegate<OTTrade>   tradeDelegate,
			IDataDelegate<OTMMQuote> mmQuoteDelegate,
			IDataDelegate<OTBBO>     bboDelegate,
			
			ICompletionDelegate completionDelegate) {

		super(command,
				requestId, 
				exchangeCode,
				symbolCode,
				volumeStyle,
				quoteDelegate,
				tradeDelegate,
				mmQuoteDelegate,
				bboDelegate,
				completionDelegate);
		
		this.expiration      = expiration;  // optional
		this.strikeRange     = strikeRange; // optional

		int mask = 0;
		if(quoteDelegate != null) {
			QuoteReader rdr = QuoteReader.quoteReaderExEx(quoteDelegate);
			map.put(rdr.type.code, rdr);
			mask |= rdr.mask;
		}
		if(tradeDelegate != null) {
			QuoteReader rdr = QuoteReader.tradeReaderExEx(tradeDelegate);
			map.put(rdr.type.code, rdr);
			mask |= rdr.mask;
		}
		if(mmQuoteDelegate != null) {
			QuoteReader rdr = QuoteReader.mmQuoteReaderExEx(mmQuoteDelegate);
			map.put(rdr.type.code, rdr);
			mask |= rdr.mask;
		}
		if(bboDelegate != null) {
			// TODO: bbo events impossible here?
			QuoteReader rdr = QuoteReader.bboReaderExEx(bboDelegate);
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
		
		// FIXME: dirty
		// superclass wrote more than we actually need. step back
		out.position(out.position() - 6);

		int paramMask = 0;

		if(expiration != null) {
			out.putShort((short) expiration.month);
			out.putInt(expiration.year);
			paramMask |= ParamTypeEnum.BY_DATE.code;
		} else {
			out.putShort((short) 0);
			out.putInt(0);
		}

		out.putInt(mask);

		if(strikeRange != null) {
			out.putDouble(strikeRange.min); // ??? endianess
			out.putDouble(strikeRange.max); // ??? endianess
			paramMask |= ParamTypeEnum.BY_PRICE.code;
		} else {
			out.putDouble(0.0); // ??? endianess
			out.putDouble(0.0); // ??? endianess
		}

		out.putInt(paramMask);         // ??? endianess
	}

	@Override
	public JobStatus handleMessage(Header header, ByteBuffer in) {

		if(header.getCommand() != CommandEnum.REQUEST_OPTION_CHAIN) {
			throw new ProtocolException("mismatch in command type: " 
				+ header.getCommand(), in);
		}

		int typeCode = in.get();
		if(typeCode == DataEnum.EOD.code) {
			return JobStatus.FINISHED;
		}

		QuoteReader reader = map.get(typeCode);
		if(reader == null) {
			throw new ProtocolException("unrecognized type: " + typeCode, in);
		}

		reader.read(header, in);

		return JobStatus.ACTIVE;
	}
}
