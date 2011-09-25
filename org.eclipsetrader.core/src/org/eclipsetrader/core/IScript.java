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

package org.eclipsetrader.core;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Interface implemented by script objects.
 *
 * @since 1.0
 */
public interface IScript extends IAdaptable {

    public static final String NAME = "name";
    public static final String LANGUAGE = "language";
    public static final String TEXT = "text";

    public String getName();

    public String getLanguage();

    public String getText();
}
