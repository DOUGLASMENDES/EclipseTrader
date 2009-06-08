/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.internal.ui.trading;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbench;

public class NewAlertWizard extends Wizard {
	private IWorkbench workbench;
	private IStructuredSelection selection;

	public NewAlertWizard(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;

		setForcePreviousAndNextButtons(true);
		setWindowTitle(Messages.NewAlertWizard_WindowTitle);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		addPage(new AlertSelectionPage(workbench, selection));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getDefaultPageImage()
	 */
	@Override
	public Image getDefaultPageImage() {
		return Activator.getDefault().getImageRegistry().get(Activator.ALERT_WIZARD_IMAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		return true;
	}
}
