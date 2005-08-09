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
package net.sourceforge.eclipsetrader.directaworld;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import net.sourceforge.eclipsetrader.IExtendedData;
import net.sourceforge.eclipsetrader.RealtimeChartDataProvider;
import sun.misc.BASE64Encoder;

/**
 */
public class SnapshotDataProvider extends RealtimeChartDataProvider
{
  private Timer timer;
  private String userName = "";
  private String password = "";
  private NumberFormat nf = NumberFormat.getInstance();
  private NumberFormat pf = NumberFormat.getInstance();
  private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  
  public SnapshotDataProvider()
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBasicDataProvider#startStreaming()
   */
  public void startStreaming()
  {
    if (connectServer() == true)
    {
      if (timer == null)
      {
        timer = new Timer();
        timer.schedule(new TimerTask() {
          public void run() {
            update();
          }
        }, 2 * 100);
      }
      super.startStreaming();
    }
  }
  
  private boolean connectServer()
  {
    if (userName.length() == 0 || password.length() == 0)
    {
      LoginDialog dlg = new LoginDialog();
      if (dlg.open() != LoginDialog.OK)
        return false;
      userName = dlg.getUserName();
      password = dlg.getPassword();
    }
    
    return true;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBasicDataProvider#stopStreaming()
   */
  public void stopStreaming()
  {
    if (timer != null)
    {
      timer.cancel();
      timer = null;
    }
    
    super.stopStreaming();
  }

  public void update() 
  {
    int i;
    String inputLine;

    nf.setGroupingUsed(true);
    nf.setMinimumFractionDigits(0);
    nf.setMaximumFractionDigits(0);

    pf.setGroupingUsed(true);
    pf.setMinimumFractionDigits(4);
    pf.setMaximumFractionDigits(4);

    try {
      // Legge la pagina contenente gli ultimi prezzi
      String request = "http://registrazioni.directaworld.it/cgi-bin/qta?idx=alfa&modo=t&appear=n";
      for (i = 0; i < getData().length; i++)
        request += "&id" + (i + 1) + "=" + getData()[i].getTicker();
      for (; i < 30; i++)
        request += "&id" + (i + 1) + "=";
      request += "&u=" + userName + "&p=" + password;
      URL web = new URL(request);

      HttpURLConnection.setFollowRedirects(false);
      HttpURLConnection con = (HttpURLConnection)web.openConnection();
      String proxyHost = (String)System.getProperties().get("http.proxyHost");
      String proxyUser = (String)System.getProperties().get("http.proxyUser");
      String proxyPassword = (String)System.getProperties().get("http.proxyPassword");
      if (proxyHost != null && proxyHost.length() != 0 && proxyUser != null && proxyUser.length() != 0 && proxyPassword != null)
      {
        String login = proxyUser + ":" + proxyPassword;
        String encodedLogin = new BASE64Encoder().encodeBuffer(login.getBytes());
        con.setRequestProperty("Proxy-Authorization", "Basic " + encodedLogin.trim());
      }
      con.setAllowUserInteraction(true);
      con.setRequestMethod("GET");
      con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows 98; QuoteTracker-DirectaWorld)");
      con.connect();
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      while ((inputLine = in.readLine()) != null) 
      {
        if (inputLine.indexOf("<!--QT START HERE-->") != -1) 
        {
          while ((inputLine = in.readLine()) != null) 
          {
            if (inputLine.indexOf("<!--QT STOP HERE-->") != -1)
              break;
            parseLine(inputLine);
          }
        }
      }
      in.close();
    } catch (Exception ex) { ex.printStackTrace(); };
    
    // Propaga l'aggiornamento ai listeners.
    fireDataUpdated();

    // Schedula il prossimo aggiornamento.
    int refresh = DirectaWorldPlugin.getDefault().getPreferenceStore().getInt("refresh");
    try {
      if (timer != null)
      {
        timer.schedule(new TimerTask() {
          public void run() {
            update();
          }
        }, refresh * 1000);
      }
    } catch(IllegalStateException e) {};
  }
  
  public void parseLine(String line) throws ParseException
  {
    String[] item = line.split(";");
    
    IExtendedData data = getTicker(item[0]);
    if (data != null)
    {
      // item[1] - Nome
      data.setLastPrice(pf.parse(item[2]).doubleValue());
      // item[3] - Variazione
      data.setVolume(nf.parse(item[4]).intValue());
      try {
        data.setDate(df.parse(item[6] + " " + item[5]));
      } catch(Exception e) {};
      data.setBidPrice(pf.parse(item[7]).doubleValue());
      data.setBidSize(nf.parse(item[8]).intValue());
      data.setAskPrice(pf.parse(item[9]).doubleValue());
      data.setAskSize(nf.parse(item[10]).intValue());
      // item[11] - ???
      data.setOpenPrice(pf.parse(item[12]).doubleValue());
      data.setClosePrice(pf.parse(item[13]).doubleValue());
      data.setLowPrice(pf.parse(item[14]).doubleValue());
      data.setHighPrice(pf.parse(item[15]).doubleValue());
    }
  }

  public boolean checkLogin() 
  {
    boolean ret = false;
    String inputLine;

    try {
      // Legge la pagina contenente gli ultimi prezzi
      String request = "http://registrazioni.directaworld.it/cgi-bin/qta?idx=alfa&modo=t&appear=n&id1=STM";
      request += "&u=" + userName + "&p=" + password;
      URL web = new URL(request);

      HttpURLConnection.setFollowRedirects(false);
      HttpURLConnection con = (HttpURLConnection)web.openConnection();
      String proxyHost = (String)System.getProperties().get("http.proxyHost");
      String proxyUser = (String)System.getProperties().get("http.proxyUser");
      String proxyPassword = (String)System.getProperties().get("http.proxyPassword");
      if (proxyHost != null && proxyHost.length() != 0 && proxyUser != null && proxyUser.length() != 0 && proxyPassword != null)
      {
        String login = proxyUser + ":" + proxyPassword;
        String encodedLogin = new BASE64Encoder().encodeBuffer(login.getBytes());
        con.setRequestProperty("Proxy-Authorization", "Basic " + encodedLogin.trim());
      }
      con.setAllowUserInteraction(true);
      con.setRequestMethod("GET");
      con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows 98; QuoteTracker-DirectaWorld)");
      con.connect();
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      while ((inputLine = in.readLine()) != null) 
      {
        if (inputLine.indexOf("<!--QT START HERE-->") != -1)
          ret = true;
      }
      in.close();
    } catch (Exception ex) { ex.printStackTrace(); };
    
    return ret;
  }
}
