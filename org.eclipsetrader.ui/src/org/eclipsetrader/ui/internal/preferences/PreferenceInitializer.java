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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipsetrader.ui.internal.UIActivator;

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
    }
}
