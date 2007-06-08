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

package net.sourceforge.eclipsetrader.core.ui.preferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class IntradayDataOptions
{
    private Button enable;
    private Text beginTime;
    private Text endTime;
    private Text keepDays;
    private Button sun;
    private Button mon;
    private Button tue;
    private Button wed;
    private Button thu;
    private Button fri;
    private Button sat;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm"); //$NON-NLS-1$
    private Calendar time = Calendar.getInstance();

    public IntradayDataOptions()
    {
    }

    public Composite createControls(Composite parent, Security security)
    {
        Composite control = createControls(parent);
        
        if (security != null)
        {
            enable.setSelection(security.isEnableDataCollector());
            time.set(Calendar.HOUR_OF_DAY, security.getBeginTime() / 60);
            time.set(Calendar.MINUTE, security.getBeginTime() % 60);
            beginTime.setText(timeFormat.format(time.getTime()));
            time.set(Calendar.HOUR_OF_DAY, security.getEndTime() / 60);
            time.set(Calendar.MINUTE, security.getEndTime() % 60);
            endTime.setText(timeFormat.format(time.getTime()));
            sun.setSelection((security.getWeekDays() & Security.SUN) != 0);
            mon.setSelection((security.getWeekDays() & Security.MON) != 0);
            tue.setSelection((security.getWeekDays() & Security.TUE) != 0);
            wed.setSelection((security.getWeekDays() & Security.WED) != 0);
            thu.setSelection((security.getWeekDays() & Security.THU) != 0);
            fri.setSelection((security.getWeekDays() & Security.FRI) != 0);
            sat.setSelection((security.getWeekDays() & Security.SAT) != 0);
            keepDays.setText(String.valueOf(security.getKeepDays()));
        }

        return control;
    }
    
    public Composite createControls(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        enable = new Button(content, SWT.CHECK);
        enable.setText(Messages.IntradayDataOptions_Enable);
        
        label = new Label(content, SWT.NONE);
        label.setText(Messages.IntradayDataOptions_Begin);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        beginTime = new Text(content, SWT.BORDER);
        beginTime.setLayoutData(new GridData(60, SWT.DEFAULT));
        beginTime.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e)
            {
                try
                {
                    time.setTime(timeFormat.parse(beginTime.getText()));
                    beginTime.setText(timeFormat.format(time.getTime()));
                }
                catch (ParseException e1) {
                    CorePlugin.logException(e1);
                }
            }
        });
        
        label = new Label(content, SWT.NONE);
        label.setText(Messages.IntradayDataOptions_End);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        endTime = new Text(content, SWT.BORDER);
        endTime.setLayoutData(new GridData(60, SWT.DEFAULT));
        endTime.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e)
            {
                try
                {
                    time.setTime(timeFormat.parse(endTime.getText()));
                    endTime.setText(timeFormat.format(time.getTime()));
                }
                catch (ParseException e1) {
                    CorePlugin.logException(e1);
                }
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText(Messages.IntradayDataOptions_WeekDays);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        Composite group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(1, true);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        sun = new Button(group, SWT.CHECK);
        sun.setText(Messages.IntradayDataOptions_Sun);
        mon = new Button(group, SWT.CHECK);
        mon.setText(Messages.IntradayDataOptions_Mon);
        tue = new Button(group, SWT.CHECK);
        tue.setText(Messages.IntradayDataOptions_Tue);
        wed = new Button(group, SWT.CHECK);
        wed.setText(Messages.IntradayDataOptions_Wed);
        thu = new Button(group, SWT.CHECK);
        thu.setText(Messages.IntradayDataOptions_Thu);
        fri = new Button(group, SWT.CHECK);
        fri.setText(Messages.IntradayDataOptions_Fri);
        sat = new Button(group, SWT.CHECK);
        sat.setText(Messages.IntradayDataOptions_Sat);
        
        label = new Label(content, SWT.NONE);
        label.setText(Messages.IntradayDataOptions_KeepDays);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        keepDays = new Text(content, SWT.BORDER);
        keepDays.setLayoutData(new GridData(60, SWT.DEFAULT));

        enable.setSelection(false);
        time.set(Calendar.HOUR_OF_DAY, 9);
        time.set(Calendar.MINUTE, 30);
        beginTime.setText(timeFormat.format(time.getTime()));
        time.set(Calendar.HOUR_OF_DAY, 17);
        time.set(Calendar.MINUTE, 30);
        endTime.setText(timeFormat.format(time.getTime()));
        sun.setSelection(false);
        mon.setSelection(true);
        tue.setSelection(true);
        wed.setSelection(true);
        thu.setSelection(true);
        fri.setSelection(true);
        sat.setSelection(false);
        keepDays.setText(String.valueOf(1));

        return content;
    }
    
    public boolean saveSettings(Security security)
    {
        security.setEnableDataCollector(enable.getSelection());

        int weekdays = 0;
        if (sun.getSelection())
            weekdays |= Security.SUN;
        if (mon.getSelection())
            weekdays |= Security.MON;
        if (tue.getSelection())
            weekdays |= Security.TUE;
        if (wed.getSelection())
            weekdays |= Security.WED;
        if (thu.getSelection())
            weekdays |= Security.THU;
        if (fri.getSelection())
            weekdays |= Security.FRI;
        if (sat.getSelection())
            weekdays |= Security.SAT;
        security.setWeekDays(weekdays);
        security.setKeepDays(Integer.parseInt(keepDays.getText()));
        
        try
        {
            time.setTime(timeFormat.parse(beginTime.getText()));
            security.setBeginTime(time.get(Calendar.HOUR_OF_DAY) * 60 + time.get(Calendar.MINUTE));
            time.setTime(timeFormat.parse(endTime.getText()));
            security.setEndTime(time.get(Calendar.HOUR_OF_DAY) * 60 + time.get(Calendar.MINUTE));
        }
        catch (ParseException e) {
            CorePlugin.logException(e);
            return false;
        }
        
        return true;
    }
}
