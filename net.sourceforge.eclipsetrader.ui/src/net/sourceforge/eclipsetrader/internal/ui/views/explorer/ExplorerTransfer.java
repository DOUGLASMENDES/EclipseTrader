/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.internal.ui.views.explorer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.PersistentObject;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.SecurityGroup;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

public class ExplorerTransfer extends ByteArrayTransfer {
	private static final String TYPENAME = ExplorerTransfer.class.getName();

	private static final int TYPEID = registerType(TYPENAME);

	private static ExplorerTransfer _instance = new ExplorerTransfer();

	private ExplorerTransfer() {
	}

	public static ExplorerTransfer getInstance() {
		return _instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
	 */
	protected String[] getTypeNames() {
		return new String[] { TYPENAME };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
	 */
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(java.lang.Object, org.eclipse.swt.dnd.TransferData)
	 */
	protected void javaToNative(Object object, TransferData transferData) {
		if (!checkMyType(object) || !isSupportedType(transferData))
			DND.error(DND.ERROR_INVALID_DATA);

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream writeOut = new ObjectOutputStream(out);

			if (object instanceof PersistentObject) {
				writeOut.writeObject(object.getClass().getName());
				writeOut.writeInt(1);
				writeOut.writeObject(((PersistentObject) object).getId());
			} else if (object instanceof Object[]) {
				Object[] array = (Object[]) object;
				writeOut.writeObject(array[0].getClass().getName());
				writeOut.writeInt(array.length);
				for (int i = 0; i < array.length; i++)
					writeOut.writeObject(((PersistentObject) array[i]).getId());
			}

			byte[] buffer = out.toByteArray();
			writeOut.close();
			super.javaToNative(buffer, transferData);
		} catch (IOException e) {
			CorePlugin.logException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.ByteArrayTransfer#nativeToJava(org.eclipse.swt.dnd.TransferData)
	 */
	protected Object nativeToJava(TransferData transferData) {
		if (isSupportedType(transferData)) {
			byte[] buffer = (byte[]) super.nativeToJava(transferData);
			if (buffer == null)
				return null;

			try {
				ByteArrayInputStream in = new ByteArrayInputStream(buffer);
				ObjectInputStream readIn = new ObjectInputStream(in);
				
				String className = (String) readIn.readObject();
				Class clazz = Class.forName(className);

				int length = readIn.readInt();
				Object[] objects = new Object[length];
				for (int i = 0; i < length; i++)
					objects[i] = CorePlugin.getRepository().load(clazz, (Integer) readIn.readObject());

				readIn.close();
				return objects;
			} catch (Exception e) {
				CorePlugin.logException(e);
			}
		}

		return null;
	}

	public boolean checkMyType(Object object) {
		if (object instanceof Security || object instanceof Security[])
			return true;
		if (object instanceof SecurityGroup || object instanceof SecurityGroup[])
			return true;

		if (object instanceof Object[]) {
			Object[] array = (Object[]) object;
			if (array[0] instanceof Security) {
				for (int i = 0; i < array.length; i++) {
					if (!(array[i] instanceof Security))
						return false;
				}
				return true;
			}
			else if (array[0] instanceof SecurityGroup) {
				for (int i = 0; i < array.length; i++) {
					if (!(array[i] instanceof SecurityGroup))
						return false;
				}
				return true;
			}
		}

		return false;
	}
}
