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

package net.sourceforge.eclipsetrader.core.ui.widgets;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;

public interface IEditableColumn
{

    public boolean isEditable();

    public void setEditable(boolean editable);

    public Control getEditor(Item item, int index);

    public void destroyEditor();

}