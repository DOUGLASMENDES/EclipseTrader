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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import net.sourceforge.eclipsetrader.charts.ChartsPlugin;
import net.sourceforge.eclipsetrader.charts.dialogs.IndicatorSettingsDialog;
import net.sourceforge.eclipsetrader.charts.internal.Messages;
import net.sourceforge.eclipsetrader.charts.views.ChartView;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Chart;
import net.sourceforge.eclipsetrader.core.db.ChartIndicator;
import net.sourceforge.eclipsetrader.core.db.ChartObject;
import net.sourceforge.eclipsetrader.core.db.ChartRow;
import net.sourceforge.eclipsetrader.core.db.ChartTab;
import net.sourceforge.eclipsetrader.core.db.visitors.IChartVisitor;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DefaultConfigurationPage extends PreferencePage implements IWorkbenchPreferencePage
{
    Combo period;
    Text begin;
    Text end;
    Combo interval;
    Tree tree;
    Button addRow;
    Button addTab;
    Button addIndicator;
    Button settings;
    Button remove;
    Chart chart = ChartsPlugin.createDefaultChart();

    public DefaultConfigurationPage()
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

        Label label = new Label(content, SWT.NONE);
        label.setText(Messages.DefaultConfigurationPage_Period);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        period = new Combo(content, SWT.READ_ONLY);
        period.add(Messages.Period_All);
        period.add(Messages.Period_2Years);
        period.add(Messages.Period_1Year);
        period.add(Messages.Period_6Months);
        period.add(Messages.Period_Custom);
        switch(chart.getPeriod())
        {
            case ChartView.PERIOD_ALL:
                period.select(0);
                break;
            case ChartView.PERIOD_LAST2YEARS:
                period.select(1);
                break;
            case ChartView.PERIOD_LASTYEAR:
                period.select(2);
                break;
            case ChartView.PERIOD_LAST6MONTHS:
                period.select(3);
                break;
            case ChartView.PERIOD_CUSTOM:
                period.select(4);
                break;
        }
        period.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                begin.setEnabled(period.getSelectionIndex() == 4);
                end.setEnabled(period.getSelectionIndex() == 4);
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText(Messages.DefaultConfigurationPage_BeginDate);
        begin = new Text(content, SWT.BORDER);
        if (chart.getBeginDate() != null)
            begin.setText(CorePlugin.getDateFormat().format(chart.getBeginDate()));
        begin.setEnabled(period.getSelectionIndex() == 4);
        begin.setLayoutData(new GridData(70, SWT.DEFAULT));
        begin.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e)
            {
                adjustDateFormat(begin);
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText(Messages.DefaultConfigurationPage_EndDate);
        end = new Text(content, SWT.BORDER);
        if (chart.getEndDate() != null)
            end.setText(CorePlugin.getDateFormat().format(chart.getEndDate()));
        end.setEnabled(period.getSelectionIndex() == 4);
        end.setLayoutData(new GridData(70, SWT.DEFAULT));
        end.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e)
            {
                adjustDateFormat(end);
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText(Messages.DefaultConfigurationPage_Interval);
        interval = new Combo(content, SWT.READ_ONLY);
        interval.add(Messages.Interval_Monthly);
        interval.add(Messages.Interval_Weekly);
        interval.add(Messages.Interval_Daily);
        interval.add(Messages.Interval_1Hour);
        interval.add(Messages.Interval_30Min);
        interval.add(Messages.Interval_15Min);
        interval.add(Messages.Interval_10Min);
        interval.add(Messages.Interval_5Min);
        interval.add(Messages.Interval_2Min);
        interval.add(Messages.Interval_1Min);
        interval.select(10 - chart.getCompression());
        
        label = new Label(content, SWT.NONE);
        label.setText(Messages.DefaultConfigurationPage_Layout);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        Composite group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        tree = new Tree(group, SWT.SINGLE|SWT.FULL_SELECTION|SWT.BORDER);
        tree.setHeaderVisible(false);
        tree.setLinesVisible(false);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ((GridData)tree.getLayoutData()).heightHint = 200;
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
        });

        Composite buttonsComposite = new Composite(group, SWT.NONE);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        buttonsComposite.setLayout(gridLayout);
        buttonsComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
        
        addRow = new Button(buttonsComposite, SWT.PUSH);
        addRow.setText(Messages.DefaultConfigurationPage_AddRow);
        addRow.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        addRow.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                int index = -1;
                
                TreeItem[] selection = tree.getSelection();
                if (selection.length != 0)
                {
                    TreeItem treeItem = selection[0];
                    while(treeItem.getParentItem() != null)
                        treeItem = treeItem.getParentItem();
                    index = tree.indexOf(selection[0]);
                }

                if (index != -1)
                    chart.add(index + 1, new ChartRow());
                else
                    chart.add(new ChartRow());
                updateTree();
            }
        });
        
        addTab = new Button(buttonsComposite, SWT.PUSH);
        addTab.setText(Messages.DefaultConfigurationPage_AddTab);
        addTab.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        addTab.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                InputDialog dlg = new InputDialog(tree.getShell(), Messages.DefaultConfigurationPage_AddTabDialogTitle, Messages.DefaultConfigurationPage_AddTabDialogMessage, "", new IInputValidator() {//$NON-NLS-1$
                    public String isValid(String newText)
                    {
                        return newText.length() == 0 ? "" : null; //$NON-NLS-1$
                    }
                });
                if (dlg.open() == InputDialog.OK)
                {
                    ChartTab tab = new ChartTab();
                    tab.setLabel(dlg.getValue());

                    TreeItem[] selection = tree.getSelection();
                    if (selection.length != 0)
                    {
                        if (selection[0].getData() instanceof ChartRow)
                            ((ChartRow)selection[0].getData()).add(tab);
                        else if (selection[0].getData() instanceof ChartTab)
                            ((ChartTab)selection[0].getData()).getParent().add(tab);
                        else if (selection[0].getData() instanceof ChartIndicator)
                            ((ChartIndicator)selection[0].getData()).getParent().getParent().add(tab);
                        updateTree();
                    }
                }
            }
        });
        
        addIndicator = new Button(buttonsComposite, SWT.PUSH);
        addIndicator.setText(Messages.DefaultConfigurationPage_AddIndicator);
        addIndicator.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        addIndicator.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                AddIndicatorWizard wizard = new AddIndicatorWizard();
                ChartIndicator indicator = wizard.open();
                if (indicator != null)
                {
                    TreeItem[] selection = tree.getSelection();
                    if (selection.length != 0)
                    {
                        if (selection[0].getData() instanceof ChartTab)
                            ((ChartTab)selection[0].getData()).getIndicators().add(indicator);
                        else if (selection[0].getData() instanceof ChartIndicator)
                            ((ChartIndicator)selection[0].getData()).getParent().getIndicators().add(indicator);
                        updateTree();
                    }
                }
            }
        });
        
        settings = new Button(buttonsComposite, SWT.PUSH);
        settings.setText(Messages.DefaultConfigurationPage_Settings);
        GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        gridData.verticalIndent = 5;
        settings.setLayoutData(gridData);
        settings.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                TreeItem[] selection = tree.getSelection();
                if (selection.length != 0)
                {
                    if (selection[0].getData() instanceof ChartTab)
                    {
                        ChartTab tab = (ChartTab)selection[0].getData();
                        InputDialog dlg = new InputDialog(tree.getShell(), Messages.DefaultConfigurationPage_TabSettingsDialogTitle, Messages.DefaultConfigurationPage_TabSettingsDialogMessage, tab.getLabel(), new IInputValidator() {
                            public String isValid(String newText)
                            {
                                return newText.length() == 0 ? "" : null; //$NON-NLS-1$
                            }
                        });
                        if (dlg.open() == InputDialog.OK)
                        {
                            tab.setLabel(dlg.getValue());
                            selection[0].setText(tab.getLabel());
                        }
                    }
                    else if (selection[0].getData() instanceof ChartIndicator)
                    {
                        ChartIndicator indicator = (ChartIndicator)selection[0].getData();
                        IndicatorSettingsDialog dlg = new IndicatorSettingsDialog(indicator, tree.getShell());
                        dlg.open();
                    }
                }
            }
        });
        
        remove = new Button(buttonsComposite, SWT.PUSH);
        remove.setText(Messages.DefaultConfigurationPage_Remove);
        gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        gridData.verticalIndent = 5;
        remove.setLayoutData(gridData);
        remove.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                TreeItem[] selection = tree.getSelection();
                if (selection.length != 0)
                {
                    if (selection[0].getData() instanceof ChartRow)
                        ((ChartRow)selection[0].getData()).getParent().getRows().remove(selection[0].getData());
                    else if (selection[0].getData() instanceof ChartTab)
                        ((ChartTab)selection[0].getData()).getParent().getTabs().remove(selection[0].getData());
                    else if (selection[0].getData() instanceof ChartIndicator)
                        ((ChartIndicator)selection[0].getData()).getParent().getIndicators().remove(selection[0].getData());
                    selection[0].dispose();
                    updateTree();
                }
            }
        });
        
        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults()
    {
        chart = ChartsPlugin.createDefaultChart();
        updateTree();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        switch(period.getSelectionIndex())
        {
            case 0:
                chart.setPeriod(ChartView.PERIOD_ALL);
                break;
            case 1:
                chart.setPeriod(ChartView.PERIOD_LAST2YEARS);
                break;
            case 2:
                chart.setPeriod(ChartView.PERIOD_LASTYEAR);
                break;
            case 3:
                chart.setPeriod(ChartView.PERIOD_LAST6MONTHS);
                break;
            case 4:
            {
                chart.setPeriod(ChartView.PERIOD_CUSTOM);
                try
                {
                    chart.setBeginDate(CorePlugin.getDateParse().parse(begin.getText()));
                    chart.setEndDate(CorePlugin.getDateParse().parse(end.getText()));
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        chart.setCompression(10 - interval.getSelectionIndex());

        ChartsPlugin.saveDefaultChart(chart);
        
        return super.performOk();
    }

    protected void updateTree()
    {
        TreeItem[] selection = tree.getSelection();
        final Object data = (selection.length != 0) ? selection[0].getData() : null;
        
        tree.setRedraw(false);
        tree.removeAll();

        chart.accept(new IChartVisitor() {

            public void visit(Chart chart)
            {
            }

            public void visit(ChartRow row)
            {
                TreeItem treeItem = new TreeItem(tree, SWT.NONE);
                treeItem.setText(Messages.DefaultConfigurationPage_Row + String.valueOf(tree.getItemCount())); 
                treeItem.setData(row);
                if (row.equals(data))
                    tree.setSelection(treeItem);
            }

            public void visit(ChartTab tab)
            {
                TreeItem parentItem = tree.getItem(tree.getItemCount() - 1);
                
                TreeItem treeItem = new TreeItem(parentItem, SWT.NONE);
                treeItem.setText(tab.getLabel());
                treeItem.setData(tab);
                if (tab.equals(data))
                    tree.setSelection(treeItem);
                
                parentItem.setExpanded(true);
            }

            public void visit(ChartIndicator indicator)
            {
                TreeItem parentItem = tree.getItem(tree.getItemCount() - 1);
                parentItem = parentItem.getItem(parentItem.getItemCount() - 1); 

                IConfigurationElement plugin = getIndicatorPlugin(indicator.getPluginId());
                
                TreeItem treeItem = new TreeItem(parentItem, SWT.NONE);
                treeItem.setText(plugin.getAttribute("name")); //$NON-NLS-1$
                treeItem.setData(indicator);
                if (indicator.equals(data))
                    tree.setSelection(treeItem);
                
                parentItem.setExpanded(true);
            }

            public void visit(ChartObject object)
            {
            }
        });
        
/*        for (int r = 0; r < chart.getRows().size(); r++)
        {
            ChartRow row = (ChartRow)chart.getRows().get(r);

            TreeItem rowNode = new TreeItem(tree, SWT.NONE);
            rowNode.setText("ROW " + String.valueOf(r + 1));
            rowNode.setData(row);

            for (int t = 0; t < row.getTabs().size(); t++)
            {
                ChartTab tab = (ChartTab)row.getTabs().get(t);

                TreeItem tabNode = new TreeItem(rowNode, SWT.NONE);
                tabNode.setText(tab.getLabel());
                tabNode.setData(tab);

                rowNode.setExpanded(true);
            
                for (int i = 0; i < tab.getIndicators().size(); i++)
                {
                    ChartIndicator indicator = (ChartIndicator)tab.getIndicators().get(i);
                    IConfigurationElement plugin = getIndicatorPlugin(indicator.getPluginId());
                    
                    TreeItem indicatorNode = new TreeItem(tabNode, SWT.NONE);
                    indicatorNode.setText(plugin.getAttribute("name"));
                    indicatorNode.setData(indicator);

                    tabNode.setExpanded(true);
                }
            }
        }*/
        
        tree.setRedraw(true);

        updateButtonsEnablement();
    }
    
    protected void updateButtonsEnablement()
    {
        TreeItem[] selection = tree.getSelection();
        addTab.setEnabled(selection.length != 0);
        addIndicator.setEnabled(selection.length != 0 && (selection[0].getData() instanceof ChartTab || selection[0].getData() instanceof ChartIndicator));
        settings.setEnabled(selection.length != 0 && (selection[0].getData() instanceof ChartTab || selection[0].getData() instanceof ChartIndicator));
        remove.setEnabled(selection.length != 0);
    }

    private static IConfigurationElement getIndicatorPlugin(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(ChartsPlugin.INDICATORS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id)) //$NON-NLS-1$
                    return item;
            }
        }
        
        return null;
    }

    protected void adjustDateFormat(Text widget)
    {
        DateFormat dateDisplay = CorePlugin.getDateFormat();
        DateFormat dateParse = CorePlugin.getDateParse();
        if (widget.getText().length() != 0)
            try
            {
                Date d = dateParse.parse(widget.getText());
                widget.setText(dateDisplay.format(d));
            }
            catch (ParseException pe) {
                CorePlugin.logException(pe);
            }
    }
}
