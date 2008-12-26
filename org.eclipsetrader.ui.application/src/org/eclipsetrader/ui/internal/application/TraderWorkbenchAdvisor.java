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

package org.eclipsetrader.ui.internal.application;

import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class TraderWorkbenchAdvisor extends WorkbenchAdvisor {
	private WorkbenchExceptionHandler exceptionHandler;

	/* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#initialize(org.eclipse.ui.application.IWorkbenchConfigurer)
     */
    @Override
    public void initialize(IWorkbenchConfigurer configurer) {
    	exceptionHandler = new WorkbenchExceptionHandler(configurer);
    	configurer.setSaveAndRestore(true);
    	super.initialize(configurer);
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#createWorkbenchWindowAdvisor(org.eclipse.ui.application.IWorkbenchWindowConfigurer)
     */
    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new TraderWorkbenchWindowAdvisor(configurer);
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#getInitialWindowPerspectiveId()
     */
    @Override
    public String getInitialWindowPerspectiveId() {
		// Use the perspective id defined in plugin_customization.ini
		return null;
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#getMainPreferencePageId()
     */
    @Override
    public String getMainPreferencePageId() {
	    return "org.eclipse.ui.preferencePages.Workbench";
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#eventLoopException(java.lang.Throwable)
     */
    @Override
    public void eventLoopException(Throwable exception) {
		super.eventLoopException(exception);
    	if (exceptionHandler != null)
    		exceptionHandler.handleException(exception);
		else {
			if (getWorkbenchConfigurer() != null)
				getWorkbenchConfigurer().emergencyClose();
		}
    }
}
