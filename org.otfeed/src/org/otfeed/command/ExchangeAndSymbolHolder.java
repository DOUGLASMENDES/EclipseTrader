package org.otfeed.command;

/**
 * Common superclass for all commands that bear
 * <code>exchangeCode</code> and <code>symbolCode</code>
 * properties.
 */
abstract class ExchangeAndSymbolHolder extends CompletionDelegateHolder {

	private String exchangeCode;
	
	/**
	 * Exchange code.
	 * 
	 * @return exchange code.
	 */
	public final String getExchangeCode() {
		return exchangeCode;
	}

	/**
	 * Sets the exchange code.
	 * 
	 * @param exchangeCode exchange code.
	 */
	public final void setExchangeCode(String exchangeCode) {
		this.exchangeCode = exchangeCode;
	}

	private String symbolCode;

	/**
	 * Symbol code.
	 * 
	 * @return symbol code.
	 */
	public final String getSymbolCode() { 
		return symbolCode; 
	}

	/**
	 * Sets symbol code.
	 * 
	 * @param val symbol code.
	 */
	public final void setSymbolCode(String val) {
		symbolCode = val;
	}
}
