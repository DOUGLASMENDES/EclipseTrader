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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;


/**
 * @author Marco
 */
public class OscillatorDialog extends TitleAreaDialog implements SelectionListener
{
  private List list;
  private String label = "";
  private String id = "";
  
  public OscillatorDialog()
  {
    super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
  }
  
  /**
   * @see org.eclipse.jface.window.Window#configureShell(Shell)
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Indicators");
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  protected Control createDialogArea(Composite parent)
  {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));
    composite.setLayout(new GridLayout(1, false));
    
    list = new List(composite, SWT.SINGLE|SWT.BORDER);
    GridData gridData = new GridData(GridData.GRAB_VERTICAL|GridData.VERTICAL_ALIGN_FILL|GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL);
    gridData.heightHint = 250;
    list.setLayoutData(gridData);
    list.addSelectionListener(this);
    
    // Add the plugin names to the listbox
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.chartPlotter");
    if (extensionPoint != null)
    {
      IConfigurationElement[] members = extensionPoint.getConfigurationElements();
      for (int i = 0; i < members.length; i++)
      {
        list.add(members[i].getAttribute("label"), i);
        list.setData(String.valueOf(i), members[i].getAttribute("id"));
      }
    }

    return super.createDialogArea(parent);
  }

  public int open()
  {
    create();
    
    setTitle("Select Indicator");
    setMessage("Select the indicator to add to the chart.");
    
    return super.open();
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
    int index = list.getSelectionIndex();
    label = list.getItem(index);
    id = (String)list.getData(String.valueOf(index));
  }

  /**
   * Method to return the id field.<br>
   *
   * @return Returns the id.
   */
  public String getId()
  {
    return id;
  }
  /**
   * Method to return the label field.<br>
   *
   * @return Returns the label.
   */
  public String getLabel()
  {
    return label;
  }
}
