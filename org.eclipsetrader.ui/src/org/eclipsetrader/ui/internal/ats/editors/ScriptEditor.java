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

package org.eclipsetrader.ui.internal.ats.editors;

import java.net.URI;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.ats.ScriptStrategy;
import org.eclipsetrader.core.repositories.IRepositoryChangeListener;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.RepositoryChangeEvent;
import org.eclipsetrader.core.repositories.RepositoryResourceDelta;
import org.eclipsetrader.ui.internal.ats.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ScriptEditor extends ViewPart implements ISaveablePart {

    public static final String VIEW_ID = "org.eclipsetrader.ui.ats.scritpstrategy.editor";

    private StyledText text;
    private Label cursorLocation;
    private Font font;

    private URI uri;
    private ScriptStrategy script;

    private boolean dirty;
    IDialogSettings dialogSettings;
    IRepositoryService repositoryService;

    private final IRepositoryChangeListener changeListener = new IRepositoryChangeListener() {

        @Override
        public void repositoryResourceChanged(RepositoryChangeEvent event) {
            for (RepositoryResourceDelta delta : event.getDeltas()) {
                if (delta.getResource() != script) {
                    continue;
                }
                if ((delta.getKind() & RepositoryResourceDelta.REMOVED) != 0) {
                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            IWorkbench workbench = PlatformUI.getWorkbench();
                            workbench.getActiveWorkbenchWindow().getActivePage().hideView(ScriptEditor.this);
                        }
                    });
                }
                else if ((delta.getKind() & RepositoryResourceDelta.MOVED_TO) != 0) {
                    dialogSettings.put(Activator.K_URI, script.getStore().toURI().toString());
                }
            }
        }
    };

    public ScriptEditor() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);

        BundleContext bundleContext = Activator.getDefault().getBundle().getBundleContext();

        ServiceReference<IRepositoryService> serviceReference = bundleContext.getServiceReference(IRepositoryService.class);
        repositoryService = bundleContext.getService(serviceReference);

        dialogSettings = Activator.getDefault().getDialogSettings().getSection(Activator.K_VIEWS_SECTION).getSection(site.getSecondaryId());

        try {
            uri = new URI(dialogSettings.get(Activator.K_URI));
            script = (ScriptStrategy) repositoryService.getObjectFromURI(uri);
        } catch (Exception e) {
            throw new PartInitException("Error loading view " + site.getSecondaryId(), e);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        font = new Font(parent.getDisplay(), "mono", 9, SWT.NORMAL);

        Composite contents = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        contents.setLayout(gridLayout);

        text = new StyledText(contents, SWT.FULL_SELECTION | SWT.WRAP | SWT.V_SCROLL);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        text.setFont(font);
        text.setMargins(5, 5, 5, 5);
        text.addLineStyleListener(new JavaScriptLineStyler());
        text.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                updateCursorLocation();
            }
        });

        createStatusBar(contents);

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

        repositoryService.addRepositoryResourceListener(changeListener);

        updateCursorLocation();
    }

    private void createStatusBar(Composite parent) {
        Composite contents = new Composite(parent, SWT.NONE);
        contents.setLayout(new GridLayout(5, false));
        contents.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        GC gc = new GC(parent);
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();

        int heightHint = Dialog.convertVerticalDLUsToPixels(fontMetrics, 12);

        Label label = new Label(contents, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        label = new Label(contents, SWT.SEPARATOR | SWT.VERTICAL);
        label.setLayoutData(new GridData(SWT.DEFAULT, heightHint));

        cursorLocation = new Label(contents, SWT.NONE);
        cursorLocation.setLayoutData(new GridData(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 60), SWT.DEFAULT));
        cursorLocation.setAlignment(SWT.CENTER);

        label = new Label(contents, SWT.SEPARATOR | SWT.VERTICAL);
        label.setLayoutData(new GridData(SWT.DEFAULT, heightHint));

        label = new Label(contents, SWT.NONE);
        label.setLayoutData(new GridData(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 16), SWT.DEFAULT));
    }

    private void updateCursorLocation() {
        int caret = text.getCaretOffset();
        int line = text.getLineAtOffset(caret);
        int column = caret - text.getOffsetAtLine(line);
        cursorLocation.setText(String.format("%d : %d", column + 1, line + 1));
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
            final ScriptStrategy newScript = new ScriptStrategy(dlg.getValue());
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
                dialogSettings.put(Activator.K_URI, uri.toString());
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
        BundleContext bundleContext = Activator.getDefault().getBundle().getBundleContext();

        ServiceReference<IRepositoryService> serviceReference = bundleContext.getServiceReference(IRepositoryService.class);
        if (serviceReference != null && repositoryService != null) {
            repositoryService.removeRepositoryResourceListener(changeListener);
            bundleContext.ungetService(serviceReference);
        }

        if (font != null) {
            font.dispose();
        }

        super.dispose();
    }
}
