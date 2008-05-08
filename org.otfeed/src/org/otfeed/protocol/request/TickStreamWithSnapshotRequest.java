package org.otfeed.protocol.request;

import org.otfeed.IConnection;
import org.otfeed.IRequest;
import org.otfeed.command.SnapshotCommand;
import org.otfeed.command.TickStreamCommand;
import org.otfeed.command.VolumeStyleEnum;
import org.otfeed.event.ICompletionDelegate;
import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTBBO;
import org.otfeed.event.OTMMQuote;
import org.otfeed.event.OTQuote;
import org.otfeed.event.OTTrade;

/**
 * Synthetic request: issues {@link SnapshotRequest}, followed by {@link TickStreamExRequest}.
 */
public class TickStreamWithSnapshotRequest extends AbstractStreamWithSnapshotRequest implements IRequest {
	
	private final SnapshotCommand snapshotCommand;
	private final TickStreamCommand streamCommand;
	
	public TickStreamWithSnapshotRequest(IConnection connection,
			String exchangeCode,
			String symbolCode,
			VolumeStyleEnum volumeStyle,
			IDataDelegate<OTQuote> quoteDelegate,
			IDataDelegate<OTTrade> tradeDelegate,
			IDataDelegate<OTMMQuote> mmQuoteDelegate,
			IDataDelegate<OTBBO> bboDelegate,
			ICompletionDelegate completionDelegate
			) {
		super(connection, completionDelegate);

		snapshotCommand = new SnapshotCommand(exchangeCode, symbolCode, volumeStyle);
		snapshotCommand.setQuoteDelegate(quoteDelegate);
		snapshotCommand.setTradeDelegate(tradeDelegate);
		snapshotCommand.setMmQuoteDelegate(mmQuoteDelegate);
		snapshotCommand.setBboDelegate(bboDelegate);

		streamCommand = new TickStreamCommand(exchangeCode, symbolCode, volumeStyle);
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
		streamCommand.setCompletionDelegate(d);
		return connection.prepareRequest(streamCommand);
	}
}
