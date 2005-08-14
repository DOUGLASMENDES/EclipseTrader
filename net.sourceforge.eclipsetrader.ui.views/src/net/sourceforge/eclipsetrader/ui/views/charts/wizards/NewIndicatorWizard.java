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
import net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

/**
 */
public class NewIndicatorWizard extends Wizard
{
  public final static int SELECTED_ZONE = 1;
  public final static int BELOW_SELECTED_ZONE = 2;
  public final static int ABOVE_SELECTED_ZONE = 3;
  private IndicatorPlugin indicator;
  private IndicatorsPage indicatorsPage = new IndicatorsPage();
  private ParametersPage parametersPage;
  private ZonePage zonePage = new ZonePage();
  private int position = NewIndicatorWizard.BELOW_SELECTED_ZONE;
  
  public NewIndicatorWizard()
  {
    addPage(indicatorsPage);
    setWindowTitle(Messages.getString("NewIndicatorWizard.title")); //$NON-NLS-1$
    setForcePreviousAndNextButtons(true);
    zonePage.setWizard(this);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  public boolean performFinish()
  {
    parametersPage.performFinish();
    position = zonePage.getZone();
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
   */
  public IWizardPage getNextPage(IWizardPage page)
  {
    if (page == indicatorsPage)
      return parametersPage;
    if (page == parametersPage)
      return zonePage;
    return null;
  }

  public boolean open()
  {
    WizardDialog dlg = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), this);
    return dlg.open() == WizardDialog.OK;
  }

  /**
   * Returns the new indicator instance.
   * 
   * @return - the new indicator
   */
  public IndicatorPlugin getIndicator()
  {
    return indicator;
  }
  
  /**
   * Set the indicator instance.
   * 
   * @param indicator - the indicator instance
   */
  public void setIndicator(IndicatorPlugin indicator)
  {
    this.indicator = indicator;
    if (parametersPage != null)
      parametersPage.dispose();
    parametersPage = new ParametersPage(this.indicator.getParametersPage());
    parametersPage.setWizard(this);
  }

  /**
   * Returns the chart zone where the new indicator must be placed.
   * 
   * @return - the zone
   */
  public int getZone()
  {
    return position;
  }
}
