/*******************************************************************************
 * Copyright (c) 2004-2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.charts.wizards;

import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.views.charts.IChartConfigurer;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 */
public class ParametersPage extends WizardPage
{
  public final static int SELECTED_ZONE = 1;
  public final static int NEW_ZONE = 2;
  public static final int NEW_CHART = 1;
  public static final int EDIT_CHART = 2;
  private Text text;
  private ColorSelector colorSelector;
  private IChartConfigurer configurer;

  public ParametersPage()
  {
    super(Messages.getString("NewIndicatorWizard.title")); //$NON-NLS-1$
    setTitle(Messages.getString("ParametersPage.title")); //$NON-NLS-1$
    setDescription(Messages.getString("ParametersPage.description")); //$NON-NLS-1$
  }

  public ParametersPage(IChartConfigurer configurer)
  {
    super(Messages.getString("NewIndicatorWizard.title")); //$NON-NLS-1$
    setTitle(Messages.getString("ParametersPage.title")); //$NON-NLS-1$
    setDescription(Messages.getString("ParametersPage.description")); //$NON-NLS-1$
    this.configurer = configurer;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.wizard.IWizardPage#getNextPage()
   */
  public IWizardPage getNextPage()
  {
    ZonePage zone = ((NewIndicatorWizard)getWizard()).getZonePage();
    zone.setWizard(getWizard());
    return zone;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent)
  {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new GridLayout(2, false));
    setControl(composite);
    
    Label label = new Label(composite, SWT.NONE);
    label.setText(Messages.getString("ChartParametersDialog.name")); //$NON-NLS-1$
    label.setLayoutData(new GridData(100, SWT.DEFAULT));
    text = new Text(composite, SWT.BORDER);
    text.setData("name"); //$NON-NLS-1$
    text.setText(configurer.getName());
    text.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    text.forceFocus();
    
    label = new Label(composite, SWT.NONE);
    label.setText(Messages.getString("ChartParametersDialog.color")); //$NON-NLS-1$
    colorSelector = new ColorSelector(composite);
    colorSelector.getButton().setData("color"); //$NON-NLS-1$
    colorSelector.setColorValue(configurer.getColorParameter("color")); //$NON-NLS-1$

    // Adds the parameters specific to the given chart
    if (configurer != null)
      configurer.createContents(composite);
  }
  
  public String getPlotterName()
  {
    if (text == null)
      return configurer.getName();
    return text.getText();
  }
  
  public RGB getColor()
  {
    if (colorSelector == null)
      return new RGB(0, 0, 255);
    return colorSelector.getColorValue();
  }
}
