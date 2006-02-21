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
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class EditableTable extends Table
{
    private TableEditor editor;
    private TableItem selectedItem;
    private int selectedColumnIndex = -1;
//    private Control newEditor = null;

    public EditableTable(Composite parent, int style)
    {
        super(parent, style);
        
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e)
            {
                if (e.keyCode == 0x0D)
                {
                    TableItem item = getSelectedItem();
                    if (item != null && selectedColumnIndex == -1)
                    {
                        for (int i = 0; i < getColumnCount(); i++)
                        {
                            if (getColumn(i) instanceof IEditableColumn)
                            {
                                selectedColumnIndex = i;
                                activateEditor();
                                break;
                            }
                        }
                    }
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e)
            {
                if (selectedItem != null && selectedColumnIndex != -1 && editor.getEditor() != null && !editor.getEditor().isDisposed())
                {
                    IEditableColumn column = (IEditableColumn)getColumn(selectedColumnIndex);
                    if (column.isEditable())
                        column.destroyEditor();
                }

                TableItem item = getItem(new Point(e.x, e.y));
                if (item != null)
                {
                    int columnIndex = -1;
                    for (int i = 0; i < getColumnCount(); i++)
                    {
                        if (!(getColumn(i) instanceof IEditableColumn))
                            continue;
                        if (!((IEditableColumn)getColumn(i)).isEditable())
                            continue;
                        if (item.getBounds(i).contains(e.x, e.y))
                            columnIndex = i;
                    }
                    
                    if (columnIndex != -1 && item == selectedItem && columnIndex == selectedColumnIndex)
                        activateEditor();

                    selectedItem = item;
                    selectedColumnIndex = columnIndex;
                }
            }
        });

        editor = new TableEditor(this);
        editor.horizontalAlignment = SWT.CENTER;
        editor.grabHorizontal = true;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Table#checkSubclass()
     */
    protected void checkSubclass()
    {
    }
    
    private TableItem getSelectedItem()
    {
        if (getSelectionCount() != 1)
            return null;
        return getSelection()[0];
    }
    
    private TableItem selectNextRow()
    {
        if (getSelectionCount() != 1)
            return null;
        TableItem item = getSelection()[0];

        int row = indexOf(item) + 1;
        if (row < getItemCount())
        {
            TableItem[] selection = { getItem(row) };
            setSelection(selection);
            return selection[0];
        }
        
        return null;
    }
    
    private TableItem selectPreviousRow()
    {
        if (getSelectionCount() != 1)
            return null;
        TableItem item = getSelection()[0];

        int row = indexOf(item) - 1;
        if (row >= 0)
        {
            TableItem[] selection = { getItem(row) };
            setSelection(selection);
            return selection[0];
        }
        
        return null;
    }
    
    private void activateEditor()
    {
        TableItem item = getItem(getSelectionIndex());
        if (item == null)
            return;

        if (selectedColumnIndex == -1 || !(getColumn(selectedColumnIndex) instanceof IEditableColumn))
            return;
        IEditableColumn column = (IEditableColumn)getColumn(selectedColumnIndex);
        if (!column.isEditable())
            return;
        if ((item instanceof IEditableItem) && ( !((IEditableItem)item).isEditable() || !((IEditableItem)item).canEdit(selectedColumnIndex) ))
            return;

        Control newEditor = column.getEditor(item, selectedColumnIndex);
        if (newEditor != null)
        {
            newEditor.addTraverseListener(new TraverseListener() {
                public void keyTraversed(TraverseEvent e)
                {
                    if (e.detail == SWT.TRAVERSE_TAB_NEXT)
                    {
                        ((IEditableColumn)getColumn(selectedColumnIndex)).destroyEditor();
                        
                        do {
                            selectedColumnIndex++;
                        } while(selectedColumnIndex < getColumnCount() && (!(getColumn(selectedColumnIndex) instanceof IEditableColumn) || !((IEditableColumn)getColumn(selectedColumnIndex)).isEditable()));

                        if (selectedColumnIndex < getColumnCount())
                            activateEditor();
                        else if (selectNextRow() != null)
                        {
                            selectedColumnIndex = -1;
                            do {
                                selectedColumnIndex++;
                            } while(selectedColumnIndex < getColumnCount() && (!(getColumn(selectedColumnIndex) instanceof IEditableColumn) || !((IEditableColumn)getColumn(selectedColumnIndex)).isEditable()));
                            activateEditor();
                        }
                        else
                        {
                            setFocus();
                            selectedColumnIndex = -1;
                        }
                        e.doit = false;
                    }
                    else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS)
                    {
                        ((IEditableColumn)getColumn(selectedColumnIndex)).destroyEditor();

                        do {
                            selectedColumnIndex--;
                        } while(selectedColumnIndex >= 0 && (!(getColumn(selectedColumnIndex) instanceof IEditableColumn) || !((IEditableColumn)getColumn(selectedColumnIndex)).isEditable()));

                        if (selectedColumnIndex >= 0)
                            activateEditor();
                        else if (selectPreviousRow() != null)
                        {
                            selectedColumnIndex = getColumnCount();
                            do {
                                selectedColumnIndex--;
                            } while(selectedColumnIndex >= 0 && (!(getColumn(selectedColumnIndex) instanceof IEditableColumn) || !((IEditableColumn)getColumn(selectedColumnIndex)).isEditable()));
                            activateEditor();
                        }
                        else
                        {
                            setFocus();
                            selectedColumnIndex = -1;
                        }
                        e.doit = false;
                    }
                    else if (e.detail == SWT.TRAVERSE_ESCAPE)
                    {
                        ((IEditableColumn)getColumn(selectedColumnIndex)).destroyEditor();
                        setFocus();
                        selectedColumnIndex = -1;
                        e.doit = false;
                    }
                }
            });
            newEditor.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e)
                {
                    if (e.keyCode == SWT.ARROW_UP && !(editor.getEditor() instanceof Combo))
                    {
                        ((IEditableColumn)getColumn(selectedColumnIndex)).destroyEditor();
                        if (selectPreviousRow() != null)
                            activateEditor();
                        else
                            setFocus();
                        e.doit = false;
                    }
                    else if (e.keyCode == SWT.ARROW_DOWN && !(editor.getEditor() instanceof Combo))
                    {
                        ((IEditableColumn)getColumn(selectedColumnIndex)).destroyEditor();
                        if (selectNextRow() != null)
                            activateEditor();
                        else
                            setFocus();
                        e.doit = false;
                    }
                }
            });
            newEditor.addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent e)
                {
                    if (!(editor.getEditor() instanceof Combo))
                        ((IEditableColumn)getColumn(selectedColumnIndex)).destroyEditor();
                }
            });
            editor.setEditor(newEditor, item, selectedColumnIndex);
            newEditor.getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    editor.getEditor().setFocus();
                }
            });
        }
    }
}
