/*******************************************************************************
 * Copyright (c) 2004-2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.internal.views;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.text.NumberFormat;
import java.util.Vector;

import net.sourceforge.eclipsetrader.AlertData;
import net.sourceforge.eclipsetrader.IAlertData;
import net.sourceforge.eclipsetrader.IAlertSource;
import net.sourceforge.eclipsetrader.IExtendedData;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * This dialog allws the setup of alerts for a specific stock item.
 * <p></p>
 */
public class AlertsDialog extends TitleAreaDialog implements SelectionListener
{
  private IExtendedData data;
  private Combo item;
  private Combo condition;
  private List list;
  private Text price;
  private Text soundFile;
  private Button hilight;
  private Button playSound;
  private ColorSelector hilightColor;
  private NumberFormat pf = NumberFormat.getInstance();
  private Vector alerts = new Vector();
  
  public AlertsDialog()
  {
    super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
    
    pf.setGroupingUsed(true);
    pf.setMinimumIntegerDigits(1);
    pf.setMinimumFractionDigits(4);
    pf.setMaximumFractionDigits(4);
  }

  /**
   * @see org.eclipse.jface.window.Window#configureShell(Shell)
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Alerts");
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.window.Window#open()
   */
  public int open()
  {
    create();
    
    setTitle("Alerts");
    setMessage("Setup alerts for " + data.getDescription());
    
    return super.open();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  protected Control createDialogArea(Composite parent)
  {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));
    composite.setLayout(new GridLayout(2, false));

    createAlertsGroup(composite);

    composite = new Composite(composite, SWT.NONE);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout gridLayout = new GridLayout(1, false);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    composite.setLayout(gridLayout);
    
    createConditionsGroup(composite);
    createActionsGroup(composite);

    return super.createDialogArea(parent);
  }
  
  private void createAlertsGroup(Composite parent)
  {
    Group group = new Group(parent, SWT.NONE);
    group.setText("Alerts");
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group.setLayout(new GridLayout(1, false));

    list = new List(group, SWT.BORDER);
    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.heightHint = 150;
    list.setLayoutData(gridData);
    IAlertData[] array = ((IAlertSource)data).getAlerts();
    for (int i = 0; i < array.length; i++)
    {
      list.add(array[i].toString());
      alerts.add(new AlertData(array[i]));
    }
    list.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e)
      {
      }
      public void widgetSelected(SelectionEvent e)
      {
        if (list.getSelectionIndex() != -1)
        {
          IAlertData ad = (AlertData)alerts.elementAt(list.getSelectionIndex());
          item.select(ad.getItem());
          condition.select(ad.getCondition());
          price.setText(pf.format(ad.getPrice()));
          hilight.setSelection(ad.isHilight());
          hilightColor.setColorValue(ad.getHilightColor());
          playSound.setSelection(ad.isPlaySound());
          soundFile.setText(ad.getSoundFile());
        }        
      }
    });
        
    Composite comp = new Composite(group, SWT.NONE);
    gridData = new GridData();
    gridData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_CENTER;
    comp.setLayoutData(gridData);
    RowLayout rowLayout = new RowLayout();
    comp.setLayout(rowLayout);

    Button button = new Button(comp, SWT.PUSH);
    button.setText("Add / Change");
    button.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e)
      {
      }
      public void widgetSelected(SelectionEvent e)
      {
        AlertData ad = new AlertData();
        if (list.getSelectionIndex() != -1)
          ad = (AlertData)alerts.elementAt(list.getSelectionIndex());

        ad.setTrigger(false);
        ad.setAcknowledge(false);
        ad.setItem(item.getSelectionIndex());
        ad.setCondition(condition.getSelectionIndex());
        try {
          ad.setPrice(pf.parse(price.getText()).doubleValue());
        } catch(Exception ex) {};
        ad.setHilight(hilight.getSelection());
        ad.setHilightColor(hilightColor.getColorValue());
        ad.setPlaySound(playSound.getSelection());
        ad.setSoundFile(soundFile.getText());
        
        if (list.getSelectionIndex() != -1)
          list.setItem(list.getSelectionIndex(), ad.toString());
        else
        {
          list.add(ad.toString());
          alerts.add(ad);
        }
        list.deselectAll();
      }
    });

    button = new Button(comp, SWT.PUSH);
    button.setText("Delete");
    button.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e)
      {
      }
      public void widgetSelected(SelectionEvent e)
      {
        int index = list.getSelectionIndex(); 
        if (index != -1)
        {
          list.remove(index);
          list.setSelection(-1);
          alerts.remove(index);
        }
      }
    });
    
    button = new Button(comp, SWT.PUSH);
    button.setText("Delete All");
    button.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e)
      {
      }
      public void widgetSelected(SelectionEvent e)
      {
        list.removeAll();
        list.setSelection(-1);
        alerts.removeAllElements();
      }
    });
  }
  
  private void createConditionsGroup(Composite parent)
  {
    Group group = new Group(parent, SWT.NONE);
    group.setText("Conditions");
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group.setLayout(new GridLayout(1, false));
    GridData gridData = new GridData();
    group.setLayoutData(gridData);
    
    Composite comp = new Composite(group, SWT.NONE);
    comp.setLayoutData(new GridData());
    GridLayout gridLayout = new GridLayout(3, false);
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    comp.setLayout(gridLayout);
    
    item = new Combo(comp, SWT.DROP_DOWN|SWT.READ_ONLY);
    item.add("Last Price");
    item.add("Bid");
    item.add("Ask");
    item.select(0);
    gridData = new GridData();
    gridData.widthHint = 100;
    item.setLayoutData(gridData);
    
    condition = new Combo(comp, SWT.DROP_DOWN|SWT.READ_ONLY);
    condition.add("Falls Below");
    condition.add("Raise Above");
    condition.select(0);
    gridData = new GridData();
    gridData.widthHint = 100;
    condition.setLayoutData(gridData);

    price = new Text(comp, SWT.BORDER);
    gridData = new GridData();
    gridData.widthHint = 55;
    price.setLayoutData(gridData);
    price.setText(pf.format(data.getLastPrice()));
  }
  
  private void createActionsGroup(Composite parent)
  {
    Group group = new Group(parent, SWT.NONE);
    group.setText("Actions");
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group.setLayout(new GridLayout(1, false));
    
    Composite comp = new Composite(group, SWT.NONE);
    comp.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout gridLayout = new GridLayout(2, false);
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    comp.setLayout(gridLayout);

    hilight = new Button(comp, SWT.CHECK);
    hilight.setText("Hilight row with color");
    hilight.setSelection(true);
    hilightColor = new ColorSelector(comp);
    hilightColor.setColorValue(new RGB(255, 0, 0));
    
    comp = new Composite(group, SWT.NONE);
    comp.setLayoutData(new GridData(GridData.FILL_BOTH));
    gridLayout = new GridLayout(4, false);
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    comp.setLayout(gridLayout);
    
    playSound = new Button(comp, SWT.CHECK);
    playSound.setText("Play sound");
    
    soundFile = new Text(comp, SWT.BORDER);
    soundFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    Button button = new Button(comp, SWT.PUSH);
    button.setText("...");
    button.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e)
      {
        FileDialog dlg = new FileDialog(((Button)e.widget).getShell(), SWT.OPEN);
        String s = dlg.open();
        if (s != null)
          soundFile.setText(s);
      }
    });
    
    button = new Button(comp, SWT.PUSH);
    button.setText("Play");
    button.addSelectionListener(new SelectionAdapter() {
      AudioClip clip = null;
      public void widgetSelected(SelectionEvent e)
      {
        if (clip != null)
          clip.stop();
        clip = null;
        
        File file = new File(soundFile.getText());
        System.out.println("Play " + soundFile.getText());
        try {
          clip = Applet.newAudioClip(file.toURL());
          clip.play();
        } catch(Exception ex) {
          ex.printStackTrace();
        }
      }
    });
  }
  
  public void setData(IExtendedData data)
  {
    this.data = data;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  protected void okPressed()
  {
    IAlertSource as = (IAlertSource)data;
    as.removeAllAlerts();
    for (int i = 0; i < alerts.size(); i++)
      as.addAlert((IAlertData)alerts.elementAt(i));
    super.okPressed();
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
  }
}
