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

import net.sourceforge.eclipsetrader.core.db.feed.FeedSource;
import net.sourceforge.eclipsetrader.core.ui.preferences.FeedOptions;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class FeedSelectionDialog extends TitleAreaDialog
{
    FeedOptions feedOptions;
    FeedSource feedSource;

    public FeedSelectionDialog(Shell parentShell, String categoryId)
    {
        super(parentShell);
        feedOptions = new FeedOptions(categoryId) {
            public Composite createControls(Composite parent)
            {
                Composite control = super.createControls(parent);
                symbol.setEnabled(false);
                return control;
            }
        };
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        newShell.setText("Feed");
        super.configureShell(newShell);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        setTitle("Change Feed");
        setMessage("Select the feed that will be applied to the selected securities");
        Control control = feedOptions.createControls((Composite)super.createDialogArea(parent));
        return control;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        feedSource = feedOptions.getFeed();
        super.okPressed();
    }

    public FeedSource getFeedSource()
    {
        return feedSource;
    }
}
