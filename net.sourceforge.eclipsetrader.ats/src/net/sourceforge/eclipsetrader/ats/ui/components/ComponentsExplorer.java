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

package net.sourceforge.eclipsetrader.ats.ui.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.eclipsetrader.ats.ATSPlugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class ComponentsExplorer extends ViewPart {
	TreeViewer viewer;

	private Log log = LogFactory.getLog(getClass());

	Job updateJob = new Job("Components Explorer") {
		protected IStatus run(IProgressMonitor monitor) {
			final Collection input = getInput();
			try {
				viewer.getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							viewer.setInput(input);
						} catch (Exception e) {
							log.error(e, e);
						}
					}
				});
			} catch (Exception e) {
				log.error(e, e);
			}
			return Status.OK_STATUS;
		}
	};

	public ComponentsExplorer() {
		updateJob.setUser(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
		viewer.setContentProvider(new ComponentsContentProvider());
		viewer.setLabelProvider(new ComponentsLabelProvider());

		updateJob.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	protected Collection getInput() {
		List input = new ArrayList();

		/*        ComponentCategory category = new ComponentCategory("Entry", ATSPlugin.getDefault().getImageRegistry().get(ATSPlugin.ENTRY_COMPONENT_ICON));
		 category.components.add(new Component("High/Low Reversal"));
		 category.components.add(new Component("Key Reversal"));
		 category.components.add(new Component("Lindhal Buy"));
		 input.add(category);
		 
		 category = new ComponentCategory("Exit", ATSPlugin.getDefault().getImageRegistry().get(ATSPlugin.EXIT_COMPONENT_ICON));
		 category.components.add(new Component("High/Low Reversal"));
		 category.components.add(new Component("Key Reversal"));
		 category.components.add(new Component("Lindhal Sell"));
		 input.add(category);

		 category = new ComponentCategory("Risk", ATSPlugin.getDefault().getImageRegistry().get(ATSPlugin.RISK_COMPONENT_ICON));
		 input.add(category);

		 category = new ComponentCategory("Money", ATSPlugin.getDefault().getImageRegistry().get(ATSPlugin.MONEY_COMPONENT_ICON));
		 input.add(category);

		 category = new ComponentCategory("Exposure", ATSPlugin.getDefault().getImageRegistry().get(ATSPlugin.EXPOSURE_COMPONENT_ICON));
		 input.add(category);*/

		ComponentCategory category = new ComponentCategory("Strategy", ATSPlugin.getDefault().getImageRegistry().get(ATSPlugin.STRATEGY_ICON));
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(ATSPlugin.STRATEGIES_EXTENSION_ID);
		if (extensionPoint != null) {
			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
			for (int i = 0; i < elements.length; i++)
				category.components.add(new Component(elements[i]));
		}
		input.add(category);

		return input;
	}
}
