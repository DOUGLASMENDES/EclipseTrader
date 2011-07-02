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

package org.eclipsetrader.ui.internal.markets;

import java.text.DateFormat;
import java.util.Calendar;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketDay;
import org.eclipsetrader.ui.UIConstants;
import org.eclipsetrader.ui.internal.UIActivator;

public class MarketLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider {

    private Image market;
    private Color closedColor = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);

    public MarketLabelProvider() {
        if (UIActivator.getDefault() != null) {
            market = UIActivator.getDefault().getImageRegistry().get(UIConstants.MARKET_OBJECT);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (columnIndex == 0) {
            if (element instanceof IMarket) {
                return market;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText(Object element) {
        return getColumnText(element, 0);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof IMarket) {
            IMarket market = (IMarket) element;
            switch (columnIndex) {
                case 0:
                    return market.getName();
                case 1: {
                    IMarketDay day = market.getToday();
                    if (day.isOpen()) {
                        return day.getMessage() != null ? day.getMessage() : Messages.MarketLabelProvider_Open;
                    }
                    else {
                        if (day.getMessage() != null) {
                            return NLS.bind(Messages.MarketLabelProvider_ClosedFor, new Object[] {
                                day.getMessage()
                            });
                        }
                        else {
                            return Messages.MarketLabelProvider_Closed;
                        }
                    }
                }
                case 2: {
                    Calendar now = Calendar.getInstance();
                    IMarketDay day = market.getToday();
                    if (day.isOpen()) {
                        long secondsToClose = (day.getCloseTime().getTime() - now.getTimeInMillis()) / 1000;
                        if (secondsToClose < 60) {
                            return Messages.MarketLabelProvider_ClosesInLessThanOneMinute;
                        }
                        else {
                            long minutesToClose = secondsToClose / 60 + 1;
                            if (minutesToClose < 60) {
                                return NLS.bind(Messages.MarketLabelProvider_ClosesInMinutes, new Object[] {
                                    minutesToClose
                                });
                            }
                            else {
                                return NLS.bind(Messages.MarketLabelProvider_ClosesInHours, new Object[] {
                                        minutesToClose / 60,
                                        minutesToClose % 60
                                });
                            }
                        }
                    }
                    else {
                        Calendar midnight = Calendar.getInstance();
                        midnight.set(Calendar.HOUR_OF_DAY, 0);
                        midnight.set(Calendar.MINUTE, 0);
                        midnight.set(Calendar.SECOND, 0);
                        midnight.set(Calendar.MILLISECOND, 0);
                        midnight.add(Calendar.DATE, 1);

                        day = market.getNextDay();
                        if (day != null && day.getOpenTime() != null) {
                            long secondsToNextOpen = (day.getOpenTime().getTime() - now.getTimeInMillis()) / 1000;
                            if (secondsToNextOpen < 60) {
                                return Messages.MarketLabelProvider_OpenInLessThanOneMinute;
                            }
                            else {
                                long minutesToNextOpen = secondsToNextOpen / 60 + 1;
                                if (minutesToNextOpen < 60) {
                                    return NLS.bind(Messages.MarketLabelProvider_OpensInMinutes, new Object[] {
                                        minutesToNextOpen
                                    });
                                }
                                else if (day.getOpenTime().before(midnight.getTime()) && minutesToNextOpen < 1440) {
                                    return NLS.bind(Messages.MarketLabelProvider_OpensInHours, new Object[] {
                                            minutesToNextOpen / 60,
                                            minutesToNextOpen % 60
                                    });
                                }
                                else {
                                    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG); // Util.getDateFormat();
                                    DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
                                    return NLS.bind(Messages.MarketLabelProvider_OpenDate, new Object[] {
                                            dateFormat.format(day.getOpenTime()),
                                            timeFormat.format(day.getOpenTime()),
                                    });
                                }
                            }
                        }
                    }
                }
            }
        }
        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
     */
    @Override
    public Color getBackground(Object element) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
     */
    @Override
    public Color getForeground(Object element) {
        if (element instanceof IMarket) {
            if (!((IMarket) element).isOpen()) {
                return closedColor;
            }
        }
        return null;
    }
}
