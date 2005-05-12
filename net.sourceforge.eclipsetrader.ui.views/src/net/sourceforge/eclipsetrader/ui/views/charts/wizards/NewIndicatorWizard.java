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
import net.sourceforge.eclipsetrader.ui.views.charts.ChartView;
import net.sourceforge.eclipsetrader.ui.views.charts.IChartConfigurer;
import net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 */
public class NewIndicatorWizard extends Wizard
{
  private IChartPlotter plotter;
  private ParametersPage parameters = new ParametersPage();
  private ZonePage zone = new ZonePage();
  private ChartView view;
  
  public NewIndicatorWizard()
  {
    addPage(new OscillatorPage());
    setWindowTitle(Messages.getString("NewIndicatorWizard.title")); //$NON-NLS-1$
    setForcePreviousAndNextButtons(true);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  public boolean performFinish()
  {
    IChartConfigurer chartConfigurer = (IChartConfigurer)plotter;

    RGB rgb = parameters.getColor(); 
    chartConfigurer.setParameter("color", String.valueOf(rgb.red) + "," + String.valueOf(rgb.green) + "," + String.valueOf(rgb.blue)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    chartConfigurer.setParameter("name", parameters.getPlotterName()); //$NON-NLS-1$

    if (parameters.getControl() != null)
    {
      Control[] c = ((Composite)parameters.getControl()).getChildren();
      for (int i = 0; i < c.length; i++)
      {
        if (c[i].getData() == null || !(c[i].getData() instanceof String))
          continue;
        if (c[i] instanceof Text)
          chartConfigurer.setParameter((String)c[i].getData(), ((Text)c[i]).getText());
        else if (c[i] instanceof Combo)
          chartConfigurer.setParameter((String)c[i].getData(), String.valueOf(((Combo)c[i]).getSelectionIndex()));
        else if (c[i] instanceof Button)
          chartConfigurer.setParameter((String)c[i].getData(), null);
      }
    }
    
    if (view != null)
      view.addOscillator(plotter, zone.getZone());
    
    return true;
  }

  public void open()
  {
    WizardDialog dlg = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), this);
    dlg.open();
  }
  
  public void setChartPlotter(IChartPlotter plotter)
  {
    this.plotter = plotter;
  }
  
  public void setParametersPage(ParametersPage parameters)
  {
    this.parameters = parameters;
  }
  
  public ZonePage getZonePage()
  {
    return zone;
  }
  
  public void setChartView(ChartView view)
  {
    this.view = view;
  }
}
