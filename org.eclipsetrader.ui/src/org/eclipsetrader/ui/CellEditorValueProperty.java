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

import org.eclipse.core.databinding.property.value.ValueProperty;

public class CellEditorValueProperty {

    private CellEditorValueProperty() {
    }

    public static ValueProperty doubleValue() {
        return new CellEditorDoubleValueProperty();
    }

    public static ValueProperty longValue() {
        return new CellEditorLongValueProperty();
    }

    public static ValueProperty dateValue() {
        return new CellEditorDateValueProperty();
    }

    public static ValueProperty timeValue() {
        return new CellEditorTimeValueProperty();
    }
}
