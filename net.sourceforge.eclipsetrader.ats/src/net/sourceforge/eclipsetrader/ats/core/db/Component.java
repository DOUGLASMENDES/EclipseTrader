/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.ats.core.db;

import net.sourceforge.eclipsetrader.core.db.PersistentObject;
import net.sourceforge.eclipsetrader.core.db.PersistentPreferenceStore;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class Component extends PersistentObject {
	String pluginId;

	PersistentPreferenceStore preferences = new PersistentPreferenceStore();

	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			setChanged();
		}
	};

	public Component() {
		preferences.addPropertyChangeListener(propertyChangeListener);
	}

	public Component(Integer id) {
		super(id);
	}

	public Component(String pluginId) {
		this.pluginId = pluginId;
	}

	public String getPluginId() {
		return pluginId;
	}

	public void setPluginId(String pluginId) {
		if (this.pluginId != pluginId) {
			this.pluginId = pluginId;
			setChanged();
		}
	}

	public PersistentPreferenceStore getPreferences() {
		return preferences;
	}
}
