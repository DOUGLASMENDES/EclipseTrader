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

package org.eclipsetrader.directa.internal.core.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CreaMsg {
	public static final int LOGIN_MSG = 1;
	public static final int LOGIN_ACK_MSG = 2;
	public static final int LOGOUT_MSG = 3;
	public static final int PORT_MSG = 4;
	public static final int START_DATA_MSG = 5;
	public static final int STOP_DATA_MSG = 6;
	public static final int DATA_MSG = 7;
	public static final int ERROR_MSG = 8;

	public static final int PORT_ADD = 1;
	public static final int PORT_DEL = 2;
	public static final int PORT_MOD = 3;
	public static final int PORT_DST = 4;

	public CreaMsg() {
	}

	public static byte[] creaLoginMsg(String urt, String prt, String appName, String version) {
		byte[] msg = new byte[424];
		internalCreaMsg(msg, LOGIN_MSG, 420);
		System.arraycopy(urt.getBytes(), 0, msg, 4, urt.getBytes().length);
		System.arraycopy(prt.getBytes(), 0, msg, 14, prt.getBytes().length);
		System.arraycopy(appName.getBytes(), 0, msg, 24, Math.min(appName.getBytes().length, 10));
		System.arraycopy(version.getBytes(), 0, msg, 34, Math.min(version.getBytes().length, 10));
		System.arraycopy("DIRECTA".getBytes(), 0, msg, 44, Math.min("DIRECTA".getBytes().length, 10));
		System.arraycopy("141".getBytes(), 0, msg, 54, Math.min("141".getBytes().length, 10));
		return msg;
	}

	public static byte[] creaPortMsg(int op, String symbols[], int flags[]) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		int j = Math.min(symbols.length, 30);
		byte[] msg = new byte[4 + j * 33 + 2];
		internalCreaMsg(msg, PORT_MSG, j * 33 + 2);
		msg[4] = (byte) op;
		msg[5] = (byte) j;
		if (symbols.length != flags.length || symbols.length == 0)
			return msg;
		int k = 0;
		int l = 6;
		for (int i1 = 38; k < j; i1 += 33) {
			System.arraycopy(symbols[k].getBytes(), 0, msg, l, symbols[k].getBytes().length);
			msg[i1] = (byte) flags[k];
			k++;
			l += 33;
		}
		try {
	        os.write(msg);
        } catch (IOException e) {
	        e.printStackTrace();
        }

		return os.toByteArray();
	}

	public static byte[] creaStartDataMsg() {
		byte[] msg = new byte[4];
		internalCreaMsg(msg, START_DATA_MSG, 0);
		return msg;
	}

	public static byte[] creaStopDataMsg() {
		byte[] msg = new byte[4];
		internalCreaMsg(msg, STOP_DATA_MSG, 0);
		return msg;
	}

	public static byte[] creaLogoutMsg() {
		byte[] msg = new byte[4];
		internalCreaMsg(msg, LOGOUT_MSG, 0);
		return msg;
	}

	private static void internalCreaMsg(byte[] msg, int type, int length) {
		if (msg.length < 4)
			return;
		byte byte0 = (byte) (length >> 8);
		byte byte1 = (byte) (length - (byte0 << 8));
		byte abyte1[] = { 35, (byte) type, byte1, byte0 };
		for (int k = 0; k < abyte1.length; k++)
			msg[k] = abyte1[k];

	}
}
