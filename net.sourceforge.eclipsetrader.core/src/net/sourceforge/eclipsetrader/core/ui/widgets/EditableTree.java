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
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class EditableTree extends Tree
{
    private TreeEditor editor;
    private TreeItem selectedItem;
    private int selectedColumnIndex = -1;
    private DisposeListener disposeListener = new DisposeListener()
    {
        public void widgetDisposed(DisposeEvent e)
        {
            if (editor.getEditor() != null && !editor.getEditor().isDisposed())
                editor.getEditor().dispose();
        }
    };

    public EditableTree(Composite parent, int style)
    {
        super(parent, style);
        
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e)
            {
                if (e.keyCode == 0x0D)
                {
                    TreeItem item = getSelectedItem();
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

                TreeItem item = getItem(new Point(e.x, e.y));
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

        editor = new TreeEditor(this);
        editor.horizontalAlignment = SWT.CENTER;
        editor.grabHorizontal = true;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Table#checkSubclass()
     */
    protected void checkSubclass()
    {
    }
    
    private TreeItem getSelectedItem()
    {
        if (getSelectionCount() != 1)
            return null;
        return getSelection()[0];
    }
    
    private TreeItem selectNextRow()
    {
        if (getSelectionCount() != 1)
            return null;
        TreeItem item = getSelection()[0];
        
        if (item.getParentItem() == null && !item.getExpanded())
        {
            int row = indexOf(item) + 1;
            if (row < getItemCount())
            {
                TreeItem[] selection = { getItem(row) };
                setSelection(selection);
                return selection[0];
            }
        }

        if (item.getParentItem() == null && item.getExpanded())
        {
            int row = 0;
            if (row < item.getItemCount())
            {
                TreeItem[] selection = { item.getItem(row) };
                setSelection(selection);
                return selection[0];
            }
        }

        if (item.getParentItem() != null && item.getParentItem().getExpanded())
        {
            int row = item.getParentItem().indexOf(item) + 1;
            if (row < item.getParentItem().getItemCount())
            {
                TreeItem[] selection = { item.getParentItem().getItem(row) };
                setSelection(selection);
                return selection[0];
            }
        }

        return null;
    }
    
    private TreeItem selectPreviousRow()
    {
        if (getSelectionCount() != 1)
            return null;
        TreeItem item = getSelection()[0];

        if (item.getParentItem() != null)
        {
            int index = item.getParentItem().indexOf(item) - 1;
            if (index >= 0)
            {
                TreeItem[] selection = { item.getParentItem().getItem(0) };
                setSelection(selection);
                return selection[0];
            }
        }

        int index = item.getParent().indexOf(item) + 1;
        if (index >= 0)
        {
            TreeItem[] selection = { item.getParent().getItem(0) };
            setSelection(selection);
            return selection[0];
        }
        
        return null;
    }
    
    private void activateEditor()
    {
        TreeItem item = getSelectedItem();
        if (item == null || !(item instanceof IEditableItem))
            return;

        if (selectedColumnIndex == -1 || !(getColumn(selectedColumnIndex) instanceof IEditableColumn))
            return;
        IEditableColumn column = (IEditableColumn)getColumn(selectedColumnIndex);
        if (!column.isEditable() || !((IEditableItem)item).canEdit(selectedColumnIndex))
            return;

        Control newEditor = column.getEditor(item, selectedColumnIndex);
        if (newEditor != null)
        {
            item.removeDisposeListener(disposeListener);
            item.addDisposeListener(disposeListener);
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
