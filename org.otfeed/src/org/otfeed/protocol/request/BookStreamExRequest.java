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
import org.otfeed.event.OTBookCancel;
import org.otfeed.event.OTBookChange;
import org.otfeed.event.OTBookDelete;
import org.otfeed.event.OTBookExecute;
import org.otfeed.event.OTBookOrder;
import org.otfeed.event.OTBookPriceLevel;
import org.otfeed.event.OTBookPurge;
import org.otfeed.event.OTBookReplace;
import org.otfeed.protocol.CommandEnum;
import org.otfeed.protocol.DataEnum;
import org.otfeed.protocol.ProtocolException;

import org.otfeed.protocol.request.book.BookReader;

import java.nio.ByteBuffer;

import java.util.Map;
import java.util.HashMap;

/**
 * Request to receive real time book event stream.
 */
public final class BookStreamExRequest extends AbstractSessionRequest {

	private final String exchangeCode;
	private final String symbolCode;
	private final int mask;

	private final Map<Integer,BookReader> map = new HashMap<Integer,BookReader>();

	public String getExchangeCode()  { return exchangeCode; }
	public String getSymbolCode()    { return symbolCode; }

	public BookStreamExRequest(int requestId, 
			String exchangeCode,
			String symbolCode,
			IDataDelegate<OTBookOrder>   orderDelegate,
			IDataDelegate<OTBookChange>  changeDelegate,
			IDataDelegate<OTBookReplace> replaceDelegate,
			IDataDelegate<OTBookCancel>  cancelDelegate,
			IDataDelegate<OTBookPurge>   purgeDelegate,
			IDataDelegate<OTBookExecute> executeDelegate,
			IDataDelegate<OTBookDelete>  deleteDelegate,
			IDataDelegate<OTBookPriceLevel> priceLevelDelegate,
			ICompletionDelegate completionDelegate) {

		super(CommandEnum.REQUEST_BOOK_STREAM_EX, 
				requestId,
				completionDelegate);

		Check.notNull(exchangeCode,  "exchangeCode");
		Check.notNull(symbolCode,  "symbolCode");

		this.exchangeCode  = exchangeCode;
		this.symbolCode    = symbolCode;

		int mask = 0;
		if(orderDelegate != null) {
			BookReader rdr = BookReader.orderReader(orderDelegate);
			map.put(rdr.type.code, rdr);
			mask |= rdr.mask;
		}
		if(changeDelegate != null) {
			BookReader rdr = BookReader.changeReader(changeDelegate);
			map.put(rdr.type.code, rdr);
			mask |= rdr.mask;
		}
		if(replaceDelegate != null) {
			BookReader rdr = BookReader.replaceReader(replaceDelegate);
			map.put(rdr.type.code, rdr);
			mask |= rdr.mask;
		}
		if(cancelDelegate != null) {
			BookReader rdr = BookReader.cancelReader(cancelDelegate);
			map.put(rdr.type.code, rdr);
			mask |= rdr.mask;
		}
		if(purgeDelegate != null) {
			BookReader rdr = BookReader.purgeReader(purgeDelegate);
			map.put(rdr.type.code, rdr);
			mask |= rdr.mask;
		}
		if(executeDelegate != null) {
			BookReader rdr = BookReader.executeReader(executeDelegate);
			map.put(rdr.type.code, rdr);
			mask |= rdr.mask;
		}
		if(deleteDelegate != null) {
			BookReader rdr = BookReader.deleteReader(deleteDelegate);
			map.put(rdr.type.code, rdr);
			mask |= rdr.mask;
		}
		if(priceLevelDelegate != null) {
			BookReader rdr = BookReader.priceLevelReader(priceLevelDelegate);
			map.put(rdr.type.code, rdr);
			mask |= rdr.mask;
		}
		
		if(mask == 0) {
			throw new IllegalArgumentException("you must set one of the book event delegates");
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
		if(header.getCommand() != CommandEnum.REQUEST_BOOK_STREAM) {
			throw new ProtocolException("unexpected command: "
				+ header.getCommand(), in);
		}

		int typeCode = in.get();
		if(typeCode == DataEnum.EOD.code) {
			return JobStatus.FINISHED;
		}

		BookReader reader = map.get(typeCode);
		if(reader == null) {
			throw new ProtocolException("unrecognized type: " + typeCode, in);
		}

		reader.read(header, in);

		return JobStatus.ACTIVE;
	}

	@Override
	public final CommandEnum getCancelCommand() {
		return CommandEnum.CANCEL_BOOK_STREAM;
	}
}
