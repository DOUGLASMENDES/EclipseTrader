/*
 * Created on 5-feb-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.eclipsetrader;


/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IAlertSource
{
  public void addAlert(IAlertData alertData);
  public void removeAlert(IAlertData alertData);
  public void removeAllAlerts();

  public IAlertData[] getAlerts();
  
  public void addAlertListener(IAlertListener listener);
  public void removeAlertListener(IAlertListener listener);
}