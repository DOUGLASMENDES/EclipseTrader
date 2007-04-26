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

package net.sourceforge.eclipsetrader.ats.ui.tradingsystem.properties;

import net.sourceforge.eclipsetrader.ats.core.db.TradingSystem;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Shell;

public class TradingSystemPropertiesDialog extends PreferenceDialog {

	public TradingSystemPropertiesDialog(Shell parentShell, TradingSystem tradingSystem) {
		super(parentShell, new PreferenceManager());

		getPreferenceManager().addToRoot(new PreferenceNode("general", new GeneralPage(tradingSystem)));
	}
}
