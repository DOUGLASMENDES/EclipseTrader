/*******************************************************************************
 * Copyright (c) 2004 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.charts;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


/**
 * @author Marco
 */
public class ChartParametersDialog extends TitleAreaDialog
{
  public final static int SELECTED_ZONE = 1;
  public final static int NEW_ZONE = 2;
  public static final int NEW_CHART = 1;
  public static final int EDIT_CHART = 2;
  private int type = NEW_CHART;
  protected int position = SELECTED_ZONE;
//  private Text chartName;
  private Group paramGroup;
  private Button selectedZone;
  private Button newZone;
  private ColorSelector colorSelector;
  private IChartConfigurer chartConfigurer;
  
  public ChartParametersDialog(IChartConfigurer chartConfigurer)
  {
    super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
    this.chartConfigurer = chartConfigurer;
  }
  
  /**
   * @see org.eclipse.jface.window.Window#configureShell(Shell)
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Parameters " + chartConfigurer.getName());
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  protected Control createDialogArea(Composite parent)
  {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));
    composite.setLayout(new GridLayout(1, false));
    
    paramGroup = new Group(composite, SWT.NONE);
    paramGroup.setText("Parameters definition");
    paramGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
    paramGroup.setLayout(new GridLayout(2, false));
    
    Label label = new Label(paramGroup, SWT.NONE);
    label.setText("Name");
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    Text text = new Text(paramGroup, SWT.BORDER);
    text.setData("name");
    text.setText(chartConfigurer.getName());
    GridData gridData = new GridData();
    gridData.widthHint = 175;
    text.setLayoutData(gridData);

    // Adds the parameters specific to the given chart
    if (chartConfigurer != null)
      chartConfigurer.createContents(paramGroup);
    
    label = new Label(paramGroup, SWT.NONE);
    label.setText("Color");
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    colorSelector = new ColorSelector(paramGroup);
    colorSelector.getButton().setData("color");
    colorSelector.setColorValue(chartConfigurer.getColorParameter("color"));
    
/*    group = new Group(composite, SWT.NONE);
    group.setText("Parametri grafico");
    group.setLayoutData(new GridData(GridData.FILL_BOTH));
    group.setLayout(new GridLayout(2, false));
    
    Combo combo1 = new Combo(group, SWT.READ_ONLY|SWT.BORDER);
    combo1.add("CAPITALIA");
    combo1.add("Media Mobile (30)");
    combo1.select(0);
    combo1.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    Combo combo2 = new Combo(group, SWT.READ_ONLY|SWT.BORDER);
    combo2.add("Chiusura");
    combo2.add("Apertura");
    combo2.add("Massimo");
    combo2.add("Minimo");
    combo2.select(0);
    gridData = new GridData();
    combo2.setLayoutData(gridData);*/

    if (type == NEW_CHART)
    {
      Group group = new Group(composite, SWT.NONE);
      group.setText("Insert indicator");
      group.setLayoutData(new GridData(GridData.FILL_BOTH));
      group.setLayout(new GridLayout(1, false));
      
      selectedZone = new Button(group, SWT.RADIO);
      selectedZone.setText("On the selected zone");
      selectedZone.setSelection(true);
      newZone = new Button(group, SWT.RADIO);
      newZone.setText("On a new zone");
    }

    return super.createDialogArea(parent);
  }

  /**
   * Override this method to provide custom parameters configuration.
   * <p></p>
   */
//  public abstract void createPartControl(Composite parent);

  /**
   * Open the dialog for a new chart.
   * <p></p>
   */
  public int open()
  {
    create();

    setTitle(chartConfigurer.getName());
    setMessage("Set parameters for " + chartConfigurer.getName());
    
    return super.open();
  }

  /**
   * Open the dialog for editing.
   * <p></p>
   */
  public int openEdit()
  {
    type = EDIT_CHART;
    return open();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  protected void okPressed()
  {
    if (type == NEW_CHART)
    {
      if (selectedZone.getSelection() == true)
        position = SELECTED_ZONE;
      else if (newZone.getSelection() == true)
        position = NEW_ZONE;
    }

    RGB rgb = colorSelector.getColorValue(); 
    chartConfigurer.setParameter("color", String.valueOf(rgb.red) + "," + String.valueOf(rgb.green) + "," + String.valueOf(rgb.blue));
    
    Control[] c = paramGroup.getChildren();
    for (int i = 0; i < c.length; i++)
    {
      if (c[i].getData() == null || !(c[i].getData() instanceof String))
        continue;
      if (c[i] instanceof Text)
        chartConfigurer.setParameter((String)c[i].getData(), ((Text)c[i]).getText());
    }

    super.okPressed();
  }

  /**
   * Method to return the position field.<br>
   *
   * @return Returns the position.
   */
  public int getPosition()
  {
    return position;
  }
  /**
   * Method to set the position field.<br>
   * 
   * @param position The position to set.
   */
  public void setPosition(int position)
  {
    this.position = position;
  }
}
