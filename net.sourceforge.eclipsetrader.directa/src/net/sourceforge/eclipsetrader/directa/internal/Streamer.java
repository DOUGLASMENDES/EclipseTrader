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
package net.sourceforge.eclipsetrader.directa.internal;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.Inflater;

import org.eclipse.jface.preference.IPreferenceStore;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IExtendedData;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.directa.BookDataProvider;
import net.sourceforge.eclipsetrader.directa.DirectaPlugin;
import net.sourceforge.eclipsetrader.internal.ChartData;
import traderlink.small.PushClass1_3.Convert;
import traderlink.small.PushClass1_3.CreaMsg;
import traderlink.small.PushClass1_3.Read_DATA_MSG;
import traderlink.small.PushClass1_3.TipiRecord;
import traderlink.small.PushClass1_3._ASTA;
import traderlink.small.PushClass1_3._ASTACHIUSURA;
import traderlink.small.PushClass1_3._BIDASK;
import traderlink.small.PushClass1_3._BOOK;
import traderlink.small.PushClass1_3._DATA_MSG;
import traderlink.small.PushClass1_3._ERROR_MSG;
import traderlink.small.PushClass1_3._HEADER;
import traderlink.small.PushClass1_3._PRICE;
import traderlink.small.bookclass.ConstMpush;
import traderlink.small.bookclass.GetURL;
import traderlink.small.bookclass.Load;


/**
 */
public class Streamer implements Runnable
{
  private static Streamer streamer;
  private String userName = "";
  private String password = "";
  private boolean loggedIn = false;
  private static String prt = "";
  private static String urt = "";
  private static String user = "";
  private String cookie = "";
  private String transactServer = "directatrading.com";
  private String connectMethod = "http";
  private String streamingServer = "213.92.13.59";
  private String backfillServer = "213.92.13.15";
  private int streamingPort = 8002;
  private String streamingVersion = "1.00";
  private Socket socket;
  private OutputStream os;
  private DataInputStream is;
  private SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
  private SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
  private Calendar today = Calendar.getInstance();
  private boolean runThread = false;
  private double dispAz = 0;
  private double liqAz = 0;
  private double valAz = 0;
  private double dispDer = 0;
  private double liqDer = 0;
  private double valDer = 0;
  private Vector monitorSymbol = new Vector();
  private OrderData[] orderData = new OrderData[0];
  private Vector eventReceivers = new Vector();
  
  private Streamer()
  {
    streamer = this;
    IPreferenceStore ps = DirectaPlugin.getDefault().getPreferenceStore();
    transactServer = ps.getString("transact.server");
    streamingServer = ps.getString("streaming.server");
    backfillServer = ps.getString("backfill.server");
  }
  
  public static Streamer getInstance()
  {
    if (streamer == null)
      streamer = new Streamer();
    return streamer;
  }

  public static String getUSER()
  {
    return user;
  }
  
  /**
   * Return the user-code for the realtime server.
   * <p></p>
   * @return Realtime server user-code.
   */
  public static String getURT()
  {
    return urt;
  }
  
  /**
   * Return the password for the realtime server.
   * <p></p>
   * @return Realtime server password.
   */
  public static String getPRT()
  {
    return prt;
  }
  
  public void addEventReceiver(IStreamerEventReceiver eventReceiver)
  {
    if (eventReceivers.contains(eventReceiver) == false)
      eventReceivers.add(eventReceiver);
  }
  
  public void removeEventReceiver(IStreamerEventReceiver eventReceiver)
  {
    eventReceivers.remove(eventReceiver);
  }
  
  private void fireDataUpdated()
  {
    Enumeration e = eventReceivers.elements();
    while(e.hasMoreElements() == true)
      ((IStreamerEventReceiver)e.nextElement()).dataUpdated();
  }
  
  private void fireDataUpdated(IBasicData data)
  {
    Enumeration e = eventReceivers.elements();
    while(e.hasMoreElements() == true)
      ((IStreamerEventReceiver)e.nextElement()).dataUpdated(data);
  }
  
  private void fireOrderStatusChanged()
  {
    Enumeration e = eventReceivers.elements();
    while(e.hasMoreElements() == true)
      ((IStreamerEventReceiver)e.nextElement()).orderStatusChanged();
  }

  public boolean login(String userName, String password)
  {
    int s, e;
    String inputLine, traderLink = "";

    this.userName = userName;
    this.password = password;
    
    loggedIn = false;
    cookie = "";

    // Invia username e password
    try {
      URL url = new URL(connectMethod + "://" + transactServer + "/trading/collegc_3?USER=" + userName + "&PASSW=" + password + "&PAG=VT4.4.0.0&TAPPO=X");
      HttpURLConnection con = (HttpURLConnection)url.openConnection();
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      while ((inputLine = in.readLine()) != null) 
      {
        if (inputLine.indexOf("<!--USER") != -1) 
        {
          s = inputLine.indexOf("USER") + 4;
          e = inputLine.indexOf("-", s);
          user = inputLine.substring(s, e);
        }
        else if (inputLine.indexOf("<!--URT") != -1) 
        {
          s = inputLine.indexOf("URT") + 3;
          e = inputLine.indexOf("-", s);
          urt = inputLine.substring(s, e);
        }
        else if (inputLine.indexOf("<!--PRT") != -1) 
        {
          s = inputLine.indexOf("PRT") + 3;
          e = inputLine.indexOf("-", s);
          prt = inputLine.substring(s, e);
        }
      }
      in.close();

      int i = 0;
      while ((inputLine = con.getHeaderField(i)) != null) 
      {
        if (inputLine.startsWith("IP_") == true)
          cookie = inputLine;
        i++;
      }
    } catch (Exception ex) { return false; };
    if (cookie.length() > 0)
      cookie = cookie.substring(0, cookie.indexOf("; "));

    System.out.println("USER: " + user);
    System.out.println("URT: " + urt);
    System.out.println("PRT: " + prt);
    
    if (urt.length() != 0 && prt.length() != 0)
    {
      loggedIn = true;
      updateValues();
      updateOrderStatus();
    }
    
    return loggedIn;
  }
  
  public boolean isLoggedIn()
  {
    return loggedIn;
  }

  // Si connette al server delle quotazioni
  public boolean connect() 
  {
    System.out.println(getClass().getName() + ": Connessione");
    runThread = false;

    // Apertura del socket verso il server
    try {
      socket = new Socket(streamingServer, streamingPort);
      os = socket.getOutputStream();
      is = new DataInputStream(socket.getInputStream());
      if (socket == null || os == null || is == null)
        return false;
    } catch(Exception e) { return false; };

    // Login
    try {
      os.write(CreaMsg.creaLoginMsg(urt, prt, streamingVersion));
      os.flush();
      byte bHeaderLogin[] = new byte[4];
      int n = 0;
      n = is.read(bHeaderLogin);
      int lenMsg = Convert.getLEN_MSG(bHeaderLogin, 2);
      if ((char)bHeaderLogin[0] != '#' && n != -1) 
      {
        System.out.println(getClass().getName() + ": Errore di login");
        return false;
      }
      byte msgResp[] = new byte[lenMsg];
      is.read(msgResp);
      if (Convert.bytetoInt(bHeaderLogin[1]) == 8) 
      {
        _ERROR_MSG err = new _ERROR_MSG(msgResp);
        System.out.println(getClass().getName() + ": Errore " + err.nTipeError + " - " + err.sMessageError);
        return false;
      }
    } catch(Exception e) {
      e.printStackTrace();
      return false;
    };

    System.out.println(getClass().getName() + ": Login effettuato");
    runThread = true;

    IExtendedData[] data = TraderPlugin.getData();
    String sTit[] = new String[data.length];
    int flag[] = new int[data.length];
    for (int i = 0; i < data.length; i++) 
    {
      sTit[i] = data[i].getTicker();
      flag[i] = 1;
    }

    // Avvia il push
    try {
      os.write(CreaMsg.creaPortMsg(1, sTit, flag));
      os.flush();
      os.write(CreaMsg.creaStartDataMsg());
      os.flush();
    } catch(Exception e) {};

    return true;
  }

  public void run() 
  {
    byte bHeader[] = new byte[4];
    int n = 0, i;
    long lastUpdate = System.currentTimeMillis();
    long lastActivity = System.currentTimeMillis();
    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    NumberFormat nf = NumberFormat.getInstance();
    Calendar transaction = Calendar.getInstance();

    nf.setMinimumFractionDigits(2);
    nf.setMaximumFractionDigits(2);

    // Loop di aggiornamento dei dati
    while (runThread == true) 
    {
      // Legge l'header di un messaggio (se c'e')
      try {
        if ((n = is.read(bHeader)) == -1)
          continue;
        while (n < 4) {
          int r = is.read(bHeader, n, 4 - n);
          n += r;
        }
        lastActivity = System.currentTimeMillis();
      } catch(Exception e) {
        break;
      };

      // Verifica la correttezza dell'header e legge il resto del messaggio
      _HEADER h = new _HEADER();
      h.start = (char)Convert.bytetoInt(bHeader[0]);
      if (h.start == '#') {
        h.tipo = Convert.getByte(bHeader[1]);
        h.len = Convert.getLEN_MSG(bHeader, 2);
        byte mes[] = new byte[h.len];
        try {
          n = is.read(mes);
          while (n < h.len) {
            int r = is.read(mes, n, h.len - n);
            n += r;
          }
        } catch(Exception e) {
        };
        if (h.tipo == 8) {
          _ERROR_MSG err = new _ERROR_MSG(mes);
          System.out.println("Errore " + err.nTipeError + " - " + err.sMessageError);
        }
        else if (h.len > 0) {
          Read_DATA_MSG readData = new Read_DATA_MSG(mes);
          _DATA_MSG obj = readData.getMsg();
          if (obj == null)
            continue;

          IExtendedData[] data = TraderPlugin.getData();
          for (i = 0; i < data.length; i++) 
          {
            if (data[i].getTicker().equalsIgnoreCase(obj.head.key) == true) {
              switch (obj.head.tipo) {
                case TipiRecord.TIP_PRICE: {
                  _PRICE pm = (_PRICE)obj;
                  data[i].setVolume((int)pm.qta_prgs);
                  data[i].setLowPrice(pm.min);
                  data[i].setHighPrice(pm.max);
                  data[i].setLastPrice(pm.val_ult);
                  transaction.setTime(new Date(pm.ora_ult));
                  today.set(Calendar.HOUR_OF_DAY, transaction.get(Calendar.HOUR_OF_DAY));
                  today.set(Calendar.MINUTE, transaction.get(Calendar.MINUTE));
                  today.set(Calendar.SECOND, transaction.get(Calendar.SECOND));
                  data[i].setDate(today.getTime());
                  fireDataUpdated(data[i]);
                  break;
                }
                case TipiRecord.TIP_BOOK: {
                  _BOOK bm = (_BOOK)obj;
                  PriceBook pb = (PriceBook)BookDataProvider.getDefault().bookData.get(data[i].getTicker());
                  if (pb != null)
                  {
                    for (int m = 0; m < 5; m++) {
                      pb.bid[m].setNumber(bm.n_pdn_c[m]);
                      pb.bid[m].setQuantity((int)bm.q_pdn_c[m]);
                      pb.bid[m].setPrice(bm.val_c[m]);
                      pb.ask[m].setNumber(bm.n_pdn_v[m]);
                      pb.ask[m].setQuantity((int)bm.q_pdn_v[m]);
                      pb.ask[m].setPrice(bm.val_v[m]);
                    }
                    pb.fireBookUpdated(data[i]);
                  }
                  if (data[i].getTicker().equalsIgnoreCase("FIB") == true) {
                    data[i].setBidSize((int)bm.q_pdn_c[0]);
                    data[i].setBidPrice(bm.val_c[0]);
                    data[i].setAskSize((int)bm.q_pdn_v[0]);
                    data[i].setAskPrice(bm.val_v[0]);
                    fireDataUpdated(data[i]);
                  }
                  break;
                }
                case TipiRecord.TIP_BIDASK: {
                  _BIDASK bam = (_BIDASK)obj;
                  data[i].setBidSize((int)bam.qmpc);
                  data[i].setBidPrice(bam.pmpc);
                  data[i].setAskSize((int)bam.qmpv);
                  data[i].setAskPrice(bam.pmpv);
                  fireDataUpdated(data[i]);
                  break;
                }
                case TipiRecord.TIP_ASTA: {
                  _ASTA ap = (_ASTA)obj;
                  data[i].setLastPrice(ap.val_aper);
                  data[i].setOpenPrice(ap.val_aper);
                  data[i].setVolume((int)ap.qta_aper);
                  fireDataUpdated(data[i]);
                  break;
                }
                case TipiRecord.TIP_ASTACHIUSURA: {
                  _ASTACHIUSURA ac = (_ASTACHIUSURA)obj;
                  data[i].setLastPrice(ac.val_chiu);
                  fireDataUpdated(data[i]);
                  break;
                }
                default:
                  System.out.println("Unknow data: obj.head.tipo=" + obj.head.tipo);
                  break;
              }
              break;
            }
          }
          if (i >= data.length)
            System.out.println("Not present: " + obj.head.key);

          // Propaga l'aggiornamento ai listeners.
          if ((System.currentTimeMillis() - lastUpdate) >= 1000) 
          {
            fireDataUpdated();
            lastUpdate = System.currentTimeMillis();
          }
        }
      }
    }
  }
  
  public void disconnect()
  {
    runThread = false;
    loggedIn = false;

    try {
      os.write(CreaMsg.creaLogoutMsg());
      os.flush();
    } catch(Exception e) {};
    try {
      os.close();
      is.close();
      socket.close();
    } catch(Exception e) {};
  }

  /**
   * Lettura dei dati iniziali.
   * <p>Il server di streaming invia solamente le variazioni dei dati, quindi
   * occorre prima di tutto leggere la situazione iniziale.</p>
   */
  public void readInitialData()
  {
    IExtendedData[] data = TraderPlugin.getData();
    String sTit[] = new String[data.length];
    int flag[] = new int[data.length];
    for (int i = 0; i < data.length; i++) 
    {
      sTit[i] = data[i].getTicker();
      flag[i] = 1;
    }

    System.out.println(getClass().getName() + ": Lettura dati iniziali");
    try {
      // Legge la pagina contenente gli ultimi prezzi
      URL web = GetURL.createURLalpha(sTit, GetURL.DATI, streamingServer, urt, prt);
      Hashtable hashFixedValue = new Hashtable();
      hashFixedValue = Load.LoadMpushData(web, hashFixedValue, true);

      for (int i = 0; i < data.length; i++) 
      {
        String sVal[] = (String[])hashFixedValue.get(data[i].getTicker());
        if (sVal == null)
          continue;
        data[i].setLastPrice(Double.parseDouble(sVal[ConstMpush.PREZZO]));
        data[i].setOpenPrice(Double.parseDouble(sVal[ConstMpush.APERTURA]));
        data[i].setHighPrice(Double.parseDouble(sVal[ConstMpush.MASSIMO]));
        data[i].setLowPrice(Double.parseDouble(sVal[ConstMpush.MINIMO]));
        data[i].setClosePrice(Double.parseDouble(sVal[ConstMpush.PRECEDENTE]));
        data[i].setVolume(Integer.parseInt(sVal[ConstMpush.VOLUME]));
        try {
          if (sVal[ConstMpush.DATA].equalsIgnoreCase("0") == true)
            data[i].setDate(tf.parse(sVal[ConstMpush.ORA]));
          else
            data[i].setDate(df.parse(sVal[ConstMpush.DATA] + " " + sVal[ConstMpush.ORA]));
        } catch(Exception e) {
          System.out.println(e.getMessage());
          System.out.println(sVal[ConstMpush.DATA] + " " + sVal[ConstMpush.ORA]);
        };

        if (data[i].getTicker().equalsIgnoreCase("FIB") == true) {
          int k = ConstMpush.INZIO_BOOK;
          k++;
          data[i].setBidSize(Integer.parseInt(sVal[k++]));
          data[i].setBidPrice(Double.parseDouble(sVal[k++]));
          k++;
          data[i].setAskSize(Integer.parseInt(sVal[k++]));
          data[i].setAskPrice(Double.parseDouble(sVal[k++]));
        }
        else {
          data[i].setBidPrice(Double.parseDouble(sVal[ConstMpush.BID_PREZZO]));
          data[i].setBidSize(Integer.parseInt(sVal[ConstMpush.BID_QUANTITA]));
          data[i].setAskPrice(Double.parseDouble(sVal[ConstMpush.ASK_PREZZO]));
          data[i].setAskSize(Integer.parseInt(sVal[ConstMpush.ASK_QUANTITA]));
        }

        PriceBook pb = (PriceBook)BookDataProvider.getDefault().bookData.get(data[i].getTicker());
        if (pb != null)
        {
          int k = ConstMpush.INZIO_BOOK;
          for (int x = 0; x < 5; x++) 
          {
            pb.bid[x].setNumber(Integer.parseInt(sVal[k++]));
            pb.bid[x].setQuantity(Integer.parseInt(sVal[k++]));
            pb.bid[x].setPrice(Double.parseDouble(sVal[k++]));
            pb.ask[x].setNumber(Integer.parseInt(sVal[k++]));
            pb.ask[x].setQuantity(Integer.parseInt(sVal[k++]));
            pb.ask[x].setPrice(Double.parseDouble(sVal[k++]));
          }
        }
      }
    } catch (Exception ex) { ex.printStackTrace(); };

    // Notify listeners of the data update
    fireDataUpdated();
    for (int i = 0; i < data.length; i++)
    {
      PriceBook pb = (PriceBook)BookDataProvider.getDefault().bookData.get(data[i].getTicker());
      if (pb != null)
        pb.fireBookUpdated(data[i]);
    }
  }

  /**
   * Legge lo stato iniziale del book.
   * <p>Il server di streaming invia solamente le variazioni dei dati, quindi
   * occorre prima di tutto leggere la situazione iniziale.</p>
   */
  public void readInitialBookValues(final IBasicData data)
  {
    new Thread(new Runnable() {
      public void run()
      {
        String sTit[] = { data.getTicker() };
        int flag[] = { 1 };

        try {
          // Legge la pagina contenente gli ultimi prezzi
          URL web = GetURL.createURLalpha(sTit, GetURL.DATI, streamingServer, urt, prt);
          System.out.println(web);
          Hashtable hashFixedValue = new Hashtable();
          hashFixedValue = Load.LoadMpushData(web, hashFixedValue, true);

          String sVal[] = (String[])hashFixedValue.get(data.getTicker());
          if (sVal != null)
          {
            PriceBook pb = (PriceBook)BookDataProvider.getDefault().bookData.get(data.getTicker());
            if (pb != null)
            {
              int k = ConstMpush.INZIO_BOOK;
              for (int x = 0; x < 5; x++) 
              {
                pb.bid[x].setNumber(Integer.parseInt(sVal[k++]));
                pb.bid[x].setQuantity(Integer.parseInt(sVal[k++]));
                pb.bid[x].setPrice(Double.parseDouble(sVal[k++]));
                pb.ask[x].setNumber(Integer.parseInt(sVal[k++]));
                pb.ask[x].setQuantity(Integer.parseInt(sVal[k++]));
                pb.ask[x].setPrice(Double.parseDouble(sVal[k++]));
              }
              pb.fireBookUpdated(data);
            }
          }
        } catch (Exception ex) { ex.printStackTrace(); };
      }
    }).start();
  }

  /**
   * Backfill dei dati per il grafico intraday.
   */
  public Vector backfill(IBasicData data, int period)
  {
    Vector _v = new Vector();
    
    if (loggedIn == true)
      try {
        String uncompressLen = "", compressLen = "";
        byte[] buffer = new byte[1];
        URL url = new URL("http://" + backfillServer + "/cgi-bin/mpush.php?modo=g&uvt=&ver=04040601_02040405_&u=" + urt + "&p=" + prt + "&cod=A&stcmd=" + data.getTicker() + ",,,1," + period + ",0");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        BufferedInputStream in = new BufferedInputStream(con.getInputStream());
        while(in.read(buffer) == 1)
        {
          if (buffer[0] == 'L')
          {
            if (in.read(buffer) != 1)
              break;
            if (buffer[0] == '=')
            {
              for (;;)
              {
                if (in.read(buffer) != 1)
                  break;
                if (buffer[0] == ' ')
                  break;
                uncompressLen += new String(buffer);
              }
            }
            else if (buffer[0] == 'C')
            {
              if (in.read(buffer) != 1)
                break;
              if (buffer[0] == '=')
              {
                for (;;)
                {
                  if (in.read(buffer) != 1)
                    break;
                  if (buffer[0] == ' ')
                    break;
                  compressLen += new String(buffer);
                }
              }
            }
          }
          if (buffer[0] == 0x78)
          {
            int readed = 1, len;
            byte[] input = new byte[Integer.parseInt(compressLen)];
            input[0] = buffer[0];
            do
            {
              len = in.read(input, readed, input.length - readed);
              readed += len;
            } while(len > 0 && readed < input.length);
  
            Inflater infl = new Inflater();
            infl.setInput(input, 0, readed);
            byte[] output = new byte[Integer.parseInt(uncompressLen)];
            int outlength = infl.inflate(output);
            infl.end();
            
            for (int i = 0; i < outlength; i += 24)
            {
              IChartData cd = new ChartData();
              cd.setDate(getDataOra(output, i));
              cd.setOpenPrice(getFloat(output, i + 12));
              cd.setClosePrice(getFloat(output, i + 20));
              cd.setMaxPrice(getFloat(output, i + 8));
              cd.setMinPrice(getFloat(output, i + 4));
              cd.setVolume((int)getFloat(output, i + 16));
              _v.addElement(cd);
            }
          }
        }
        in.close();
      } catch(Exception e) { e.printStackTrace(); };

    return _v;
  }

  /**
   * Aggiorna i valori di liquidità e disponibilità del conto.
   */
  public void updateValues() 
  {
    double dispAz = 0;
    double liqAz = 0;
    double valAz = 0;
    String inputLine;

    if (loggedIn == false)
      return;

    try {
      URL web = new URL(connectMethod + "://" + transactServer + "/trading/select?USER=" + user + "&INCR=N");
      HttpURLConnection con = (HttpURLConnection)web.openConnection();
      con.setAllowUserInteraction(true);
      con.setRequestMethod("GET");
      con.setRequestProperty("Cookie", cookie);
      con.connect();
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

      while ((inputLine = in.readLine()) != null) 
      {
        if (inputLine.indexOf(">totale EU<") != -1) 
        {
          if ((inputLine = in.readLine()) == null)
            break;
          try {
            int s = inputLine.indexOf(">") + 1;
            int e = inputLine.indexOf("<", s);
            String value = inputLine.substring(s, e);
            while (value.indexOf(".") != -1)
              value = value.substring(0, value.indexOf(".")) + value.substring(value.indexOf(".") + 1, value.length());
            valAz += Double.parseDouble(value.replace(',', '.'));
          } catch (Exception ex) {};
        }
        else if (inputLine.indexOf(">azioni<") != -1) 
        {
          if ((inputLine = in.readLine()) == null)
            break;
          try {
            int s = inputLine.indexOf(">") + 1;
            int e = inputLine.indexOf("<", s);
            String value = inputLine.substring(s, e);
            while (value.indexOf(".") != -1)
              value = value.substring(0, value.indexOf(".")) + value.substring(value.indexOf(".") + 1, value.length());
            dispAz += Double.parseDouble(value.replace(',', '.'));
          } catch (Exception ex) {};
          if ((inputLine = in.readLine()) == null)
            break;
          try {
            int s = inputLine.indexOf(">") + 1;
            int e = inputLine.indexOf("<", s);
            String value = inputLine.substring(s, e);
            while (value.indexOf(".") != -1)
              value = value.substring(0, value.indexOf(".")) + value.substring(value.indexOf(".") + 1, value.length());
            liqAz += Double.parseDouble(value.replace(',', '.'));
          } catch (Exception ex) {};
        }
      }
      in.close();
    } catch (Exception ex) {};
    
    if (this.dispAz != dispAz || this.liqAz != liqAz || this.valAz != valAz)
    {
      this.dispAz = dispAz;
      this.liqAz = liqAz;
      this.valAz = valAz;
      fireOrderStatusChanged();
    }
  }

  /**
   * Ritorna la disponibilità liquida per il mercato azionario.
   */
  public double getLiquiditaAzioni()
  {
    return liqAz;
  }
  
  /**
   * Ritorna la disponibilità per il mercato azionario.
   */
  public double getDisponibileAzioni()
  {
    return dispAz;
  }
  
  /**
   * Ritorna il valore delle azioni possedute.
   */
  public double getValoreAzioni()
  {
    return valAz;
  }
  
  /**
   * Ritorna la disponibilità liquida per il mercato dei derivati.
   */
  public double getLiquiditaDerivati()
  {
    return liqDer;
  }
  
  /**
   * Ritorna la disponibilità per il mercato dei derivati.
   */
  public double getDisponibileDerivati()
  {
    return dispDer;
  }
  
  /**
   * Ritorna il valore dei derivati posseduti.
   */
  public double getValoreDerivati()
  {
    return valDer;
  }
  
  /**
   * Ritorna la disponibilità totale.
   */
  public double getDisponibile()
  {
    return dispAz + dispDer;
  }

  private int bytetoInt(byte byte0)
  {
    int i = byte0;
    if(byte0 < 0)
      i = 256 + byte0;
    return i;
  }

  private float getFloat(byte abyte0[], int i)
  {
    if(abyte0.length < i + 4)
      return 0.0F;
    int j = 0;
    int k = 0;
    for(int l = 0; l < 4; l++)
    {
      k += bytetoInt(abyte0[i + l]) << j;
      j += 8;
    }

    float f = Float.intBitsToFloat(k);
    return f;
  }

  /**
   * Send an order to the transaction server.
   * <p></p>
   * @param ticker The symbol to trade.
   * @param buy Buy quantity or empty if it is a sell order.
   * @param sell Sell quantity or empty if it is a buy order.
   * @param price Price to buy/sell at.
   * @param valid30 true if the order should be kept valid for 30 days.
   * @param market Market phase.<br>
   * @return true if the order was accepted by the server.
   */
  public boolean sendOrder(String ticker, String buy, String sell, String price, boolean valid30, String market)
  {
    int i;
    boolean ok = false;
    boolean noconfirm = false;
    String inputLine;
    
    if (loggedIn == false)
      return false;

    // Inserisce l'ordine di acquisto
    // VALID=M - 30 giorni
    // FAS5= 1=immed, 2=MTA, 4=clos-MTA, 5=AfterHours
    try {
      String s = connectMethod + "://" + transactServer + "/trading/ordimm5c?ACQAZ=" + buy + "&VENAZ=" + sell + "&PRZACQ=" + price + "&SCTLX=immetti+Borsa+Ita&USER=" + user + "&GEST=AZIONARIO&TITO=" + ticker + "&MODO=C&QPAR=";
      if (valid30 == true)
        s += "&VALID=M";
      s += "&FAS5=" + market; 
      URL url = new URL(s);
System.out.println(url);
      HttpURLConnection con = (HttpURLConnection)url.openConnection();
      con.setAllowUserInteraction(true);
      con.setRequestMethod("GET");
      con.setRequestProperty("Cookie", cookie);
      con.connect();
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      while ((inputLine = in.readLine()) != null) {
System.out.println(inputLine);
        if (inputLine.indexOf("VI TRASMETTO L'ORDINE DI") != -1)
          ok = true;
        if (inputLine.indexOf("ORDINE IMMESSO") != -1) {
          ok = true;
          noconfirm = true;
        }
      }
      in.close();
    } catch (Exception ex) {};

    // Se viene richiesta invia anche la conferma d'ordine
    if (ok == true && noconfirm == false) {
      ok = false;

      try {
        URL url = new URL(connectMethod + "://" + transactServer + "/trading/ordimm5c?ACQAZ=" + buy + "&VENAZ=" + sell + "&PRZACQ=" + price + "&SCTLX=immetti+Borsa+Ita&USER=" + user + "&GEST=AZIONARIO&TITO=" + ticker + "&MODO=V&QPAR=");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setAllowUserInteraction(true);
        con.setRequestMethod("GET");
        con.setRequestProperty("Cookie", cookie);
        con.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        while ((inputLine = in.readLine()) != null) {
          if (inputLine.indexOf("ORDINE IMMESSO") != -1)
            ok = true;
        }
        in.close();
      } catch (Exception ex) {};
    }
    
    System.out.println("Directa: sendOrder=" + ok);
    
    if (ok == true)
    {
      addToMonitorSymbol(ticker);
      updateValues();
      updateOrderStatus();
    }

    return ok;
  }

  /**
   * Cancel the order.
   * <p></p>
   * @param id The order-id to cancel.<br>
   * @return true if the cancellation was accepted by the server.
   */
  public boolean cancelOrder(String id) 
  {
    boolean ok = false;
    String inputLine, cancelUrl = "";
    
    for (int i = 0; i < orderData.length; i++)
    {
      if (orderData[i].id.equalsIgnoreCase(id) == true)
      {
        cancelUrl = "&RIF=" + orderData[i].id + "&TITO=" + orderData[i].symbol;
        break;
      }
    }

    try {
      URL web = new URL(connectMethod + "://" + transactServer + "/trading/ordmod5c");
      HttpURLConnection con = (HttpURLConnection)web.openConnection();
      con.setAllowUserInteraction(true);
      con.setDoOutput(true);
      con.setRequestMethod("POST");
      con.setRequestProperty("Cookie", cookie);
      con.connect();
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
      out.write("USER=" + user + cancelUrl + "&TIPO=I&PRZO=&FILL=REVOCA");
      out.flush();
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      while ((inputLine = in.readLine()) != null) {
        if (inputLine.indexOf("INOLTRATA LA RICHIESTA DI REVOCA") != -1)
          ok = true;
      }
      in.close();
      out.close();
    } catch (Exception ex) {};
    
    System.out.println("Directa: cancelOrder=" + ok);
    
    if (ok == true)
    {
      updateOrderStatus();
      updateValues();
    }

    return ok;
  }

  /**
   * Ritorna l'elenco degli ordini inseriti.
   */
  public void updateOrderStatus() 
  {
    String line;
    NumberFormat nf = NumberFormat.getInstance();
    Vector vdata = new Vector();
    IExtendedData[] data = TraderPlugin.getData();

    nf.setMinimumFractionDigits(4);
    nf.setMaximumFractionDigits(4);

    if (loggedIn == true) 
    {
      try {
        String request = "http://194.185.126.186/jscript/ordinij?DSUSER=" + user + "&DSTITO=&DSFUNZ=2&PAG=VT4.4.0.6&TAPPO=X";
        URL web = new URL(request);
        HttpURLConnection con = (HttpURLConnection)web.openConnection();
        con.setAllowUserInteraction(true);
        con.setRequestMethod("GET");
        con.setRequestProperty("Cookie", cookie);
        con.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        while((line = in.readLine()) != null)
        {
          if (line.startsWith("tr01[") == true)
          {
            int s = line.indexOf("\"") + 1;
            int e = line.indexOf("\"", s);
            String[] item = line.substring(s, e).split(";");
            if (item[2].equalsIgnoreCase("zA") == true)
              continue;
            addToMonitorSymbol(item[0]);
          }
        }
        in.close();
      } catch (Exception ex) {
        ex.printStackTrace();
      };

      for (int i = 0; i < monitorSymbol.size(); i++)
        try {
          String request = "http://194.185.126.186/jscript/ordinij?DSUSER=" + user + "&DSTITO=" + (String)monitorSymbol.elementAt(i) + "&DSFUNZ=2&PAG=VT4.4.0.6&TAPPO=X";
          URL web = new URL(request);
          HttpURLConnection con = (HttpURLConnection)web.openConnection();
          con.setAllowUserInteraction(true);
          con.setRequestMethod("GET");
          con.setRequestProperty("Cookie", cookie);
          con.connect();
          BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
          while((line = in.readLine()) != null)
          {
            if (line.startsWith("tr01[") == true)
            {
              int s = line.indexOf("\"") + 1;
              int e = line.indexOf("\"", s);
              String[] item = line.substring(s, e).split(";");
              if (item[2].equalsIgnoreCase("zA") == true)
                continue;
              OrderData od = new OrderData();
              od.code = "";
              od.symbol = item[0];
              for (int x = 0; x < data.length; x++)
              {
                if (data[x].getTicker().equalsIgnoreCase(od.symbol) == true)
                {
                  od.code = data[x].getDescription();
                  break;
                }
              }
              od.id = item[1];
              od.date = item[3];
              od.time = item[4];
              if (item[5].equalsIgnoreCase("V") == true)
                od.type = "Vendita";
              else
                od.type = "Acquisto";
              od.quantity = item[6];
              od.price = item[7];
              od.executedQuantity = item[10];
              od.executedPrice = item[11];
              if (item[2].equalsIgnoreCase("e") == true)
                od.status = "Eseguito";
              else if (item[2].equalsIgnoreCase("n") == true)
                od.status = "In Negoziazione";
              else if (item[2].equalsIgnoreCase("b") == true)
                od.status = "Accodato";
              else
                od.status = item[2];
              vdata.addElement(od);
            }
          }
          in.close();
        } catch (Exception ex) {
          ex.printStackTrace();
        };
    }

    OrderData[] newOrderData = new OrderData[vdata.size()];
    vdata.toArray(newOrderData);

    // Verifica se l'elenco e' stato aggiornato
    boolean fireUpdate = false;
    if (orderData == null)
      fireUpdate = true;
    else
    {
      for (int i = 0; i < newOrderData.length; i++)
      {
        OrderData odNew = newOrderData[i];
        OrderData odOld = null;
        for (int m = 0; m < orderData.length; m++)
        {
          if (orderData[m].id.equalsIgnoreCase(odNew.id) == true)
          {
            odOld = orderData[m];
            break;
          }
        }
        if (odOld == null)
          fireUpdate = true;
        else
        {
          if (odOld.status.equalsIgnoreCase(odNew.status) == false)
            fireUpdate = true;
          else if (odOld.quantity.equalsIgnoreCase(odNew.quantity) == false)
            fireUpdate = true;
          else if (odOld.executedQuantity.equalsIgnoreCase(odNew.executedQuantity) == false)
            fireUpdate = true;
          else if (odOld.executedPrice.equalsIgnoreCase(odNew.executedPrice) == false)
            fireUpdate = true;
        }

        // Verifica se deve aggiornare lo stato del portafoglio
        if (odNew.status.equalsIgnoreCase("ESEGUITO") == true && (odOld == null || odOld.status.equalsIgnoreCase(odNew.status) == false))
        {
          for (int x = 0; x < data.length; x++)
          {
            if (data[x].getTicker().equalsIgnoreCase(odNew.symbol) == true)
            {
              if (odNew.type.equalsIgnoreCase("Vendita") == true)
              {
                data[x].setQuantity(data[x].getQuantity() - Integer.parseInt(odNew.executedQuantity));
                if (data[x].getQuantity() == 0)
                  data[x].setPaid(0);
              }
              else
              {
                double paid = Integer.parseInt(odNew.executedQuantity) * Double.parseDouble(odNew.executedPrice);
                paid += data[x].getQuantity() * data[x].getPaid();
                data[x].setQuantity(data[x].getQuantity() + Integer.parseInt(odNew.executedQuantity));
                if (data[x].getQuantity() == 0)
                  data[x].setPaid(0);
                else
                  data[x].setPaid(paid / data[x].getQuantity());
              }
              break;
            }
          }
        }
      }
    }
    
    if (orderData == null || orderData.length != newOrderData.length)
      fireUpdate = true;
    orderData = newOrderData;
    
    if (fireUpdate == true)
      fireOrderStatusChanged();
  }
  
  public OrderData[] getOrderData()
  {
    return orderData;
  }

  /**
   * Aggiunge un ticker all'elenco dei simboli monitorati per lo stato degli ordini.
   * 
   * @param ticker - Simbolo da aggiungere all'elenco.
   */
  private void addToMonitorSymbol(String ticker)
  {
    for (int i = 0; i < monitorSymbol.size(); i++) 
    {
      if (ticker.equalsIgnoreCase((String)monitorSymbol.elementAt(i)) == true)
        return;
    }
    
    monitorSymbol.addElement(ticker);
  }

  private Date getDataOra(byte abyte0[], int i)
  {
    int k = 0;
    int l = 0;
    for(int i1 = 0; i1 < 4; i1++)
    {
      k += bytetoInt(abyte0[i1 + i]) << l;
      l += 8;
    }

    int j1 = k >> 20;
    int k1 = k - (j1 << 20);
    k1 /= 12;
    int l1 = k1 / 3600;
    int i2 = (k1 - l1 * 3600) / 60;
    int j2 = k1 - i2 * 60 - l1 * 3600;
    GregorianCalendar gregoriancalendar = new GregorianCalendar(2000, 0, 1, 0, 0, 0);
    long l2 = gregoriancalendar.getTime().getTime();
    long l3 = (long)j1 * (long)3600 * (long)24 * (long)1000 + (long)(k1 * 1000);
    long l4 = l2 + l3;
    gregoriancalendar.setTime(new Date(l4));
    gregoriancalendar.set(11, l1);
    return gregoriancalendar.getTime();
  }
}
