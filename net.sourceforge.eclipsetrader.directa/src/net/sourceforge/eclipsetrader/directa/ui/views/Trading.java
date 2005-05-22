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
package net.sourceforge.eclipsetrader.directa.ui.views;

import java.text.NumberFormat;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.directa.DirectaPlugin;
import net.sourceforge.eclipsetrader.directa.internal.IStreamerEventReceiver;
import net.sourceforge.eclipsetrader.directa.internal.Streamer;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;


public class Trading extends ViewPart implements SelectionListener, IStreamerEventReceiver, IPropertyChangeListener
{
  private Composite entryTable;
  private Text symbol;
  private Text buy;
  private Text sell;
  private Text price;
  private Combo marketPhase;
  private Button valid30Days;
  private NumberFormat pf = NumberFormat.getInstance();
  private Color foreground = new Color(null, 0, 0, 0);
  private Color background = new Color(null, 222, 253, 254);
  private Color valForeground = new Color(null, 192, 0, 0);
  private Label label1, label2, label3, label4, label5, label6, label7;

  public Trading()
  {
    pf.setGroupingUsed(true);
    pf.setMinimumIntegerDigits(1);
    pf.setMinimumFractionDigits(4);
    pf.setMaximumFractionDigits(4);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent)
  {
    IPreferenceStore pref = DirectaPlugin.getDefault().getPreferenceStore();
    pref.addPropertyChangeListener(this);

    foreground = new Color(null, PreferenceConverter.getColor(pref, "trading.text_color")); //$NON-NLS-1$
    valForeground = new Color(null, PreferenceConverter.getColor(pref, "trading.values_color")); //$NON-NLS-1$
    background = new Color(null, PreferenceConverter.getColor(pref, "trading.background_color")); //$NON-NLS-1$

    entryTable = new Composite(parent, SWT.NULL);
    entryTable.setLayoutData(new GridData(GridData.FILL_VERTICAL));
    entryTable.setLayout(new GridLayout(8, false));

    DropTarget target = new DropTarget(entryTable, DND.DROP_COPY);
    final TextTransfer textTransfer = TextTransfer.getInstance();
    Transfer[] types = new Transfer[] { textTransfer };
    target.setTransfer(types);
    target.addDropListener(new DropTargetListener() {
      public void dragEnter(DropTargetEvent event)
      {
        event.detail = DND.DROP_COPY;
      }
      public void dragOver(DropTargetEvent event)
      {
        event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
      }
      public void dragOperationChanged(DropTargetEvent event)
      {
      }
      public void dragLeave(DropTargetEvent event)
      {
      }
      public void dropAccept(DropTargetEvent event)
      {
      }
      public void drop(DropTargetEvent event)
      {
        String[] item = ((String)event.data).split(";");
        symbol.setText(item[2]);
        if (item[0].equalsIgnoreCase("B") == true)
        {
          buy.setText(item[3]);
          sell.setText("");
        }
        else
        {
          buy.setText("");
          sell.setText(item[3]);
        }
        price.setText(pf.format(Double.parseDouble(item[4])));
      }
    });

    // Prima riga
    Label label = new Label(entryTable, SWT.NONE);
    label.setText("Simbolo");
    label.setLayoutData(new GridData());
    symbol = new Text(entryTable, SWT.BORDER);
    GridData gridData = new GridData();
    gridData.widthHint = 50;
    symbol.setLayoutData(gridData);

    label = new Label(entryTable, SWT.NONE);
    label.setText("Compra");
    label.setLayoutData(new GridData());
    buy = new Text(entryTable, SWT.BORDER);
    gridData = new GridData();
    gridData.widthHint = 50;
    buy.setLayoutData(gridData);

    label = new Label(entryTable, SWT.NONE);
    label.setText("");
    label.setLayoutData(new GridData());

    valid30Days = new Button(entryTable, SWT.CHECK);
    valid30Days.setText("Valido per 30 giorni");
    valid30Days.setLayoutData(new GridData());

    label = new Label(entryTable, SWT.NONE);
    label.setText("Fase");
    label.setLayoutData(new GridData());
    marketPhase = new Combo(entryTable, SWT.BORDER|SWT.READ_ONLY);
    marketPhase.add("immed.");
    marketPhase.add("MTA");
    marketPhase.add("clos-MTA");
    marketPhase.add("After Hours");
    marketPhase.add("open//");
    marketPhase.setText(marketPhase.getItem(0));
    marketPhase.setLayoutData(new GridData());
    // Prima riga

    // Seconda riga
    label = new Label(entryTable, SWT.NONE);
    label.setText("");
    gridData = new GridData();
    gridData.horizontalSpan = 2;
    label.setLayoutData(gridData);

    label = new Label(entryTable, SWT.NONE);
    label.setText("Vendi");
    label.setLayoutData(new GridData());
    sell = new Text(entryTable, SWT.BORDER);
    gridData = new GridData();
    gridData.widthHint = 50;
    sell.setLayoutData(gridData);

    label = new Label(entryTable, SWT.NONE);
    label.setText("Prezzo");
    label.setLayoutData(new GridData());
    price = new Text(entryTable, SWT.BORDER);
    gridData = new GridData();
    gridData.widthHint = 50;
    price.setLayoutData(gridData);
    price.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent event)
      {
        if (event.keyCode == SWT.ARROW_UP)
        {
          try {
            double value = pf.parse(price.getText()).doubleValue();
            if (value != 0)
            {
              value += getPriceTick(value);
              price.setText(pf.format(value));
            }
          } catch(Exception e) {};
        }
        else if (event.keyCode == SWT.ARROW_DOWN)
        {
          try {
            double value = pf.parse(price.getText()).doubleValue();
            if (value != 0)
            {
              value -= getPriceTick(value);
              price.setText(pf.format(value));
            }
          } catch(Exception e) {};
        }
      }
      public void keyReleased(KeyEvent event)
      {
      }
    });

    Button button = new Button(entryTable, SWT.NONE);
    button.setText("Immetti Borsa");
    button.setData("sendOrder");
    button.addSelectionListener(this);
    gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    gridData.horizontalSpan = 2;
    button.setLayoutData(gridData);
    // Seconda riga

    Composite info = new Composite(entryTable, SWT.NONE);
    gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    gridData.horizontalSpan = 8;
    info.setLayoutData(gridData);
    GridLayout gridLayout = new GridLayout(8, false);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    info.setLayout(gridLayout);
    label = new Label(info, SWT.NONE);
    label.setText("Liq. Tot:");
    label.setLayoutData(new GridData());
    label1 = new Label(info, SWT.RIGHT);
    label1.setText("0 (Lit. 0)");
    label1.setLayoutData(new GridData());
    label = new Label(info, SWT.NONE);
    label.setText("Liq. Az:");
    label.setLayoutData(new GridData());
    label2 = new Label(info, SWT.RIGHT);
    label2.setText("0");
    label2.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    label = new Label(info, SWT.NONE);
    label.setText("Val. Az:");
    label.setLayoutData(new GridData());
    label3 = new Label(info, SWT.RIGHT);
    label3.setText("0");
    label3.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    label = new Label(info, SWT.NONE);
    label.setText("Disp. Az:");
    label.setLayoutData(new GridData());
    label4 = new Label(info, SWT.RIGHT);
    label4.setText("0");
    label4.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

    label = new Label(info, SWT.NONE);
    label.setText("");
    label.setLayoutData(new GridData());
    label = new Label(info, SWT.NONE);
    label.setText("");
    label.setLayoutData(new GridData());
    label = new Label(info, SWT.NONE);
    label.setText("Liq. Der:");
    label.setLayoutData(new GridData());
    label5 = new Label(info, SWT.RIGHT);
    label5.setText("0");
    label5.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    label = new Label(info, SWT.NONE);
    label.setText("Val. Der:");
    label.setLayoutData(new GridData());
    label6 = new Label(info, SWT.RIGHT);
    label6.setText("0");
    label6.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    label = new Label(info, SWT.NONE);
    label.setText("Disp. Der:");
    label.setLayoutData(new GridData());
    label7 = new Label(info, SWT.RIGHT);
    label7.setText("0");
    label7.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

    DropTarget dropSell = new DropTarget(sell, DND.DROP_COPY);
    dropSell.setTransfer(types);
    dropSell.addDropListener(new DropTargetListener() {
      public void dragEnter(DropTargetEvent event)
      {
        event.detail = DND.DROP_COPY;
      }
      public void dragOver(DropTargetEvent event)
      {
        event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
      }
      public void dragOperationChanged(DropTargetEvent event)
      {
      }
      public void dragLeave(DropTargetEvent event)
      {
      }
      public void dropAccept(DropTargetEvent event)
      {
      }
      public void drop(DropTargetEvent event)
      {
        String[] item = ((String)event.data).split(";");
        symbol.setText(item[2]);
        buy.setText("");
        sell.setText(item[3]);
        price.setText(pf.format(Double.parseDouble(item[4])));
      }
    });

    DropTarget dropBuy = new DropTarget(buy, DND.DROP_COPY);
    dropBuy.setTransfer(types);
    dropBuy.addDropListener(new DropTargetListener() {
      public void dragEnter(DropTargetEvent event)
      {
        event.detail = DND.DROP_COPY;
      }
      public void dragOver(DropTargetEvent event)
      {
        event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
      }
      public void dragOperationChanged(DropTargetEvent event)
      {
      }
      public void dragLeave(DropTargetEvent event)
      {
      }
      public void dropAccept(DropTargetEvent event)
      {
      }
      public void drop(DropTargetEvent event)
      {
        String[] item = ((String)event.data).split(";");
        symbol.setText(item[2]);
        buy.setText(item[3]);
        sell.setText("");
        price.setText(pf.format(Double.parseDouble(item[4])));
      }
    });

    changeBackground(entryTable);
    changeForeground(entryTable);
    label1.setForeground(valForeground);
    label2.setForeground(valForeground);
    label3.setForeground(valForeground);
    label4.setForeground(valForeground);
    label5.setForeground(valForeground);
    label6.setForeground(valForeground);
    label7.setForeground(valForeground);

    orderStatusChanged();
    Streamer.getInstance().addEventReceiver(this);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose()
  {
    IPreferenceStore pref = DirectaPlugin.getDefault().getPreferenceStore();
    pref.removePropertyChangeListener(this);

    Streamer.getInstance().removeEventReceiver(this);

    super.dispose();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus()
  {
    symbol.setFocus();
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.directa.internal.IStreamerEventReceiver#dataUpdated()
   */
  public void dataUpdated()
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.directa.internal.IStreamerEventReceiver#dataUpdated(net.sourceforge.eclipsetrader.IBasicData)
   */
  public void dataUpdated(IBasicData data)
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.directa.internal.IStreamerEventReceiver#orderStatusChanged()
   */
  public void orderStatusChanged()
  {
    System.out.println("Trading: orderStatusChanged");
    NumberFormat nf = NumberFormat.getInstance();
    nf.setGroupingUsed(true);
    nf.setMinimumIntegerDigits(1);
    nf.setMaximumFractionDigits(0);
    nf.setMinimumFractionDigits(0);

    Streamer streamer = Streamer.getInstance();
    label1.setText(nf.format(streamer.getDisponibile()) + " (Lit. " + nf.format(streamer.getDisponibile() * 1936.27) + ")");
    label2.setText(nf.format(streamer.getLiquiditaAzioni()));
    label3.setText(nf.format(streamer.getValoreAzioni()));
    label4.setText(nf.format(streamer.getDisponibileAzioni()));
    label1.getParent().layout();
  }

  public void widgetSelected(SelectionEvent e)
  {
    if (e.getSource() instanceof Button)
    {
      Button b = (Button)e.getSource();
      String action = (String)b.getData();

      if (action.equalsIgnoreCase("sendOrder") == true)
      {
        boolean valid = valid30Days.getSelection();
        String phase = "";
        switch(marketPhase.getSelectionIndex())
        {
          case 0: // immed.
            phase = "1";
            break;
          case 1: // MTA
            phase = "2";
            break;
          case 2: // clos-MTA
            phase = "4";
            break;
          case 3: // After-hours
            phase = "5";
            break;
          case 4: // open
            phase = "7";
            break;
        }
        Streamer streamer = Streamer.getInstance();
        if (streamer.isLoggedIn() == false)
        {
          if (DirectaPlugin.connectServer() == false)
            return;
        }
        streamer.sendOrder(symbol.getText(), buy.getText(), sell.getText(), price.getText(), valid, phase);
      }
    }
  }

  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  private double getPriceTick(double price)
  {
    if (price <= 0.25)
      return 0.0001;
    else if (price <= 1)
      return 0.0005;
    else if (price <= 2)
      return 0.0010;
    else if (price <= 5)
      return 0.0025;
    else if (price <= 10)
      return 0.0050;
    else
      return 0.0100;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
    String property = event.getProperty();
    IPreferenceStore pref = DirectaPlugin.getDefault().getPreferenceStore();
    if (property.equalsIgnoreCase("trading.text_color") == true)
    {
      foreground = new Color(null, PreferenceConverter.getColor(pref, "trading.text_color")); //$NON-NLS-1$
      changeForeground(entryTable);
    }
    else if (property.equalsIgnoreCase("trading.values_color") == true)
    {
      valForeground = new Color(null, PreferenceConverter.getColor(pref, "trading.values_color")); //$NON-NLS-1$
      label1.setForeground(valForeground);
      label2.setForeground(valForeground);
      label3.setForeground(valForeground);
      label4.setForeground(valForeground);
      label5.setForeground(valForeground);
      label6.setForeground(valForeground);
      label7.setForeground(valForeground);
    }
    else if (property.equalsIgnoreCase("trading.background_color") == true)
    {
      background = new Color(null, PreferenceConverter.getColor(pref, "trading.background_color")); //$NON-NLS-1$
      changeBackground(entryTable);
    }
  }

  private void changeBackground(Composite composite)
  {
    composite.setBackground(background);
    Control[] controls = composite.getChildren();
    for (int i = 0; i < controls.length; i++)
    {
      if (controls[i] instanceof Text || controls[i] instanceof Combo || (controls[i] instanceof Button && controls[i] != valid30Days))
        continue;
      controls[i].setBackground(background);
      if (controls[i] instanceof Composite)
        changeBackground((Composite)controls[i]);
    }
  }

  private void changeForeground(Composite composite)
  {
    composite.setForeground(foreground);
    Control[] controls = composite.getChildren();
    for (int i = 0; i < controls.length; i++)
    {
      if (controls[i] instanceof Text)
        continue;
      if (controls[i] == label1 || controls[i] == label2 || controls[i] == label3 || controls[i] == label4 || controls[i] == label5 || controls[i] == label6 || controls[i] == label7)
        continue;
      controls[i].setForeground(foreground);
      if (controls[i] instanceof Composite)
        changeForeground((Composite)controls[i]);
    }
  }
}
