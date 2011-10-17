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

package org.eclipsetrader.core.charts.repository;

import org.eclipsetrader.core.instruments.ISecurity;

/**
 * Security to display overlayed to the main data source.
 *
 * @since 1.0
 */
public interface ISecuritySection {

    public ISecurity getSecurity();

    public IElementSection[] getIndicators();

    public void setIndicators(IElementSection[] indicators);

    public void accept(IChartVisitor visitor);
}
