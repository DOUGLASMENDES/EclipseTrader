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

package org.eclipsetrader.ui.internal.preferences;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.charts.Period;
import org.eclipsetrader.ui.internal.charts.PeriodList;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    public PreferenceInitializer() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences node = DefaultScope.INSTANCE.getNode(UIActivator.PLUGIN_ID);
        node.putBoolean(UIActivator.PREFS_WATCHLIST_ALTERNATE_BACKGROUND, true);
        node.putBoolean(UIActivator.PREFS_WATCHLIST_ENABLE_TICK_DECORATORS, true);
        node.putBoolean(UIActivator.PREFS_WATCHLIST_DRAW_TICK_OUTLINE, true);
        node.putBoolean(UIActivator.PREFS_WATCHLIST_FADE_TO_BACKGROUND, false);

        IPreferenceStore preferences = UIActivator.getDefault().getPreferenceStore();

        PeriodList list = new PeriodList();
        list.add(new Period("2 Years", TimeSpan.years(2), TimeSpan.days(1)));
        list.add(new Period("1 Year", TimeSpan.years(1), TimeSpan.days(1)));
        list.add(new Period("6 Months (30min)", TimeSpan.months(6), TimeSpan.minutes(30)));
        list.add(new Period("3 Months (30min)", TimeSpan.months(3), TimeSpan.minutes(30)));
        list.add(new Period("1 Month (15min)", TimeSpan.months(1), TimeSpan.minutes(15)));
        list.add(new Period("5 Days (5min)", TimeSpan.days(5), TimeSpan.minutes(5)));
        list.add(new Period("1 Day (1min)", TimeSpan.days(1), TimeSpan.minutes(1)));
        try {
            preferences.setDefault(UIActivator.PREFS_CHART_PERIODS, marshal(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String marshal(PeriodList object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }
}
