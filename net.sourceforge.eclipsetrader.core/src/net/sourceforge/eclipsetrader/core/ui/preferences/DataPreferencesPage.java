/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francois Cottet - Initial Release
 */

package net.sourceforge.eclipsetrader.core.ui.preferences;

import net.sourceforge.eclipsetrader.core.CorePlugin;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DataPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage
{
    private Text securityHistoryRange;
    private static final String LABEL_RANGE = "Security history range (in years)";
    private static final String TOOLTIP_RANGE = "Max range used for the first download of a security historical prices.";
    private static final String ERROR_MESSAGE_BAD_RANGE = "The range should be an integer between 1 and 50.";
    private String previousValue = null;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);

        Label label = new Label(content, SWT.NONE);
        label.setText(LABEL_RANGE);
        label.setLayoutData(new GridData(200, SWT.DEFAULT));
        securityHistoryRange = new Text(content, SWT.BORDER);

        securityHistoryRange.setText(getPreviousRangeValue());
        securityHistoryRange.setLayoutData(new GridData(25, SWT.DEFAULT));
        securityHistoryRange.setToolTipText(TOOLTIP_RANGE);
        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#isValid()
     */
    public boolean isValid()
    {
        boolean isValid = true;

        String currentValue = securityHistoryRange.getText();
        if (currentValue == null || "".equals(currentValue))
            currentValue = getPreviousRangeValue();

        int value = 0;
        try
        {
            value = Integer.parseInt(currentValue);
        }
        catch (NumberFormatException e)
        {
            value = 0;
            isValid = false;
            e.printStackTrace();
        }
        if (value <= 0 || value > 50)
        {
            isValid = false;
            setErrorMessage(ERROR_MESSAGE_BAD_RANGE);
            currentValue = getPreviousRangeValue();
        }

        securityHistoryRange.setText(currentValue);

        return isValid;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        if (isValid())
        {
            CorePlugin.getDefault().getPreferenceStore().setValue(CorePlugin.PREFS_HISTORICAL_PRICE_RANGE, securityHistoryRange.getText());
            return super.performOk();
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    protected void performApply()
    {
        if (isValid())
            CorePlugin.getDefault().getPreferenceStore().setValue(CorePlugin.PREFS_HISTORICAL_PRICE_RANGE, securityHistoryRange.getText());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults()
    {
        securityHistoryRange.setText(CorePlugin.getDefault().getPreferenceStore().getDefaultString(CorePlugin.PREFS_HISTORICAL_PRICE_RANGE));
    }

    private String getPreviousRangeValue()
    {
        if (this.previousValue != null)
            return this.previousValue;

        this.previousValue = CorePlugin.getDefault().getPreferenceStore().getString(CorePlugin.PREFS_HISTORICAL_PRICE_RANGE);
        if (this.previousValue == null || "".equals(this.previousValue))
            this.previousValue = CorePlugin.getDefault().getPreferenceStore().getDefaultString(CorePlugin.PREFS_HISTORICAL_PRICE_RANGE);

        return this.previousValue;
    }
}
