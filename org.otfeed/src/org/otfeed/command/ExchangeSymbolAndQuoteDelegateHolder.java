package org.otfeed.command;

import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTBBO;
import org.otfeed.event.OTMMQuote;
import org.otfeed.event.OTQuote;
import org.otfeed.event.OTTrade;

/**
 * Common superclass for commands that bear
 * <code>types</code> and <code>volumeStyle</code>
 * properties.
 */
abstract class ExchangeSymbolAndQuoteDelegateHolder 
		extends ExchangeAndSymbolHolder {
	
	/**
	 * Sets volume reporting style.
	 * 
	 * @param val volume style.
	 */
	public final void setVolumeStyle(VolumeStyleEnum val) {
		volumeStyle = val;
	}

	private IDataDelegate<OTQuote> quoteDelegate;
	public IDataDelegate<OTQuote> getQuoteDelegate() {
		return quoteDelegate;
	}
	public void setQuoteDelegate(IDataDelegate<OTQuote> val) {
		quoteDelegate = val;
	}

	private IDataDelegate<OTTrade> tradeDelegate;
	public IDataDelegate<OTTrade> getTradeDelegate() {
		return tradeDelegate;
	}
	public void setTradeDelegate(IDataDelegate<OTTrade> val) {
		tradeDelegate = val;
	}

	private IDataDelegate<OTMMQuote> mmQuoteDelegate;
	public IDataDelegate<OTMMQuote> getMmQuoteDelegate() {
		return mmQuoteDelegate;
	}
	public void setMmQuoteDelegate(IDataDelegate<OTMMQuote> val) {
		mmQuoteDelegate = val;
	}

	private IDataDelegate<OTBBO> bboDelegate;
	public IDataDelegate<OTBBO> getBboDelegate() {
		return bboDelegate;
	}
	public void setBboDelegate(IDataDelegate<OTBBO> val) {
		bboDelegate = val;
	}
	
	private VolumeStyleEnum volumeStyle;
	
	/**
	 * Specifies whether quote events will contain
	 * individual volume, or compound volume.
	 * By default, <code>COMPOUND</code> volume is requested.
	 * 
	 * @return volume style.
	 */
	public final VolumeStyleEnum getVolumeStyle() {
		return volumeStyle;
	}
}
