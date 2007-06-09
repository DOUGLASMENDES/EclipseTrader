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

package net.sourceforge.eclipsetrader.yahoo.internal.updater.ui;

import net.sourceforge.eclipsetrader.yahoo.internal.updater.AbstractListUpdateJob;
import net.sourceforge.eclipsetrader.yahoo.internal.updater.ListUpdateJob;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class SecuritiesListImportWizard extends Wizard implements IImportWizard {
	ListSourcePage source;

	public SecuritiesListImportWizard() {
		setWindowTitle(Messages.SecuritiesListImportWizard_WindowTitle);
		source = new ListSourcePage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
	    addPage(source);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		AbstractListUpdateJob[] jobs = source.getSelectedJobs();
		
		ListUpdateJob job = new ListUpdateJob(jobs);
		job.setUser(true);
		job.schedule();
		
		return true;
	}
}
