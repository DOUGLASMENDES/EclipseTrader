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
import org.otfeed.command.BookDeleteTypeEnum;
import org.otfeed.command.ListSymbolEnum;
import org.otfeed.command.OptionTypeEnum;
import org.otfeed.command.ListSymbolsCommand.MatchStyleEnum;
import org.otfeed.event.DividendPropertyEnum;
import org.otfeed.event.InstrumentEnum;
import org.otfeed.event.OTError;
import org.otfeed.event.TradePropertyEnum;
import org.otfeed.event.TradeSideEnum;
import org.otfeed.protocol.CommandEnum;
import org.otfeed.protocol.ErrorEnum;
import org.otfeed.protocol.MessageEnum;
import org.otfeed.protocol.ProtocolException;
import org.otfeed.protocol.StatusEnum;

import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Serialization and other utility functions.
 */
public final class Util {

	private static final Charset UTF8 = Charset.forName("utf-8");

	private Util() { }

	/**
	 * Writes a variable-length string to buffer.
	 * Internal format is a two-byte little-endian "short"
	 * specifying the length, followed by utf-8 encoded bytes.
	 */
	public static final void writeString(ByteBuffer out, String val) {
		ByteBuffer encoded = UTF8.encode(val);
		out.putShort((short) encoded.limit());
		out.put(encoded);
	}

	/**
	 * Reads variable-length string.
	 */
	public static final String readString(ByteBuffer in) {
		int len = in.getShort();

		ByteBuffer encoded = in.duplicate();
		in.position(in.position() + len);

		encoded.limit(encoded.position() + len);

		return UTF8.decode(encoded).toString().trim();
	}


	/**
	 * Writes a fixed-length string (padded by binary zeroes if needed).
	 */
	public static final void writeString(ByteBuffer out, String val, int len) {
		ByteBuffer encoded = UTF8.encode(val);

		if(encoded.limit() >= len) {
			encoded.limit(len);
			out.put(encoded);
		} else {
			int padding = len - encoded.limit();

			out.put(encoded);

			while(padding-- > 0) {
				out.put((byte) 0);
			}
		}
	}

	/**
	 * Reads a fixed-length string.
	 */
	public static final String readString(ByteBuffer in, int len) {

		ByteBuffer encoded = in.duplicate();
		in.position(in.position() + len);

		while(len > 0) {
			if(encoded.get(encoded.position() + len - 1) == 0) {
				len--;
			} else {
				break;
			}
		}
		encoded.limit(encoded.position() + len);

		return UTF8.decode(encoded).toString().trim();
	}
	
	public static final OTError readError(int reqID, ByteBuffer in) {

		short errorCode = in.getShort();
		String reason = Util.readString(in);

		return new OTError(reqID, errorCode, reason);
	}

	public static final OTError newError(ErrorEnum code, String message) {
		return new OTError(0, code.code, message);
	}

	public static final OTError newError(String message) {
		return new OTError(0, ErrorEnum.E_OTFEED_INTERNAL.code, message);
	}

	public static final Date readDate(ByteBuffer in) {
		int stamp = in.getInt();
		
		if(stamp == 0) return null;
		
		return new Date(1000L * stamp);
	}

	public static final void writeDate(ByteBuffer out, Date date) {
		if(date == null) {
			out.putInt(0);
		} else {
			int stamp = (int) (date.getTime() / 1000L);
			out.putInt(stamp);
		}
	}

	public static final boolean readBoolean(ByteBuffer in) {
		int c;
		// not sure whether this is supposed to be 'Y' or '1'
		// .NET driver uses 'Y', Java and C++ use '1'.
		// untill known, let me be very suspicious here
		//
		// well, testing showed that
		// book events use Y/N, others use 1/0. Therefore have to
		// support both types.
		c = in.get();
		if(c == 0) {
			return false;
		} else if(c == 1) {
			return true;
		} else if(c == 'N') {
			return false;
		} else if(c == 'Y') {
			return true;
		} else {
			throw new ProtocolException("boolean format is incorrect: " + ((char) c), in);
		}
	}
	
	public static final TradeSideEnum readTradeSideEnum(ByteBuffer in) {
		char sideCode = (char) in.get();
		if(sideCode == 'B') {
			return TradeSideEnum.BUYER;
		} else if(sideCode == 'S') {
			return TradeSideEnum.SELLER;
		} else {
			throw new ProtocolException("unexpected side code: " + sideCode, in);
		}
	}

	public static final OptionTypeEnum readOptionTypeEnum(ByteBuffer in) {
		char code = (char) in.get();
		if(code == 'A') {
			return OptionTypeEnum.AMERICAN;
		} else if(code == 'E') {
			return OptionTypeEnum.EUROPEAN;
		} else if(code == 'C') {
			return OptionTypeEnum.CAPPED;
		} else {
			throw new ProtocolException("unexpected option type code: " + code, in);
		}
	}
	
	public static final InstrumentEnum readInstrumentEnum(ByteBuffer in) {

		int code = in.get();
		InstrumentEnum i = InstrumentEnum.decoder.get(code);
		if(i == null) {
			throw new ProtocolException("unknown instrument code: " + code, in);
		}
		
		return i;
	}
	
	public static final BookDeleteTypeEnum readBookDeleteTypeEnum(ByteBuffer in) {
		int code = in.get();
		
		BookDeleteTypeEnum deleteType = BookDeleteTypeEnum.decoder.get(code);
		if(deleteType == null) {
			throw new ProtocolException("unknown delete type code: " + code, in);
		}
		
		return deleteType;
	}
	
	public static final StatusEnum readStatusEnum(ByteBuffer in) {
		int code = in.get();
		
		StatusEnum status = StatusEnum.decode(code);
		if(status == null) {
			throw new ProtocolException("unknown status code: " + code, in);
		}
		return status;
	}
	
	public static final MessageEnum readMessageEnum(ByteBuffer in) {
		int code = in.get();
		
		MessageEnum mess = MessageEnum.decoder.get(code);
		if(mess == null) {
			throw new ProtocolException("unknown message type code: " + code, in);
		}
		return mess;
	}
	
	public static final CommandEnum readCommandEnum(ByteBuffer in) {
		int code = in.getInt();

		CommandEnum command = CommandEnum.decoder.get(code);
		if(command == null) {
			throw new ProtocolException("unknown command code: " + code, in);
		}
		return command;
	}
	
	private static final Set<TradePropertyEnum> TRADE_PROPERTY_ENUM_ALL 
			= EnumSet.allOf(TradePropertyEnum.class);

	public static final Set<TradePropertyEnum> readTradePropertySet(ByteBuffer in) {
		int mask = in.get();
		if(mask < 0) mask += 256; // unsigned byte
		
		Set<TradePropertyEnum> set = EnumSet.noneOf(TradePropertyEnum.class);

		for(TradePropertyEnum p : TRADE_PROPERTY_ENUM_ALL) {
			if((mask & p.code) != 0) {
				set.add(p);
				mask ^= p.code;
			}
		}

		if(mask != 0) {
			throw new ProtocolException("extra bits in the trade properties mask: " + mask, in);
		}

		return set;
	}

	private final static Set<DividendPropertyEnum> DIVIDEND_PROPERTY_ENUM_ALL
			= EnumSet.allOf(DividendPropertyEnum.class);
	
	public static final Set<DividendPropertyEnum> readDividendProperrtyEnumSet(ByteBuffer in) {
		int mask = in.getShort();
		
		Set<DividendPropertyEnum> set = EnumSet.noneOf(DividendPropertyEnum.class);
		
		for(DividendPropertyEnum p : DIVIDEND_PROPERTY_ENUM_ALL) {
			if((mask & p.code) != 0) {
				set.add(p);
				mask ^= p.code;
			}
		}

//
//      mpk: for "N/PG" server returns 0x3030, which leaves unexplained
//		bits of 0x3000...
//
//		if(mask != 0) {
//			throw new ProtocolException("unhandled bits in the dividend property mask: 0x" + Integer.toHexString(mask), in);
//		}

		return set;
	}
	
	public static final void writeListSymbolMask(ByteBuffer out, Set<ListSymbolEnum> types, MatchStyleEnum matchStyle) {

		int mask = 0;
		for(ListSymbolEnum t : types) {
			mask |= t.code;
		}

		switch(matchStyle) {
		case PREFIX: 
			break;
		case CONTAINS:
			mask |= ListSymbolEnum.CONTAINS_FLAG;
			break;
		default:
			throw new IllegalArgumentException("unexpected match type");
		}
		
		out.putInt(mask);
	}
	
	public static final void writeAggreagationSpan(ByteBuffer out, AggregationSpan span) {
		out.put((byte) span.units.code);
		out.put((byte) 0);
		out.putShort((short) span.length);
	}
}
