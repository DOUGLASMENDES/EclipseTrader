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

/**
 * Interface that all table or tree items must implement to be editable.
 */
public interface IEditableItem
{

    /**
     * Return wether the given column index can be edited in the receiver.
     * 
     * @param index - the column index
     * @return true if the item can be edited, false otherwise
     */
    public boolean canEdit(int index);

    public boolean isEditable();
    
    public void itemEdited(int index, String text);
}
