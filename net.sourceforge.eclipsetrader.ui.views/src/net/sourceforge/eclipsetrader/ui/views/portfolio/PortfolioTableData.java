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


/**
 */
public class PortfolioTableData
{
  private static int VARIANCE_DELAY = 15000;
  private double lastPrice = 0;
  private double lastPriceVariance = 0;
  private long lastPriceTimestamp = 0;
  private double bidPrice = 0;
  private double bidPriceVariance = 0;
  private long bidPriceTimestamp = 0;
  public int bidSize = 0;
  public double bidSizeVariance = 0;
  private long bidSizeTimestamp = 0;
  public double askPrice = 0;
  public double askPriceVariance = 0;
  private long askPriceTimestamp = 0;
  public int askSize = 0;
  public double askSizeVariance = 0;
  private long askSizeTimestamp = 0;

  public double getLastPrice()
  {
    return lastPrice;
  }
  public void setLastPrice(double lastPrice)
  {
    if (lastPrice != 0 && this.lastPrice != 0 && (this.lastPrice != lastPrice || (System.currentTimeMillis() - lastPriceTimestamp) >= VARIANCE_DELAY))
    {
      this.lastPriceVariance = lastPrice - this.lastPrice;
      this.lastPriceTimestamp = System.currentTimeMillis();
    }
    this.lastPrice = lastPrice;
  }
  public double getLastPriceVariance()
  {
    if ((System.currentTimeMillis() - lastPriceTimestamp) >= VARIANCE_DELAY)
      lastPriceVariance = 0;
    return lastPriceVariance;
  }

  public double getBidPrice()
  {
    return bidPrice;
  }
  public void setBidPrice(double bidPrice)
  {
    if (bidPrice != 0 && this.bidPrice != 0 && (this.bidPrice != bidPrice || (System.currentTimeMillis() - bidPriceTimestamp) >= VARIANCE_DELAY))
    {
      this.bidPriceVariance = bidPrice - this.bidPrice;
      this.bidPriceTimestamp = System.currentTimeMillis();
    }
    this.bidPrice = bidPrice;
  }
  public double getBidPriceVariance()
  {
    if ((System.currentTimeMillis() - bidPriceTimestamp) >= VARIANCE_DELAY)
      bidPriceVariance = 0;
    return bidPriceVariance;
  }

  public int getBidSize()
  {
    return bidSize;
  }
  public void setBidSize(int bidSize)
  {
    if (bidSize != 0 && this.bidSize != 0 && (this.bidSize != bidSize || (System.currentTimeMillis() - bidSizeTimestamp) >= VARIANCE_DELAY))
    {
      this.bidSizeVariance = bidSize - this.bidSize;
      this.bidSizeTimestamp = System.currentTimeMillis();
    }
    this.bidSize = bidSize;
  }
  public double getBidSizeVariance()
  {
    if ((System.currentTimeMillis() - bidSizeTimestamp) >= VARIANCE_DELAY)
      bidSizeVariance = 0;
    return bidSizeVariance;
  }
  
  public double getAskPrice()
  {
    return askPrice;
  }
  public void setAskPrice(double askPrice)
  {
    if (askPrice != 0 && this.askPrice != 0 && (this.askPrice != askPrice || (System.currentTimeMillis() - askPriceTimestamp) >= VARIANCE_DELAY))
    {
      this.askPriceVariance = askPrice - this.askPrice;
      this.askPriceTimestamp = System.currentTimeMillis();
    }
    this.askPrice = askPrice;
  }
  public double getAskPriceVariance()
  {
    if ((System.currentTimeMillis() - askPriceTimestamp) >= VARIANCE_DELAY)
      askPriceVariance = 0;
    return askPriceVariance;
  }

  public int getAskSize()
  {
    return askSize;
  }
  public void setAskSize(int askSize)
  {
    if (askSize != 0 && this.askSize != 0 && (this.askSize != askSize || (System.currentTimeMillis() - askSizeTimestamp) >= VARIANCE_DELAY))
    {
      this.askSizeVariance = askSize - this.askSize;
      this.askSizeTimestamp = System.currentTimeMillis();
    }
    this.askSize = askSize;
  }
  public double getAskSizeVariance()
  {
    if ((System.currentTimeMillis() - askSizeTimestamp) >= VARIANCE_DELAY)
      askSizeVariance = 0;
    return askSizeVariance;
  }
}
