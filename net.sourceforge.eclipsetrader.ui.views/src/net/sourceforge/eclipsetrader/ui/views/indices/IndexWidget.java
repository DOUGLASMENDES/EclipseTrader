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
package net.sourceforge.eclipsetrader.ui.views.indices;

import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 */
public class IndexWidget extends Composite implements PaintListener, IPropertyChangeListener
{
  private String symbol = "";
  private double price = 0;
  private long timeStamp = 0;
  private Label name;
  private Label time;
  private Label value;
  private Label change;
  private Label icon;
  private Composite row1;
  private Composite row2;
  private Composite composite;
  private Color background = new Color(null, 255, 255, 255);
  private Color foreground = new Color(null, 0, 0, 0);
  private Color negativeForeground = new Color(null, 240, 0, 0);
  private Color positiveForeground = new Color(null, 0, 192, 0);

  public IndexWidget(Composite parent, int style)
  {
    super(parent, style);

    // Read the preferences
    IPreferenceStore pref = ViewsPlugin.getDefault().getPreferenceStore();
    foreground = new Color(null, PreferenceConverter.getColor(pref, "index.text_color")); //$NON-NLS-1$
    background = new Color(null, PreferenceConverter.getColor(pref, "index.background")); //$NON-NLS-1$
    negativeForeground = new Color(null, PreferenceConverter.getColor(pref, "index.negative_value_color")); //$NON-NLS-1$
    positiveForeground = new Color(null, PreferenceConverter.getColor(pref, "index.positive_value_color")); //$NON-NLS-1$
    pref.addPropertyChangeListener(this);
    
    GridLayout gridLayout = new GridLayout(1, false);
    gridLayout.marginWidth = 5;
    gridLayout.marginHeight = 3;
    gridLayout.horizontalSpacing = 5;
    gridLayout.verticalSpacing = 2;
    setLayout(gridLayout);
    setBackground(background);
    addPaintListener(this);
    
    row1 = new Composite(this, SWT.NONE);
    gridLayout = new GridLayout(2, false);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.horizontalSpacing = 3;
    gridLayout.verticalSpacing = 0;
    row1.setLayout(gridLayout);
    row1.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
    row1.setBackground(background);
    
    name = new Label(row1, SWT.NONE);
    name.setLayoutData(new GridData());
    name.setForeground(foreground);
    name.setBackground(background);
    
    time = new Label(row1, SWT.NONE);
    time.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
    time.setForeground(foreground);
    time.setBackground(background);
    time.setAlignment(SWT.RIGHT);
    
    row2 = new Composite(this, SWT.NONE);
    gridLayout = new GridLayout(2, false);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.horizontalSpacing = 3;
    gridLayout.verticalSpacing = 0;
    row2.setLayout(gridLayout);
    row2.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
    row2.setBackground(background);
    
    value = new Label(row2, SWT.NONE);
    value.setLayoutData(new GridData());
    FontData[] fontData = value.getFont().getFontData();
    for (int i = 0; i < fontData.length; i++)
      fontData[i].setStyle(fontData[i].getStyle() | SWT.BOLD);
    value.setFont(new Font(value.getDisplay(), fontData));
    value.setForeground(foreground);
    value.setBackground(background);
    
    composite = new Composite(row2, SWT.NONE);
    gridLayout = new GridLayout(2, false);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.horizontalSpacing = 3;
    gridLayout.verticalSpacing = 0;
    composite.setLayout(gridLayout);
    composite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
    composite.setBackground(background);
    
    change = new Label(composite, SWT.NONE);
    change.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
    change.setAlignment(SWT.RIGHT);
    change.setForeground(foreground);
    change.setBackground(background);
    change.setForeground(negativeForeground);
    
    icon = new Label(composite, SWT.NONE);
    icon.setLayoutData(new GridData());
    icon.setBackground(background);
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  public void dispose()
  {
    IPreferenceStore pref = ViewsPlugin.getDefault().getPreferenceStore();
    pref.removePropertyChangeListener(this);
    
    background.dispose();
    foreground.dispose();
    negativeForeground.dispose();
    positiveForeground.dispose();
    
    super.dispose();
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  public void paintControl(PaintEvent e)
  {
    e.gc.setForeground(foreground);
    Rectangle rect = getClientArea();
    e.gc.drawRectangle(0, 0, rect.width - 1, rect.height - 1);
  }
  
  /**
   * Set the widget's values.
   */
  public void setValues(String name, String value, String change, String time, Image image)
  {
    this.name.setText(name);
    this.value.setText(value);
    this.change.setText(change);
    if (change.indexOf("-") != -1)
      this.change.setForeground(negativeForeground);
    else
      this.change.setForeground(positiveForeground);
    this.icon.setImage(image);
    this.time.setText(time);
    this.layout();
  }
  public void setValues(String name, String value, String change, String time)
  {
    this.name.setText(name);
    this.value.setText(value);
    this.change.setText(change);
    if (change.indexOf("-") != -1)
      this.change.setForeground(negativeForeground);
    else
      this.change.setForeground(positiveForeground);
    this.time.setText(time);
    this.layout();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
    if (isDisposed() == true)
      return;
    if (event.getProperty().startsWith("index.") == true)
    {
      background.dispose();
      foreground.dispose();
      negativeForeground.dispose();
      positiveForeground.dispose();
  
      IPreferenceStore pref = ViewsPlugin.getDefault().getPreferenceStore();
      foreground = new Color(null, PreferenceConverter.getColor(pref, "index.text_color")); //$NON-NLS-1$
      background = new Color(null, PreferenceConverter.getColor(pref, "index.background")); //$NON-NLS-1$
      negativeForeground = new Color(null, PreferenceConverter.getColor(pref, "index.negative_value_color")); //$NON-NLS-1$
      positiveForeground = new Color(null, PreferenceConverter.getColor(pref, "index.positive_value_color")); //$NON-NLS-1$
  
      setBackground(background);
      name.setForeground(foreground);
      name.setBackground(background);
      time.setForeground(foreground);
      time.setBackground(background);
      value.setForeground(foreground);
      value.setBackground(background);
      change.setForeground(foreground);
      change.setBackground(background);
      if (change.getText().indexOf("-") != -1)
        change.setForeground(negativeForeground);
      else
        change.setForeground(positiveForeground);
      icon.setBackground(background);
      row1.setBackground(background);
      row2.setBackground(background);
      composite.setBackground(background);
      update();
      redraw();
    }
  }
  
  public void setSymbol(String symbol)
  {
    this.symbol = symbol;
  }
  
  public String getSymbol()
  {
    return symbol;
  }
  
  public void setPrice(double price)
  {
    this.price = price;
  }
  
  public double getPrice()
  {
    return price;
  }
  
  public void setTimeStamp(long timeStamp)
  {
    this.timeStamp = timeStamp;
  }
  
  public long getTimeStamp()
  {
    return timeStamp;
  }
}
