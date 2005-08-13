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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

/**
 */
public class IndicatorsPage extends WizardPage implements SelectionListener
{
  private List list;

  public IndicatorsPage()
  {
    super(Messages.getString("NewIndicatorWizard.title")); //$NON-NLS-1$
    setTitle(Messages.getString("OscillatorPage.title")); //$NON-NLS-1$
    setDescription(Messages.getString("OscillatorPage.description")); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent)
  {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new GridLayout(1, false));
    composite.forceFocus();
    setControl(composite);
    
    list = new List(composite, SWT.SINGLE|SWT.BORDER|SWT.V_SCROLL);
    GridData gridData = new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL);
    gridData.heightHint = list.getItemHeight() * 15;
    list.setLayoutData(gridData);
    list.addSelectionListener(this);
    
    // Add the plugin names to the listbox
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.chartPlotter"); //$NON-NLS-1$
    if (extensionPoint != null)
    {
      IConfigurationElement[] members = extensionPoint.getConfigurationElements();

      // Creates an arraylist from the members array 
      ArrayList m = new ArrayList();
      for (int i = 0; i < members.length; i++)
        m.add(members[i]);
      
      // Sorts the list
      Collections.sort(m, new Comparator() {
        public int compare(Object arg0, Object arg1)
        {
          if ((arg0 instanceof IConfigurationElement) && (arg1 instanceof IConfigurationElement))
          {
            String s0 = ((IConfigurationElement)arg0).getAttribute("label"); //$NON-NLS-1$
            String s1 = ((IConfigurationElement)arg1).getAttribute("label"); //$NON-NLS-1$
            return s0.compareTo(s1);
          }
          return 0;
        }
      });
      m.toArray(members);

      // Adds the sorted members to the list widget
      for (int i = 0; i < members.length; i++)
      {
        list.add(members[i].getAttribute("label"), i); //$NON-NLS-1$
        list.setData(String.valueOf(i), members[i]);
      }
    }

    setPageComplete(false);
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

    if (index != -1)
    {
      IConfigurationElement member = (IConfigurationElement)list.getData(String.valueOf(index));
      try {
        Object obj = member.createExecutableExtension("class"); //$NON-NLS-1$
        if (obj instanceof IndicatorPlugin)
          ((NewIndicatorWizard)getWizard()).setIndicator((IndicatorPlugin)obj);
      } catch(Exception x) { x.printStackTrace(); };
    }

    setPageComplete(index != -1);
  }
}
