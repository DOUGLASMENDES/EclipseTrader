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

package net.sourceforge.eclipsetrader.news.actions;

import net.sourceforge.eclipsetrader.news.NewsPlugin;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class StopFeedAction implements IWorkbenchWindowActionDelegate, IPropertyChangeListener
{
    private IAction action;

    public StopFeedAction()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose()
    {
        NewsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window)
    {
        NewsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
        NewsPlugin.getDefault().stopFeed();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
        this.action = action;
        action.setEnabled(NewsPlugin.getDefault().getPreferenceStore().getBoolean(NewsPlugin.FEED_RUNNING));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        if (event.getProperty().equals(NewsPlugin.FEED_RUNNING))
        {
            if (action != null)
                action.setEnabled(NewsPlugin.getDefault().getPreferenceStore().getBoolean(NewsPlugin.FEED_RUNNING));
        }
    }
}
