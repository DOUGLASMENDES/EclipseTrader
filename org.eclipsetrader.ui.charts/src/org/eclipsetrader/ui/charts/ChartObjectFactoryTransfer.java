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

package org.eclipsetrader.ui.charts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

public class ChartObjectFactoryTransfer extends ByteArrayTransfer {
	private static ChartObjectFactoryTransfer instance = new ChartObjectFactoryTransfer();
	private static final String TYPENAME = ChartObjectFactoryTransfer.class.getName();
	private static final int TYPEID = registerType(TYPENAME);

	public ChartObjectFactoryTransfer() {
	}

	public static ChartObjectFactoryTransfer getInstance() {
    	return instance;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
	 */
	@Override
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
	 */
	@Override
	protected String[] getTypeNames() {
		return new String[] { TYPENAME };
	}

	@Override
	protected void javaToNative(Object object, TransferData transferData) {
		if (!checkMyType(object) || !isSupportedType(transferData))
			DND.error(DND.ERROR_INVALID_DATA);

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream writeOut = new ObjectOutputStream(out);

			if (object instanceof String)
				object = new String[] { (String) object };
			if (object instanceof String[])
				writeOut.writeObject(object);

			byte[] buffer = out.toByteArray();
			writeOut.close();
			super.javaToNative(buffer, transferData);
		} catch (Exception e) {
			// TODO
		}
	}

	@Override
	protected Object nativeToJava(TransferData transferData) {
		if (isSupportedType(transferData)) {
			byte[] buffer = (byte[]) super.nativeToJava(transferData);
			if (buffer == null)
				return null;

			try {
				ByteArrayInputStream in = new ByteArrayInputStream(buffer);
				ObjectInputStream readIn = new ObjectInputStream(in);

				String[] s = (String[]) readIn.readObject();
				return s;
			} catch (Exception e) {
				// TODO
			}
		}
		return new String[0];
	}

	public static boolean checkMyType(Object object) {
		// TODO
		return true;
	}
}
