/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.ta_lib.internal;

import net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public abstract class TALibIndicatorPreferencePage extends IndicatorPluginPreferencePage
{

    public TALibIndicatorPreferencePage()
    {
    }
    
    public Combo addMovingAverageSelector(Composite parent, String id, String text, int defaultValue)
    {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        Combo combo = new Combo(parent, SWT.READ_ONLY);
        combo.add("Simple");
        combo.add("Exponential");
        combo.add("Weighted");
        combo.add("Double Exponential");
        combo.add("Triple Exponential");
        combo.add("Triangular");
        combo.add("Kaufman Adaptive");
        combo.add("MESA Adaptive");
        combo.add("Triple Exponential (T3)");
        combo.select(getSettings().getInteger(id, defaultValue).intValue());
        addControl(id, combo);
        return combo;
    }
}
