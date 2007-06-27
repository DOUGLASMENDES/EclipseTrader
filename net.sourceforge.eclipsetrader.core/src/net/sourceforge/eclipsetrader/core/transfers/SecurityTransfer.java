/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.core.transfers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.SecurityGroup;

import org.eclipse.core.runtime.Status;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

/**
 * Implements the support for securities drag&drop and copy/paste operations.
 */
public class SecurityTransfer extends ByteArrayTransfer {
	private static final String TYPENAME = Security.class.getName();

	private static final int TYPEID = registerType(TYPENAME);

	private static SecurityTransfer _instance = new SecurityTransfer();

	private SecurityTransfer() {
	}

	public static SecurityTransfer getInstance() {
		return _instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
	 */
	@Override
	protected String[] getTypeNames() {
		return new String[] { TYPENAME };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
	 */
	@Override
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(java.lang.Object, org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	protected void javaToNative(Object object, TransferData transferData) {
		if (!checkMyType(object) || !isSupportedType(transferData))
			DND.error(DND.ERROR_INVALID_DATA);

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream writeOut = new ObjectOutputStream(out);

			List<Security> list = new ArrayList<Security>();
			buildSecuritiesList(list, object);

			writeOut.writeInt(list.size());
			for (Security security : list)
				writeOut.writeObject(security.getId());

			byte[] buffer = out.toByteArray();
			writeOut.close();
			super.javaToNative(buffer, transferData);
		} catch (IOException e) {
			CorePlugin.getDefault().getLog().log(new Status(Status.ERROR, CorePlugin.PLUGIN_ID, 0, "Exception while transfering data", e));
		}
	}

	private void buildSecuritiesList(List<Security> list, Object root) {
		if (root instanceof Security)
			list.add((Security) root);
		else if (root instanceof SecurityGroup) {
			for (Object child : ((SecurityGroup) root).getChildrens())
				buildSecuritiesList(list, child);
		} else if (root instanceof Object[]) {
			Object[] array = (Object[]) root;
			for (int i = 0; i < array.length; i++)
				buildSecuritiesList(list, array[i]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.ByteArrayTransfer#nativeToJava(org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	protected Object nativeToJava(TransferData transferData) {
		if (isSupportedType(transferData)) {
			byte[] buffer = (byte[]) super.nativeToJava(transferData);
			if (buffer == null)
				return null;

			try {
				ByteArrayInputStream in = new ByteArrayInputStream(buffer);
				ObjectInputStream readIn = new ObjectInputStream(in);

				int length = readIn.readInt();
				Security[] security = new Security[length];
				for (int i = 0; i < length; i++)
					security[i] = (Security) CorePlugin.getRepository().load(Security.class, (Integer) readIn.readObject());

				readIn.close();
				return security;
			} catch (Exception e) {
				CorePlugin.getDefault().getLog().log(new Status(Status.ERROR, CorePlugin.PLUGIN_ID, 0, "Exception while transfering data", e));
			}
		}

		return null;
	}

	private boolean checkMyType(Object object) {
		// Security and security arrays are supported
		if (object instanceof Security || object instanceof Security[])
			return true;

		// If it is a generic object array makes sure that all objects are supported 
		if (object instanceof Object[]) {
			Object[] array = (Object[]) object;
			for (int i = 0; i < array.length; i++) {
				if (!(array[i] instanceof Security))
					return false;
			}
			return true;
		}

		return false;
	}
}
