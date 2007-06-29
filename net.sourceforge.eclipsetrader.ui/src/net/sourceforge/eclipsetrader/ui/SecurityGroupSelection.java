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

package net.sourceforge.eclipsetrader.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.SecurityGroup;

import org.eclipse.jface.viewers.IStructuredSelection;

public class SecurityGroupSelection implements IStructuredSelection {
	private List<SecurityGroup> selection = new ArrayList<SecurityGroup>();

	public SecurityGroupSelection(SecurityGroup group) {
		selection = new ArrayList<SecurityGroup>();
		selection.add(group);
	}

	public SecurityGroupSelection(SecurityGroup[] groups) {
		selection = new ArrayList<SecurityGroup>();
		selection.addAll(Arrays.asList(groups));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredSelection#getFirstElement()
	 */
	public Object getFirstElement() {
		return selection.size() != 0 ? selection.get(0) : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredSelection#iterator()
	 */
	public Iterator iterator() {
		return new ArrayList(selection).iterator();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredSelection#size()
	 */
	public int size() {
		return selection.size();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredSelection#toArray()
	 */
	public Object[] toArray() {
		return selection.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredSelection#toList()
	 */
	public List toList() {
		return new ArrayList(selection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelection#isEmpty()
	 */
	public boolean isEmpty() {
		return selection.size() == 0;
	}

	public Security[] getAllSecurities() {
		List<Security> list = new ArrayList<Security>();
		for (SecurityGroup group : selection)
			buildSecuritiesList(list, group);
		return (Security[]) list.toArray(new Security[list.size()]);
	}

	protected void buildSecuritiesList(List<Security> list, Object root) {
		if (root instanceof Security)
			list.add((Security) root);
		else if (root instanceof SecurityGroup) {
			for (Object child : ((SecurityGroup) root).getChildrens())
				buildSecuritiesList(list, child);
		} else if (root instanceof Object[]) {
			Object[] array = (Object[]) root;
			for (int i = 0; i < array.length; i++)
				buildSecuritiesList(list, array[i]);
		}
	}
}
