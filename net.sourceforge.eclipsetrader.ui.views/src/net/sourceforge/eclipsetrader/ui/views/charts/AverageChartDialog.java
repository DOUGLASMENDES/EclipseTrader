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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


/**
 * @author Marco
 */
public class AverageChartDialog extends TitleAreaDialog implements ModifyListener
{
  private List list;
  private String name = "Media Mobile";
  private String period = "30";
  private RGB color = new RGB(0, 0, 255);
  private Text text1;
  private Text text2;
  private ColorSelector colorSelector;
  
  public AverageChartDialog()
  {
    super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
  }
  
  /**
   * @see org.eclipse.jface.window.Window#configureShell(Shell)
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Parametri Media Mobile");
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  protected Control createDialogArea(Composite parent)
  {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));
    composite.setLayout(new GridLayout(1, false));
    
    Group group = new Group(composite, SWT.NONE);
    group.setText("Definizione parametri");
    group.setLayoutData(new GridData(GridData.FILL_BOTH));
    group.setLayout(new GridLayout(2, false));
    
    Label label = new Label(group, SWT.NONE);
    label.setText("Nome");
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    text1 = new Text(group, SWT.BORDER);
    text1.addModifyListener(this);
    text1.setText(name);
    GridData gridData = new GridData();
    gridData.widthHint = 175;
    text1.setLayoutData(gridData);
    
    label = new Label(group, SWT.NONE);
    label.setText("Periodi Selezionati");
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    text2 = new Text(group, SWT.BORDER);
    text2.addModifyListener(this);
    text2.setText(period);
    gridData = new GridData();
    gridData.widthHint = 25;
    text2.setLayoutData(gridData);
    
/*    label = new Label(group, SWT.NONE);
    label.setText("Tipo di Media Mobile");
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    Combo combo = new Combo(group, SWT.READ_ONLY|SWT.BORDER);
    combo.add("Semplice");
    combo.add("Aritmetica");
    combo.select(0);
    gridData = new GridData();
    combo.setLayoutData(gridData);
    
    label = new Label(group, SWT.NONE);
    label.setText("Periodi di shift orizzontale");
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    Text text = new Text(group, SWT.BORDER);
    text.setText("0");
    gridData = new GridData();
    gridData.widthHint = 25;
    text.setLayoutData(gridData);
    
    label = new Label(group, SWT.NONE);
    label.setText("Percentuale di shift verticale");
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    text = new Text(group, SWT.BORDER);
    text.setText("0");
    gridData = new GridData();
    gridData.widthHint = 25;
    text.setLayoutData(gridData);*/
    
    group = new Group(composite, SWT.NONE);
    group.setText("Parametri grafico");
    group.setLayoutData(new GridData(GridData.FILL_BOTH));
    group.setLayout(new GridLayout(2, false));
    
    label = new Label(group, SWT.NONE);
    label.setText("Colore");
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    colorSelector = new ColorSelector(group);
    colorSelector.setColorValue(color);

    return super.createDialogArea(parent);
  }

  public int open()
  {
    create();
    
    setTitle("Media Mobile");
    setMessage("Impostare i parametri per la media mobile.");
    
    int ret = super.open();
    color = colorSelector.getColorValue();
    
    return ret;
  }

  
  /* (non-Javadoc)
   * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
   */
  public void modifyText(ModifyEvent e)
  {
    if (e.getSource() == text1)
      name = text1.getText();
    else if (e.getSource() == text2)
      period = text2.getText();
  }

  /**
   * Method to return the name field.<br>
   *
   * @return Returns the name.
   */
  public String getName()
  {
    return name;
  }
  /**
   * Method to set the name field.<br>
   * 
   * @param name The name to set.
   */
  public void setName(String name)
  {
    this.name = name;
  }
  /**
   * Method to return the period field.<br>
   *
   * @return Returns the period.
   */
  public int getPeriod()
  {
    return Integer.parseInt(period);
  }
  /**
   * Method to set the period field.<br>
   * 
   * @param period The period to set.
   */
  public void setPeriod(int period)
  {
    this.period = String.valueOf(period);
  }

  /**
   * Method to return the color field.<br>
   *
   * @return Returns the color.
   */
  public RGB getColor()
  {
    return color;
  }
  /**
   * Method to set the color field.<br>
   * 
   * @param color The color to set.
   */
  public void setColor(RGB color)
  {
    this.color = color;
  }

}
