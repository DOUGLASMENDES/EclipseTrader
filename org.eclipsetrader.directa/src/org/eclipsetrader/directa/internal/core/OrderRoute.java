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

package org.eclipsetrader.directa.internal.core;

import org.eclipsetrader.core.trading.IOrderRoute;

public class OrderRoute implements IOrderRoute {
	private String name;
	private String description;

	public OrderRoute(String name, String description) {
	    this.name = name;
	    this.description = description;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrderRoute#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	    return description;
    }
}
