/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.views;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IDataProviderFactory;
import org.eclipsetrader.core.views.IWatchListColumn;

public class WatchListViewColumn {
	private IWatchListColumn reference;

	private String name;
	private IDataProviderFactory dataProviderFactory;

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public WatchListViewColumn(IWatchListColumn reference) {
		this.reference = reference;
	    this.name = reference.getName();
	    this.dataProviderFactory = reference.getDataProviderFactory();
	}

	public WatchListViewColumn(IColumn column) {
	    this.name = column.getName();
	    this.dataProviderFactory = column.getDataProviderFactory();
	}

	public WatchListViewColumn(String name, IDataProviderFactory dataProviderFactory) {
	    this.name = name;
	    this.dataProviderFactory = dataProviderFactory;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
    	Object oldValue = this.name;
		this.name = name;
		propertyChangeSupport.firePropertyChange(IWatchListColumn.NAME, oldValue, this.name);
	}

	public IDataProviderFactory getDataProviderFactory() {
		return dataProviderFactory;
	}

	public IWatchListColumn getReference() {
    	return reference;
    }

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}
}
