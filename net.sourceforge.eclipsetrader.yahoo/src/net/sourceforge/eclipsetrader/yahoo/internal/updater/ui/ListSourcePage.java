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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.eclipsetrader.yahoo.internal.updater.AbstractListUpdateJob;
import net.sourceforge.eclipsetrader.yahoo.internal.updater.FrenchListUpdateJob;
import net.sourceforge.eclipsetrader.yahoo.internal.updater.GermanyListUpdateJob;
import net.sourceforge.eclipsetrader.yahoo.internal.updater.USListUpdateJob;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class ListSourcePage extends WizardPage {
	CheckboxTableViewer viewer;

	SelectionAdapter selectionListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			setPageComplete(isPageComplete());
		}
	};

	public ListSourcePage() {
		super("source", Messages.ListSourcePage_Title, null); //$NON-NLS-1$
		setDescription(Messages.ListSourcePage_Description);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(1, false));
		setControl(content);
		
		viewer = CheckboxTableViewer.newCheckList(content, SWT.FULL_SELECTION|SWT.BORDER);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
	            return ((Job)element).getName();
            }
		});
		viewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
	            return ((Job)e1).getName().compareTo(((Job)e2).getName());
            }
		});
		viewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
            	setPageComplete(isPageComplete());
            }
		});

		List<AbstractListUpdateJob> input = new ArrayList<AbstractListUpdateJob>();
		input.add(new USListUpdateJob());
		input.add(new GermanyListUpdateJob());
		input.add(new FrenchListUpdateJob());
		viewer.setInput(input);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
	 */
	public boolean isPageComplete() {
		return viewer.getCheckedElements().length != 0;
	}
	
	public AbstractListUpdateJob[] getSelectedJobs() {
		Object[] o = viewer.getCheckedElements();
		AbstractListUpdateJob[] jobs = new AbstractListUpdateJob[o.length];
		System.arraycopy(o, 0, jobs, 0, jobs.length);
		return jobs;
	}
}
