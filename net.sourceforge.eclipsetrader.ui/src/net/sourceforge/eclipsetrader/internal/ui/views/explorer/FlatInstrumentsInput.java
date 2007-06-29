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

package net.sourceforge.eclipsetrader.internal.ui.views.explorer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.SecurityGroup;

public class FlatInstrumentsInput extends InstrumentsInput {

	public FlatInstrumentsInput() {
		refresh();
	}

	@Override
	public void refresh() {
		list = new ArrayList();

		for (Iterator<SecurityGroup> iter = CorePlugin.getRepository().allSecurityGroups().iterator(); iter.hasNext();)
			list.add(iter.next());

		for (Iterator<Security> iter = CorePlugin.getRepository().allSecurities().iterator(); iter.hasNext();) {
			Security s = iter.next();
			if (s.getGroup() == null)
				list.add(s);
		}
	}
}
