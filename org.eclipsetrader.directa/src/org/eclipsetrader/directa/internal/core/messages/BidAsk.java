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

package org.eclipsetrader.directa.internal.core.messages;

public class BidAsk extends DataMessage {

    public long num_bid;
    public double bid;
    public long num_ask;
    public double ask;

    public BidAsk(byte[] packet, int i) {
        num_bid = Util.getUlong(packet, i);
        i += 4;
        bid = Util.getFloat(packet, i);
        i += 4;
        num_ask = Util.getUlong(packet, i);
        i += 4;
        ask = Util.getFloat(packet, i);
    }
}
