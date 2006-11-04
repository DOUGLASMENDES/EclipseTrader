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
        
        addIntegerValueSelector(content, "period", Messages.AdaptiveStochPreferencePage_Period, 1, 9999, AdaptiveStoch.DEFAULT_PERIOD); //$NON-NLS-1$
        addIntegerValueSelector(content, "minLookback", Messages.AdaptiveStochPreferencePage_MinLookbackPeriod, 1, 9999, AdaptiveStoch.DEFAULT_MIN_LOOKBACK); //$NON-NLS-1$
        addIntegerValueSelector(content, "maxLookback", Messages.AdaptiveStochPreferencePage_MaxLookbackPeriod, 1, 9999, AdaptiveStoch.DEFAULT_MAX_LOOKBACK); //$NON-NLS-1$
        addColorSelector(content, "kcolor", Messages.AdaptiveStochPreferencePage_KColor, AdaptiveStoch.DEFAULT_KCOLOR); //$NON-NLS-1$
        addLineTypeSelector(content, "klineType", Messages.AdaptiveStochPreferencePage_KLineType, AdaptiveStoch.DEFAULT_KLINETYPE); //$NON-NLS-1$
        addLabelField(content, "klabel", Messages.AdaptiveStochPreferencePage_KLabel, AdaptiveStoch.DEFAULT_KLABEL); //$NON-NLS-1$
        addMovingAverageSelector(content, "kMaType", Messages.AdaptiveStochPreferencePage_KSMotthingType, AdaptiveStoch.DEFAULT_K_MATYPE); //$NON-NLS-1$
        addIntegerValueSelector(content, "kperiod", Messages.AdaptiveStochPreferencePage_KPeriod, 1, 9999, AdaptiveStoch.DEFAULT_KPERIOD); //$NON-NLS-1$
        addColorSelector(content, "dcolor", Messages.AdaptiveStochPreferencePage_DColor, AdaptiveStoch.DEFAULT_DCOLOR); //$NON-NLS-1$
        addLineTypeSelector(content, "dlineType", Messages.AdaptiveStochPreferencePage_DLineType, AdaptiveStoch.DEFAULT_DLINETYPE); //$NON-NLS-1$
        addLabelField(content, "dlabel", Messages.AdaptiveStochPreferencePage_DLabel, AdaptiveStoch.DEFAULT_DLABEL); //$NON-NLS-1$
        addMovingAverageSelector(content, "dMaType", Messages.AdaptiveStochPreferencePage_DSmoothingType, AdaptiveStoch.DEFAULT_D_MATYPE); //$NON-NLS-1$
        addIntegerValueSelector(content, "dperiod", Messages.AdaptiveStochPreferencePage_DPeriod, 1, 9999, AdaptiveStoch.DEFAULT_DPERIOD); //$NON-NLS-1$
        addColorSelector(content, "buyColor", Messages.AdaptiveStochPreferencePage_BuyColor, AdaptiveStoch.DEFAULT_BUYCOLOR); //$NON-NLS-1$
        addColorSelector(content, "sellColor", Messages.AdaptiveStochPreferencePage_SellColor, AdaptiveStoch.DEFAULT_SELLCOLOR); //$NON-NLS-1$
        addIntegerValueSelector(content, "buyLine", Messages.AdaptiveStochPreferencePage_BuyLine, 0, 100, AdaptiveStoch.DEFAULT_BUYLINE); //$NON-NLS-1$
        addIntegerValueSelector(content, "sellLine", Messages.AdaptiveStochPreferencePage_SellLine, 0, 100, AdaptiveStoch.DEFAULT_SELLLINE); //$NON-NLS-1$
    }
}
