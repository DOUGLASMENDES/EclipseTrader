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

import org.eclipsetrader.core.charts.IDataSeries;

/**
 * This interface is implemented by chart object factories.
 *
 * @since 1.0
 */
public interface IChartObjectFactory {

    public String getId();

    public String getName();

    public IChartObject createObject(IDataSeries source);

    public void setParameters(IChartParameters parameters);

    public IChartParameters getParameters();
}
