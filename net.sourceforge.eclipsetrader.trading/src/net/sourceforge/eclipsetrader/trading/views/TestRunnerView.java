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

package net.sourceforge.eclipsetrader.trading.views;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.Transaction;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;
import net.sourceforge.eclipsetrader.core.db.trading.TradingSystem;
import net.sourceforge.eclipsetrader.trading.TradingPlugin;
import net.sourceforge.eclipsetrader.trading.TradingSystemPlugin;
import net.sourceforge.eclipsetrader.trading.internal.TestProgressBar;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class TestRunnerView extends ViewPart
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.trading.test";
    protected TestProgressBar fProgressBar;
    protected CounterPanel fCounterPanel;
    protected Table fLog;
    protected Text fDetails;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    private NumberFormat priceFormatter = NumberFormat.getInstance();
    private NumberFormat amountFormatter = NumberFormat.getInstance();
    private NumberFormat numberFormatter = NumberFormat.getInstance();
    private NumberFormat percentFormatter = NumberFormat.getInstance();

    public TestRunnerView()
    {
        priceFormatter.setGroupingUsed(true);
        priceFormatter.setMinimumIntegerDigits(1);
        priceFormatter.setMinimumFractionDigits(4);
        priceFormatter.setMaximumFractionDigits(4);

        amountFormatter.setGroupingUsed(true);
        amountFormatter.setMinimumIntegerDigits(1);
        amountFormatter.setMinimumFractionDigits(2);
        amountFormatter.setMaximumFractionDigits(2);

        numberFormatter.setGroupingUsed(true);
        numberFormatter.setMinimumIntegerDigits(1);
        numberFormatter.setMinimumFractionDigits(0);
        numberFormatter.setMaximumFractionDigits(0);

        percentFormatter.setGroupingUsed(true);
        percentFormatter.setMinimumIntegerDigits(1);
        percentFormatter.setMinimumFractionDigits(2);
        percentFormatter.setMaximumFractionDigits(2);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
        content.setLayout(gridLayout);

        fCounterPanel = new CounterPanel(content);
        fCounterPanel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        fProgressBar = new TestProgressBar(content);
        fProgressBar.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

        SashForm sashForm = new SashForm(content, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        ViewForm left = new ViewForm(sashForm, SWT.NONE);
        CLabel label = new CLabel(left, SWT.NONE);
        label.setText("Log");
        left.setTopLeft(label);
        fLog = new Table(left, SWT.SINGLE|SWT.FULL_SELECTION);
        new TableColumn(fLog, SWT.LEFT);
        new TableColumn(fLog, SWT.LEFT);
        new TableColumn(fLog, SWT.LEFT);
        fLog.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                updateDetails();
            }
        });
        left.setContent(fLog);

        ViewForm right = new ViewForm(sashForm, SWT.NONE);
        label = new CLabel(right, SWT.NONE);
        label.setText("Details");
        right.setTopLeft(label);
        fDetails = new Text(right, SWT.BORDER|SWT.MULTI|SWT.V_SCROLL|SWT.H_SCROLL);
        right.setContent(fDetails);

        sashForm.setWeights(new int[] { 50, 50 });
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus()
    {
    }
    
    /**
     * Run a trading system simulation.
     * 
     * @param system the trading system to test
     * @param begin the first date in the history period
     * @param end the last date in the history period
     */
    public void runTradingSystem(TradingSystem system, Date begin, Date end)
    {
        fProgressBar.reset();
        fCounterPanel.reset();
        fLog.removeAll();
        fDetails.setText("");

        Job job = new SimulatorJob(system, begin, end);
        job.setSystem(true);
        job.setUser(false);
        job.schedule();
    }
    
    private void updateDetails()
    {
        TableItem[] selection = fLog.getSelection();
        Transaction transaction = (Transaction)selection[0].getData();
        if (transaction != null)
        {
            StringBuffer sb = new StringBuffer();
            sb.append(dateFormatter.format(transaction.getDate()) + "\r\n");
            sb.append(transaction.getQuantity() > 0 ? "Buy\r\n" : "Sell\r\n");
            sb.append("Quantity: " + numberFormatter.format(Math.abs(transaction.getQuantity())) + "\r\n");
            sb.append("Expenses: " + amountFormatter.format(Math.abs(transaction.getExpenses())) + "\r\n");
            sb.append("Total: " + amountFormatter.format(Math.abs(transaction.getAmount())) + "\r\n");
            
            if (transaction.getData() != null)
            {
                Transaction ref = (Transaction)transaction.getData();
                sb.append("\r\nClosing position opened on " + dateFormatter.format(ref.getDate()) + "\r\n");
                sb.append("Gain: " + amountFormatter.format(Math.abs(transaction.getAmount()) - Math.abs(ref.getAmount())) + "\r\n");
            }
            
            fDetails.setText(sb.toString());
        }
        else
            fDetails.setText("");
    }
    
    private class SimulatorJob extends Job
    {
        private Account account;
        private Security security;
        private java.util.List original = new ArrayList();
        private java.util.List history = new ArrayList();
        private TradingSystemPlugin plugin;
        private int signals = 0;
        private int successCount = 0;
        private int failureCount = 0;
        private boolean firstBuy = true;
        private double savedBalance = 0;
        private Transaction lastBuyTransaction = null;
        
        public SimulatorJob(TradingSystem system, Date begin, Date end)
        {
            super("Trading System Simulator");

            // Get the original history
            original = system.getSecurity().getHistory();
            
            int first = -1, last = -1;
            for (int i = 0; i < original.size() && (first == -1 || last == -1); i++)
            {
                Bar bar = (Bar)original.get(i);
                if (first == -1 && (bar.getDate().equals(begin) || bar.getDate().after(begin)))
                    first = i;
                if (last == -1 && (bar.getDate().equals(end) || bar.getDate().after(end)))
                    last = i;
            }
            if (first != -1 && last != -1)
                original = new ArrayList(original.subList(first, last));

            // Create a fake subclass of the security item that returns our history data
            security = new Security(system.getSecurity().getId()) {
                public java.util.List getHistory()
                {
                    return history;
                }
            };
            security.setCode(system.getSecurity().getCode());
            security.setDescription(system.getSecurity().getDescription());

            // Create a fake account to hold the simulation transactions
            account = new Account();
            account.setInitialBalance(system.getAccount().getBalance());
            account.setFixedCommissions(system.getAccount().getFixedCommissions());
            account.setVariableCommissions(system.getAccount().getVariableCommissions());
            account.setMinimumCommission(system.getAccount().getMinimumCommission());
            account.setMaximumCommission(system.getAccount().getMaximumCommission());

            // Create a new instance of the trading system plugin to run the simulation
            plugin = TradingPlugin.createTradingSystemPlugin(system.getPluginId());
            plugin.setAccount(account);
            plugin.setSecurity(security);
            plugin.setParameters(system.getParameters());
            
            plugin.setMaxExposure(system.getMaxExposure());
            plugin.setMinAmount(system.getMinAmount());
            plugin.setMaxAmount(system.getMaxAmount());
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        protected IStatus run(IProgressMonitor monitor)
        {
            fProgressBar.setMaximum(original.size());

            for (int i = 0; i < original.size(); i++)
            {
                final Bar quote = (Bar)original.get(i);
                security.setQuote(new Quote(quote.getClose(), quote.getClose(), quote.getClose()));
                if (plugin.getSignal() == TradingSystem.SIGNAL_BUY)
                {
                    if (firstBuy)
                    {
                        savedBalance = account.getBalance();
                        firstBuy = false;
                    }
                    final Transaction transaction = new Transaction(quote.getDate(), security, plugin.getQuantity(), quote.getOpen());
                    double expenses = account.getFixedCommissions() + (Math.abs(plugin.getQuantity() * quote.getOpen()) / 100.0 * account.getVariableCommissions());
                    if (expenses < account.getMinimumCommission())
                        expenses = account.getMinimumCommission();
                    if (account.getMaximumCommission() != 0 && expenses > account.getMaximumCommission())
                        expenses = account.getMaximumCommission();
                    transaction.setExpenses(expenses);
                    account.getTransactions().add(transaction);
                    lastBuyTransaction = transaction;

                    fLog.getDisplay().syncExec(new Runnable() {
                        public void run()
                        {
                            TableItem tableItem = new TableItem(fLog, SWT.NONE);
                            tableItem.setText(0, dateFormatter.format(transaction.getDate()));
                            tableItem.setText(1, "Buy");
                            tableItem.setText(2, numberFormatter.format(plugin.getQuantity()) + " at " + priceFormatter.format(quote.getOpen()));
                            tableItem.setData(transaction);
                            for (int i = 0; i < fLog.getColumnCount(); i++)
                                fLog.getColumn(i).pack();
                        }
                    });
                }
                else if (plugin.getSignal() == TradingSystem.SIGNAL_SELL)
                {
                    final Transaction transaction = new Transaction(quote.getDate(), security, - plugin.getQuantity(), quote.getOpen());
                    double expenses = account.getFixedCommissions() + (Math.abs(plugin.getQuantity() * quote.getOpen()) / 100.0 * account.getVariableCommissions());
                    if (expenses < account.getMinimumCommission())
                        expenses = account.getMinimumCommission();
                    if (account.getMaximumCommission() != 0 && expenses > account.getMaximumCommission())
                        expenses = account.getMaximumCommission();
                    transaction.setExpenses(expenses);
                    account.getTransactions().add(transaction);
                    transaction.setData(lastBuyTransaction);
                    lastBuyTransaction = null;

                    fLog.getDisplay().syncExec(new Runnable() {
                        public void run()
                        {
                            TableItem tableItem = new TableItem(fLog, SWT.NONE);
                            tableItem.setText(0, dateFormatter.format(transaction.getDate()));
                            tableItem.setText(1, "Sell");
                            tableItem.setText(2, numberFormatter.format(plugin.getQuantity()) + " at " + priceFormatter.format(quote.getOpen()));
                            tableItem.setData(transaction);
                            for (int i = 0; i < fLog.getColumnCount(); i++)
                                fLog.getColumn(i).pack();
                        }
                    });

                    if (account.getBalance() > savedBalance)
                        successCount++;
                    else
                        failureCount++;
                    firstBuy = true;
                }

                fProgressBar.getDisplay().syncExec(new Runnable() {
                    public void run()
                    {
                        fCounterPanel.setRunValue(signals);
                        fCounterPanel.setGainsValue(successCount);
                        fCounterPanel.setLossesValue(failureCount);
                        fProgressBar.step(0);
                    }
                });
                
                history = new ArrayList(original.subList(0, i));
                plugin.run();
                
                if (plugin.getSignal() != TradingSystem.SIGNAL_NONE)
                {
                    fLog.getDisplay().syncExec(new Runnable() {
                        public void run()
                        {
                            TableItem tableItem = new TableItem(fLog, SWT.NONE);
                            tableItem.setText(0, dateFormatter.format(quote.getDate()));
                            tableItem.setText(1, "Signal");
                            switch(plugin.getSignal())
                            {
                                case TradingSystem.SIGNAL_BUY:
                                    tableItem.setText(2, "Buy");
                                    break;
                                case TradingSystem.SIGNAL_SELL:
                                    tableItem.setText(2, "Sell");
                                    break;
                                case TradingSystem.SIGNAL_HOLD:
                                    tableItem.setText(2, "Hold");
                                    break;
                                case TradingSystem.SIGNAL_NEUTRAL:
                                    tableItem.setText(2, "Neutral");
                                    break;
                            }
                            for (int i = 0; i < fLog.getColumnCount(); i++)
                                fLog.getColumn(i).pack();
                        }
                    });
                    signals++;
                }
            }
            
            fLog.getDisplay().syncExec(new Runnable() {
                public void run()
                {
                    double initial = account.getInitialBalance();
                    double current = account.getBalance() + account.getPortfolio(security).getMarketValue();
                    double gain = current - initial;
                    TableItem tableItem = new TableItem(fLog, SWT.NONE);
                    tableItem.setText(0, "");
                    tableItem.setText(1, "Total");
                    tableItem.setText(2, amountFormatter.format(gain) + " (" + percentFormatter.format(gain / initial * 100) + "%)");
                    for (int i = 0; i < fLog.getColumnCount(); i++)
                        fLog.getColumn(i).pack();
                }
            });

            return Status.OK_STATUS;
        }
    }
}
