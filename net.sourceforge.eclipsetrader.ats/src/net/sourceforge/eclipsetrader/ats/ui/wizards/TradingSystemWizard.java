/*
 * Copyright (c) 2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.ats.ui.wizards;

import org.eclipse.jface.wizard.Wizard;

public class TradingSystemWizard extends Wizard {
	private SystemSelectionPage selectionPage = new SystemSelectionPage();

	public TradingSystemWizard() {
	}

	@Override
	public String getWindowTitle() {
		return "New Trade System";
	}

	@Override
	public void addPages() {
		addPage(selectionPage);
		setForcePreviousAndNextButtons(true);
	}

	@Override
	public boolean canFinish() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		return false;
	}
}
