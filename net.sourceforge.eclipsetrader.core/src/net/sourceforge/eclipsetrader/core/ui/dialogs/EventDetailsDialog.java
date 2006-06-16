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

package net.sourceforge.eclipsetrader.core.ui.dialogs;

import java.text.SimpleDateFormat;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.Event;
import net.sourceforge.eclipsetrader.core.ui.internal.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class EventDetailsDialog extends Dialog implements ICollectionObserver
{
    Event event;
    Label date;
    Label security;
    Text message;
    Text longMessage;
    Button up;
    Button down;
    Image upImage = CorePlugin.getImageDescriptor("icons/elcl16/prev_nav.gif").createImage(); //$NON-NLS-1$
    Image downImage = CorePlugin.getImageDescriptor("icons/elcl16/next_nav.gif").createImage(); //$NON-NLS-1$
    SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$
    private DisposeListener dialogDisposeListener = new DisposeListener() {
        public void widgetDisposed(DisposeEvent e)
        {
            CorePlugin.getRepository().allEvents().removeCollectionObserver(EventDetailsDialog.this);
            upImage.dispose();
            downImage.dispose();
        }
    };

    public EventDetailsDialog(Event event, Shell parentShell)
    {
        super(parentShell);
        this.event = event;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(Messages.EventDetailsDialog_Title);
        newShell.addDisposeListener(dialogDisposeListener);
        CorePlugin.getRepository().allEvents().addCollectionObserver(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        gridLayout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        gridLayout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        gridLayout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

        Label label = new Label(content, SWT.NONE);
        label.setText(Messages.EventDetailsDialog_Date);
        label.setLayoutData(new GridData(60, SWT.DEFAULT));
        date = new Label(content, SWT.NONE);

        Composite buttons = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        buttons.setLayout(gridLayout);
        buttons.setLayoutData(new GridData(GridData.BEGINNING, GridData.FILL, false, false, 1, 3));
        createNavigationButtons(buttons);

        label = new Label(content, SWT.NONE);
        label.setText(Messages.EventDetailsDialog_Security);
        label.setLayoutData(new GridData(60, SWT.DEFAULT));
        security = new Label(content, SWT.NONE);

        label = new Label(content, SWT.NONE);
        label.setText(Messages.EventDetailsDialog_Message);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        message = new Text(content, SWT.BORDER|SWT.READ_ONLY|SWT.WRAP);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = 300;
        gridData.heightHint = 40;
        message.setLayoutData(gridData);

        label = new Label(content, SWT.NONE);
        label.setText(Messages.EventDetailsDialog_Details);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 3, 1));
        longMessage = new Text(content, SWT.BORDER|SWT.READ_ONLY|SWT.MULTI|SWT.V_SCROLL|SWT.WRAP);
        gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 3, 1);
        gridData.widthHint = 350;
        gridData.heightHint = 150;
        longMessage.setLayoutData(gridData);
        
        updateEvent();
        
        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent)
    {
        Control contents = super.createContents(parent);
        getButton(IDialogConstants.OK_ID).setFocus();
        return contents;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent)
    {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }
    
    protected void createNavigationButtons(Composite parent)
    {
        up = new Button(parent, SWT.PUSH);
        up.setImage(upImage);
        up.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                upPressed();
            }
        });
        
        down = new Button(parent, SWT.PUSH);
        down.setImage(downImage);
        down.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                downPressed();
            }
        });

        updateButtonStatus();
    }
    
    protected void upPressed()
    {
        int index = CorePlugin.getRepository().allEvents().indexOf(event);
        if (index < (CorePlugin.getRepository().allEvents().size() - 1))
        {
            event = (Event) CorePlugin.getRepository().allEvents().get(index + 1);
            updateEvent();
        }
        updateButtonStatus();
    }
    
    protected void downPressed()
    {
        int index = CorePlugin.getRepository().allEvents().indexOf(event);
        if (index > 0)
        {
            event = (Event) CorePlugin.getRepository().allEvents().get(index - 1);
            updateEvent();
        }
        updateButtonStatus();
    }
    
    protected void updateButtonStatus()
    {
        int index = CorePlugin.getRepository().allEvents().indexOf(event);
        up.setEnabled(index < (CorePlugin.getRepository().allEvents().size() - 1));
        down.setEnabled(index > 0);
    }
    
    protected void updateEvent()
    {
        date.setText(dateTimeFormatter.format(event.getDate()));
        security.setText(event.getSecurity() != null ? event.getSecurity().getDescription() : ""); //$NON-NLS-1$
        message.setText(event.getMessage());
        longMessage.setText(event.getLongMessage());
        message.getParent().layout();
    }

    protected Event getEvent()
    {
        return event;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemAdded(java.lang.Object)
     */
    public void itemAdded(Object o)
    {
        if (!getShell().isDisposed())
        {
            getShell().getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    if (!getShell().isDisposed())
                        updateButtonStatus();
                }
            });
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemRemoved(java.lang.Object)
     */
    public void itemRemoved(Object o)
    {
        if (!getShell().isDisposed())
        {
            getShell().getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    if (!getShell().isDisposed())
                        updateButtonStatus();
                }
            });
        }
    }
}
