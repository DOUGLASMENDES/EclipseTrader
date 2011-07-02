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

public class Message {

    public static final int TIP_PRICE = 1;
    public static final int TIP_BOOK_5 = 2;
    public static final int TIP_BIDASK = 3;
    public static final int TIP_ASTA = 4;
    public static final int TIP_STATOVALMOB = 5;
    public static final int TIP_CHIUSURA = 6;
    public static final int TIP_INDICIFIX = 7;
    public static final int TIP_INDICIDAY = 8;
    public static final int TIP_ASTACHIUSURA = 9;
    public static final int TIP_TUTTIPREZZI = 10;
    public static final int TIP_BOOK_10 = 11;
    public static final int TIP_BOOK_15 = 13;
    public static final int TIP_BOOK_20 = 14;
    public static final int TIP_ECHO = 0x6E;

    public static DataMessage decodeMessage(byte[] arr) {
        HeaderRecord head = decodeHeader(arr);
        if (head == null) {
            return null;
        }

        switch (head.tipo) {
            case TIP_PRICE: {
                DataMessage msg = new Price(arr, head.lenHeader, head.decade);
                msg.head = head;
                return msg;
            }
            case TIP_BOOK_5: {
                DataMessage msg = new Book(arr, head.lenHeader, 0);
                msg.head = head;
                return msg;
            }
            case TIP_BOOK_10: {
                DataMessage msg = new Book(arr, head.lenHeader, 5);
                msg.head = head;
                return msg;
            }
            case TIP_BOOK_15: {
                DataMessage msg = new Book(arr, head.lenHeader, 10);
                msg.head = head;
                return msg;
            }
            case TIP_BOOK_20: {
                DataMessage msg = new Book(arr, head.lenHeader, 15);
                msg.head = head;
                return msg;
            }
            case TIP_BIDASK: {
                DataMessage msg = new BidAsk(arr, head.lenHeader);
                msg.head = head;
                return msg;
            }
            case TIP_ASTA: {
                DataMessage msg = new AstaApertura(arr, head.lenHeader, head.decade);
                msg.head = head;
                return msg;
            }
            case TIP_INDICIDAY: {
                byte data[] = new byte[21];
                System.arraycopy(arr, head.lenHeader, data, 0, data.length);
                return decodeIndexDayMessage(head, data);
            }
            case TIP_ASTACHIUSURA: {
                DataMessage msg = new AstaChiusura(arr, head.lenHeader, head.decade);
                msg.head = head;
                return msg;
            }
            case TIP_TUTTIPREZZI: {
                byte data[] = new byte[9];
                System.arraycopy(arr, head.lenHeader, data, 0, data.length);
                return decodeAllPricesMessage(head, data);
            }
        }

        return null;
    }

    private static HeaderRecord decodeHeader(byte packet[]) {
        if (packet.length < 7) {
            return null;
        }

        HeaderRecord head = new HeaderRecord();
        head.tipo = Util.getByte(packet[0]);
        int i = Util.byteToInt(packet[5]);
        int j = i & 0x80;
        if (j == 0) {
            head.decade = 0;
        }
        else {
            head.decade = 1;
        }

        int k = i & 0xf;
        head.key = new String(packet, 6, k + 1);
        head.oraMsg = Util.getDataOra(packet, 1, j);
        head.lenHeader = 7 + k;

        return head;
    }

    private static TuttiPrezzi decodeAllPricesMessage(HeaderRecord head, byte packet[]) {
        if (packet.length < 9) {
            return null;
        }

        TuttiPrezzi msg = new TuttiPrezzi();
        msg.head = head;
        int i = 0;
        msg.val_contr = Util.getFloat(packet, i);
        i += 4;
        msg.qta_contr = Util.getUlong(packet, i);
        i += 4;
        msg.cross_order = Util.getByte(packet[i]);
        return msg;
    }

    private static IndiciDay decodeIndexDayMessage(HeaderRecord head, byte packet[]) {
        if (packet.length < 8) {
            return null;
        }

        IndiciDay msg = new IndiciDay();
        msg.head = head;
        int i = 0;
        msg.val_ult = Util.getFloat(packet, i);
        i += 4;
        msg.ora_ult = Util.getDataOra(packet, i, head.decade);
        i += 4;
        msg.tendenza = Util.getByte(packet[i]);
        i++;
        msg.percent = Util.getFloat(packet, i);
        i += 4;
        msg.max = Util.getFloat(packet, i);
        i += 4;
        msg.min = Util.getFloat(packet, i);
        i += 4;
        return msg;
    }
}
