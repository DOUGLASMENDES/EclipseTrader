/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.yahoo.internal.news;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.htmlparser.Parser;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

public class ItalianNewsHandler implements INewsHandler {

    public ItalianNewsHandler() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.yahoo.internal.news.INewsHandler#parseNewsPages(java.net.URL[], org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public HeadLine[] parseNewsPages(URL[] url, IProgressMonitor monitor) {
        List<HeadLine> headlinesList = new ArrayList<HeadLine>();

        for (int i = 0; i < url.length && !monitor.isCanceled(); i++) {
            monitor.subTask(url[i].toString());

            try {
                Parser parser = new Parser(url[i].toString());

                NodeList list = parser.extractAllNodesThatMatch(new OrFilter(new TagNameFilter("dt"), new TagNameFilter("li"))); //$NON-NLS-1$ //$NON-NLS-2$
                for (SimpleNodeIterator iter = list.elements(); iter.hasMoreNodes();) {
                    TagNode root = (TagNode) iter.nextNode();
                    list = root.getChildren();

                    if (root.getTagName().equalsIgnoreCase("dt") && list.size() == 12) {
                        LinkTag link = (LinkTag) list.elementAt(3);

                        String source = list.elementAt(9).getText();
                        source = source.replaceAll("[\r\n]", " "); //$NON-NLS-1$ //$NON-NLS-2$
                        source = source.replaceAll("[()]", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        source = source.replaceAll("[ ]{2,}", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$

                        Date publishedDate = parseDateString(root.getNextSibling().getNextSibling().getNextSibling().getChildren().elementAt(1).getText());

                        headlinesList.add(new HeadLine(publishedDate, source, decode(link.getLinkText().trim()), null, link.getLink()));
                    }
                    else if (root.getTagName().equalsIgnoreCase("li") && list.size() == 14) {
                        LinkTag link = (LinkTag) list.elementAt(1);

                        String source = list.elementAt(6).getText();
                        source = source.replaceAll("[\r\n]", " "); //$NON-NLS-1$ //$NON-NLS-2$
                        source = source.replaceAll("[()]", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        source = source.replaceAll("[ ]{2,}", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$

                        Date publishedDate = parseDateString(list.elementAt(10).getText());

                        headlinesList.add(new HeadLine(publishedDate, source, decode(link.getLinkText().trim()), null, link.getLink()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            monitor.worked(1);
        }

        return headlinesList.toArray(new HeadLine[headlinesList.size()]);
    }

    private Date parseDateString(String date) {
        Calendar gc = Calendar.getInstance(Locale.ITALY);
        StringTokenizer st = new StringTokenizer(date, " ,:"); //$NON-NLS-1$
        st.nextToken();
        Integer vint = new Integer(st.nextToken());
        gc.set(Calendar.DAY_OF_MONTH, vint.intValue());
        gc.set(Calendar.MONTH, getMonth(st.nextToken()) - 1);
        vint = new Integer(st.nextToken());
        gc.set(Calendar.YEAR, vint.intValue());
        vint = new Integer(st.nextToken());
        gc.set(Calendar.HOUR_OF_DAY, vint.intValue());
        vint = new Integer(st.nextToken());
        gc.set(Calendar.MINUTE, vint.intValue());
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);
        return gc.getTime();
    }

    private int getMonth(String t) {
        if (t.equalsIgnoreCase("Gennaio") == true) {
            return 1;
        }
        if (t.equalsIgnoreCase("Febbraio") == true) {
            return 2;
        }
        if (t.equalsIgnoreCase("Marzo") == true) {
            return 3;
        }
        if (t.equalsIgnoreCase("Aprile") == true) {
            return 4;
        }
        if (t.equalsIgnoreCase("Maggio") == true) {
            return 5;
        }
        if (t.equalsIgnoreCase("Giugno") == true) {
            return 6;
        }
        if (t.equalsIgnoreCase("Luglio") == true) {
            return 7;
        }
        if (t.equalsIgnoreCase("Agosto") == true) {
            return 8;
        }
        if (t.equalsIgnoreCase("Settembre") == true) {
            return 9;
        }
        if (t.equalsIgnoreCase("Ottobre") == true) {
            return 10;
        }
        if (t.equalsIgnoreCase("Novembre") == true) {
            return 11;
        }
        if (t.equalsIgnoreCase("Dicembre") == true) {
            return 12;
        }

        return 0;
    }

    private String decode(String s) {
        if (s.indexOf("&#") == -1) {
            return s;
        }

        int i = 0;
        StringBuffer sb = new StringBuffer();
        byte[] bytes = new byte[0];
        try {
            bytes = s.getBytes();
        } catch (Exception e) {
            e.printStackTrace();
            bytes = s.getBytes();
        }

        while (i < bytes.length) {
            byte c = bytes[i++];
            if (c == '&' && i < bytes.length) {
                c = bytes[i++];
                if (c == '#') {
                    int data = 0;
                    while (i < bytes.length) {
                        c = bytes[i++];
                        if (c < '0' || c > '9') {
                            break;
                        }
                        data = data * 10 + c - '0';
                    }
                    if (data >= ' ') {
                        try {
                            sb.append(new String(new byte[] {
                                (byte) data
                            }));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    try {
                        sb.append('&');
                        sb.append(new String(new byte[] {
                            c
                        }));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (c >= ' ') {
                try {
                    sb.append(new String(new byte[] {
                        c
                    }));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }
}
