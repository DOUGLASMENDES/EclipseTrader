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
package net.sourceforge.eclipsetrader.ui.views.portfolio;

import java.text.NumberFormat;
import java.text.ParseException;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.internal.views.StockList;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


/**
 * Dialog for selection of the stock item to add or edit.
 * <p></p>
 */
public class PortfolioDialog extends TitleAreaDialog implements ModifyListener
{
  private String symbol = ""; //$NON-NLS-1$
  private String ticker = ""; //$NON-NLS-1$
  private String description = ""; //$NON-NLS-1$
  private int minimumQuantity = 1;
  private int quantity = 0;
  private double paid = 0;
  private Text text1;
  private Text text2;
  private Text text3;
  private Text text4;
  private Text text5;
  private Combo combo;
  private StockList stockList = new StockList();
  private NumberFormat pf = NumberFormat.getInstance();
  private NumberFormat nf = NumberFormat.getInstance();
  
  public PortfolioDialog()
  {
    super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
    
    pf.setGroupingUsed(true);
    pf.setMinimumIntegerDigits(1);
    pf.setMinimumFractionDigits(4);
    pf.setMaximumFractionDigits(4);
    
    nf.setGroupingUsed(true);
    nf.setMinimumIntegerDigits(1);
    nf.setMinimumFractionDigits(0);
    nf.setMaximumFractionDigits(0);
  }
  
  /**
   * @see org.eclipse.jface.window.Window#configureShell(Shell)
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(Messages.getString("PortfolioDialog.4")); //$NON-NLS-1$
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
    group.setText(Messages.getString("PortfolioDialog.5"));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group.setLayout(new GridLayout(2, false));
    
    Label label = new Label(group, SWT.NONE);
    label.setText(Messages.getString("PortfolioDialog.Symbol")); //$NON-NLS-1$
    GridData gridData = new GridData();
    gridData.widthHint = 100;
    label.setLayoutData(gridData);
    text1 = new Text(group, SWT.BORDER);
    text1.addModifyListener(this);
    text1.setText(symbol);
    if (symbol.length() != 0)
    {
      text1.setEditable(false);
      text1.setEnabled(false);
    }
    gridData = new GridData();
    gridData.widthHint = 100;
    text1.setLayoutData(gridData);

    label = new Label(group, SWT.NONE);
    label.setText(Messages.getString("PortfolioDialog.Ticker")); //$NON-NLS-1$
    label.setLayoutData(new GridData());
    text2 = new Text(group, SWT.BORDER);
    text2.setText(ticker);
    if (ticker.length() != 0)
    {
      text2.setEditable(false);
      text2.setEnabled(false);
    }
    text2.addModifyListener(this);
    gridData = new GridData();
    gridData.widthHint = 100;
    text2.setLayoutData(gridData);

    label = new Label(group, SWT.NONE);
    label.setText(Messages.getString("PortfolioDialog.8")); //$NON-NLS-1$
    label.setLayoutData(new GridData());
    combo = new Combo(group, SWT.BORDER);
    loadStocklist();
    combo.setText(description);
    combo.addModifyListener(this);
    gridData = new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL);
    combo.setLayoutData(gridData);
    combo.setVisibleItemCount(20);

    label = new Label(group, SWT.NONE);
    label.setText(Messages.getString("PortfolioDialog.9")); //$NON-NLS-1$
    label.setLayoutData(new GridData());
    text3 = new Text(group, SWT.BORDER);
    text3.setText(nf.format(minimumQuantity));
    text3.addModifyListener(this);
    gridData = new GridData();
    gridData.widthHint = 75;
    text3.setLayoutData(gridData);
    
    group = new Group(composite, SWT.NONE);
    group.setText(Messages.getString("PortfolioDialog.10"));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group.setLayout(new GridLayout(2, false));

    label = new Label(group, SWT.NONE);
    label.setText(Messages.getString("PortfolioDialog.11")); //$NON-NLS-1$
    gridData = new GridData();
    gridData.widthHint = 100;
    label.setLayoutData(gridData);
    text4 = new Text(group, SWT.BORDER);
    text4.setText(nf.format(quantity));
    text4.addModifyListener(this);
    gridData = new GridData();
    gridData.widthHint = 75;
    text4.setLayoutData(gridData);

    label = new Label(group, SWT.NONE);
    label.setText(Messages.getString("PortfolioDialog.12")); //$NON-NLS-1$
    label.setLayoutData(new GridData());
    text5 = new Text(group, SWT.BORDER);
    text5.setText(pf.format(paid));
    text5.addModifyListener(this);
    gridData = new GridData();
    gridData.widthHint = 75;
    text5.setLayoutData(gridData);

    return super.createDialogArea(parent);
  }

  /**
   * Method to return the description field.<br>
   *
   * @return Returns the description.
   */
  public String getDescription()
  {
    return description;
  }
  /**
   * Method to set the description field.<br>
   * 
   * @param description The description to set.
   */
  public void setDescription(String description)
  {
    this.description = description;
  }
  /**
   * Method to return the symbol field.<br>
   *
   * @return Returns the symbol.
   */
  public String getSymbol()
  {
    return symbol;
  }
  /**
   * Method to set the symbol field.<br>
   * 
   * @param symbol The symbol to set.
   */
  public void setSymbol(String symbol)
  {
    this.symbol = symbol;
  }
  /**
   * Method to return the ticker field.<br>
   *
   * @return Returns the ticker.
   */
  public String getTicker()
  {
    return ticker;
  }
  /**
   * Method to set the ticker field.<br>
   * 
   * @param ticker The ticker to set.
   */
  public void setTicker(String ticker)
  {
    this.ticker = ticker;
  }
  public int open()
  {
    create();
    
    setTitle(Messages.getString("PortfolioDialog.13")); //$NON-NLS-1$
    setMessage(Messages.getString("PortfolioDialog.14")); //$NON-NLS-1$
    
    return super.open();
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
   */
  public void modifyText(ModifyEvent e)
  {
    if (e.getSource() == text1)
      symbol = text1.getText();
    else if (e.getSource() == text2)
      ticker = text2.getText();
    else if (e.getSource() == text3)
    {
      try {
        minimumQuantity = nf.parse(text3.getText()).intValue();
      } catch (ParseException e1) {}
    }
    else if (e.getSource() == text4)
    {
      try {
        quantity = nf.parse(text4.getText()).intValue();
      } catch (ParseException e1) {}
    }
    else if (e.getSource() == text5)
    {
      try {
        paid = pf.parse(text5.getText()).doubleValue();
      } catch (ParseException e1) {}
    }
    else if (e.getSource() == combo)
    {
      int index = combo.getSelectionIndex();
      if (index != -1)
      {
        IBasicData data = stockList.getData()[index];
        text1.setText(data.getSymbol());
        symbol = text1.getText();
        text2.setText(data.getTicker());
        ticker = text2.getText();
        text3.setText(nf.format(data.getMinimumQuantity()));
        minimumQuantity = data.getMinimumQuantity();
      }
      description = combo.getText();
    }
  }

  private void loadStocklist()
  {
    IBasicData[] data = stockList.getData();
    for (int i = 0; i < data.length; i++)
      combo.add(data[i].getDescription());
  }
  /**
   * Method to return the paid field.<br>
   *
   * @return Returns the paid.
   */
  public double getPaid()
  {
    return paid;
  }
  /**
   * Method to set the paid field.<br>
   * 
   * @param paid The paid to set.
   */
  public void setPaid(double paid)
  {
    this.paid = paid;
  }
  /**
   * Method to return the quantity field.<br>
   *
   * @return Returns the quantity.
   */
  public int getQuantity()
  {
    return quantity;
  }
  /**
   * Method to set the quantity field.<br>
   * 
   * @param quantity The quantity to set.
   */
  public void setQuantity(int quantity)
  {
    this.quantity = quantity;
  }
  /**
   * Method to return the minimumQuantity field.<br>
   *
   * @return Returns the minimumQuantity.
   */
  public int getMinimumQuantity()
  {
    return minimumQuantity;
  }
  /**
   * Method to set the minimumQuantity field.<br>
   * 
   * @param minimumQuantity The minimumQuantity to set.
   */
  public void setMinimumQuantity(int minimumQuantity)
  {
    this.minimumQuantity = minimumQuantity;
  }
}
