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

public class RecordType {

    public static final int C_ERR_LOGIN = 81;
    public static final int C_ERR_LOGOUT = 82;
    public static final int C_ERR_PORT = 83;
    public static final int C_ERR_START_DATA = 84;
    public static final int C_ERR_STOP_DATA = 85;
    public static final int C_ERR_DATA = 86;
    public static final int C_ERR_PKTLOST = 99;
    public static final int SIZE_HADER_REC_MIN = 7;
    public static final int SIZE_PRICE = 32;
    public static final int SIZE_BOOK = 100;
    public static final int SIZE_BIDASK = 16;
    public static final int SIZE_ASTA = 18;
    public static final int SIZE_STATOVALMOBILE = 7;
    public static final int SIZE_CHIUSURA = 40;
    public static final int SIZE_HEADER = 4;
    public static final int SIZE_LOGIN = 420;
    public static final int SIZE_TIT_PORT = 33;
    public static final int SIZE_INDICIDAY = 21;
    public static final int SIZE_INDICIFIX = 20;
    public static final int SIZE_TUTTIPREZZI = 9;
    public static final int SIZE_ASTACHIUSURA = 8;
    public static final int LEN_S_STR = 12;
    public static final int LEN_M_STR = 100;
    public static final int LEN_L_STR = 300;
    public static final int LEN_ULONG = 4;
    public static final int LEN_USHORT = 2;
    public static final int LEN_USER_PWD = 10;
    public static final int LEN_KEY_FIXED = 32;
    public static final int LEN_SFLOAT = 4;
    public static final int LEN_DATE = 4;
    public static final int LEN_MSG = 2;
    public static final int LEN_BYTE = 1;
    public static final int MAX_NUM_TIT_PORT = 30;
    public static final int FLAG_PR_BID = 0;
    public static final int FLAG_PR_BID_BOOK = 1;
    public static final int FLAG_PR_BID_ALLPR = 2;
    public static final int FLAG_PR_BID_BOOK_ALLPR = 3;
    public static final char CHAR_START = 35;
}
