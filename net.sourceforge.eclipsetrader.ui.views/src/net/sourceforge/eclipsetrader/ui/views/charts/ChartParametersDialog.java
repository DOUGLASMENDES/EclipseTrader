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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
public abstract class ChartParametersDialog extends TitleAreaDialog implements ModifyListener, SelectionListener
{
  public final static int SELECTED_ZONE = 1;
  public final static int NEW_ZONE = 2;
  public static final int NEW_CHART = 1;
  public static final int EDIT_CHART = 2;
  private int type = NEW_CHART;
  protected String name = "";
  protected RGB color = new RGB(0, 0, 255);
  protected int position = SELECTED_ZONE;
  private Text text1;
  private Button button1;
  private Button button2;
  private ColorSelector colorSelector;
  
  public ChartParametersDialog()
  {
    super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
  }
  
  /**
   * @see org.eclipse.jface.window.Window#configureShell(Shell)
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Parametri " + name);
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

    // Adds the paramters specific to the given chart
    createPartControl(group);
    
    label = new Label(group, SWT.NONE);
    label.setText("Colore");
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    colorSelector = new ColorSelector(group);
    colorSelector.setColorValue(color);
    
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
      group = new Group(composite, SWT.NONE);
      group.setText("Inserire l'oscillatore");
      group.setLayoutData(new GridData(GridData.FILL_BOTH));
      group.setLayout(new GridLayout(1, false));
      
      button1 = new Button(group, SWT.RADIO);
      button1.addSelectionListener(this);
      button1.setText("Nella zona selezionata");
      button1.setSelection(true);
      button2 = new Button(group, SWT.RADIO);
      button2.addSelectionListener(this);
      button2.setText("In una nuova zona");
    }

    return super.createDialogArea(parent);
  }

  /**
   * Override this method to provide customer parameters configuration.
   * <p><p/>
   */
  public abstract void createPartControl(Composite parent);

  public int open()
  {
    create();
    
    setTitle(name);
    setMessage("Impostare i parametri per " + name);
    
    int ret = super.open();
    color = colorSelector.getColorValue();
    
    return ret;
  }

  public int openEdit()
  {
    type = EDIT_CHART;
    return open();
  }

  
  /* (non-Javadoc)
   * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
   */
  public void modifyText(ModifyEvent e)
  {
    if (e.getSource() == text1)
      name = text1.getText();
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
   */
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
   */
  public void widgetSelected(SelectionEvent e)
  {
    if (e.getSource() == button1)
      position = SELECTED_ZONE;
    else if (e.getSource() == button2)
      position = NEW_ZONE;
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
