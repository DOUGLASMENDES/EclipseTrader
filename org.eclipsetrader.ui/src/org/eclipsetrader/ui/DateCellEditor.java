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

package org.eclipsetrader.ui;

import java.text.DateFormat;
import java.text.ParseException;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

public class DateCellEditor extends TextCellEditor {

    DateFormat dateFormat = Util.getDateFormat();

    public DateCellEditor() {
    }

    public DateCellEditor(Composite parent) {
        super(parent);
    }

    public DateCellEditor(Composite parent, int style) {
        super(parent, style);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.TextCellEditor#doGetValue()
     */
    @Override
    protected Object doGetValue() {
        String text = (String) super.doGetValue();
        try {
            return dateFormat.parse(text);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.TextCellEditor#doSetValue(java.lang.Object)
     */
    @Override
    protected void doSetValue(Object value) {
        super.doSetValue(dateFormat.format(value));
    }
}
