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
 * Chart section.
 * <p>Sections are displayed in the UI as tab folders.</p>
 *
 * @since 1.0
 */
public interface IChartSection {

    /**
     * Returns the identifier of this section. Identifiers must be unique
     * within a chart tree.
     *
     * @return the unique identifier.
     */
    public String getId();

    /**
     * Returns the name of the receiver used in the UI.
     *
     * @return the name.
     */
    public String getName();

    /**
     * Sets the name of the receiver.
     *
     * @param name the name to set.
     */
    public void setName(String name);

    public IElementSection[] getElements();

    public void setElements(IElementSection[] indicators);

    public ISecuritySection[] getSecurities();

    public void setSecurities(ISecuritySection[] securities);

    public void accept(IChartVisitor visitor);
}
