package net.sourceforge.eclipsetrader.core.transfers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.core.transfers.messages"; //$NON-NLS-1$
	public static String SecurityTransfer_Exception;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
