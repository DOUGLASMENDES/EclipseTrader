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

package net.sourceforge.eclipsetrader.directaworld.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.directaworld.wizards.messages"; //$NON-NLS-1$
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
