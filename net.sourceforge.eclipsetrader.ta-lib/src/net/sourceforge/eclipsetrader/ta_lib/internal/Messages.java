package net.sourceforge.eclipsetrader.ta_lib.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.ta_lib.internal.messages"; //$NON-NLS-1$

	public static String TALibIndicatorPreferencePage_DoubleExponential;

	public static String TALibIndicatorPreferencePage_Exponential;

	public static String TALibIndicatorPreferencePage_KaufmanAdaptive;

	public static String TALibIndicatorPreferencePage_MESAAdaptive;

	public static String TALibIndicatorPreferencePage_Simple;

	public static String TALibIndicatorPreferencePage_Triangular;

	public static String TALibIndicatorPreferencePage_TripleExponential;

	public static String TALibIndicatorPreferencePage_TripleExponentialT3;

	public static String TALibIndicatorPreferencePage_Weighted;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
