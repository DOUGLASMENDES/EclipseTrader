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

package net.sourceforge.eclipsetrader.charts.dialogs;

import java.text.SimpleDateFormat;

import net.sourceforge.eclipsetrader.charts.internal.Messages;
import net.sourceforge.eclipsetrader.charts.views.ChartView;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Chart;
import net.sourceforge.eclipsetrader.core.db.History;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GeneralPage extends PreferencePage
{
    private Chart chart;
    private Text chartTitle;
    private Combo period;
    private Combo begin;
    private Combo end;
    private Combo interval;
    private Button clearDataButton;
    private Button saveAsDefaultButton;
    private boolean clearData = false;
    private boolean saveAsDefault = false;
    private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$

    public GeneralPage(Chart chart)
    {
        super(Messages.GeneralPage_Title);
        this.chart = chart;
        noDefaultAndApplyButton();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        
        Label label = new Label(content, SWT.NONE);
        label.setText(Messages.GeneralPage_ChartTitle);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        chartTitle = new Text(content, SWT.BORDER);
        chartTitle.setText(chart.getTitle());
        chartTitle.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));

        label = new Label(content, SWT.NONE);
        label.setText(Messages.GeneralPage_Period);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        period = new Combo(content, SWT.READ_ONLY);
        period.add(Messages.Period_All);
        period.add(Messages.Period_2Years);
        period.add(Messages.Period_1Year);
        period.add(Messages.Period_6Months);
        period.add(Messages.Period_Custom);
        switch(chart.getPeriod())
        {
            case ChartView.PERIOD_ALL:
                period.select(0);
                break;
            case ChartView.PERIOD_LAST2YEARS:
                period.select(1);
                break;
            case ChartView.PERIOD_LASTYEAR:
                period.select(2);
                break;
            case ChartView.PERIOD_LAST6MONTHS:
                period.select(3);
                break;
            case ChartView.PERIOD_CUSTOM:
                period.select(4);
                break;
        }
        period.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                begin.setEnabled(period.getSelectionIndex() == 4);
                end.setEnabled(period.getSelectionIndex() == 4);
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText(Messages.GeneralPage_BeginDate);
        begin = new Combo(content, SWT.READ_ONLY);
        begin.setVisibleItemCount(25);
        begin.setEnabled(period.getSelectionIndex() == 4);

        label = new Label(content, SWT.NONE);
        label.setText(Messages.GeneralPage_EndDate);
        end = new Combo(content, SWT.READ_ONLY);
        end.setVisibleItemCount(25);
        end.setEnabled(period.getSelectionIndex() == 4);

        label = new Label(content, SWT.NONE);
        label.setText(Messages.GeneralPage_Interval);
        interval = new Combo(content, SWT.READ_ONLY);
        interval.add(Messages.Interval_Monthly);
        interval.add(Messages.Interval_Weekly);
        interval.add(Messages.Interval_Daily);
        interval.add(Messages.Interval_1Hour);
        interval.add(Messages.Interval_30Min);
        interval.add(Messages.Interval_15Min);
        interval.add(Messages.Interval_10Min);
        interval.add(Messages.Interval_5Min);
        interval.add(Messages.Interval_2Min);
        interval.add(Messages.Interval_1Min);
        interval.select(10 - chart.getCompression());

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, true, 2, 1));
        
        History history = chart.getSecurity().getHistory();
        
        label = new Label(content, SWT.NONE);
        label.setText(Messages.GeneralPage_FirstDate);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        Text text = new Text(content, SWT.BORDER);
        if (history.size() != 0)
            text.setText(df.format(((Bar) history.get(0)).getDate()));
        text.setLayoutData(new GridData(80, SWT.DEFAULT));
        text.setEnabled(false);
        
        label = new Label(content, SWT.NONE);
        label.setText(Messages.GeneralPage_LastDate);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        text = new Text(content, SWT.BORDER);
        if (history.size() != 0)
            text.setText(df.format(((Bar) history.get(history.size() - 1)).getDate()));
        text.setLayoutData(new GridData(80, SWT.DEFAULT));
        text.setEnabled(false);
        
        clearDataButton = new Button(content, SWT.CHECK);
        clearDataButton.setText(Messages.GeneralPage_ClearData);
        clearDataButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, true, false, 2, 1));

        saveAsDefaultButton = new Button(content, SWT.CHECK);
        saveAsDefaultButton.setText(Messages.GeneralPage_SaveDefault);
        saveAsDefaultButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, true, false, 2, 1));

        int first = -1, last = -1;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
        for (int i = 0; i < history.size(); i++)
        {
            Bar bar = (Bar)history.get(i);
            begin.add(dateFormat.format(bar.getDate()));
            end.add(dateFormat.format(bar.getDate()));
            if (first == -1 && chart.getBeginDate() != null && (bar.getDate().equals(chart.getBeginDate()) || bar.getDate().after(chart.getBeginDate())))
                first = i;
            if (chart.getEndDate() != null && (bar.getDate().equals(chart.getEndDate()) || chart.getEndDate().after(bar.getDate())))
                last = i;
        }
        begin.select(first != -1 ? first : 0);
        end.select(last != -1 ? last : end.getItemCount() - 1);

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        if (chartTitle != null)
        {
            chart.setTitle(chartTitle.getText());
            switch(period.getSelectionIndex())
            {
                case 0:
                    chart.setPeriod(ChartView.PERIOD_ALL);
                    break;
                case 1:
                    chart.setPeriod(ChartView.PERIOD_LAST2YEARS);
                    break;
                case 2:
                    chart.setPeriod(ChartView.PERIOD_LASTYEAR);
                    break;
                case 3:
                    chart.setPeriod(ChartView.PERIOD_LAST6MONTHS);
                    break;
                case 4:
                {
                    History history = chart.getSecurity().getHistory();
                    chart.setPeriod(ChartView.PERIOD_CUSTOM);
                    chart.setBeginDate(history.get(begin.getSelectionIndex()).getDate());
                    chart.setEndDate(history.get(end.getSelectionIndex()).getDate());
                    break;
                }
            }
            chart.setCompression(10 - interval.getSelectionIndex());

            clearData = clearDataButton.getSelection();
            saveAsDefault = saveAsDefaultButton.getSelection();
        }
        
        return super.performOk();
    }
    
    public boolean getSaveAsDefault()
    {
        return saveAsDefault;
    }
    
    public boolean getClearData()
    {
        return clearData;
    }
}
