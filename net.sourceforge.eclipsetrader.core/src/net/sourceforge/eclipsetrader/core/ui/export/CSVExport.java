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

package net.sourceforge.eclipsetrader.core.ui.export;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.ui.internal.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

public class CSVExport extends Wizard implements IExportWizard
{
    ExportSelectionPage securitySelectionPage = new ExportSelectionPage();
    String separator = ","; //$NON-NLS-1$
    String eol = "\r\n"; //$NON-NLS-1$

    public CSVExport()
    {
        setWindowTitle(Messages.CSVExport_Title);
        securitySelectionPage.setTitle(Messages.CSVExport_Title);
        securitySelectionPage.setDescription(Messages.CSVExport_Description);
        addPage(securitySelectionPage);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish()
    {
        final Security[] security = securitySelectionPage.getSelectedSecurity();
        final int export = securitySelectionPage.getDataToExport();
        final String file = securitySelectionPage.getFile();
        
        Job job = new Job(Messages.CSVExport_JobName) {
            protected IStatus run(IProgressMonitor monitor)
            {
                SimpleDateFormat dateFormat = CorePlugin.getDateFormat();
                SimpleDateFormat timeFormat = CorePlugin.getTimeFormat();
                
                NumberFormat priceFormat = NumberFormat.getInstance(Locale.US);
                priceFormat.setMinimumIntegerDigits(1);
                priceFormat.setMinimumFractionDigits(4);
                priceFormat.setMaximumFractionDigits(4);
                priceFormat.setGroupingUsed(false);
                
                NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
                numberFormat.setMinimumIntegerDigits(1);
                numberFormat.setMinimumFractionDigits(0);
                numberFormat.setMaximumFractionDigits(0);
                numberFormat.setGroupingUsed(false);
                
                try
                {
                    Writer writer = new BufferedWriter(new FileWriter(file));;
                    
                    if (export == ExportSelectionPage.HISTORICAL_PRICES)
                    {
                        monitor.beginTask(Messages.CSVExport_HistoricalTask, security.length);
                        for (int i = 0; i < security.length; i++)
                        {
                            monitor.subTask(security[i].getDescription());
                            for (Iterator iter = security[i].getHistory().iterator(); iter.hasNext(); )
                            {
                                Bar bar = (Bar)iter.next();
                                writer.write(security[i].getCode() + separator);
                                writer.write(dateFormat.format(bar.getDate()) + separator);
                                writer.write(priceFormat.format(bar.getOpen()) + separator);
                                writer.write(priceFormat.format(bar.getHigh()) + separator);
                                writer.write(priceFormat.format(bar.getLow()) + separator);
                                writer.write(priceFormat.format(bar.getClose()) + separator);
                                writer.write(numberFormat.format(bar.getVolume()));
                                writer.write(eol);
                            }
                            monitor.worked(1);
                        }
                    }
                    else if (export == ExportSelectionPage.INTRADAY_PRICES)
                    {
                        monitor.beginTask(Messages.CSVExport_IntradayTask, security.length);
                        for (int i = 0; i < security.length; i++)
                        {
                            monitor.setTaskName(security[i].getDescription());
                            for (Iterator iter = security[i].getIntradayHistory().iterator(); iter.hasNext(); )
                            {
                                Bar bar = (Bar)iter.next();
                                writer.write(security[i].getCode() + separator);
                                writer.write(dateFormat.format(bar.getDate()) + separator);
                                writer.write(priceFormat.format(bar.getOpen()) + separator);
                                writer.write(priceFormat.format(bar.getHigh()) + separator);
                                writer.write(priceFormat.format(bar.getLow()) + separator);
                                writer.write(priceFormat.format(bar.getClose()) + separator);
                                writer.write(numberFormat.format(bar.getVolume()) + separator);
                                writer.write(timeFormat.format(bar.getDate()));
                                writer.write(eol);
                            }
                            monitor.worked(1);
                        }
                    }
                    else if (export == ExportSelectionPage.LAST_PRICES)
                    {
                        monitor.beginTask(Messages.CSVExport_LastTask, security.length);
                        for (int i = 0; i < security.length; i++)
                        {
                            monitor.setTaskName(security[i].getDescription());
                            if (security[i].getQuote() != null && security[i].getQuote().getDate() != null)
                            {
                                writer.write(security[i].getCode() + separator);
                                writer.write(dateFormat.format(security[i].getQuote().getDate()) + separator);
                                writer.write(priceFormat.format(security[i].getOpen() != null ? security[i].getOpen() : new Double(0)) + separator);
                                writer.write(priceFormat.format(security[i].getHigh() != null ? security[i].getHigh() : new Double(0)) + separator);
                                writer.write(priceFormat.format(security[i].getLow() != null ? security[i].getLow() : new Double(0)) + separator);
                                writer.write(priceFormat.format(security[i].getClose() != null ? security[i].getClose() : new Double(0)) + separator);
                                writer.write(numberFormat.format(security[i].getQuote().getVolume()) + separator);
                                writer.write(timeFormat.format(security[i].getQuote().getDate()));
                                writer.write(eol);
                            }
                            monitor.worked(1);
                        }
                    }
                    
                    writer.flush();
                    writer.close();
                }
                catch (IOException e)
                {
                    CorePlugin.logException(e);
                    return new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.OK, Messages.CSVExport_ErrorMessage, e);
                }
                
                monitor.done();
                
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();

        return true;
    }
}
