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

/**
 * Root of the chart template document.
 *
 * @since 1.0
 */
public interface IChartTemplate {

    public String getName();

    public void setName(String name);

    public IChartSection[] getSections();

    public void setSections(IChartSection[] sections);

    public IChartSection getSectionWithName(String name);

    public IChartSection getSectionWithId(String id);

    public void accept(IChartVisitor visitor);
}
