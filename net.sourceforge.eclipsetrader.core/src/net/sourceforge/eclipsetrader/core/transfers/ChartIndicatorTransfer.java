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
import java.util.Map;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.ChartIndicator;
import net.sourceforge.eclipsetrader.core.db.ChartTab;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

public class ChartIndicatorTransfer extends ByteArrayTransfer
{
    private static final String TYPENAME = ChartIndicator.class.getName();
    private static final int TYPEID = registerType(TYPENAME);
    private static ChartIndicatorTransfer _instance = new ChartIndicatorTransfer();

    private ChartIndicatorTransfer()
    {
    }
    
    public static ChartIndicatorTransfer getInstance()
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

        ChartIndicator indicator = (ChartIndicator) object;
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream writeOut = new ObjectOutputStream(out);
            writeOut.writeObject(indicator.getParent().getLabel());
            writeOut.writeObject(indicator.getPluginId());
            writeOut.writeObject(indicator.getParameters());
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

                ChartTab tab = new ChartTab();
                tab.setLabel((String)readIn.readObject());
                
                ChartIndicator indicator = new ChartIndicator();
                indicator.setParent(tab);
                indicator.setPluginId((String)readIn.readObject());
                indicator.setParameters((Map)readIn.readObject());
                
                readIn.close();
                return indicator;
            }
            catch (Exception e) {
                CorePlugin.logException(e);
            }
        }

        return null;
    }

    private boolean checkMyType(Object object)
    {
        return (object instanceof ChartIndicator);
    }
}
