/*******************************************************************************
 * Copyright (c) 2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *     Stephen Bate     - Dukascopy plugin
 *******************************************************************************/
package net.sourceforge.eclipsetrader.dukascopy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IChartDataProvider;
import net.sourceforge.eclipsetrader.internal.ChartData;
import sun.misc.BASE64Encoder;

/**
 * Dukascopy chart data provider.
 * <p>
 * </p>
 */
public class ChartDataProvider implements IChartDataProvider
{
  protected SimpleDateFormat dataDateFormat = new SimpleDateFormat(Messages.getString("ChartDataProvider.report.dateFormat.java")); //$NON-NLS-1$

  private static final SimpleDateFormat requestDateFormat = new SimpleDateFormat(Messages.getString("ChartDataProvider.request.dateFormat")); //$NON-NLS-1$

  /*
   * (non-Javadoc)
   * 
   * @see net.sourceforge.eclipsetrader.IChartDataProvider#getData(net.sourceforge.eclipsetrader.IBasicData)
   */
  public IChartData[] getData(IBasicData data)
  {
    return update(data, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.sourceforge.eclipsetrader.IChartDataProvider#update(net.sourceforge.eclipsetrader.IBasicData,
   *      net.sourceforge.eclipsetrader.IChartData[])
   */
  public IChartData[] update(IBasicData data, IChartData[] chartData)
  {
    Vector v = new Vector();
    if (chartData != null)
    {
      for (int i = 0; i < chartData.length; i++)
        v.add(chartData[i]);
    }

    Calendar from = Calendar.getInstance();

    // If no data is avalable, start from one year back.
    if (v.size() == 0)
    {
      int value = DukascopyPlugin.getDefault().getPreferenceStore().getInt("NEW_CHART_YEARS"); //$NON-NLS-1$
      from.add(Calendar.YEAR, -value);
    }

    // Start reading data from the most recent data available +1
    if (v.size() != 0)
    {
      IChartData cd = (IChartData) v.elementAt(v.size() - 1);
      from.setTime(cd.getDate());
      from.add(Calendar.DATE, 1);
    }

    try
    {
      int numberOfPoints = 1000;
      String interval = Messages.getString("ChartDataProvider.interval"); //$NON-NLS-1$
      String dateFormat = Messages.getString("ChartDataProvider.report.dateFormat.duka"); //$NON-NLS-1$
      Integer stockId = (Integer) stockIdMapping.get(data.getSymbol());
      URL url = new URL("http://freeserv.dukascopy.com/exp/exp.php?fromD=" + requestDateFormat.format(from.getTime()) //$NON-NLS-1$
                        + "&np=" + numberOfPoints + "&interval=" + interval + "&DF=" + dateFormat + "&Stock=" + stockId //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        + "&endSym=unix&split=coma"); //$NON-NLS-1$
      System.out.println(getClass() + " " + dataDateFormat.format(from.getTime()) + " " + url); //$NON-NLS-1$ //$NON-NLS-2$

      BufferedReader in = requestDataUsingHttp(url);
      String inputLine = in.readLine(); // skip headers
      while ((inputLine = in.readLine()) != null)
      {
        String[] item = inputLine.split(","); //$NON-NLS-1$
        IChartData cd = new ChartData();
        cd.setDate(dataDateFormat.parse(item[0].substring(0, 10)));
        cd.setOpenPrice(Double.parseDouble(item[3]));
        cd.setMaxPrice(Double.parseDouble(item[6]));
        cd.setMinPrice(Double.parseDouble(item[5]));
        cd.setClosePrice(Double.parseDouble(item[4]));
        cd.setVolume((int) Double.parseDouble(item[2]));
        if (cd.getVolume() > 0)
        {
          v.add(cd);
        }
      }
      in.close();
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    sort(v);

    chartData = new IChartData[v.size()];
    v.toArray(chartData);

    return chartData;
  }

  private BufferedReader requestDataUsingHttp(URL url) throws IOException, ProtocolException
  {
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    String proxyHost = (String) System.getProperties().get("http.proxyHost"); //$NON-NLS-1$
    String proxyUser = (String) System.getProperties().get("http.proxyUser"); //$NON-NLS-1$
    String proxyPassword = (String) System.getProperties().get("http.proxyPassword"); //$NON-NLS-1$
    if (proxyHost != null && proxyHost.length() != 0 && proxyUser != null && proxyUser.length() != 0 && proxyPassword != null)
    {
      String login = proxyUser + ":" + proxyPassword; //$NON-NLS-1$
      String encodedLogin = new BASE64Encoder().encodeBuffer(login.getBytes());
      con.setRequestProperty("Proxy-Authorization", "Basic " + encodedLogin.trim()); //$NON-NLS-1$ //$NON-NLS-2$
    }
    con.setAllowUserInteraction(true);
    con.setRequestMethod("GET"); //$NON-NLS-1$
    con.setInstanceFollowRedirects(true);
    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    return in;
  }

  /**
   * Order by date.
   */
  private void sort(Vector v)
  {
    java.util.Collections.sort(v, new Comparator() {
      public int compare(Object o1, Object o2)
      {
        IChartData d1 = (IChartData) o1;
        IChartData d2 = (IChartData) o2;
        if (d1.getDate().after(d2.getDate()) == true)
          return 1;
        else if (d1.getDate().before(d2.getDate()) == true)
          return -1;
        return 0;
      }
    });
  }

  /**
   * Dukascopy charts are requested using a specified stock ID rather than the
   * symbol. The following mapping maps symbols to their stock ID.
   */

  private static HashMap stockIdMapping = new HashMap();

  static
  {
    stockIdMapping.put("AAGLO-AMS", new Integer(208)); //$NON-NLS-1$
    stockIdMapping.put("AAPL", new Integer(72)); //$NON-NLS-1$
    stockIdMapping.put("ABBN-SWX", new Integer(221)); //$NON-NLS-1$
    stockIdMapping.put("ACU", new Integer(119)); //$NON-NLS-1$
    stockIdMapping.put("ADBE", new Integer(62)); //$NON-NLS-1$
    stockIdMapping.put("ADEN-SWX", new Integer(222)); //$NON-NLS-1$
    stockIdMapping.put("ADI", new Integer(700)); //$NON-NLS-1$
    stockIdMapping.put("ADTN", new Integer(701)); //$NON-NLS-1$
    stockIdMapping.put("AGE-FRA", new Integer(238)); //$NON-NLS-1$
    stockIdMapping.put("AGNAF-AMS", new Integer(209)); //$NON-NLS-1$
    stockIdMapping.put("AIG", new Integer(702)); //$NON-NLS-1$
    stockIdMapping.put("AKZA-AMS", new Integer(226)); //$NON-NLS-1$
    stockIdMapping.put("ALREA-AMS", new Integer(227)); //$NON-NLS-1$
    stockIdMapping.put("ALREB-BRU", new Integer(202)); //$NON-NLS-1$
    stockIdMapping.put("ALTR", new Integer(65)); //$NON-NLS-1$
    stockIdMapping.put("ALV-FRA", new Integer(165)); //$NON-NLS-1$
    stockIdMapping.put("AMAT", new Integer(86)); //$NON-NLS-1$
    stockIdMapping.put("AMCC", new Integer(286)); //$NON-NLS-1$
    stockIdMapping.put("AMGN", new Integer(96)); //$NON-NLS-1$
    stockIdMapping.put("AMZN", new Integer(102)); //$NON-NLS-1$
    stockIdMapping.put("ANL-LON", new Integer(143)); //$NON-NLS-1$
    stockIdMapping.put("APA", new Integer(703)); //$NON-NLS-1$
    stockIdMapping.put("APOL", new Integer(70)); //$NON-NLS-1$
    stockIdMapping.put("ARE-PAR", new Integer(255)); //$NON-NLS-1$
    stockIdMapping.put("AXP", new Integer(32)); //$NON-NLS-1$
    stockIdMapping.put("AZN-LON", new Integer(142)); //$NON-NLS-1$
    stockIdMapping.put("Arab_Heavy", new Integer(551)); //$NON-NLS-1$
    stockIdMapping.put("Arab_Light", new Integer(550)); //$NON-NLS-1$
    stockIdMapping.put("BA", new Integer(34)); //$NON-NLS-1$
    stockIdMapping.put("BA.-LON", new Integer(127)); //$NON-NLS-1$
    stockIdMapping.put("BAC", new Integer(704)); //$NON-NLS-1$
    stockIdMapping.put("BARC-LON", new Integer(128)); //$NON-NLS-1$
    stockIdMapping.put("BAS-FRA", new Integer(166)); //$NON-NLS-1$
    stockIdMapping.put("BAY-FRA", new Integer(167)); //$NON-NLS-1$
    stockIdMapping.put("BAY-LON", new Integer(132)); //$NON-NLS-1$
    stockIdMapping.put("BBBY", new Integer(88)); //$NON-NLS-1$
    stockIdMapping.put("BBY", new Integer(705)); //$NON-NLS-1$
    stockIdMapping.put("BCE", new Integer(252)); //$NON-NLS-1$
    stockIdMapping.put("BEAS", new Integer(287)); //$NON-NLS-1$
    stockIdMapping.put("BGO", new Integer(256)); //$NON-NLS-1$
    stockIdMapping.put("BI", new Integer(120)); //$NON-NLS-1$
    stockIdMapping.put("BIIB", new Integer(706)); //$NON-NLS-1$
    stockIdMapping.put("BKIR-LON", new Integer(130)); //$NON-NLS-1$
    stockIdMapping.put("BMET", new Integer(81)); //$NON-NLS-1$
    stockIdMapping.put("BMO", new Integer(250)); //$NON-NLS-1$
    stockIdMapping.put("BNS", new Integer(251)); //$NON-NLS-1$
    stockIdMapping.put("BOBJ", new Integer(707)); //$NON-NLS-1$
    stockIdMapping.put("BP.-LON", new Integer(131)); //$NON-NLS-1$
    stockIdMapping.put("BRCD", new Integer(288)); //$NON-NLS-1$
    stockIdMapping.put("BRCM", new Integer(708)); //$NON-NLS-1$
    stockIdMapping.put("BSY-LON", new Integer(135)); //$NON-NLS-1$
    stockIdMapping.put("BVIT-LON", new Integer(241)); //$NON-NLS-1$
    stockIdMapping.put("Brent", new Integer(505)); //$NON-NLS-1$
    stockIdMapping.put("C", new Integer(36)); //$NON-NLS-1$
    stockIdMapping.put("CA-PAR", new Integer(249)); //$NON-NLS-1$
    stockIdMapping.put("CAJ", new Integer(317)); //$NON-NLS-1$
    stockIdMapping.put("CALM", new Integer(709)); //$NON-NLS-1$
    stockIdMapping.put("CAT", new Integer(35)); //$NON-NLS-1$
    stockIdMapping.put("CBK-FRA", new Integer(169)); //$NON-NLS-1$
    stockIdMapping.put("CBRY-LON", new Integer(243)); //$NON-NLS-1$
    stockIdMapping.put("CCU", new Integer(710)); //$NON-NLS-1$
    stockIdMapping.put("CDWC", new Integer(104)); //$NON-NLS-1$
    stockIdMapping.put("CEPH", new Integer(291)); //$NON-NLS-1$
    stockIdMapping.put("CGE-PAR", new Integer(181)); //$NON-NLS-1$
    stockIdMapping.put("CHIR", new Integer(83)); //$NON-NLS-1$
    stockIdMapping.put("CHKP", new Integer(293)); //$NON-NLS-1$
    stockIdMapping.put("CHL", new Integer(323)); //$NON-NLS-1$
    stockIdMapping.put("CIEN", new Integer(292)); //$NON-NLS-1$
    stockIdMapping.put("CMCSA", new Integer(93)); //$NON-NLS-1$
    stockIdMapping.put("CNA-LON", new Integer(145)); //$NON-NLS-1$
    stockIdMapping.put("COF", new Integer(711)); //$NON-NLS-1$
    stockIdMapping.put("COST", new Integer(69)); //$NON-NLS-1$
    stockIdMapping.put("CSCO", new Integer(97)); //$NON-NLS-1$
    stockIdMapping.put("CSGN-SWX", new Integer(216)); //$NON-NLS-1$
    stockIdMapping.put("CTAS", new Integer(82)); //$NON-NLS-1$
    stockIdMapping.put("CTSH", new Integer(712)); //$NON-NLS-1$
    stockIdMapping.put("CTXS", new Integer(294)); //$NON-NLS-1$
    stockIdMapping.put("CW.-LON", new Integer(144)); //$NON-NLS-1$
    stockIdMapping.put("Copper", new Integer(506)); //$NON-NLS-1$
    stockIdMapping.put("DBK-FRA", new Integer(171)); //$NON-NLS-1$
    stockIdMapping.put("DCX-FRA", new Integer(170)); //$NON-NLS-1$
    stockIdMapping.put("DD", new Integer(39)); //$NON-NLS-1$
    stockIdMapping.put("DDB-BRU", new Integer(228)); //$NON-NLS-1$
    stockIdMapping.put("DELB-BRU", new Integer(200)); //$NON-NLS-1$
    stockIdMapping.put("DELL", new Integer(95)); //$NON-NLS-1$
    stockIdMapping.put("DHI", new Integer(713)); //$NON-NLS-1$
    stockIdMapping.put("DIA", new Integer(330)); //$NON-NLS-1$
    stockIdMapping.put("DIE-BRU", new Integer(229)); //$NON-NLS-1$
    stockIdMapping.put("DIS", new Integer(38)); //$NON-NLS-1$
    stockIdMapping.put("DISH", new Integer(295)); //$NON-NLS-1$
    stockIdMapping.put("DLA", new Integer(121)); //$NON-NLS-1$
    stockIdMapping.put("DLTR", new Integer(714)); //$NON-NLS-1$
    stockIdMapping.put("DPW-FRA", new Integer(172)); //$NON-NLS-1$
    stockIdMapping.put("DTE-FRA", new Integer(173)); //$NON-NLS-1$
    stockIdMapping.put("DW", new Integer(122)); //$NON-NLS-1$
    stockIdMapping.put("Dubai", new Integer(552)); //$NON-NLS-1$
    stockIdMapping.put("E", new Integer(326)); //$NON-NLS-1$
    stockIdMapping.put("EAD-LSIN", new Integer(306)); //$NON-NLS-1$
    stockIdMapping.put("EBAY", new Integer(91)); //$NON-NLS-1$
    stockIdMapping.put("EDLB-BRU", new Integer(204)); //$NON-NLS-1$
    stockIdMapping.put("EK", new Integer(40)); //$NON-NLS-1$
    stockIdMapping.put("ELEB-BRU", new Integer(205)); //$NON-NLS-1$
    stockIdMapping.put("ELR-PAR", new Integer(231)); //$NON-NLS-1$
    stockIdMapping.put("EN", new Integer(327)); //$NON-NLS-1$
    stockIdMapping.put("ERTS", new Integer(78)); //$NON-NLS-1$
    stockIdMapping.put("ESRX", new Integer(715)); //$NON-NLS-1$
    stockIdMapping.put("EX-PAR", new Integer(194)); //$NON-NLS-1$
    stockIdMapping.put("FIA", new Integer(325)); //$NON-NLS-1$
    stockIdMapping.put("FISV", new Integer(75)); //$NON-NLS-1$
    stockIdMapping.put("FITB", new Integer(716)); //$NON-NLS-1$
    stockIdMapping.put("FLE-PAR", new Integer(235)); //$NON-NLS-1$
    stockIdMapping.put("FLEX", new Integer(103)); //$NON-NLS-1$
    stockIdMapping.put("FNM", new Integer(717)); //$NON-NLS-1$
    stockIdMapping.put("FPU", new Integer(123)); //$NON-NLS-1$
    stockIdMapping.put("FRE", new Integer(718)); //$NON-NLS-1$
    stockIdMapping.put("FRX", new Integer(719)); //$NON-NLS-1$
    stockIdMapping.put("FTE-PAR", new Integer(183)); //$NON-NLS-1$
    stockIdMapping.put("FUJIY", new Integer(320)); //$NON-NLS-1$
    stockIdMapping.put("GAM-PAR", new Integer(237)); //$NON-NLS-1$
    stockIdMapping.put("GBB-PAR", new Integer(233)); //$NON-NLS-1$
    stockIdMapping.put("GCI", new Integer(720)); //$NON-NLS-1$
    stockIdMapping.put("GD", new Integer(721)); //$NON-NLS-1$
    stockIdMapping.put("GDT", new Integer(722)); //$NON-NLS-1$
    stockIdMapping.put("GDW", new Integer(723)); //$NON-NLS-1$
    stockIdMapping.put("GE", new Integer(42)); //$NON-NLS-1$
    stockIdMapping.put("GENZ", new Integer(77)); //$NON-NLS-1$
    stockIdMapping.put("GFC-PAR", new Integer(232)); //$NON-NLS-1$
    stockIdMapping.put("GILD", new Integer(71)); //$NON-NLS-1$
    stockIdMapping.put("GL-PAR", new Integer(236)); //$NON-NLS-1$
    stockIdMapping.put("GM", new Integer(43)); //$NON-NLS-1$
    stockIdMapping.put("GMN-LON", new Integer(244)); //$NON-NLS-1$
    stockIdMapping.put("GOOGLE", new Integer(766)); //$NON-NLS-1$
    stockIdMapping.put("GS", new Integer(724)); //$NON-NLS-1$
    stockIdMapping.put("GSK-LON", new Integer(146)); //$NON-NLS-1$
    stockIdMapping.put("HD", new Integer(45)); //$NON-NLS-1$
    stockIdMapping.put("HDI", new Integer(725)); //$NON-NLS-1$
    stockIdMapping.put("HIT", new Integer(312)); //$NON-NLS-1$
    stockIdMapping.put("HKL-FRA", new Integer(174)); //$NON-NLS-1$
    stockIdMapping.put("HMC", new Integer(313)); //$NON-NLS-1$
    stockIdMapping.put("HOE-FRA", new Integer(175)); //$NON-NLS-1$
    stockIdMapping.put("HON", new Integer(46)); //$NON-NLS-1$
    stockIdMapping.put("HOV", new Integer(726)); //$NON-NLS-1$
    stockIdMapping.put("HPQ", new Integer(44)); //$NON-NLS-1$
    stockIdMapping.put("HSBA-LON", new Integer(149)); //$NON-NLS-1$
    stockIdMapping.put("HYUA-LSIN", new Integer(265)); //$NON-NLS-1$
    stockIdMapping.put("IACI", new Integer(84)); //$NON-NLS-1$
    stockIdMapping.put("IBM", new Integer(47)); //$NON-NLS-1$
    stockIdMapping.put("ICDD-LON", new Integer(150)); //$NON-NLS-1$
    stockIdMapping.put("ICOS", new Integer(727)); //$NON-NLS-1$
    stockIdMapping.put("IMCL", new Integer(728)); //$NON-NLS-1$
    stockIdMapping.put("IMI", new Integer(328)); //$NON-NLS-1$
    stockIdMapping.put("IMT-LON", new Integer(245)); //$NON-NLS-1$
    stockIdMapping.put("INGDF-AMS", new Integer(211)); //$NON-NLS-1$
    stockIdMapping.put("INTC", new Integer(99)); //$NON-NLS-1$
    stockIdMapping.put("INTU", new Integer(89)); //$NON-NLS-1$
    stockIdMapping.put("IP", new Integer(48)); //$NON-NLS-1$
    stockIdMapping.put("IVGN", new Integer(729)); //$NON-NLS-1$
    stockIdMapping.put("JNJ", new Integer(49)); //$NON-NLS-1$
    stockIdMapping.put("JPI-LON", new Integer(139)); //$NON-NLS-1$
    stockIdMapping.put("JPM", new Integer(53)); //$NON-NLS-1$
    stockIdMapping.put("KGF-LON", new Integer(151)); //$NON-NLS-1$
    stockIdMapping.put("KLAC", new Integer(76)); //$NON-NLS-1$
    stockIdMapping.put("KLM-AMS", new Integer(212)); //$NON-NLS-1$
    stockIdMapping.put("KMRT", new Integer(730)); //$NON-NLS-1$
    stockIdMapping.put("KNEB-FRA", new Integer(284)); //$NON-NLS-1$
    stockIdMapping.put("KO", new Integer(37)); //$NON-NLS-1$
    stockIdMapping.put("KPWD-LSIN", new Integer(266)); //$NON-NLS-1$
    stockIdMapping.put("KSS", new Integer(731)); //$NON-NLS-1$
    stockIdMapping.put("LEH", new Integer(732)); //$NON-NLS-1$
    stockIdMapping.put("LFC", new Integer(733)); //$NON-NLS-1$
    stockIdMapping.put("LG-PAR", new Integer(198)); //$NON-NLS-1$
    stockIdMapping.put("LKOA-LSIN", new Integer(258)); //$NON-NLS-1$
    stockIdMapping.put("LLOY-LON", new Integer(152)); //$NON-NLS-1$
    stockIdMapping.put("LLTC", new Integer(87)); //$NON-NLS-1$
    stockIdMapping.put("LLY", new Integer(734)); //$NON-NLS-1$
    stockIdMapping.put("LOW", new Integer(735)); //$NON-NLS-1$
    stockIdMapping.put("LRCX", new Integer(736)); //$NON-NLS-1$
    stockIdMapping.put("LXK", new Integer(737)); //$NON-NLS-1$
    stockIdMapping.put("Light", new Integer(504)); //$NON-NLS-1$
    stockIdMapping.put("MASN-SWX", new Integer(248)); //$NON-NLS-1$
    stockIdMapping.put("MC", new Integer(315)); //$NON-NLS-1$
    stockIdMapping.put("MCD", new Integer(50)); //$NON-NLS-1$
    stockIdMapping.put("MCHP", new Integer(738)); //$NON-NLS-1$
    stockIdMapping.put("MEDI", new Integer(66)); //$NON-NLS-1$
    stockIdMapping.put("MERQ", new Integer(739)); //$NON-NLS-1$
    stockIdMapping.put("MITSY", new Integer(310)); //$NON-NLS-1$
    stockIdMapping.put("MKS-LON", new Integer(153)); //$NON-NLS-1$
    stockIdMapping.put("ML-PAR", new Integer(192)); //$NON-NLS-1$
    stockIdMapping.put("MLEA", new Integer(318)); //$NON-NLS-1$
    stockIdMapping.put("MLW-LON", new Integer(246)); //$NON-NLS-1$
    stockIdMapping.put("MMM", new Integer(52)); //$NON-NLS-1$
    stockIdMapping.put("MNST", new Integer(740)); //$NON-NLS-1$
    stockIdMapping.put("MO", new Integer(54)); //$NON-NLS-1$
    stockIdMapping.put("MPP", new Integer(118)); //$NON-NLS-1$
    stockIdMapping.put("MRK", new Integer(51)); //$NON-NLS-1$
    stockIdMapping.put("MRVL", new Integer(741)); //$NON-NLS-1$
    stockIdMapping.put("MSFT", new Integer(100)); //$NON-NLS-1$
    stockIdMapping.put("MTF", new Integer(309)); //$NON-NLS-1$
    stockIdMapping.put("MWD", new Integer(742)); //$NON-NLS-1$
    stockIdMapping.put("MX", new Integer(285)); //$NON-NLS-1$
    stockIdMapping.put("MXIM", new Integer(92)); //$NON-NLS-1$
    stockIdMapping.put("NEM", new Integer(743)); //$NON-NLS-1$
    stockIdMapping.put("NESN-SWX", new Integer(218)); //$NON-NLS-1$
    stockIdMapping.put("NEX-LON", new Integer(242)); //$NON-NLS-1$
    stockIdMapping.put("NFLX", new Integer(744)); //$NON-NLS-1$
    stockIdMapping.put("NOC", new Integer(745)); //$NON-NLS-1$
    stockIdMapping.put("NOK", new Integer(280)); //$NON-NLS-1$
    stockIdMapping.put("NSANY", new Integer(314)); //$NON-NLS-1$
    stockIdMapping.put("NTAP", new Integer(746)); //$NON-NLS-1$
    stockIdMapping.put("NTE", new Integer(747)); //$NON-NLS-1$
    stockIdMapping.put("NTES", new Integer(748)); //$NON-NLS-1$
    stockIdMapping.put("NTT", new Integer(311)); //$NON-NLS-1$
    stockIdMapping.put("NVLS", new Integer(749)); //$NON-NLS-1$
    stockIdMapping.put("NXTL", new Integer(90)); //$NON-NLS-1$
    stockIdMapping.put("OHB", new Integer(115)); //$NON-NLS-1$
    stockIdMapping.put("OMV-LSIN", new Integer(297)); //$NON-NLS-1$
    stockIdMapping.put("OR-PAR", new Integer(191)); //$NON-NLS-1$
    stockIdMapping.put("ORCL", new Integer(94)); //$NON-NLS-1$
    stockIdMapping.put("OVTI", new Integer(750)); //$NON-NLS-1$
    stockIdMapping.put("PAYX", new Integer(80)); //$NON-NLS-1$
    stockIdMapping.put("PCAR", new Integer(64)); //$NON-NLS-1$
    stockIdMapping.put("PG", new Integer(55)); //$NON-NLS-1$
    stockIdMapping.put("PHIA-AMS", new Integer(215)); //$NON-NLS-1$
    stockIdMapping.put("PIN-BRU", new Integer(206)); //$NON-NLS-1$
    stockIdMapping.put("PSFT", new Integer(73)); //$NON-NLS-1$
    stockIdMapping.put("QCOM", new Integer(98)); //$NON-NLS-1$
    stockIdMapping.put("QLGC", new Integer(751)); //$NON-NLS-1$
    stockIdMapping.put("QQQ", new Integer(329)); //$NON-NLS-1$
    stockIdMapping.put("RBACW-AMS", new Integer(213)); //$NON-NLS-1$
    stockIdMapping.put("RBS-LON", new Integer(141)); //$NON-NLS-1$
    stockIdMapping.put("RDA-AMS", new Integer(214)); //$NON-NLS-1$
    stockIdMapping.put("RGX", new Integer(117)); //$NON-NLS-1$
    stockIdMapping.put("RIMM", new Integer(752)); //$NON-NLS-1$
    stockIdMapping.put("RMBS", new Integer(753)); //$NON-NLS-1$
    stockIdMapping.put("RNO-PAR", new Integer(193)); //$NON-NLS-1$
    stockIdMapping.put("RO-SWX", new Integer(223)); //$NON-NLS-1$
    stockIdMapping.put("ROS", new Integer(321)); //$NON-NLS-1$
    stockIdMapping.put("RR.-LON", new Integer(156)); //$NON-NLS-1$
    stockIdMapping.put("RTR-LON", new Integer(155)); //$NON-NLS-1$
    stockIdMapping.put("RUKN-SWX", new Integer(224)); //$NON-NLS-1$
    stockIdMapping.put("RY", new Integer(253)); //$NON-NLS-1$
    stockIdMapping.put("S", new Integer(754)); //$NON-NLS-1$
    stockIdMapping.put("SANYY", new Integer(319)); //$NON-NLS-1$
    stockIdMapping.put("SBC", new Integer(56)); //$NON-NLS-1$
    stockIdMapping.put("SBRY-LON", new Integer(138)); //$NON-NLS-1$
    stockIdMapping.put("SBUX", new Integer(85)); //$NON-NLS-1$
    stockIdMapping.put("SEO", new Integer(282)); //$NON-NLS-1$
    stockIdMapping.put("SFW-LON", new Integer(137)); //$NON-NLS-1$
    stockIdMapping.put("SGA", new Integer(116)); //$NON-NLS-1$
    stockIdMapping.put("SGGD-LSIN", new Integer(259)); //$NON-NLS-1$
    stockIdMapping.put("SIE-FRA", new Integer(177)); //$NON-NLS-1$
    stockIdMapping.put("SIF-LSIN", new Integer(261)); //$NON-NLS-1$
    stockIdMapping.put("SINA", new Integer(755)); //$NON-NLS-1$
    stockIdMapping.put("SMH", new Integer(332)); //$NON-NLS-1$
    stockIdMapping.put("SMSD-LSIN", new Integer(264)); //$NON-NLS-1$
    stockIdMapping.put("SNDK", new Integer(756)); //$NON-NLS-1$
    stockIdMapping.put("SNE", new Integer(308)); //$NON-NLS-1$
    stockIdMapping.put("SNP", new Integer(324)); //$NON-NLS-1$
    stockIdMapping.put("SNPS", new Integer(757)); //$NON-NLS-1$
    stockIdMapping.put("SOF-FRA", new Integer(239)); //$NON-NLS-1$
    stockIdMapping.put("SOHU", new Integer(758)); //$NON-NLS-1$
    stockIdMapping.put("SPLS", new Integer(61)); //$NON-NLS-1$
    stockIdMapping.put("SPY", new Integer(331)); //$NON-NLS-1$
    stockIdMapping.put("SYMC", new Integer(63)); //$NON-NLS-1$
    stockIdMapping.put("T", new Integer(33)); //$NON-NLS-1$
    stockIdMapping.put("TEVA", new Integer(68)); //$NON-NLS-1$
    stockIdMapping.put("TKA-FRA", new Integer(178)); //$NON-NLS-1$
    stockIdMapping.put("TNT", new Integer(322)); //$NON-NLS-1$
    stockIdMapping.put("TRP", new Integer(254)); //$NON-NLS-1$
    stockIdMapping.put("TSCO-LON", new Integer(158)); //$NON-NLS-1$
    stockIdMapping.put("TSM", new Integer(307)); //$NON-NLS-1$
    stockIdMapping.put("TTWO", new Integer(759)); //$NON-NLS-1$
    stockIdMapping.put("UBSN-SWX", new Integer(219)); //$NON-NLS-1$
    stockIdMapping.put("UG-PAR", new Integer(195)); //$NON-NLS-1$
    stockIdMapping.put("ULVR-LON", new Integer(159)); //$NON-NLS-1$
    stockIdMapping.put("UPM", new Integer(281)); //$NON-NLS-1$
    stockIdMapping.put("UTSI", new Integer(760)); //$NON-NLS-1$
    stockIdMapping.put("UTX", new Integer(57)); //$NON-NLS-1$
    stockIdMapping.put("VK-PAR", new Integer(234)); //$NON-NLS-1$
    stockIdMapping.put("VOD-LON", new Integer(136)); //$NON-NLS-1$
    stockIdMapping.put("VOW-FRA", new Integer(179)); //$NON-NLS-1$
    stockIdMapping.put("VRTS", new Integer(67)); //$NON-NLS-1$
    stockIdMapping.put("WFMI", new Integer(761)); //$NON-NLS-1$
    stockIdMapping.put("WLP", new Integer(762)); //$NON-NLS-1$
    stockIdMapping.put("WMT", new Integer(58)); //$NON-NLS-1$
    stockIdMapping.put("WY", new Integer(763)); //$NON-NLS-1$
    stockIdMapping.put("XLNX", new Integer(79)); //$NON-NLS-1$
    stockIdMapping.put("XMSR", new Integer(764)); //$NON-NLS-1$
    stockIdMapping.put("XOM", new Integer(41)); //$NON-NLS-1$
    stockIdMapping.put("YHOO", new Integer(765)); //$NON-NLS-1$
    stockIdMapping.put("YUK-LSIN", new Integer(260)); //$NON-NLS-1$
    stockIdMapping.put("AUD/JPY", new Integer(60)); //$NON-NLS-1$
    stockIdMapping.put("AUD/USD", new Integer(10)); //$NON-NLS-1$
    stockIdMapping.put("CHF/JPY", new Integer(521)); //$NON-NLS-1$
    stockIdMapping.put("EUR/CHF", new Integer(511)); //$NON-NLS-1$
    stockIdMapping.put("EUR/GBP", new Integer(510)); //$NON-NLS-1$
    stockIdMapping.put("EUR/JPY", new Integer(509)); //$NON-NLS-1$
    stockIdMapping.put("EUR/SAR", new Integer(20)); //$NON-NLS-1$
    stockIdMapping.put("EUR/SEK", new Integer(29)); //$NON-NLS-1$
    stockIdMapping.put("EUR/USD", new Integer(1)); //$NON-NLS-1$
    stockIdMapping.put("GBP/CHF", new Integer(518)); //$NON-NLS-1$
    stockIdMapping.put("GBP/EUR", new Integer(516)); //$NON-NLS-1$
    stockIdMapping.put("GBP/JOD", new Integer(532)); //$NON-NLS-1$
    stockIdMapping.put("GBP/JPY", new Integer(517)); //$NON-NLS-1$
    stockIdMapping.put("GBP/SAR", new Integer(543)); //$NON-NLS-1$
    stockIdMapping.put("GBP/USD", new Integer(2)); //$NON-NLS-1$
    stockIdMapping.put("Gold", new Integer(333)); //$NON-NLS-1$
    stockIdMapping.put("JPY/CHF", new Integer(515)); //$NON-NLS-1$
    stockIdMapping.put("NZD/USD", new Integer(11)); //$NON-NLS-1$
    stockIdMapping.put("Palladium", new Integer(336)); //$NON-NLS-1$
    stockIdMapping.put("Platinum", new Integer(335)); //$NON-NLS-1$
    stockIdMapping.put("Silver", new Integer(334)); //$NON-NLS-1$
    stockIdMapping.put("USD/CAD", new Integer(9)); //$NON-NLS-1$
    stockIdMapping.put("USD/CHF", new Integer(3)); //$NON-NLS-1$
    stockIdMapping.put("USD/DKK", new Integer(13)); //$NON-NLS-1$
    stockIdMapping.put("USD/EGP", new Integer(5)); //$NON-NLS-1$
    stockIdMapping.put("USD/JOD", new Integer(6)); //$NON-NLS-1$
    stockIdMapping.put("USD/JPY", new Integer(4)); //$NON-NLS-1$
    stockIdMapping.put("USD/NOK", new Integer(12)); //$NON-NLS-1$
    stockIdMapping.put("USD/QAR", new Integer(7)); //$NON-NLS-1$
    stockIdMapping.put("USD/SAR", new Integer(15)); //$NON-NLS-1$
    stockIdMapping.put("USD/SEK", new Integer(14)); //$NON-NLS-1$
    stockIdMapping.put("USD/SGD", new Integer(30)); //$NON-NLS-1$
    stockIdMapping.put("USD/TND", new Integer(16)); //$NON-NLS-1$
    stockIdMapping.put("USD/ZAR", new Integer(74)); //$NON-NLS-1$
    stockIdMapping.put("AMMEKS", new Integer(27)); //$NON-NLS-1$
    stockIdMapping.put("CAAC-40", new Integer(24)); //$NON-NLS-1$
    stockIdMapping.put("D", new Integer(21)); //$NON-NLS-1$
    stockIdMapping.put("DAAX", new Integer(25)); //$NON-NLS-1$
    stockIdMapping.put("DJE50XX", new Integer(503)); //$NON-NLS-1$
    stockIdMapping.put("Futsee-100", new Integer(345)); //$NON-NLS-1$
    stockIdMapping.put("H-Kong", new Integer(502)); //$NON-NLS-1$
    stockIdMapping.put("N225Jap", new Integer(500)); //$NON-NLS-1$
    stockIdMapping.put("NQ-100", new Integer(23)); //$NON-NLS-1$
    stockIdMapping.put("NQ-comp", new Integer(26)); //$NON-NLS-1$
    stockIdMapping.put("Nizee-comp", new Integer(28)); //$NON-NLS-1$
    stockIdMapping.put("OIXXoil", new Integer(341)); //$NON-NLS-1$
    stockIdMapping.put("SC-Korea", new Integer(501)); //$NON-NLS-1$
    stockIdMapping.put("SWMI", new Integer(225)); //$NON-NLS-1$
    stockIdMapping.put("SandP-500", new Integer(22)); //$NON-NLS-1$
    stockIdMapping.put("EUROrand", new Integer(339)); //$NON-NLS-1$
    stockIdMapping.put("Geiger", new Integer(8)); //$NON-NLS-1$
    stockIdMapping.put("Pseudo", new Integer(338)); //$NON-NLS-1$
    stockIdMapping.put("Pseudo1", new Integer(340)); //$NON-NLS-1$
    stockIdMapping.put("CHF/EUR", new Integer(520)); //$NON-NLS-1$
    stockIdMapping.put("CHF/GBP", new Integer(522)); //$NON-NLS-1$
    stockIdMapping.put("CHF/USD", new Integer(519)); //$NON-NLS-1$
    stockIdMapping.put("EGP/CHF", new Integer(527)); //$NON-NLS-1$
    stockIdMapping.put("EGP/EUR", new Integer(524)); //$NON-NLS-1$
    stockIdMapping.put("EGP/GBP", new Integer(526)); //$NON-NLS-1$
    stockIdMapping.put("EGP/JPY", new Integer(525)); //$NON-NLS-1$
    stockIdMapping.put("EGP/USD", new Integer(523)); //$NON-NLS-1$
    stockIdMapping.put("EUR/EGP", new Integer(17)); //$NON-NLS-1$
    stockIdMapping.put("EUR/JOD", new Integer(18)); //$NON-NLS-1$
    stockIdMapping.put("EUR/QAR", new Integer(19)); //$NON-NLS-1$
    stockIdMapping.put("EUR/TND", new Integer(59)); //$NON-NLS-1$
    stockIdMapping.put("JOD/CHF", new Integer(533)); //$NON-NLS-1$
    stockIdMapping.put("JOD/EUR", new Integer(529)); //$NON-NLS-1$
    stockIdMapping.put("JOD/GBP", new Integer(531)); //$NON-NLS-1$
    stockIdMapping.put("JOD/JPY", new Integer(530)); //$NON-NLS-1$
    stockIdMapping.put("JOD/USD", new Integer(528)); //$NON-NLS-1$
    stockIdMapping.put("JPY/EUR", new Integer(513)); //$NON-NLS-1$
    stockIdMapping.put("JPY/GBP", new Integer(514)); //$NON-NLS-1$
    stockIdMapping.put("JPY/USD", new Integer(512)); //$NON-NLS-1$
    stockIdMapping.put("QAR/CHF", new Integer(538)); //$NON-NLS-1$
    stockIdMapping.put("QAR/EUR", new Integer(534)); //$NON-NLS-1$
    stockIdMapping.put("QAR/GBP", new Integer(537)); //$NON-NLS-1$
    stockIdMapping.put("QAR/JPY", new Integer(536)); //$NON-NLS-1$
    stockIdMapping.put("QAR/USD", new Integer(535)); //$NON-NLS-1$
    stockIdMapping.put("SAR/CHF", new Integer(544)); //$NON-NLS-1$
    stockIdMapping.put("SAR/EUR", new Integer(540)); //$NON-NLS-1$
    stockIdMapping.put("SAR/GBP", new Integer(542)); //$NON-NLS-1$
    stockIdMapping.put("SAR/JPY", new Integer(541)); //$NON-NLS-1$
    stockIdMapping.put("SAR/USD", new Integer(539)); //$NON-NLS-1$
    stockIdMapping.put("TND/CHF", new Integer(549)); //$NON-NLS-1$
    stockIdMapping.put("TND/EUR", new Integer(546)); //$NON-NLS-1$
    stockIdMapping.put("TND/GBP", new Integer(548)); //$NON-NLS-1$
    stockIdMapping.put("TND/JPY", new Integer(547)); //$NON-NLS-1$
    stockIdMapping.put("TND/USD", new Integer(545)); //$NON-NLS-1$
    stockIdMapping.put("USD/EUR", new Integer(507)); //$NON-NLS-1$
    stockIdMapping.put("USD/GBP", new Integer(508)); //$NON-NLS-1$
  }
}