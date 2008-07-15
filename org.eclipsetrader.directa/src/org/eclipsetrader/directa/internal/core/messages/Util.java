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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Util {

	public Util() {
	}

	public static float getFloat(byte[] arr, int i) {
		if (arr.length < i + 4)
			return 0.0F;
		int j = 0;
		int k = 0;
		for (int l = 0; l < 4; l++) {
			k += byteToInt(arr[i + l]) << j;
			j += 8;
		}

		float f = Float.intBitsToFloat(k);
		return f;
	}

	public static int getUInt(byte[] arr, int i) {
		return (int) unpackLong(arr, i, 2);
	}

	public static int getMessageLength(byte[] arr, int i) {
		if (arr.length < i + 2)
			return 0;
		int j = 0;
		int k = 0;
		for (int l = 0; l < 2; l++) {
			j += byteToInt(arr[i + l]) << k;
			k += 8;
		}

		return j;
	}

	public static short getByte(byte b) {
		return (short) byteToInt(b);
	}

	public static long getUlong(byte[] arr, int i) {
		return unpackLong(arr, i, 4);
	}

	public static long getDataOra(byte[] arr, int i, int j) {
		long k = 0;
		int l = 0;
		for (int i1 = 0; i1 < 4; i1++) {
			k += (long) byteToInt(arr[i1 + i]) << l;
			l += 8;
		}

		long j1 = k >> 20;
		long k1 = k - (j1 << 20);
		k1 /= 12;
		long l1 = k1 / 3600;
		GregorianCalendar gregoriancalendar = new GregorianCalendar(2000 + j * 10, 0, 1, 0, 0, 0);
		long l2 = ((Calendar) (gregoriancalendar)).getTime().getTime();
		long l3 = j1 * 3600 * 24 * 1000 + (k1 * 1000);
		long l4 = l2 + l3;
		((Calendar) (gregoriancalendar)).setTime(new Date(l4));
		((Calendar) (gregoriancalendar)).set(11, (int) l1);
		return ((Calendar) (gregoriancalendar)).getTime().getTime();
	}

	public static int byteToInt(byte b) {
		int i = b;
		if (b < 0)
			i = 256 + b;
		return i;
	}

	private static long unpackLong(byte[] arr, int i, int j) {
		if (arr.length < i + j)
			return 0L;
		long l = 0L;
		int k = (j - 1) * 8;
		for (int i1 = 0; i1 < j; i1++) {
			l += byteToInt(arr[i + i1]) << k;
			k -= 8;
		}
		return l;
	}
}
