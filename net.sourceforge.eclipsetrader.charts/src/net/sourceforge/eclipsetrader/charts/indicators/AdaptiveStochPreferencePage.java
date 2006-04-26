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

package net.sourceforge.eclipsetrader.charts.indicators;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage;

public class AdaptiveStochPreferencePage extends IndicatorPluginPreferencePage
{

    public AdaptiveStochPreferencePage()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        setControl(content);
        
        addIntegerValueSelector(content, "period", "Period", 1, 9999, AdaptiveStoch.DEFAULT_PERIOD);
        addIntegerValueSelector(content, "minLookback", "Min Lookback Period", 1, 9999, AdaptiveStoch.DEFAULT_MIN_LOOKBACK);
        addIntegerValueSelector(content, "maxLookback", "Max Lookback Period", 1, 9999, AdaptiveStoch.DEFAULT_MAX_LOOKBACK);
        addColorSelector(content, "kcolor", "%K Color", AdaptiveStoch.DEFAULT_KCOLOR);
        addLineTypeSelector(content, "klineType", "%K Line Type", AdaptiveStoch.DEFAULT_KLINETYPE);
        addLabelField(content, "klabel", "%K Label", AdaptiveStoch.DEFAULT_KLABEL);
        addMovingAverageSelector(content, "kMaType", "%K Smoothing Type", AdaptiveStoch.DEFAULT_K_MATYPE);
        addIntegerValueSelector(content, "kperiod", "%K Period", 1, 9999, AdaptiveStoch.DEFAULT_KPERIOD);
        addColorSelector(content, "dcolor", "%D Color", AdaptiveStoch.DEFAULT_DCOLOR);
        addLineTypeSelector(content, "dlineType", "%D Line Type", AdaptiveStoch.DEFAULT_DLINETYPE);
        addLabelField(content, "dlabel", "%D Label", AdaptiveStoch.DEFAULT_DLABEL);
        addMovingAverageSelector(content, "dMaType", "%D Smoothing Type", AdaptiveStoch.DEFAULT_D_MATYPE);
        addIntegerValueSelector(content, "dperiod", "%D Period", 1, 9999, AdaptiveStoch.DEFAULT_DPERIOD);
        addColorSelector(content, "buyColor", "Buy Color", AdaptiveStoch.DEFAULT_BUYCOLOR);
        addColorSelector(content, "sellColor", "Sell Color", AdaptiveStoch.DEFAULT_SELLCOLOR);
        addIntegerValueSelector(content, "buyLine", "Buy Line", 0, 100, AdaptiveStoch.DEFAULT_BUYLINE);
        addIntegerValueSelector(content, "sellLine", "Sell Line", 0, 100, AdaptiveStoch.DEFAULT_SELLLINE);
    }
}
