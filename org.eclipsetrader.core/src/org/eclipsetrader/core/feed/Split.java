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

package org.eclipsetrader.core.feed;

import java.util.Date;

public class Split implements ISplit {
	private Date date;
	private double oldQuantity;
	private double newQuantity;

	public Split(Date date, double oldQuantity, double newQuantity) {
	    this.date = date;
	    this.oldQuantity = oldQuantity;
	    this.newQuantity = newQuantity;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.ISplit#getDate()
	 */
	public Date getDate() {
		return date;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.ISplit#getNewQuantity()
	 */
	public Double getNewQuantity() {
		return newQuantity;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.ISplit#getOldQuantity()
	 */
	public Double getOldQuantity() {
		return oldQuantity;
	}
}
