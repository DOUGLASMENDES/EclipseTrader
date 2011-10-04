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

package org.eclipsetrader.ui.navigator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.ui.internal.UIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class RepositoryObjectTransfer extends ByteArrayTransfer {

    private static RepositoryObjectTransfer instance = new RepositoryObjectTransfer();
    private static final String TYPENAME = RepositoryObjectTransfer.class.getName();
    private static final int TYPEID = registerType(TYPENAME);

    private RepositoryObjectTransfer() {
    }

    public static RepositoryObjectTransfer getInstance() {
        return instance;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
     */
    @Override
    protected int[] getTypeIds() {
        return new int[] {
            TYPEID
        };
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
     */
    @Override
    protected String[] getTypeNames() {
        return new String[] {
            TYPENAME
        };
    }

    @Override
    protected void javaToNative(Object object, TransferData transferData) {
        if (!checkMyType(object) || !isSupportedType(transferData)) {
            DND.error(DND.ERROR_INVALID_DATA);
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream writeOut = new ObjectOutputStream(out);

            if (object instanceof IAdaptable) {
                writeOut.writeInt(1);
                IStoreObject storeObject = (IStoreObject) ((IAdaptable) object).getAdapter(IStoreObject.class);
                writeOut.writeObject(storeObject.getStore().toURI());
            }
            else if (object instanceof IAdaptable[]) {
                writeOut.writeInt(((IAdaptable[]) object).length);
                for (IAdaptable adaptable : (IAdaptable[]) object) {
                    IStoreObject storeObject = (IStoreObject) adaptable.getAdapter(IStoreObject.class);
                    writeOut.writeObject(storeObject.getStore().toURI());
                }
            }

            byte[] buffer = out.toByteArray();
            writeOut.close();
            super.javaToNative(buffer, transferData);
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, 0, "Error building transfer object", e); //$NON-NLS-1$
            UIActivator.getDefault().getLog().log(status);
        }
    }

    @Override
    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    protected Object nativeToJava(TransferData transferData) {
        if (isSupportedType(transferData)) {
            byte[] buffer = (byte[]) super.nativeToJava(transferData);
            if (buffer == null) {
                return new IAdaptable[0];
            }

            BundleContext context = UIActivator.getDefault().getBundle().getBundleContext();
            ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
            try {
                IRepositoryService service = (IRepositoryService) context.getService(serviceReference);

                ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                ObjectInputStream readIn = new ObjectInputStream(in);

                int length = readIn.readInt();
                IAdaptable[] adaptables = new IAdaptable[length];
                for (int i = 0; i < length; i++) {
                    URI uri = (URI) readIn.readObject();
                    adaptables[i] = (IAdaptable) service.getObjectFromURI(uri);
                }

                return adaptables;
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, 0, "Error reassembling transfer object", e); //$NON-NLS-1$
                UIActivator.log(status);
            } finally {
                context.ungetService(serviceReference);
            }
        }

        return new IAdaptable[0];
    }

    public static boolean checkMyType(Object object) {
        if (object instanceof IAdaptable) {
            return ((IAdaptable) object).getAdapter(IStoreObject.class) != null;
        }
        if (object instanceof IAdaptable[]) {
            for (IAdaptable adaptable : (IAdaptable[]) object) {
                if (adaptable.getAdapter(IStoreObject.class) == null) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
