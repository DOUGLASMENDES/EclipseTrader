/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
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
    @Override
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.ISplit#getNewQuantity()
     */
    @Override
    public Double getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(double newQuantity) {
        this.newQuantity = newQuantity;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.ISplit#getOldQuantity()
     */
    @Override
    public Double getOldQuantity() {
        return oldQuantity;
    }

    public void setOldQuantity(double oldQuantity) {
        this.oldQuantity = oldQuantity;
    }
}
