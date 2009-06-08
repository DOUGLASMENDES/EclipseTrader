package org.eclipsetrader.ui.charts;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipsetrader.ui.charts.messages"; //$NON-NLS-1$
	public static String BaseChartViewer_ExceptionErrorMessage;
	public static String ChartCanvas_RenderingChartError;
	public static String ChartCanvas_VerticalScaleRenderingError;
	public static String ChartView_NotificationErrorMessage;
	public static String ChartViewer_HorizontalScaleRenderingError;
	public static String ChartViewer_RenderingErrorMessage;
	public static String DateScaleCanvas_HorizontalScaleRenderingError;
	public static String RenderStyle_DashText;
	public static String RenderStyle_DotText;
	public static String RenderStyle_HistogramBarsText;
	public static String RenderStyle_HistogramText;
	public static String RenderStyle_Invisible;
	public static String RenderStyle_LineText;
	public static String SummaryNumberItem_Label;
	public static String ToolsContributionItem_Action;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
