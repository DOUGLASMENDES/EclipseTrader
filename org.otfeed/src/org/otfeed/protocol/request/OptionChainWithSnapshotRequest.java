package org.otfeed.protocol.request;

import org.otfeed.IConnection;
import org.otfeed.IRequest;
import org.otfeed.command.MonthAndYear;
import org.otfeed.command.OptionChainCommand;
import org.otfeed.command.OptionChainSnapshotCommand;
import org.otfeed.command.PriceRange;
import org.otfeed.command.VolumeStyleEnum;
import org.otfeed.event.ICompletionDelegate;
import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTBBO;
import org.otfeed.event.OTMMQuote;
import org.otfeed.event.OTQuote;
import org.otfeed.event.OTTrade;

/**
 * Synthetic request: issues {@link OptionChainSnapshotRequest}, followed by {@link OptionChainRequest}.
 */
public class OptionChainWithSnapshotRequest extends AbstractStreamWithSnapshotRequest implements IRequest {
	
	private final OptionChainSnapshotCommand snapshotCommand;
	private final OptionChainCommand streamCommand;
	
	public OptionChainWithSnapshotRequest(IConnection connection,
			String exchangeCode,
			String symbolCode,
			MonthAndYear monthAndYear,
			PriceRange priceRange,
			VolumeStyleEnum volumeStyle,
			IDataDelegate<OTQuote> quoteDelegate,
			IDataDelegate<OTTrade> tradeDelegate,
			IDataDelegate<OTMMQuote> mmQuoteDelegate,
			IDataDelegate<OTBBO> bboDelegate,
			ICompletionDelegate completionDelegate
			) {
		super(connection, completionDelegate);
		
		snapshotCommand = new OptionChainSnapshotCommand(exchangeCode, symbolCode, monthAndYear, priceRange, volumeStyle);
		snapshotCommand.setQuoteDelegate(quoteDelegate);
		snapshotCommand.setTradeDelegate(tradeDelegate);
		snapshotCommand.setMmQuoteDelegate(mmQuoteDelegate);
		snapshotCommand.setBboDelegate(bboDelegate);

		streamCommand = new OptionChainCommand(exchangeCode, symbolCode, monthAndYear, priceRange, volumeStyle);
		streamCommand.setQuoteDelegate(quoteDelegate);
		streamCommand.setTradeDelegate(tradeDelegate);
		streamCommand.setMmQuoteDelegate(mmQuoteDelegate);
		streamCommand.setBboDelegate(bboDelegate);
	}

	@Override
	IRequest prepareSnapshotRequest(IConnection connection, ICompletionDelegate d) {
		snapshotCommand.setCompletionDelegate(d);
		return connection.prepareRequest(snapshotCommand);
	}

	@Override
	IRequest prepareStreamRequest(IConnection connection, ICompletionDelegate d) {
		snapshotCommand.setCompletionDelegate(d);
		return connection.prepareRequest(streamCommand);
	}
}
