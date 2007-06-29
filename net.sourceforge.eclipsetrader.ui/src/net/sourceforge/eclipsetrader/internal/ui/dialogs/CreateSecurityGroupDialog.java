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

package net.sourceforge.eclipsetrader.internal.ui.dialogs;

import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.SecurityGroup;
import net.sourceforge.eclipsetrader.internal.ui.views.explorer.FlatContentProvider;
import net.sourceforge.eclipsetrader.internal.ui.views.explorer.FlatInstrumentsInput;
import net.sourceforge.eclipsetrader.internal.ui.views.explorer.FlatLabelProvider;
import net.sourceforge.eclipsetrader.internal.ui.views.explorer.InstrumentsViewerComparator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CreateSecurityGroupDialog extends TitleAreaDialog {
	private TableViewer viewer;
	private Text groupName;

	public CreateSecurityGroupDialog(Shell parentShell) {
		super(parentShell);
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
	    super.configureShell(newShell);
	    newShell.setText("Create Group");
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
		setTitle("Group");
		setMessage("Create a new group.");

		parent = new Composite(parent, SWT.NONE);
    	parent.setLayout(new GridLayout(2, false));
    	parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(parent, SWT.NONE);
		label.setText("Select the parent group:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

    	viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        ((GridData)viewer.getControl().getLayoutData()).heightHint = 250;
		viewer.setContentProvider(new FlatContentProvider());
		viewer.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
	            return !(element instanceof Security);
            }
		});
		viewer.setLabelProvider(new DecoratingLabelProvider(new FlatLabelProvider(), null));
		viewer.setComparator(new InstrumentsViewerComparator());
		
		viewer.setInput(new FlatInstrumentsInput());
		
		label = new Label(parent, SWT.NONE);
		label.setText("Group name:");
		groupName = new Text(parent, SWT.BORDER);
		groupName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		groupName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	getButton(IDialogConstants.OK_ID).setEnabled(isValid());
            }
		});
		groupName.setFocus();

        Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        return parent;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createButtonBar(Composite parent) {
	    Control control = super.createButtonBar(parent);
    	getButton(IDialogConstants.OK_ID).setEnabled(false);
	    return control;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
    	SecurityGroup group = new SecurityGroup();
    	group.setDescription(groupName.getText());

    	IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
    	if (!selection.isEmpty()) {
    		SecurityGroup parentGroup = (SecurityGroup) selection.getFirstElement();
    		group.setParentGroup(parentGroup);
    	}

    	CorePlugin.getRepository().save(group);
	    
    	super.okPressed();
    }

	protected boolean isValid() {
    	if (groupName.getText().equals("")) {
    		setMessage("The 'group' name is empty");
    		return false;
    	}
    	if (isExistingFolder(groupName.getText())) {
    		setErrorMessage("The same name already exists");
    		return false;
    	}
    	setErrorMessage(null);
    	return true;
    }
    
    protected boolean isExistingFolder(String name) {
		for (Iterator<SecurityGroup> iter = CorePlugin.getRepository().allSecurityGroups().iterator(); iter.hasNext();) {
			SecurityGroup g = iter.next();
			if (g.getDescription().equals(name))
				return true;
		}
		return false;
    }
}
