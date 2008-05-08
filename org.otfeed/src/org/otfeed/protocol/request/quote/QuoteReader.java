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

package org.otfeed.protocol.request.quote;

import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTBBO;
import org.otfeed.event.OTMMQuote;
import org.otfeed.event.OTQuote;
import org.otfeed.event.OTTrade;
import org.otfeed.event.TradePropertyEnum;
import org.otfeed.event.TradeSideEnum;

import org.otfeed.protocol.DataEnum;
import org.otfeed.protocol.ProtocolException;
import org.otfeed.protocol.request.Header;
import org.otfeed.protocol.request.Util;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Set;

/**
 * Class that knows how to de-serialize miscellaneous Quote events.
 */
public abstract class QuoteReader {

	// no subclassing
	private QuoteReader(DataEnum type) {
		this.type = type;
		this.mask = 1 << (type.code - 1);
	}

	public final DataEnum type;
	public final int mask;

	/**
	 * Reads data frame, parses response from the server, and 
	 * emits events.
	 *
	 * @param h response header.
	 * @param in data frame.
	 */
	public abstract void read(Header h, ByteBuffer in);

	/**
	 * Creates reader for the Quote event.
	 */
	public static final QuoteReader quoteReader(
			final IDataDelegate<OTQuote> dataDelegate) {
		return new QuoteReader(DataEnum.QUOTE) {

			@Override
			public void read(Header h, ByteBuffer in) {
				OTQuote quote = readQuote(in);
				dataDelegate.onData(quote);
			}
			
		};
	}
	
	/**
	 * Creates reader for the Quote event.
	 */
	public static final QuoteReader quoteReaderEx(
			final IDataDelegate<OTQuote> dataDelegate) {
		return new QuoteReader(DataEnum.QUOTE) {

			@Override
			public void read(Header h, ByteBuffer in) {
				OTQuote quote = readQuoteEx(in);
				dataDelegate.onData(quote);
			}
			
		};
	}

	/**
	 * Creates reader for the Quote event.
	 */
	public static final QuoteReader quoteReaderExEx(
			final IDataDelegate<OTQuote> dataDelegate) {
		return new QuoteReader(DataEnum.QUOTE) {

			@Override
			public void read(Header h, ByteBuffer in) {
				OTQuote quote = readQuoteExEx(in);
				dataDelegate.onData(quote);
			}
			
		};
	}

	private final static OTQuote readQuote(ByteBuffer in) {
		Date quote_timestamp     = Util.readDate(in);
		int quote_bidSize        = in.getInt();
		int quote_askSize        = in.getInt();
		double quote_bidPrice    = in.getDouble();
		double quote_askPrice    = in.getDouble();
		String quote_askExchange = Util.readString(in, 2).trim();
		char quote_indicator     = (char) in.get();
		char quote_tickIndicator = (char) in.get();

		return new OTQuote(quote_timestamp,
			quote_bidSize, quote_bidPrice, quote_askSize,
			quote_askPrice, quote_askExchange, quote_indicator,
			quote_tickIndicator, "", "", "");
	}

	private final static OTQuote readQuoteEx(ByteBuffer in) {
		OTQuote quote = readQuote(in);

		String bidExchange = Util.readString(in, 2).trim();
		String exchange    = Util.readString(in, 2).trim();
		quote.setBidExchange(bidExchange);
		quote.setExchange(exchange);

		return quote;
	}

	private final static OTQuote readQuoteExEx(ByteBuffer in) {
		OTQuote quote = readQuoteEx(in);

		String symbol = Util.readString(in, 15).trim();
		quote.setSymbol(symbol);

		return quote;
	}
                    
	/**
	 * Creates reader for the Trade event.
	 */
	public static final QuoteReader tradeReader(
			final IDataDelegate<OTTrade> dataDelegate) {
		return new QuoteReader(DataEnum.TRADE) {

			@Override
			public void read(Header h, ByteBuffer in) {
				OTTrade data = readTrade(in);
				dataDelegate.onData(data);
			}
			
		};
	}

	/**
	 * Creates reader for the Trade event.
	 */
	public static final QuoteReader tradeReaderEx(
			final IDataDelegate<OTTrade> dataDelegate) {
		return new QuoteReader(DataEnum.TRADE) {

			@Override
			public void read(Header h, ByteBuffer in) {
				OTTrade data = readTradeEx(in);
				dataDelegate.onData(data);
			}
			
		};
	}

	/**
	 * Creates reader for the Trade event.
	 */
	public static final QuoteReader tradeReaderExEx(
			final IDataDelegate<OTTrade> dataDelegate) {
		return new QuoteReader(DataEnum.TRADE) {

			@Override
			public void read(Header h, ByteBuffer in) {
				OTTrade data = readTradeExEx(in);
				dataDelegate.onData(data);
			}
			
		};
	}

	private final static OTTrade readTrade(ByteBuffer in) {
		Date trade_timestamp     = Util.readDate(in);
		double trade_price       = in.getDouble();
		int trade_size           = in.getInt();
		long trade_volume        = in.getLong();
		int trade_sequenceNumber = in.getInt();
		char trade_indicator     = (char) in.get();
		char trade_tickIndicator = (char) in.get();
		Set<TradePropertyEnum> trade_properties = Util.readTradePropertySet(in);
		
		return new OTTrade(trade_timestamp,
                    trade_size, trade_price, trade_volume,
                    trade_sequenceNumber, trade_indicator,
                    trade_tickIndicator, trade_properties, "", "");
	}

	private final static OTTrade readTradeEx(ByteBuffer in) {
		OTTrade trade = readTrade(in);

		String exchange = Util.readString(in, 2).trim();
		trade.setExchange(exchange);

		return trade;
	}

	private final static OTTrade readTradeExEx(ByteBuffer in) {
		OTTrade trade = readTradeEx(in);

		String trade_symbol = Util.readString(in, 15).trim();
		trade.setSymbol(trade_symbol);

		return trade;
	}

	/**
	 * Creates reader for the MMQuote event.
	 */
	public static final QuoteReader mmQuoteReader(
			final IDataDelegate<OTMMQuote> dataDelegate) {
		return new QuoteReader(DataEnum.MMQUOTE) {

			@Override
			public void read(Header h, ByteBuffer in) {
				OTMMQuote data = readMMQuote(in);
				dataDelegate.onData(data);
			}
			
		};
	}

	/**
	 * Creates reader for the MMQuote event.
	 */
	public static final QuoteReader mmQuoteReaderEx(
			final IDataDelegate<OTMMQuote> dataDelegate) {
		return new QuoteReader(DataEnum.MMQUOTE) {

			@Override
			public void read(Header h, ByteBuffer in) {
				OTMMQuote data = readMMQuoteEx(in);
				dataDelegate.onData(data);
			}
			
		};
	}

	/**
	 * Creates reader for the MMQuote event.
	 */
	public static final QuoteReader mmQuoteReaderExEx(
			final IDataDelegate<OTMMQuote> dataDelegate) {
		return new QuoteReader(DataEnum.MMQUOTE) {

			@Override
			public void read(Header h, ByteBuffer in) {
				OTMMQuote data = readMMQuoteExEx(in);
				dataDelegate.onData(data);
			}
			
		};
	}

	private final static OTMMQuote readMMQuote(ByteBuffer in) {
		Date mm_timestamp  = Util.readDate(in);
		int mm_bidSize     = in.getInt();
		int mm_askSize     = in.getInt();
		double mm_bidPrice = in.getDouble();
		double mm_askPrice = in.getDouble();
		String MMID        = Util.readString(in, 4).trim();
		char mm_indicator  = (char) in.get();


		return new OTMMQuote(mm_timestamp,
			mm_bidSize, mm_bidPrice, mm_askSize, mm_askPrice,
			MMID, mm_indicator, "", "");
	}

	private final static OTMMQuote readMMQuoteEx(ByteBuffer in) {
		OTMMQuote mmQuote = readMMQuote(in);			

		String exchange = Util.readString(in, 2).trim();
		mmQuote.setExchange(exchange);

                return mmQuote;
	}

	private final static OTMMQuote readMMQuoteExEx(ByteBuffer in) {
		OTMMQuote mmQuote = readMMQuoteEx(in);			

		String mm_symbol = Util.readString(in, 15).trim();
		mmQuote.setSymbol(mm_symbol);

		return mmQuote;
	}

	/**
	 * Creates reader for the BBO event.
	 */
	public static final QuoteReader bboReader(
			final IDataDelegate<OTBBO> dataDelegate) {
		return new QuoteReader(DataEnum.BBO) {

			@Override
			public void read(Header h, ByteBuffer in) {
				OTBBO data = readBBO(in);
				dataDelegate.onData(data);
			}
			
		};
	}

	/**
	 * Creates reader for the BBO event.
	 */
	public static final QuoteReader bboReaderEx(
			final IDataDelegate<OTBBO> dataDelegate) {
		return new QuoteReader(DataEnum.BBO) {

			@Override
			public void read(Header h, ByteBuffer in) {
				OTBBO data = readBBOEx(in);
				dataDelegate.onData(data);
			}
			
		};
	}

	/**
	 * Creates reader for the BBO event.
	 */
	public static final QuoteReader bboReaderExEx(
			final IDataDelegate<OTBBO> dataDelegate) {
		return new QuoteReader(DataEnum.BBO) {

			@Override
			public void read(Header h, ByteBuffer in) {
				OTBBO data = readBBOExEx(in);
				dataDelegate.onData(data);
			}
			
		};
	}

	private final static OTBBO readBBO(ByteBuffer in) {
		Date bboTimestamp = Util.readDate(in);
		double bboPrice   = in.getDouble();
		int bboSize       = in.getInt();
		char sideCode = (char) in.get();
		TradeSideEnum side = TradeSideEnum.SELLER;
		if(sideCode == 'B') {
			side = TradeSideEnum.BUYER;
		} else if(sideCode == 'A') {
			side = TradeSideEnum.SELLER;
		} else {
			throw new ProtocolException("unexpected side code: " + sideCode, in);
		}

		return new OTBBO(bboTimestamp, bboPrice,
                                bboSize, side, "", "");
	}

	private final static OTBBO readBBOEx(ByteBuffer in) {
		OTBBO bbo = readBBO(in);			

		String exchange = Util.readString(in, 2).trim();
		bbo.setExchange(exchange);

                return bbo;
	}

	private final static OTBBO readBBOExEx(ByteBuffer in) {
		OTBBO bbo = readBBOEx(in);			

		String bbo_symbol = Util.readString(in, 15).trim();
		bbo.setSymbol(bbo_symbol);

		return bbo;
	}
}
