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

package org.eclipsetrader.ui.internal.securities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.ui.internal.UIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class SecurityObjectTransfer extends ByteArrayTransfer {
	private static SecurityObjectTransfer instance = new SecurityObjectTransfer();
	private static final String TYPENAME = SecurityObjectTransfer.class.getName();
	private static final int TYPEID = registerType(TYPENAME);

	private SecurityObjectTransfer() {
	}

	public static SecurityObjectTransfer getInstance() {
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

			if (object instanceof ISecurity) {
				writeOut.writeInt(1);
				IStoreObject storeObject = (IStoreObject) ((ISecurity) object).getAdapter(IStoreObject.class);
				writeOut.writeObject(storeObject.getStore().toURI());
			}
			else if (object instanceof IAdaptable[]) {
				int count = 0;
				for (IAdaptable adaptable : (IAdaptable[]) object) {
					if (adaptable instanceof ISecurity)
						count++;
				}

				writeOut.writeInt(count);
				for (IAdaptable adaptable : (IAdaptable[]) object) {
					if (adaptable instanceof ISecurity) {
						IStoreObject storeObject = (IStoreObject) adaptable.getAdapter(IStoreObject.class);
						writeOut.writeObject(storeObject.getStore().toURI());
					}
				}
			}

			byte[] buffer = out.toByteArray();
			writeOut.close();
			super.javaToNative(buffer, transferData);
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, 0, "Error building transfer object", e); //$NON-NLS-1$
			UIActivator.getDefault().getLog().log(status);
		}
	}

	@Override
	protected Object nativeToJava(TransferData transferData) {
		ISecurity[] securities = new ISecurity[0];

		if (isSupportedType(transferData)) {
			byte[] buffer = (byte[]) super.nativeToJava(transferData);
			if (buffer == null)
				return null;

			IRepositoryService service = getRepositoryService();

			try {
				ByteArrayInputStream in = new ByteArrayInputStream(buffer);
				ObjectInputStream readIn = new ObjectInputStream(in);

				int length = readIn.readInt();
				securities = new ISecurity[length];
				for (int i = 0; i < length; i++) {
					URI uri = (URI) readIn.readObject();
					securities[i] = service.getSecurityFromURI(uri);
				}
			} catch (Exception e) {
				Status status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, 0, "Error reassembling transfer object", e); //$NON-NLS-1$
				UIActivator.getDefault().getLog().log(status);
			}
		}

		return securities;
	}

	public static boolean checkMyType(Object object) {
		if (object instanceof IAdaptable)
			return object instanceof ISecurity;
		if (object instanceof IAdaptable[]) {
			for (IAdaptable adaptable : (IAdaptable[]) object) {
				if (adaptable instanceof ISecurity)
					return true;
			}
		}
		return false;
	}

	protected IRepositoryService getRepositoryService() {
		try {
			BundleContext context = UIActivator.getDefault().getBundle().getBundleContext();
			ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
			IRepositoryService service = (IRepositoryService) context.getService(serviceReference);
			context.ungetService(serviceReference);
			return service;
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, 0, "Error reading repository service", e); //$NON-NLS-1$
			UIActivator.getDefault().getLog().log(status);
		}
		return null;
	}
}
