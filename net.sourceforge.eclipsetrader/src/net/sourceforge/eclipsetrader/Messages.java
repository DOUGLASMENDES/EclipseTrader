package net.sourceforge.eclipsetrader;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.messages"; //$NON-NLS-1$

	public static String ApplicationActionBarAdvisor_Edit;

	public static String ApplicationActionBarAdvisor_File;

	public static String ApplicationActionBarAdvisor_Help0;

	public static String ApplicationActionBarAdvisor_New;

	public static String ApplicationActionBarAdvisor_OpenPerspective;

	public static String ApplicationActionBarAdvisor_ShowView;

	public static String ApplicationActionBarAdvisor_Window;

	public static String ApplicationWorkbenchWindowAdvisor_AlwaysExitWithoutPrompt;

	public static String ApplicationWorkbenchWindowAdvisor_ConfirmExit;

	public static String ApplicationWorkbenchWindowAdvisor_ExitEclipseTrader;

	public static String ApplicationWorkbenchWindowAdvisor_RestoreWindow;

	public static String ApplicationWorkbenchWindowAdvisor_Separator;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
