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

package org.eclipsetrader.ui.charts;

import java.util.Date;

import org.eclipse.swt.graphics.RGB;

/**
 * Interface to access the attributes associated to an indicator.
 *
 * @since 1.0
 */
public interface IChartParameters {

    public String[] getParameterNames();

    public boolean hasParameter(String name);

    public String getString(String name);

    public Integer getInteger(String name);

    public Double getDouble(String name);

    public Date getDate(String name);

    public RGB getColor(String name);

    public void setParameter(String name, String value);

    public void setParameter(String name, Number value);

    public void setParameter(String name, Date value);

    public void setParameter(String name, RGB value);
}
