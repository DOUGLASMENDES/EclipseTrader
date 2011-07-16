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

public class Book extends DataMessage {

    public static final int LEVELS = 5;

    public int offset;

    public int n_pdn_c[] = new int[LEVELS];
    public long q_pdn_c[] = new long[LEVELS];
    public double val_c[] = new double[LEVELS];

    public int n_pdn_v[] = new int[LEVELS];
    public long q_pdn_v[] = new long[LEVELS];
    public double val_v[] = new double[LEVELS];

    public Book(byte[] arr, int i, int offset) {
        this.offset = offset;

        for (int j = 0; j < LEVELS; j++) {
            n_pdn_c[j] = Util.getUInt(arr, i);
            i += 2;
        }

        for (int k = 0; k < LEVELS; k++) {
            q_pdn_c[k] = Util.getUlong(arr, i);
            i += 4;
        }

        for (int l = 0; l < LEVELS; l++) {
            val_c[l] = Util.getFloat(arr, i);
            i += 4;
        }

        for (int i1 = 0; i1 < LEVELS; i1++) {
            n_pdn_v[i1] = Util.getUInt(arr, i);
            i += 2;
        }

        for (int j1 = 0; j1 < LEVELS; j1++) {
            q_pdn_v[j1] = Util.getUlong(arr, i);
            i += 4;
        }

        for (int k1 = 0; k1 < LEVELS; k1++) {
            val_v[k1] = Util.getFloat(arr, i);
            i += 4;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LEVELS; i++) {
            sb.append(n_pdn_c[i]);
            sb.append(":"); //$NON-NLS-1$
            sb.append(q_pdn_c[i]);
            sb.append(":"); //$NON-NLS-1$
            sb.append(val_c[i]);
            sb.append(" - "); //$NON-NLS-1$
            sb.append(n_pdn_v[i]);
            sb.append(":"); //$NON-NLS-1$
            sb.append(q_pdn_v[i]);
            sb.append(":"); //$NON-NLS-1$
            sb.append(val_v[i]);
            sb.append("\r\n"); //$NON-NLS-1$
        }
        return sb.toString();
    }
}
