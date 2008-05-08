package org.otfeed.command;

/**
 * Enumerates styles of volume reporting.
 */
public enum VolumeStyleEnum {
	/**
	 * Indivilual volume reporting.
	 * Volume is reported only from this exchange.
	 */
	INDIVIDUAL, 
	
	/**
	 * Compound volume reporting.
	 * Volume is compound across all exchanges that trade this symbol.
	 */
	COMPOUND;

	/**
	 * Protocol flag used to indicate INDIVIDUAL volume.
	 */
	public static final int INDIVIDUAL_VOLUME_FLAG = 0x1000000;

}
