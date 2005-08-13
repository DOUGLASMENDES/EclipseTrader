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
import net.sourceforge.eclipsetrader.ui.views.charts.IndicatorParametersPage;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 */
public class ParametersPage extends WizardPage
{
  private IndicatorParametersPage parametersPage;

  public ParametersPage(IndicatorParametersPage parametersPage)
  {
    super(Messages.getString("NewIndicatorWizard.title")); //$NON-NLS-1$
    setTitle(Messages.getString("ParametersPage.title")); //$NON-NLS-1$
    setDescription(Messages.getString("ParametersPage.description")); //$NON-NLS-1$
    this.parametersPage = parametersPage;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent)
  {
    Control control = parametersPage.createControl(parent);
    setControl(control);
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.DialogPage#dispose()
   */
  public void dispose()
  {
    if (this.parametersPage != null)
      this.parametersPage.dispose();
    super.dispose();
  }

  public void performFinish()
  {
    if (this.parametersPage != null)
      this.parametersPage.performFinish();
  }
}
