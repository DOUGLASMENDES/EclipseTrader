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

import org.otfeed.command.ListSymbolEnum;
import org.otfeed.command.ListSymbolsCommand.MatchStyleEnum;
import org.otfeed.event.ICompletionDelegate;
import org.otfeed.event.IDataDelegate;
import org.otfeed.event.InstrumentEnum;
import org.otfeed.event.OTSymbol;
import org.otfeed.protocol.CommandEnum;
import org.otfeed.protocol.ProtocolException;

import java.nio.ByteBuffer;

import java.util.Set;

/**
 * Request to receive list of symbols.
 * If exchange name is "@", and symbol name is non-blank, then
 * symbol is looked up on all exchanges. If symbol name is "" (empty string)
 * all symbols are returned.
 */
public class ListSymbolsExRequest extends AbstractSessionRequest {

	private final IDataDelegate<OTSymbol> dataDelegate;

	private final String exchangeCode;
	private final String symbolCodePattern;
	private final Set<ListSymbolEnum> types;
	private final MatchStyleEnum matchStyle;

	public String getExchangeCode() {
		return exchangeCode;
	}

	public String getSymbolCodePattern() {
		return symbolCodePattern;
	}

	public Set<ListSymbolEnum> getTypes() {
		return types;
	}

	public ListSymbolsExRequest(int requestId, 
			String exchangeCode,
			String symbolCodePattern,
			Set<ListSymbolEnum> types,
			MatchStyleEnum matchStyle,
			IDataDelegate<OTSymbol> dataDelegate,
			ICompletionDelegate completionDelegate) {
		super(CommandEnum.REQUEST_LIST_SYMBOLS_EX, 
				requestId, completionDelegate);

		Check.notNull(exchangeCode,  "exchangeCode");
		Check.notNull(symbolCodePattern,  "symbolCodePattern");
		Check.notNull(matchStyle, "matchStyle");
		Check.notNull(types, "types");
		Check.notNull(dataDelegate,  "dataDelegate");

		this.exchangeCode      = exchangeCode;
		this.symbolCodePattern = symbolCodePattern;
		this.types             = types;
		this.matchStyle        = matchStyle;

		this.dataDelegate      = dataDelegate;
	}

	@Override
	public void writeRequest(ByteBuffer out) {
		super.writeRequest(out);
		
		Util.writeString(out, exchangeCode, 15);
		Util.writeString(out, symbolCodePattern, 15);
		Util.writeListSymbolMask(out, types, matchStyle);
	}

	@Override
	public JobStatus handleMessage(Header header, ByteBuffer in) {

		if(header.getRequestId() != getRequestId()) {
			throw new ProtocolException("wrong request id",  in);
		}

		if(header.getCommand() != CommandEnum.REQUEST_LIST_SYMBOLS) {
			throw new ProtocolException("wrong command: "
				+ header.getCommand(), in);
		}

		int symbolsNum = in.getShort();

		if (symbolsNum > 0) {
			for (int i = 0; i < symbolsNum; i++) {
				String currencyID         = Util.readString(in, 4);
				String symbolCodePattern  = Util.readString(in, 15);
				InstrumentEnum symbolType = Util.readInstrumentEnum(in);
				String company            = Util.readString(in);

				OTSymbol symbol = new OTSymbol(
					symbolCodePattern,
					company, 
					currencyID, 
					symbolType);

				dataDelegate.onData(symbol);
			}

			return JobStatus.ACTIVE;
		} 

		return JobStatus.FINISHED;
	}
}
