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

package org.otfeed.command;

import java.util.Date;

import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTOHLC;
import org.otfeed.protocol.ICommand;

/**
 * Request for the historical aggregated {@link OTOHLC OHLC} (open-high-low-close) data.
 * <p/>
 * The aggregation span varies from 2 ticks to years. Default
 * aggregation span is 1 day.
 * <p/>
 * Generates {@link OTOHLC ohlc} events.
 * <p/>
 * For the request to receive raw ticks data (not aggregated),
 * see {@link HistTicksCommand}.
 */
public final class HistDataCommand 
		extends ExchangeSymbolAndDatesHolder implements ICommand {

	/**
	 * Creates new historical data command, initializing all 
	 * its properties.
	 * 
	 * @param exchangeCode echange code.
	 * @param symbolCode symbol code.
	 * @param startDate start date.
	 * @param endDate end date.
	 * @param aggregationSpan aggregation time span.
	 * @param dataDelegate delegate.
	 */
	public HistDataCommand(String exchangeCode,
			String symbolCode,
			Date startDate,
			Date endDate,
			AggregationSpan aggregationSpan,
			IDataDelegate<OTOHLC> dataDelegate) {
		setExchangeCode(exchangeCode);
		setSymbolCode(symbolCode);
		setStartDate(startDate);
		setEndDate(endDate);
		setAggregationSpan(aggregationSpan);
		setDataDelegate(dataDelegate);
	}
	
	/**
	 * Default constructor. 
	 * Initializes {@link #getAggregationSpan aggregationSpan}
	 * to its default value of 1 day.
	 * All other properties must be set explicitly before
	 * using this command object.
	 */
	public HistDataCommand() {
		this(null, null, null, null, 
				AggregationSpan.days(1), null);
	}

	/**
	 * Creates new historical data command with default
	 * aggregation interval of one day.
	 * 
	 * {@link #getAggregationSpan Aggreagation span}
	 * is set to one day.
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCode symbol code.
	 * @param startDate start date.
	 * @param endDate end date.
	 * @param dataDelegate data delegate.
	 */
	public HistDataCommand(String exchangeCode,
			String symbolCode, 
			Date startDate,
			Date endDate,
			IDataDelegate<OTOHLC> dataDelegate) {
		this(exchangeCode, symbolCode, 
				startDate, endDate,
				AggregationSpan.days(1), 
				dataDelegate);
	}
	
	private AggregationSpan aggregationSpan;
	
	/**
	 * Determines the length of the aggregation interval, e.g.
	 * {@link AggregationSpan#hours}.
	 * 
	 * @return aggregation span.
	 */
	public AggregationSpan getAggregationSpan() {
		return aggregationSpan;
	}
	
	/**
	 * Sets aggregation span.
	 * 
	 * @param val
	 */
	public void setAggregationSpan(AggregationSpan val) {
		aggregationSpan = val;
	}

	private IDataDelegate<OTOHLC> dataDelegate;

	/**
	 * Delegate to receive {@link OTOHLC} events.
	 * This parameter is mandatory.
	 * 
	 * @return delegate.
	 */
	public IDataDelegate<OTOHLC> getDataDelegate() {
		return dataDelegate;
	}

	/**
	 * Sets delegate.
	 * 
	 * @param dataDelegate delegate.
	 */
	public void setDataDelegate(IDataDelegate<OTOHLC> dataDelegate) {
		this.dataDelegate = dataDelegate;
	}
}
