/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.yahoo.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.yahoo.wizards.messages"; //$NON-NLS-1$

	public static String FrenchSecurityPage_CodeColumnName;

	public static String FrenchSecurityPage_Description;

	public static String FrenchSecurityPage_DescriptionColumnName;

	public static String FrenchSecurityPage_Search;

	public static String FrenchSecurityPage_Title;

	public static String GermanSecurityPage_CodeColumnName;

	public static String GermanSecurityPage_Description;

	public static String GermanSecurityPage_DescriptionColumnName;

	public static String GermanSecurityPage_Search;

	public static String GermanSecurityPage_Title;

	public static String IndicesPage_Description;

	public static String IndicesPage_Search;

	public static String IndicesPage_Title;

	public static String SecurityPage_CodeColumnName;

	public static String SecurityPage_Description;

	public static String SecurityPage_DescriptionColumnName;

	public static String SecurityPage_Search;

	public static String SecurityPage_Title;

	public static String SecurityWizard_FranceWizardTitle;

	public static String SecurityWizard_GermanWizardTitle;

	public static String SecurityWizard_IndexWizardTitle;

	public static String SecurityWizard_IntradayChartsDescription;

	public static String SecurityWizard_IntradayChartsTitle;

	public static String SecurityWizard_WizardTitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
