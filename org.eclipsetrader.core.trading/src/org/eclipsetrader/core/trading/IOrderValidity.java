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

package org.eclipsetrader.core.trading;

import org.eclipsetrader.core.internal.trading.OrderValidity;

public interface IOrderValidity {

    public static final IOrderValidity Day = new OrderValidity("day", Messages.IOrderValidity_Day); //$NON-NLS-1$
    public static final IOrderValidity ImmediateOrCancel = new OrderValidity("immediate-or-cancel", Messages.IOrderValidity_ImmediateOrCancel); //$NON-NLS-1$
    public static final IOrderValidity AtOpening = new OrderValidity("at-opening", Messages.IOrderValidity_AtOpening); //$NON-NLS-1$
    public static final IOrderValidity AtClosing = new OrderValidity("at-closing", Messages.IOrderValidity_AtClosing); //$NON-NLS-1$
    public static final IOrderValidity GoodTillCancel = new OrderValidity("good-till-cancel", Messages.IOrderValidity_GoodTillCancel); //$NON-NLS-1$
    public static final IOrderValidity GoodTillDate = new OrderValidity("good-till-date", Messages.IOrderValidity_GoodTillDate); //$NON-NLS-1$

    public String getId();

    public String getName();
}
