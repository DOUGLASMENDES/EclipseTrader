/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.core.db;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class SecurityGroup extends PersistentObject {
	String code = ""; //$NON-NLS-1$

	String description = ""; //$NON-NLS-1$

	Currency currency;

	SecurityGroup parentGroup;

	List<PersistentObject> childrens = new ArrayList<PersistentObject>();

	public SecurityGroup() {
	}

	public SecurityGroup(Integer id) {
		super(id);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		setChanged();
	}

	public Currency getCurrency() {
		if (currency == null && parentGroup != null)
			return parentGroup.getCurrency();
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public SecurityGroup getParentGroup() {
		return parentGroup;
	}

	public void setParentGroup(SecurityGroup group) {
		if (group != null && !group.equals(this.parentGroup))
			setChanged();
		else if (group == null && this.parentGroup != null)
			setChanged();

		if (this.parentGroup != null)
			this.parentGroup.childrens.remove(this);

		this.parentGroup = group;

		if (this.parentGroup != null)
			this.parentGroup.childrens.add(this);
	}

	public List<PersistentObject> getChildrens() {
		return childrens;
	}

	public void setChildrens(List<PersistentObject> childrens) {
		this.childrens = childrens;
	}
}
