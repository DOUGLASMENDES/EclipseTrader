/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.trading.views;

import net.sourceforge.eclipsetrader.trading.TradingPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A panel with counters for the number of Runs, Errors and Failures.
 */
public class CounterPanel extends Composite
{
    protected Text fNumberOfGains;
    protected Text fNumberOfLosses;
    protected Text fNumberOfRuns;
    protected int fTotal;
    private final Image fSuccessIcon = TradingPlugin.getImageDescriptor("icons/ovr16/success_ovr.gif").createImage(); //$NON-NLS-1$
    private final Image fFailureIcon = TradingPlugin.getImageDescriptor("icons/ovr16/error_ovr.gif").createImage(); //$NON-NLS-1$

    public CounterPanel(Composite parent)
    {
        super(parent, SWT.WRAP);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 9;
        gridLayout.makeColumnsEqualWidth = false;
        gridLayout.marginWidth = 0;
        setLayout(gridLayout);

        fNumberOfRuns = createLabel("Signals:", null, " 0 "); //$NON-NLS-1$
        fNumberOfGains = createLabel("Gains:", fSuccessIcon, " 0 "); //$NON-NLS-1$
        fNumberOfLosses = createLabel("Losses:", fFailureIcon, " 0 "); //$NON-NLS-1$

        addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                disposeIcons();
            }
        });
    }

    private void disposeIcons()
    {
        fSuccessIcon.dispose();
        fFailureIcon.dispose();
    }

    private Text createLabel(String name, Image image, String init)
    {
        Label label = new Label(this, SWT.NONE);
        if (image != null)
        {
            image.setBackground(label.getBackground());
            label.setImage(image);
        }
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

        label = new Label(this, SWT.NONE);
        label.setText(name);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        //label.setFont(JFaceResources.getBannerFont());

        Text value = new Text(this, SWT.READ_ONLY);
        value.setText(init);
        // bug: 39661 Junit test counters do not repaint correctly [JUnit] 
        value.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        value.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING));
        return value;
    }

    public void reset()
    {
        setGainsValue(0);
        setLossesValue(0);
        setRunValue(0);
        fTotal = 0;
    }

    public void setTotal(int value)
    {
        fTotal = value;
    }

    public int getTotal()
    {
        return fTotal;
    }

    public void setRunValue(int value)
    {
        //		String runString= Messages.format(JUnitMessages.CounterPanel_runcount, new String[] { Integer.toString(value), Integer.toString(fTotal) }); 
        fNumberOfRuns.setText(Integer.toString(value));
        fNumberOfRuns.redraw();
        redraw();
    }

    public void setGainsValue(int value)
    {
        fNumberOfGains.setText(Integer.toString(value));
        redraw();
    }

    public void setLossesValue(int value)
    {
        fNumberOfLosses.setText(Integer.toString(value));
        redraw();
    }
}
