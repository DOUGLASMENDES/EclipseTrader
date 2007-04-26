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

package net.sourceforge.eclipsetrader.ats.core.db.visitors;

import net.sourceforge.eclipsetrader.ats.core.db.Strategy;
import net.sourceforge.eclipsetrader.ats.core.db.TradingSystem;

public interface ITradingSystemVisitor {

	public void visit(TradingSystem obj);

	public void visit(Strategy obj);
}
