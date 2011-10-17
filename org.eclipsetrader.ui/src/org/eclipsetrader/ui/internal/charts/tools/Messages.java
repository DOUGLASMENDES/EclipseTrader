package org.eclipsetrader.ui.internal.charts.tools;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipsetrader.ui.internal.charts.tools.messages"; //$NON-NLS-1$
    public static String FanlineToolFactory_Tooltip;
    public static String FiboarcToolFactory_Tooltip;
    public static String LinePropertiesPage_ColorLabel;
    public static String LinePropertiesPage_ExtendLabel;
    public static String LinePropertiesPage_FirstPointLabel;
    public static String LinePropertiesPage_LabelLabel;
    public static String LinePropertiesPage_SecondPointLabel;
    public static String LinePropertiesPage_Title;
    public static String LineToolFactory_Tooltip;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
