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

package net.sourceforge.eclipsetrader.internal.ui;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.SecurityGroup;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class SecuritiesTreeContentProvider implements ITreeContentProvider {
	private Viewer viewer;
	
	private ICollectionObserver collectionObserver = new ICollectionObserver() {
        public void itemAdded(Object o) {
			((InstrumentsInput) viewer.getInput()).refresh();
			viewer.refresh();
        }

        public void itemRemoved(Object o) {
			((InstrumentsInput) viewer.getInput()).refresh();
			viewer.refresh();
        }
	};

	public SecuritiesTreeContentProvider() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof SecurityGroup)
			return ((SecurityGroup) parentElement).getChildrens().toArray();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof SecurityGroup)
			return ((SecurityGroup)element).getParentGroup();
		if (element instanceof Security)
			return ((Security)element).getGroup();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof SecurityGroup)
			return ((SecurityGroup) element).getChildrens().size() != 0;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof InstrumentsInput)
			return ((InstrumentsInput) inputElement).getRootItems();
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;

		if (oldInput != null && newInput == null) {
			CorePlugin.getRepository().allSecurities().addCollectionObserver(collectionObserver);
			CorePlugin.getRepository().allSecurityGroups().addCollectionObserver(collectionObserver);
		}
		
		if (oldInput == null && newInput != null) {
			CorePlugin.getRepository().allSecurities().removeCollectionObserver(collectionObserver);
			CorePlugin.getRepository().allSecurityGroups().removeCollectionObserver(collectionObserver);
		}
	}
}
