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

package net.sourceforge.eclipsetrader.trading.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Base abstract class for preference pages used in both a wizard and a preference dialog
 */
public abstract class CommonPreferencePage
{
    private String title;
    private String description;
    private Object container;
    private Control control;
    private boolean isPageComplete = true;

    public CommonPreferencePage()
    {
    }

    public Object getContainer()
    {
        return container;
    }

    public void setContainer(Object container)
    {
        this.container = container;
        if (container instanceof WizardPage)
            ((WizardPage)container).setPageComplete(isPageComplete);
    }
    
    public abstract void createControl(Composite parent);

    /**
     * The default implementation of this method 
     * returns the value of an internal state variable set by
     * <code>setPageComplete</code>. Subclasses may extend.
     */
    public boolean isPageComplete()
    {
        return isPageComplete;
    }

    /**
     * Sets whether this page is complete. 
     *
     * @param complete <code>true</code> if this page is complete, and
     *   and <code>false</code> otherwise
     * @see #isPageComplete
     */
    public void setPageComplete(boolean complete) 
    {
        isPageComplete = complete;
        if (container instanceof WizardPage)
            ((WizardPage)container).setPageComplete(complete);
    }
    
    /**
     * Subclasses must implement this method to perform
     * any special finish processing for their page.
     */
    public abstract void performFinish();

    public Control getControl()
    {
        return control;
    }

    public void setControl(Control control)
    {
        this.control = control;
    } 

    public void setVisible(boolean visible)
    {
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
