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
import net.sourceforge.eclipsetrader.core.db.Event;
import net.sourceforge.eclipsetrader.core.ui.views.EventsView;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
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

public class EventDetailsDialog extends Dialog
{
    private EventsView view;
    private int index;
    private Label date;
    private Label security;
    private Text message;
    private Text longMessage;
    private Button up;
    private Button down;
    private Image upImage = CorePlugin.getImageDescriptor("icons/elcl16/prev_nav.gif").createImage();
    private Image downImage = CorePlugin.getImageDescriptor("icons/elcl16/next_nav.gif").createImage();
    private SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public EventDetailsDialog(EventsView view, int index)
    {
        super(view.getViewSite().getShell());
        this.view = view;
        this.index = index;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Event Details");
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
        label.setText("Date");
        label.setLayoutData(new GridData(60, SWT.DEFAULT));
        date = new Label(content, SWT.NONE);

        Composite buttons = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        buttons.setLayout(gridLayout);
        buttons.setLayoutData(new GridData(GridData.BEGINNING, GridData.FILL, false, false, 1, 3));
        createNavigationButtons(buttons);

        label = new Label(content, SWT.NONE);
        label.setText("Security");
        label.setLayoutData(new GridData(60, SWT.DEFAULT));
        security = new Label(content, SWT.NONE);

        label = new Label(content, SWT.NONE);
        label.setText("Message");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        message = new Text(content, SWT.BORDER|SWT.READ_ONLY|SWT.WRAP);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.heightHint = 40;
        message.setLayoutData(gridData);

        label = new Label(content, SWT.NONE);
        label.setText("Details");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 3, 1));
        longMessage = new Text(content, SWT.BORDER|SWT.READ_ONLY|SWT.MULTI|SWT.V_SCROLL|SWT.WRAP);
        gridData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 3, 1);
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
                if (index > 0)
                {
                    index--;
                    updateEvent();
                }
                up.setEnabled(index > 0);
                down.setEnabled(index < (view.getTable().getItemCount() - 1));
            }
        });
        
        down = new Button(parent, SWT.PUSH);
        down.setImage(downImage);
        down.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                if (index < (view.getTable().getItemCount() - 1))
                {
                    index++;
                    updateEvent();
                }
                up.setEnabled(index > 0);
                down.setEnabled(index < (view.getTable().getItemCount() - 1));
            }
        });

        up.setEnabled(index > 0);
        down.setEnabled(index < (view.getTable().getItemCount() - 1));
    }
    
    private void updateEvent()
    {
        Event event = (Event)view.getTable().getItem(index).getData();
        date.setText(dateTimeFormatter.format(event.getDate()));
        security.setText(event.getSecurity() != null ? event.getSecurity().getDescription() : "");
        message.setText(event.getMessage());
        longMessage.setText(event.getLongMessage());
        message.getParent().layout();
        view.getTable().select(index);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#open()
     */
    public int open()
    {
        int result = super.open();
        upImage.dispose();
        downImage.dispose();
        return result;
    }
}
