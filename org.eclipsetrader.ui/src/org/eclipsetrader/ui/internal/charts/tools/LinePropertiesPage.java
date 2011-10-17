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

package org.eclipsetrader.ui.internal.charts.tools;

import java.text.NumberFormat;
import java.text.ParseException;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.ui.Util;

public class LinePropertiesPage extends PropertyPage {

    private Text text;
    private ColorSelector color;

    private Text value1;
    private CDateTime date1;
    private Button extend1;

    private Text value2;
    private CDateTime date2;
    private Button extend2;

    private NumberFormat numberFormat = NumberFormat.getInstance();

    private FocusAdapter numberFocusListener = new FocusAdapter() {

        @Override
        public void focusLost(FocusEvent event) {
            Text text = (Text) event.widget;
            try {
                Number number = numberFormat.parse(text.getText());
                text.setText(numberFormat.format(number));
            } catch (ParseException e) {
                text.setData("valid", Boolean.FALSE); //$NON-NLS-1$
            }
            setValid(!Boolean.FALSE.equals(value1.getData("valid")) && !Boolean.FALSE.equals(value2.getData("valid"))); //$NON-NLS-1$ //$NON-NLS-2$
        }
    };

    public LinePropertiesPage() {
        noDefaultAndApplyButton();

        numberFormat.setGroupingUsed(true);
        numberFormat.setMinimumIntegerDigits(1);
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(4);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setTitle(Messages.LinePropertiesPage_Title);

        Label label = new Label(content, SWT.NONE);
        label.setText(Messages.LinePropertiesPage_LabelLabel);
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(75), SWT.DEFAULT));
        text = new Text(content, SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        label = new Label(content, SWT.NONE);
        label.setText(Messages.LinePropertiesPage_ColorLabel);
        color = new ColorSelector(content);
        color.setColorValue(new RGB(0, 0, 255));
        color.getButton().setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        ((GridData) label.getLayoutData()).heightHint = convertVerticalDLUsToPixels(5);

        label = new Label(content, SWT.NONE);
        label.setText(Messages.LinePropertiesPage_FirstPointLabel);
        date1 = new CDateTime(content, CDT.BORDER | CDT.DATE_SHORT | CDT.DROP_DOWN | CDT.TAB_FIELDS);
        date1.setPattern(Util.getDateFormatPattern());
        value1 = new Text(content, SWT.BORDER);
        value1.setLayoutData(new GridData(convertHorizontalDLUsToPixels(65), SWT.DEFAULT));
        value1.addFocusListener(numberFocusListener);

        label = new Label(content, SWT.NONE);
        extend1 = new Button(content, SWT.CHECK);
        extend1.setText(Messages.LinePropertiesPage_ExtendLabel);
        extend1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        label = new Label(content, SWT.NONE);
        label.setText(Messages.LinePropertiesPage_SecondPointLabel);
        date2 = new CDateTime(content, CDT.BORDER | CDT.DATE_SHORT | CDT.DROP_DOWN | CDT.TAB_FIELDS);
        date2.setPattern(Util.getDateFormatPattern());
        value2 = new Text(content, SWT.BORDER);
        value2.setLayoutData(new GridData(convertHorizontalDLUsToPixels(65), SWT.DEFAULT));
        value2.addFocusListener(numberFocusListener);

        label = new Label(content, SWT.NONE);
        extend2 = new Button(content, SWT.CHECK);
        extend2.setText(Messages.LinePropertiesPage_ExtendLabel);
        extend2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        LineToolFactory object = (LineToolFactory) getElement().getAdapter(LineToolFactory.class);

        text.setText(object.getName());

        value1.setText(numberFormat.format(object.getValue1().getValue()));
        date1.setSelection(object.getValue1().getDate());
        extend1.setSelection(object.isExtend1());

        value2.setText(numberFormat.format(object.getValue2().getValue()));
        date2.setSelection(object.getValue2().getDate());
        extend2.setSelection(object.isExtend2());

        color.setColorValue(object.getColor() != null ? object.getColor() : new RGB(0, 0, 0));

        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        LineToolFactory object = (LineToolFactory) getElement().getAdapter(LineToolFactory.class);

        object.setName(text.getText());

        try {
            object.setValue1(new LineToolFactory.Value(date1.getSelection(), numberFormat.parse(value1.getText())));
            object.setValue2(new LineToolFactory.Value(date2.getSelection(), numberFormat.parse(value2.getText())));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        object.setExtend1(extend1.getSelection());
        object.setExtend2(extend2.getSelection());

        object.setColor(color.getColorValue());

        return super.performOk();
    }
}
