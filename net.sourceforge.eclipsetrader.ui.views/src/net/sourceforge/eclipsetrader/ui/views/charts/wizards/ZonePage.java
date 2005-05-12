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
import net.sourceforge.eclipsetrader.ui.views.charts.ChartParametersDialog;
import net.sourceforge.eclipsetrader.ui.views.charts.IChartConfigurer;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 */
public class ZonePage extends WizardPage
{
  private int position = ChartParametersDialog.BELOW_SELECTED_ZONE;
  private Group paramGroup;
  private Button selectedZone;
  private Button newZone;
  private Button aboveSelectedZone;
  private ColorSelector colorSelector;
  private IChartConfigurer configurer;

  public ZonePage()
  {
    super(Messages.getString("NewIndicatorWizard.title")); //$NON-NLS-1$
    setTitle(Messages.getString("ZonePage.title")); //$NON-NLS-1$
    setDescription(Messages.getString("ZonePage.description")); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent)
  {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new GridLayout(1, false));
    setControl(composite);
    
    aboveSelectedZone = new Button(composite, SWT.RADIO);
    aboveSelectedZone.setText(Messages.getString("ChartParametersDialog.aboveSelectedZone")); //$NON-NLS-1$
    selectedZone = new Button(composite, SWT.RADIO);
    selectedZone.setText(Messages.getString("ChartParametersDialog.selectedZone")); //$NON-NLS-1$
    newZone = new Button(composite, SWT.RADIO);
    newZone.setText(Messages.getString("ChartParametersDialog.belowSelectedZone")); //$NON-NLS-1$
    newZone.setSelection(true);
  }
  
  public int getZone()
  {
    if (selectedZone != null && selectedZone.getSelection() == true)
      position = ChartParametersDialog.SELECTED_ZONE;
    if (aboveSelectedZone != null && aboveSelectedZone.getSelection() == true)
      position = ChartParametersDialog.ABOVE_SELECTED_ZONE;

    return position;
  }
}
