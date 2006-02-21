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


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

public class EditableTreeColumn extends TreeColumn implements IEditableColumn
{
    public static final int TEXT = 0;
    public static final int CURRENCY = 1;
    protected boolean editable = true;
    protected Text editor;
    protected TreeItem item = null;
    protected int index = -1;

    public EditableTreeColumn(Tree parent, int style)
    {
        super(parent, style);
    }

    public EditableTreeColumn(Tree parent, int style, int index)
    {
        super(parent, style, index);
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.TableColumn#checkSubclass()
     */
    protected void checkSubclass()
    {
    }

    /* (non-Javadoc)
     * @see com.maccasoft.integra.core.internal.IEditableColumn#isEditable()
     */
    public boolean isEditable()
    {
        return editable;
    }

    /* (non-Javadoc)
     * @see com.maccasoft.integra.core.internal.IEditableColumn#setEditable(boolean)
     */
    public void setEditable(boolean editable)
    {
        this.editable = editable;
    }

    /* (non-Javadoc)
     * @see com.maccasoft.integra.core.internal.IEditableColumn#getEditor(org.eclipse.swt.widgets.TableItem, int)
     */
    public Control getEditor(Item item, int index)
    {
        this.item = (TreeItem)item;
        this.index = index;
        
        editor = new Text(getParent(), SWT.NONE);
        editor.setText(this.item.getText(index));
        editor.selectAll();
        
        return editor;
    }
    
    /* (non-Javadoc)
     * @see com.maccasoft.integra.core.internal.IEditableColumn#destroyEditor()
     */
    public void destroyEditor()
    {
        if (editor != null && !editor.isDisposed())
        {
            if (item != null && index != -1)
                setItemValue(index, editor.getText());
            editor.dispose();
        }
        editor = null;
    }
    
    protected void setItemValue(int index, String value)
    {
        String oldValue = item.getText(index);
        item.setText(index, value);
        if (item instanceof IEditableItem && !oldValue.equals(value))
            ((IEditableItem)item).itemEdited(index, value);
    }
}
