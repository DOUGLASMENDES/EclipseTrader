/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.repository.hibernate.internal;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.hibernate.cfg.AnnotationConfiguration;

public class RepositoryValidator {

    public static final int UPDATE_ID = 0;
    public static final int CREATE_ID = 1;
    public static final int CANCEL_ID = 2;

    private static final String UPDATE_LABEL = "Update";
    private static final String CREATE_LABEL = "Create";
    private static final String CANCEL_LABEL = IDialogConstants.CANCEL_LABEL;

    String name;
    AnnotationConfiguration cfg;
    int userChoice = CANCEL_ID;

    public RepositoryValidator(String name, AnnotationConfiguration cfg) {
        this.name = name;
        this.cfg = cfg;
    }

    public int validate() {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                String message = NLS.bind("The repository {0} seems missing or not valid. Do you want to create or update it now ?", new Object[] {
                    name
                });
                MessageDialog dlg = new MessageDialog(null, "Repository Validation", null, message, MessageDialog.QUESTION, new String[] {
                        UPDATE_LABEL, CREATE_LABEL, CANCEL_LABEL
                }, 0);
                userChoice = dlg.open();
            }
        });

        return userChoice;
    }
}
