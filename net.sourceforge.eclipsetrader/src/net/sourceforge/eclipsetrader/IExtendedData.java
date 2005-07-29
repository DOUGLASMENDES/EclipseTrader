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
package net.sourceforge.eclipsetrader;

import java.util.Date;

/**
 * Interface to implement an extended portfolio data.<br>
 * Extended data includes descriptions, prices, quantity, exchange time, and others.
 * 
 * @author Marco Maccaferri - 14/08&2004
 */
public interface IExtendedData extends IBasicData
{

  public abstract double getLastPrice();

  public abstract void setLastPrice(double lastPrice);

  public abstract double getBidPrice();

  public abstract void setBidPrice(double bidPrice);

  public abstract int getBidSize();

  public abstract void setBidSize(int bidSize);

  public abstract double getAskPrice();

  public abstract void setAskPrice(double askPrice);

  public abstract int getAskSize();

  public abstract void setAskSize(int askSize);

  public abstract double getOpenPrice();

  public abstract void setOpenPrice(double openPrice);

  public abstract double getHighPrice();

  public abstract void setHighPrice(double highPrice);

  public abstract double getLowPrice();

  public abstract void setLowPrice(double lowPrice);

  public abstract double getClosePrice();

  public abstract void setClosePrice(double closePrice);

  public abstract double getChange();

  public abstract double getPaid();

  public abstract void setPaid(double paid);

  public abstract int getOwnedQuantity();

  public abstract void setOwnedQuantity(int quantity);

  public abstract int getVolume();

  public abstract void setVolume(int volume);

  public abstract Date getDate();

  public abstract void setDate(Date date);
}