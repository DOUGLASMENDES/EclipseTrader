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
import java.util.List;

import net.sourceforge.eclipsetrader.charts.views.ChartView;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Chart;

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
        super("General");
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
        label.setText("Title");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        chartTitle = new Text(content, SWT.BORDER);
        chartTitle.setText(chart.getTitle());
        chartTitle.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));

        label = new Label(content, SWT.NONE);
        label.setText("Period");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        period = new Combo(content, SWT.READ_ONLY);
        period.add("All");
        period.add("2 Years");
        period.add("1 Year");
        period.add("6 Months");
        period.add("Custom");
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
        label.setText("Begin Date");
        begin = new Combo(content, SWT.READ_ONLY);
        begin.setVisibleItemCount(25);
        begin.setEnabled(period.getSelectionIndex() == 4);

        label = new Label(content, SWT.NONE);
        label.setText("End Date");
        end = new Combo(content, SWT.READ_ONLY);
        end.setVisibleItemCount(25);
        end.setEnabled(period.getSelectionIndex() == 4);

        label = new Label(content, SWT.NONE);
        label.setText("Interval");
        interval = new Combo(content, SWT.READ_ONLY);
        interval.add("Monthly");
        interval.add("Weekly");
        interval.add("Daily");
        interval.add("1 Hour");
        interval.add("30 Min.");
        interval.add("15 Min.");
        interval.add("10 Min.");
        interval.add("5 Min.");
        interval.add("2 Min.");
        interval.add("1 Min.");
        interval.select(10 - chart.getCompression());

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, true, 2, 1));
        
        List history = chart.getSecurity().getHistory();
        
        label = new Label(content, SWT.NONE);
        label.setText("First Date");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        Text text = new Text(content, SWT.BORDER);
        if (history.size() != 0)
            text.setText(df.format(((Bar) history.get(0)).getDate()));
        text.setLayoutData(new GridData(80, SWT.DEFAULT));
        text.setEnabled(false);
        
        label = new Label(content, SWT.NONE);
        label.setText("Last Date");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        text = new Text(content, SWT.BORDER);
        if (history.size() != 0)
            text.setText(df.format(((Bar) history.get(history.size() - 1)).getDate()));
        text.setLayoutData(new GridData(80, SWT.DEFAULT));
        text.setEnabled(false);
        
        clearDataButton = new Button(content, SWT.CHECK);
        clearDataButton.setText("Clear chart data");
        clearDataButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, true, false, 2, 1));

        saveAsDefaultButton = new Button(content, SWT.CHECK);
        saveAsDefaultButton.setText("Save as default");
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
            if (last == -1 && chart.getEndDate() != null && (bar.getDate().equals(chart.getEndDate()) || bar.getDate().after(chart.getEndDate())))
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
                    List history = chart.getSecurity().getHistory();
                    chart.setPeriod(ChartView.PERIOD_CUSTOM);
                    chart.setBeginDate(((Bar)history.get(begin.getSelectionIndex())).getDate());
                    chart.setEndDate(((Bar)history.get(end.getSelectionIndex())).getDate());
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
