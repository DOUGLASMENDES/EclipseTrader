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

package net.sourceforge.eclipsetrader.charts.preferences;

import java.util.Calendar;

import net.sourceforge.eclipsetrader.charts.DatePlot;
import net.sourceforge.eclipsetrader.charts.DateSummaryItem;
import net.sourceforge.eclipsetrader.charts.Indicator;
import net.sourceforge.eclipsetrader.charts.IndicatorPlot;
import net.sourceforge.eclipsetrader.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.charts.PlotLine;
import net.sourceforge.eclipsetrader.charts.ScalePlot;
import net.sourceforge.eclipsetrader.charts.Scaler;
import net.sourceforge.eclipsetrader.charts.Summary;
import net.sourceforge.eclipsetrader.charts.views.ChartView;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.BarData;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemePreview;

public class ThemePreview implements IThemePreview
{
    Composite composite;
    Summary summary;
    IndicatorPlot indicatorPlot;
    ScalePlot scalePlot;
    DatePlot datePlot;
    ITheme theme;
    IPropertyChangeListener themeChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event)
        {
            String property = event.getProperty();
            if (property.equals(ChartView.THEME_CHART_BACKGROUND))
            {
                indicatorPlot.setBackground(theme.getColorRegistry().get(property));
                indicatorPlot.redrawAll();
            }
            else if (property.equals(ChartView.THEME_CHART_GRID_COLOR))
            {
                indicatorPlot.setGridColor(theme.getColorRegistry().get(property));
                indicatorPlot.redrawAll();
            }
            else if (property.equals(ChartView.THEME_SCALE_FOREGROUND))
            {
                scalePlot.setForeground(theme.getColorRegistry().get(property));
                scalePlot.setSeparatorColor(theme.getColorRegistry().get(property));
                scalePlot.redrawAll();
            }
            else if (property.equals(ChartView.THEME_SCALE_BACKGROUND))
            {
                scalePlot.setBackground(theme.getColorRegistry().get(property));
                scalePlot.redrawAll();
            }
            else if (property.equals(ChartView.THEME_PERIOD_BACKGROUND))
            {
                datePlot.setBackground(theme.getColorRegistry().get(property));
                datePlot.redrawAll();
            }
            else if (property.equals(ChartView.THEME_PERIOD_FOREGROUND))
            {
                datePlot.setForeground(theme.getColorRegistry().get(property));
                datePlot.redrawAll();
            }
            else if (property.equals(ChartView.THEME_PERIOD_MARKERS))
            {
                datePlot.setHilight(theme.getColorRegistry().get(property));
                datePlot.redrawAll();
            }
            else if (property.equals(ChartView.THEME_SUMMARY_BACKGROUND))
            {
                summary.setBackground(theme.getColorRegistry().get(property));
                summary.layout();
            }
            else if (property.equals(ChartView.THEME_SUMMARY_FOREGROUND))
            {
                summary.setForeground(theme.getColorRegistry().get(property));
                summary.layout();
            }
        }
    };

    public ThemePreview()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.IThemePreview#createControl(org.eclipse.swt.widgets.Composite, org.eclipse.ui.themes.ITheme)
     */
    public void createControl(Composite parent, ITheme currentTheme)
    {
        composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
        composite.setLayout(gridLayout);
        
        summary = new Summary(composite, SWT.NONE);
        summary.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
        summary.setForeground(currentTheme.getColorRegistry().get(ChartView.THEME_SUMMARY_FOREGROUND));
        summary.setBackground(currentTheme.getColorRegistry().get(ChartView.THEME_SUMMARY_BACKGROUND));
        DateSummaryItem dateSummary = new DateSummaryItem(summary, SWT.NONE);

        indicatorPlot = new IndicatorPlot(composite, SWT.NONE);
        indicatorPlot.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        indicatorPlot.setBackground(currentTheme.getColorRegistry().get(ChartView.THEME_CHART_BACKGROUND));
        indicatorPlot.setGridColor(currentTheme.getColorRegistry().get(ChartView.THEME_CHART_GRID_COLOR));
        
        scalePlot = new ScalePlot(composite, SWT.NONE);
        GridData gridData = new GridData(GridData.FILL, GridData.FILL, false, true);
        gridData.widthHint = 65;
        scalePlot.setLayoutData(gridData);
        scalePlot.setForeground(currentTheme.getColorRegistry().get(ChartView.THEME_SCALE_FOREGROUND));
        scalePlot.setBackground(currentTheme.getColorRegistry().get(ChartView.THEME_SCALE_BACKGROUND));
        scalePlot.setSeparatorColor(currentTheme.getColorRegistry().get(ChartView.THEME_SCALE_FOREGROUND));

        datePlot = new DatePlot(composite, SWT.NONE);
        gridData = new GridData();
        gridData.heightHint = 24;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        datePlot.setLayoutData(gridData);
        datePlot.setScaleWidth(65);
        datePlot.setForeground(currentTheme.getColorRegistry().get(ChartView.THEME_PERIOD_FOREGROUND));
        datePlot.setBackground(currentTheme.getColorRegistry().get(ChartView.THEME_PERIOD_BACKGROUND));
        
        Calendar day = Calendar.getInstance();
        BarData barData = new BarData();
        day.set(2006, 8, 1, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.948, 0.96, 0.9355, 0.942, 7093301));
        day.set(2006, 8, 4, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.942, 0.942, 0.929, 0.9385, 4109867));
        day.set(2006, 8, 5, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.9385, 0.961, 0.9345, 0.95, 6809366));
        day.set(2006, 8, 6, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.946, 0.9655, 0.924, 0.924, 11880966));
        day.set(2006, 8, 7, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.923, 0.938, 0.9145, 0.917, 5316833));
        day.set(2006, 8, 8, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.9225, 0.929, 0.917, 0.92, 3540619));
        day.set(2006, 8, 11, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.921, 0.9285, 0.911, 0.918, 4502774));
        day.set(2006, 8, 12, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.919, 0.919, 0.896, 0.905, 7393791));
        day.set(2006, 8, 13, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.893, 0.8935, 0.804, 0.8115, 111515424));
        day.set(2006, 8, 14, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.8125, 0.829, 0.808, 0.82, 37027740));
        day.set(2006, 8, 15, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.821, 0.821, 0.782, 0.8035, 43632880));
        day.set(2006, 8, 18, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.81, 0.8245, 0.803, 0.811, 19589584));
        day.set(2006, 8, 19, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.8135, 0.822, 0.795, 0.795, 22834886));
        day.set(2006, 8, 20, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.7915, 0.81, 0.775, 0.806, 17296900));
        day.set(2006, 8, 21, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.816, 0.822, 0.8085, 0.8165, 14575569));
        day.set(2006, 8, 22, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.81, 0.8375, 0.8055, 0.823, 21964780));
        day.set(2006, 8, 25, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.82, 0.8545, 0.82, 0.84, 23396038));
        day.set(2006, 8, 26, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.8455, 0.849, 0.83, 0.8425, 13383428));
        day.set(2006, 8, 27, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.845, 0.845, 0.815, 0.824, 20362424));
        day.set(2006, 8, 28, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.829, 0.831, 0.817, 0.821, 8175691));
        day.set(2006, 8, 29, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.8225, 0.8275, 0.806, 0.8125, 11613527));
        day.set(2006, 9, 2, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.8205, 0.837, 0.808, 0.829, 18385872));
        day.set(2006, 9, 3, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.8335, 0.836, 0.817, 0.826, 9012386));
        day.set(2006, 9, 4, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.829, 0.837, 0.821, 0.83, 12492644));
        day.set(2006, 9, 5, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.808, 0.8245, 0.805, 0.8245, 16632878));
        day.set(2006, 9, 6, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.8205, 0.8225, 0.8105, 0.816, 9961312));
        day.set(2006, 9, 9, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.816, 0.823, 0.8105, 0.814, 6901545));
        day.set(2006, 9, 10, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.8225, 0.8325, 0.806, 0.83, 25326628));
        day.set(2006, 9, 11, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.8155, 0.824, 0.805, 0.8115, 24419232));
        day.set(2006, 9, 12, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.8025, 0.808, 0.731, 0.736, 103220072));
        day.set(2006, 9, 13, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.76, 0.763, 0.7385, 0.74, 65447108));
        day.set(2006, 9, 16, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.749, 0.782, 0.74, 0.7795, 50177524));
        day.set(2006, 9, 17, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.786, 0.7935, 0.7605, 0.7695, 35056876));
        day.set(2006, 9, 18, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.786, 0.789, 0.77, 0.78, 24388214));
        day.set(2006, 9, 19, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.783, 0.788, 0.774, 0.78, 15640205));
        day.set(2006, 9, 20, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.793, 0.793, 0.778, 0.783, 18727956));
        day.set(2006, 9, 23, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.785, 0.785, 0.752, 0.774, 16273436));
        day.set(2006, 9, 24, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.773, 0.778, 0.7565, 0.7735, 14935518));
        day.set(2006, 9, 25, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.767, 0.7755, 0.761, 0.7745, 6962403));
        day.set(2006, 9, 26, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.784, 0.784, 0.757, 0.7605, 12932290));
        day.set(2006, 9, 27, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.761, 0.7675, 0.751, 0.751, 11616683));
        day.set(2006, 9, 30, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.758, 0.7735, 0.752, 0.764, 13436972));
        day.set(2006, 9, 31, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.768, 0.768, 0.761, 0.763, 6510921));
        day.set(2006, 10, 1, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.761, 0.7665, 0.753, 0.76, 5058851));
        day.set(2006, 10, 2, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.7635, 0.765, 0.739, 0.745, 20657980));
        day.set(2006, 10, 3, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.75, 0.754, 0.743, 0.7525, 8508193));
        day.set(2006, 10, 6, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.754, 0.754, 0.745, 0.748, 7959199));
        day.set(2006, 10, 7, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.7495, 0.7575, 0.742, 0.751, 8174181));
        day.set(2006, 10, 8, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.7475, 0.752, 0.744, 0.7465, 7367703));
        day.set(2006, 10, 9, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.7495, 0.7495, 0.7405, 0.744, 5575260));
        day.set(2006, 10, 10, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.742, 0.773, 0.7415, 0.755, 13950922));
        day.set(2006, 10, 13, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.765, 0.796, 0.762, 0.788, 43878832));
        day.set(2006, 10, 14, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.786, 0.821, 0.776, 0.8075, 55081736));
        day.set(2006, 10, 15, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.817, 0.819, 0.793, 0.8105, 17430116));
        day.set(2006, 10, 16, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.812, 0.834, 0.805, 0.826, 29208616));
        day.set(2006, 10, 17, 0, 0, 0);
        barData.append(new Bar(day.getTime(), 0.836, 0.9115, 0.835, 0.8655, 127826336));

        indicatorPlot.setBarData(barData);
        datePlot.setBarData(barData);
        
        Scaler scaler = new Scaler();
        scaler.set(barData.getMax(), barData.getMin());
        indicatorPlot.setScaler(scaler);
        scalePlot.setScaler(scaler);
        
        PlotLine line = new PlotLine(barData, BarData.CLOSE);
        line.setType(PlotLine.HISTOGRAM);
        line.setColor(new Color(null, 0, 0, 224));
        Indicator indicator = new Indicator();
        indicator.add(line);
        indicatorPlot.addIndicator(indicator);

        PlotLine ma = IndicatorPlugin.getEMA(line, 7);
        ma.setColor(new Color(null, 224, 0, 0));
        ma.setLabel("MA7");
        indicator = new Indicator();
        indicator.add(ma);
        indicatorPlot.addIndicator(indicator);

        ma = IndicatorPlugin.getEMA(line, 14);
        ma.setColor(new Color(null, 0, 224, 0));
        ma.setLabel("MA14");
        indicator = new Indicator();
        indicator.add(ma);
        indicatorPlot.addIndicator(indicator);
        
        dateSummary.setData(Calendar.getInstance().getTime(), true);
        
        theme = currentTheme;
        theme.addPropertyChangeListener(themeChangeListener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.IThemePreview#dispose()
     */
    public void dispose()
    {
        theme.removePropertyChangeListener(themeChangeListener);
    }
}
