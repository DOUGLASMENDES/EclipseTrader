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

package org.eclipsetrader.internal.brokers.paper.schemes;

import org.eclipsetrader.internal.brokers.paper.IExpenseScheme;

public class TwoLevelsPerShareScheme implements IExpenseScheme {

    private double level1 = 0.01;
    private long level1quantity = 500;
    private double level2 = 0.005;
    private double minimum = 1.0;

    public TwoLevelsPerShareScheme() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.internal.brokers.paper.IExpenseScheme#getBuyExpenses(java.lang.Long, java.lang.Double)
     */
    @Override
    public Double getBuyExpenses(Long quantity, Double averagePrice) {
        double expenses = level1 * (quantity > level1quantity ? level1quantity : quantity);
        if (quantity > level1quantity) {
            expenses += level2 * (quantity - level1quantity);
        }
        if (expenses < minimum) {
            expenses = minimum;
        }
        return expenses;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.internal.brokers.paper.IExpenseScheme#getSellExpenses(java.lang.Long, java.lang.Double)
     */
    @Override
    public Double getSellExpenses(Long quantity, Double averagePrice) {
        double expenses = level1 * (quantity > level1quantity ? level1quantity : quantity);
        if (quantity > level1quantity) {
            expenses += level2 * (quantity - level1quantity);
        }
        if (expenses < minimum) {
            expenses = minimum;
        }
        return expenses;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return this.getClass().equals(obj.getClass());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 11 * toString().hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "2 Levels Per Share (0.01 < 500, 0.005 > 500, min=1.00)";
    }
}
