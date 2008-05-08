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

package org.otfeed.protocol.request.book;

import org.otfeed.command.BookDeleteTypeEnum;
import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTBookCancel;
import org.otfeed.event.OTBookChange;
import org.otfeed.event.OTBookDelete;
import org.otfeed.event.OTBookExecute;
import org.otfeed.event.OTBookOrder;
import org.otfeed.event.OTBookPriceLevel;
import org.otfeed.event.OTBookPurge;
import org.otfeed.event.OTBookReplace;
import org.otfeed.event.TradeSideEnum;

import org.otfeed.protocol.DataEnum;
import org.otfeed.protocol.request.Header;
import org.otfeed.protocol.request.Util;

import java.util.Date;
import java.nio.ByteBuffer;

/**
 * Class that knows how to de-serialise miscellaneous Book events.
 */
public abstract class BookReader {

	private BookReader(DataEnum t) {
		type = t;
		this.mask = 1 << (t.code - 1);
	}

	public final DataEnum type;
	public final int mask;

	/**
	 * Reads response data frame, parses it and emits events.
	 *
	 * @param header   response header.
	 * @param in       data frame.
	 */
	public abstract void read(Header header, ByteBuffer in);

	/**
	 * Creates reader for the BookOrder event.
	 */
	public static final BookReader orderReader(
			final IDataDelegate<OTBookOrder> dataDelegate) {
		
		return new BookReader(DataEnum.BOOK_ORDER) {
			@Override
			public void read(Header header, ByteBuffer in) {
				OTBookOrder data = readOrder(in);
				dataDelegate.onData(data);
			}
		};
	}
	
	private final static OTBookOrder readOrder(ByteBuffer in) {
		Date timestamp   = Util.readDate(in);
		String reference = Util.readString(in, 21).trim();
		double price     = in.getDouble();
		int size         = in.getInt();
		TradeSideEnum side = Util.readTradeSideEnum(in);
		boolean display  = Util.readBoolean(in);
		
		return new OTBookOrder(timestamp,
			reference, price, size, side, display);
	}

	/**
	 * Creates reader for the BookExecute event.
	 */
	public static final BookReader executeReader(
			final IDataDelegate<OTBookExecute> dataDelegate) {
		
		return new BookReader(DataEnum.BOOK_EXECUTE) {
			@Override
			public void read(Header header, ByteBuffer in) {
				OTBookExecute data = readExecute(in);
				dataDelegate.onData(data);
			}
		};
	}
	
	private final static OTBookExecute readExecute(ByteBuffer in) {
		Date timestamp   = Util.readDate(in);
		String reference = Util.readString(in, 21).trim();
		int size         = in.getInt();
		int matchNumber  = in.getInt();

		return new OTBookExecute(timestamp,
			reference, size, matchNumber);
	}

	/**
	 * Creates reader for the BookDelete event.
	 */
	public static final BookReader deleteReader(
			final IDataDelegate<OTBookDelete> dataDelegate) {
		
		return new BookReader(DataEnum.BOOK_DELETE) {
			@Override
			public void read(Header header, ByteBuffer in) {
				OTBookDelete data = readDelete(in);
				dataDelegate.onData(data);
			}
		};
	}
	
	private final static OTBookDelete readDelete(ByteBuffer in) {
		Date timestamp   = Util.readDate(in);
		String reference = Util.readString(in,  21).trim();
		BookDeleteTypeEnum deleteType = Util.readBookDeleteTypeEnum(in);
		TradeSideEnum side = Util.readTradeSideEnum(in);
		
		return new OTBookDelete(timestamp,
			reference, deleteType, side);
	}

	/**
	 * Creates reader for the BookCancel event.
	 */
	public static final BookReader cancelReader(
			final IDataDelegate<OTBookCancel> dataDelegate) {
		
		return new BookReader(DataEnum.BOOK_CANCEL) {
			@Override
			public void read(Header header, ByteBuffer in) {
				OTBookCancel data = readCancel(in);
				dataDelegate.onData(data);
			}
		};
	}
	
	private final static OTBookCancel readCancel(ByteBuffer in) {
		Date timestamp   = Util.readDate(in);
		String reference = Util.readString(in, 21).trim();
		int size         = in.getInt();

		return new OTBookCancel(timestamp, reference, size);
	}

	/**
	 * Creates reader for the BookChange event.
	 */
	public static final BookReader changeReader(
			final IDataDelegate<OTBookChange> dataDelegate) {
		
		return new BookReader(DataEnum.BOOK_CHANGE) {
			@Override
			public void read(Header header, ByteBuffer in) {
				OTBookChange data = readChange(in);
				dataDelegate.onData(data);
			}
		};
	}
	
	private final static OTBookChange readChange(ByteBuffer in) {
		Date timestamp   = Util.readDate(in);
		String reference = Util.readString(in, 21).trim();
		double price     = in.getDouble();
		int size         = in.getInt();

		return new OTBookChange(timestamp, reference, price, size);
	}

	/**
	 * Creates reader for the BookChange event.
	 */
	public static final BookReader replaceReader(
			final IDataDelegate<OTBookReplace> dataDelegate) {
		
		return new BookReader(DataEnum.BOOK_REPLACE) {
			@Override
			public void read(Header header, ByteBuffer in) {
				OTBookReplace data = readReplace(in);
				dataDelegate.onData(data);
			}
		};
	}
	
	private final static OTBookReplace readReplace(ByteBuffer in) {
		Date timestamp     = Util.readDate(in);
		String reference   = Util.readString(in, 21).trim();
		double price       = in.getDouble();
		int size           = in.getInt();
		TradeSideEnum side = Util.readTradeSideEnum(in);

		return new OTBookReplace(timestamp, reference, price, size, side);
	}

	/**
	 * Creates reader for the BookPurge event.
	 */
	public static final BookReader purgeReader(
			final IDataDelegate<OTBookPurge> dataDelegate) {
		
		return new BookReader(DataEnum.BOOK_PURGE) {
			@Override
			public void read(Header header, ByteBuffer in) {
				OTBookPurge data = readPurge(in);
				dataDelegate.onData(data);
			}
		};
	}
	
	private final static OTBookPurge readPurge(ByteBuffer in) {
		Date timestamp   = Util.readDate(in);
		String nameRoot  = Util.readString(in, 3).trim();

		return new OTBookPurge(timestamp, nameRoot);
	}

	/**
	 * Creates reader for the BookPriceLevel event.
	 */
	public static final BookReader priceLevelReader(
			final IDataDelegate<OTBookPriceLevel> dataDelegate) {
		
		return new BookReader(DataEnum.BOOK_PRICE_LEVEL) {
			@Override
			public void read(Header header, ByteBuffer in) {
				OTBookPriceLevel data = readPriceLevel(in);
				dataDelegate.onData(data);
			}
		};
	}

	private final static OTBookPriceLevel readPriceLevel(ByteBuffer in) {
		Date timestamp     = Util.readDate(in);
		double price       = in.getDouble();
		int size           = in.getInt();
		TradeSideEnum side = Util.readTradeSideEnum(in);
		String levelID     = Util.readString(in, 4).trim();

		return new OTBookPriceLevel(timestamp,
					price, size, side, levelID);
	}
}


