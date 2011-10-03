/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.ats;

import org.eclipsetrader.core.IScript;

/**
 * Interface implemented by trading system strategies scripts.
 *
 * @since 1.0
 */
public interface IScriptStrategy extends IStrategy {

    public static final String PROP_LANGUAGE = "language";
    public static final String PROP_TEXT = "text";
    public static final String PROP_INCLUDES = "includes";

    /**
     * Gets the script language.
     * 
     * @return the language
     */
    public String getLanguage();

    /**
     * Gets the script text.
     * 
     * @return the text
     */
    public String getText();

    /**
     * Gets a possibly empty array of included scripts.
     * 
     * @return the included scripts
     */
    public IScript[] getIncludes();
}
