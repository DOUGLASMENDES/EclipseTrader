package net.sourceforge.eclipsetrader.directaworld;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.directaworld.messages"; //$NON-NLS-1$
    public static String GeneralPreferencesPage_Password;
    public static String GeneralPreferencesPage_UserName;
    public static String GeneralPreferencesPage_WarningMessage;
    public static String LoginDialog_Caption;
    public static String LoginDialog_Description;
    public static String LoginDialog_Password;
    public static String LoginDialog_SavePassword;
    public static String LoginDialog_Title;
    public static String LoginDialog_UserName;
    public static String SecurityPage_CodeColumn;
    public static String SecurityPage_Description;
    public static String SecurityPage_DescriptionColumn;
    public static String SecurityPage_Search;
    public static String SecurityPage_Title;
    public static String SecurityWizard_IntradayChartsDescription;
    public static String SecurityWizard_IntradayChartsTitle;
    public static String SecurityWizard_Title;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
