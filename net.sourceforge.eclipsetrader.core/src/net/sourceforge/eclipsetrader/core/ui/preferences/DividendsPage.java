/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.core.ui.preferences;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Dividend;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.ui.internal.Messages;
import net.sourceforge.eclipsetrader.core.ui.widgets.EditableTable;
import net.sourceforge.eclipsetrader.core.ui.widgets.EditableTableColumn;
import net.sourceforge.eclipsetrader.core.ui.widgets.IEditableItem;

import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 */
public class DividendsPage extends PreferencePage
{
    Table table;
    Button add;
    Button delete;
    Security security;
    NumberFormat numberFormatter = NumberFormat.getInstance();
    SimpleDateFormat dateFormat = CorePlugin.getDateFormat();
    SimpleDateFormat dateParse = CorePlugin.getDateParse();

    public DividendsPage(Security security)
    {
        super(Messages.DividendsPage_Title);
        noDefaultAndApplyButton();
        setValid(false);
        this.security = security;
        
        numberFormatter.setGroupingUsed(true);
        numberFormatter.setMinimumIntegerDigits(1);
        numberFormatter.setMinimumFractionDigits(4);
        numberFormatter.setMaximumFractionDigits(4);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        table = new EditableTable(content, SWT.MULTI|SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        table.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                delete.setEnabled(table.getSelectionCount() != 0);
            }
        });
        TableColumn column = new EditableTableColumn(table, SWT.NONE);
        column.setText(Messages.DividendsPage_Date);
        column.setWidth(70);
        column = new EditableTableColumn(table, SWT.NONE);
        column.setText(Messages.DividendsPage_Value);
        column.setWidth(70);
        
        Composite buttonsComposite = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        buttonsComposite.setLayout(gridLayout);
        buttonsComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        
        add = new Button(buttonsComposite, SWT.PUSH);
        add.setText(Messages.DividendsPage_Add);
        setButtonLayoutData(add);
        add.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                EditableTableItem tableItem = new EditableTableItem(table, SWT.NONE);
                tableItem.setText(0, dateFormat.format(Calendar.getInstance().getTime()));
                tableItem.setText(1, numberFormatter.format(0.01));
            }
        });
        delete = new Button(buttonsComposite, SWT.PUSH);
        delete.setText(Messages.DividendsPage_Delete);
        setButtonLayoutData(delete);
        delete.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                TableItem[] selection = table.getSelection();
                for (int i = 0; i < selection.length; i++)
                    selection[i].dispose();
                delete.setEnabled(table.getSelectionCount() != 0);
            }
        });

        if (security != null)
        {
            for (Iterator iter = security.getDividends().iterator(); iter.hasNext(); )
            {
                Dividend dividend = (Dividend)iter.next();
                EditableTableItem tableItem = new EditableTableItem(table, SWT.NONE);
                tableItem.setText(0, dateFormat.format(dividend.getDate()));
                tableItem.setText(1, numberFormatter.format(dividend.getValue()));
            }
        }

        setValid(true);

        return content;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        if (isValid())
        {
            security.setDividends(getDividends());
            security.setChanged();
        }
        
        return super.performOk();
    }

    public List getDividends()
    {
        List list = new ArrayList();
        
        TableItem[] items = table.getItems();
        for (int i = 0; i < items.length; i++)
        {
            try {
                Dividend dividend = new Dividend();
                dividend.setDate(dateParse.parse(items[i].getText(0)));
                dividend.setValue(numberFormatter.parse(items[i].getText(1)).doubleValue());
                list.add(dividend);
            } catch(Exception e) {
                LogFactory.getLog(getClass()).error(e);
            }
        }
        
        return list;
    }

    class EditableTableItem extends TableItem implements IEditableItem
    {

        public EditableTableItem(Table parent, int style, int index)
        {
            super(parent, style, index);
        }

        public EditableTableItem(Table parent, int style)
        {
            super(parent, style);
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.TableItem#checkSubclass()
         */
        protected void checkSubclass()
        {
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ui.widgets.IEditableItem#canEdit(int)
         */
        public boolean canEdit(int index)
        {
            return true;
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ui.widgets.IEditableItem#isEditable()
         */
        public boolean isEditable()
        {
            return true;
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ui.widgets.IEditableItem#itemEdited(int, java.lang.String)
         */
        public void itemEdited(int index, String text)
        {
            if (index == 0)
            {
                try {
                    Date date = dateParse.parse(text);
                    setText(index, dateFormat.format(date));
                } catch(Exception e) {
                    LogFactory.getLog(getClass()).warn(e);
                    setText(index, dateFormat.format(Calendar.getInstance().getTime()));
                }
            }
            else if (index == 1)
            {
                try {
                    double value = numberFormatter.parse(text).doubleValue();
                    setText(index, numberFormatter.format(value));
                } catch(Exception e) {
                    LogFactory.getLog(getClass()).warn(e);
                }
            }
        }
    }
}
