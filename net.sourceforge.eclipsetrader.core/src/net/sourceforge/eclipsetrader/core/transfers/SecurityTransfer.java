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

package net.sourceforge.eclipsetrader.core.transfers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

public class SecurityTransfer extends ByteArrayTransfer
{
    private static final String TYPENAME = Security.class.getName();
    private static final int TYPEID = registerType(TYPENAME);
    private static SecurityTransfer _instance = new SecurityTransfer();

    private SecurityTransfer()
    {
    }
    
    public static SecurityTransfer getInstance()
    {
        return _instance;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
     */
    protected String[] getTypeNames()
    {
        return new String[] { TYPENAME };
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
     */
    protected int[] getTypeIds()
    {
        return new int[] { TYPEID };
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(java.lang.Object, org.eclipse.swt.dnd.TransferData)
     */
    protected void javaToNative(Object object, TransferData transferData)
    {
        if (!checkMyType(object) || !isSupportedType(transferData))
            DND.error(DND.ERROR_INVALID_DATA);

        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream writeOut = new ObjectOutputStream(out);
            
            if (object instanceof Security)
            {
                writeOut.writeInt(1);
                writeOut.writeObject(((Security)object).getId());
            }
            else if (object instanceof Security[])
            {
                Security[] array = (Security[]) object;
                writeOut.writeInt(array.length);
                for (int i = 0; i < array.length; i++)
                    writeOut.writeObject(array[i].getId());
            }
            
            byte[] buffer = out.toByteArray();
            writeOut.close();
            super.javaToNative(buffer, transferData);
        }
        catch (IOException e) {
            CorePlugin.logException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.ByteArrayTransfer#nativeToJava(org.eclipse.swt.dnd.TransferData)
     */
    protected Object nativeToJava(TransferData transferData)
    {
        if (isSupportedType(transferData))
        {
            byte[] buffer = (byte[]) super.nativeToJava(transferData);
            if (buffer == null)
                return null;

            try
            {
                ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                ObjectInputStream readIn = new ObjectInputStream(in);

                int length = readIn.readInt();
                Security[] security = new Security[length];
                for (int i = 0; i < length; i++)
                    security[i] = (Security) CorePlugin.getRepository().load(Security.class, (Integer)readIn.readObject());
                
                readIn.close();
                return security;
            }
            catch (Exception e) {
                CorePlugin.logException(e);
            }
        }

        return null;
    }

    private boolean checkMyType(Object object)
    {
        return (object instanceof Security || object instanceof Security[]);
    }
}
