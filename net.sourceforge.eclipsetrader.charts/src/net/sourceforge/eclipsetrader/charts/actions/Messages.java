package net.sourceforge.eclipsetrader.charts.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.charts.actions.messages"; //$NON-NLS-1$

	public static String AutoScaleAction_Text;

	public static String DeleteChartAction_Text;

	public static String DeleteChartAction_Title;

	public static String DeleteObjectAction_Text;

	public static String DeleteObjectAction_Title;

	public static String OpenChartAction_10Min;

	public static String OpenChartAction_15Min;

	public static String OpenChartAction_1Hr;

	public static String OpenChartAction_1Min;

	public static String OpenChartAction_2Min;

	public static String OpenChartAction_30Min;

	public static String OpenChartAction_5Min;

	public static String OpenChartAction_Daily;

	public static String OpenChartAction_Middle;

	public static String OpenChartAction_Monthly;

	public static String OpenChartAction_New;

	public static String OpenChartAction_Weekly;

	public static String UpdateAllChartsAction_UpdateChartData;

	public static String UpdateAllChartsAction_Updating;

	public static String UpdateAllChartsAction_UpdatingCharts;

	public static String UpdateChartAction_UpdateChartData;

	public static String UpdateChartAction_Updating;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
