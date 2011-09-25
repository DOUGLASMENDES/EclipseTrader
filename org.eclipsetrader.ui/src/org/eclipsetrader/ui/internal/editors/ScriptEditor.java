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

package org.eclipsetrader.ui.internal.editors;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.Script;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.ui.internal.UIActivator;

public class ScriptEditor extends ViewPart implements ISaveablePart {

    public static final String VIEW_ID = "org.eclipsetrader.ui.editors.script";
    public static final String K_VIEWS = "Views";
    public static final String K_URI = "uri";

    private StyledText text;
    private Font font;

    private URI uri;
    private Script script;

    private boolean dirty;
    IDialogSettings dialogSettings;
    IRepositoryService repositoryService;

    public ScriptEditor() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);

        repositoryService = UIActivator.getDefault().getRepositoryService();

        try {
            dialogSettings = UIActivator.getDefault().getDialogSettings().getSection(K_VIEWS).getSection(site.getSecondaryId());
            uri = new URI(dialogSettings.get(K_URI));
            IStoreObject object = UIActivator.getDefault().getRepositoryService().getObjectFromURI(uri);
            if (object instanceof Script) {
                script = (Script) object;
            }
        } catch (URISyntaxException e) {
            Status status = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, "Error loading view " + site.getSecondaryId(), e);
            UIActivator.getDefault().getLog().log(status);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        font = new Font(parent.getDisplay(), "mono", 9, SWT.NORMAL);

        text = new StyledText(parent, SWT.FULL_SELECTION | SWT.WRAP | SWT.V_SCROLL);
        text.setFont(font);
        text.setMargins(5, 5, 5, 5);
        text.addLineStyleListener(new JavaScriptLineStyler());

        if (script != null) {
            setPartName(script.getName());
            if (script.getText() != null) {
                text.setText(script.getText());
            }
        }

        text.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (!dirty) {
                    dirty = true;
                    firePropertyChange(PROP_DIRTY);
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        text.getParent().setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
        script.setText(text.getText());

        IStatus status = repositoryService.runInService(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                repositoryService.saveAdaptable(new IAdaptable[] {
                    script
                });
                return Status.OK_STATUS;
            }
        }, monitor);

        if (status == Status.OK_STATUS) {
            dirty = false;
            firePropertyChange(PROP_DIRTY);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSaveAs()
     */
    @Override
    public void doSaveAs() {
        String newName = "Copy of " + script.getName();
        InputDialog dlg = new InputDialog(getSite().getShell(), "Save As...", "New name:", newName, null);
        if (dlg.open() == InputDialog.OK) {
            final Script newScript = new Script(dlg.getValue());
            newScript.setLanguage(script.getLanguage());
            newScript.setText(text.getText());

            IStatus status = repositoryService.runInService(new IRepositoryRunnable() {

                @Override
                public IStatus run(IProgressMonitor monitor) throws Exception {
                    repositoryService.saveAdaptable(new IAdaptable[] {
                        newScript
                    });
                    return Status.OK_STATUS;
                }
            }, new NullProgressMonitor());

            if (status == Status.OK_STATUS) {
                script = newScript;
                uri = script.getStore().toURI();
                dialogSettings.put(K_URI, uri.toString());
                setPartName(script.getName());

                if (dirty) {
                    dirty = false;
                    firePropertyChange(PROP_DIRTY);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isDirty()
     */
    @Override
    public boolean isDirty() {
        return dirty;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
     */
    @Override
    public boolean isSaveOnCloseNeeded() {
        return dirty;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        if (font != null) {
            font.dispose();
        }
        super.dispose();
    }
}
