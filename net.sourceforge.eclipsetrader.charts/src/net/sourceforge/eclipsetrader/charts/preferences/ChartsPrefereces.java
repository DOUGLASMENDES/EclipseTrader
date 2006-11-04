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

package net.sourceforge.eclipsetrader.charts.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.eclipsetrader.charts.ChartsPlugin;
import net.sourceforge.eclipsetrader.charts.dialogs.ChartSettingsDialog;
import net.sourceforge.eclipsetrader.charts.views.ChartView;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Chart;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ChartsPrefereces extends PreferencePage implements IWorkbenchPreferencePage
{
    Button never;
    Button onlyOne;
    Spinner extendScale;
    Spinner extendPeriod;
    Tree tree;
    Button settings;
    Button delete;
    Font groupFont;

    public ChartsPrefereces()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
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
        
        Composite group = new Composite(content, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        group.setLayout(new GridLayout(3, false));
        
        Label label = new Label(group, SWT.NONE);
        label.setText("Extend scale by");
        extendScale = new Spinner(group, SWT.BORDER);
        extendScale.setMinimum(0);
        extendScale.setMaximum(100);
        extendScale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        label = new Label(group, SWT.NONE);
        label.setText("%");
        
        label = new Label(group, SWT.NONE);
        label.setText("Extend charts by");
        extendPeriod = new Spinner(group, SWT.BORDER);
        extendPeriod.setMinimum(0);
        extendPeriod.setMaximum(9999);
        extendPeriod.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        label = new Label(group, SWT.NONE);
        label.setText("periods");

        group = new Group(content, SWT.NONE);
        ((Group)group).setText("Hide tabs");
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        group.setLayout(new GridLayout(2, false));
        
        never = new Button(group, SWT.RADIO);
        never.setText("Never");
        onlyOne = new Button(group, SWT.RADIO);
        onlyOne.setText("When only one tab is shown");

        label = new Label(content, SWT.NONE);
        label.setText("All Charts");
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        tree = new Tree(content, SWT.SINGLE|SWT.FULL_SELECTION|SWT.BORDER);
        tree.setHeaderVisible(false);
        tree.setLinesVisible(false);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        tree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                updateButtonsEnablement();
            }
        });
        tree.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e)
            {
                if (tree.getItem(new Point(e.x, e.y)) == null)
                {
                    tree.deselectAll();
                    updateButtonsEnablement();
                }
            }

            public void mouseDoubleClick(MouseEvent e)
            {
                changeSettings();
            }
        });

        Composite buttonsComposite = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        buttonsComposite.setLayout(gridLayout);
        buttonsComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
        
        settings = createButton(buttonsComposite, "Settings");
        settings.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                changeSettings();
            }
        });
        
        delete = createButton(buttonsComposite, "Delete");
        delete.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                TreeItem[] selection = tree.getSelection();
                if (selection.length != 0 && selection[0].getData() instanceof Chart)
                {
                    if (MessageDialog.openConfirm(tree.getShell(), "Delete Chart", "Do you really want to delete the selected chart ?"))
                    {
                        CorePlugin.getRepository().delete((Chart)selection[0].getData());
                        updateTree();
                    }
                }
            }
        });
        
        Font font = tree.getFont();
        FontData fontData = font.getFontData()[0];
        groupFont = new Font(font.getDevice(), fontData.getName(), fontData.getHeight(), SWT.BOLD);
        tree.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                groupFont.dispose();
            }
        });

        performDefaults();

        return content;
    }
    
    protected Button createButton(Composite parent, String text)
    {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(text);
        Dialog.applyDialogFont(button);
        GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        gridData.widthHint = Math.max(convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH), button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
        button.setLayoutData(gridData);
        return button;
    }
    
    protected void updateTree()
    {
        tree.setRedraw(false);
        tree.removeAll();

        Map map = new HashMap();
        for (Iterator iter = CorePlugin.getRepository().allCharts().iterator(); iter.hasNext(); )
        {
            Chart chart = (Chart)iter.next();
            List list = (List)map.get(chart.getSecurity());
            if (list == null)
            {
                list = new ArrayList();
                map.put(chart.getSecurity(), list);
            }
            list.add(chart);
        }
        
        List securities = new ArrayList(map.keySet());
        Collections.sort(securities, new Comparator() {
            public int compare(Object o1, Object o2)
            {
                return ((Security)o1).getDescription().compareTo(((Security)o2).getDescription());
            }
        });
        
        for (Iterator iter = securities.iterator(); iter.hasNext(); )
        {
            Security security = (Security)iter.next();
            
            TreeItem parentItem = new TreeItem(tree, SWT.NONE);
            parentItem.setText(security.getDescription());
            parentItem.setFont(groupFont);
            
            Chart[] charts = (Chart[])((List)map.get(security)).toArray(new Chart[0]);
            for (int i = 0; i < charts.length; i++)
            {
                TreeItem treeItem = new TreeItem(parentItem, SWT.NONE);
                treeItem.setText(charts[i].getTitle());
                treeItem.setData(charts[i]);
            }
            
            parentItem.setExpanded(true);
        }
        
        tree.setRedraw(true);
        
        updateButtonsEnablement();
    }
    
    void changeSettings()
    {
        TreeItem[] selection = tree.getSelection();
        if (selection.length != 0 && selection[0].getData() instanceof Chart)
        {
            ChartSettingsDialog dlg = new ChartSettingsDialog((Chart)selection[0].getData(), tree.getShell());
            if (dlg.open() == ChartSettingsDialog.OK)
                updateTree();
        }
    }
    
    protected void updateButtonsEnablement()
    {
        TreeItem[] selection = tree.getSelection();
        settings.setEnabled(selection.length != 0 && selection[0].getData() instanceof Chart);
        delete.setEnabled(selection.length != 0 && selection[0].getData() instanceof Chart);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        IPreferenceStore pluginPreferences = ChartsPlugin.getDefault().getPreferenceStore();

        if (never.getSelection())
            pluginPreferences.setValue(ChartsPlugin.PREFS_HIDE_TABS, ChartView.HIDE_TABS_NEVER);
        else if (onlyOne.getSelection())
            pluginPreferences.setValue(ChartsPlugin.PREFS_HIDE_TABS, ChartView.HIDE_TABS_ONLYONE);

        pluginPreferences.setValue(ChartsPlugin.PREFS_EXTEND_SCALE, extendScale.getSelection());
        pluginPreferences.setValue(ChartsPlugin.PREFS_EXTEND_PERIOD, extendPeriod.getSelection());

        return super.performOk();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults()
    {
        IPreferenceStore pluginPreferences = ChartsPlugin.getDefault().getPreferenceStore();
        
        int autoHideTabs = pluginPreferences.getInt(ChartsPlugin.PREFS_HIDE_TABS);
        never.setSelection(autoHideTabs == ChartView.HIDE_TABS_NEVER);
        onlyOne.setSelection(autoHideTabs == ChartView.HIDE_TABS_ONLYONE);
        
        extendScale.setSelection(pluginPreferences.getInt(ChartsPlugin.PREFS_EXTEND_SCALE));
        extendPeriod.setSelection(pluginPreferences.getInt(ChartsPlugin.PREFS_EXTEND_PERIOD));

        updateTree();

        super.performDefaults();
    }
}
