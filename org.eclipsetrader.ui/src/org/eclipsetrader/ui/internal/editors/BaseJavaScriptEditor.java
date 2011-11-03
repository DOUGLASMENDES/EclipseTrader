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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.util.IPropertyChangeListener;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.eclipsetrader.ui.internal.UIActivator;

public abstract class BaseJavaScriptEditor extends ViewPart implements ISaveablePart {

    private StyledText text;
    private Label cursorLocation;

    private boolean dirty;
    IThemeManager themeManager;

    private final ModifyListener modifyListener = new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
            if (!dirty) {
                dirty = true;
                firePropertyChange(ISaveablePart.PROP_DIRTY);
            }
        }
    };

    private final IPropertyChangeListener preferencesChangeListener = new IPropertyChangeListener() {

        @Override
        public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
            if (UIActivator.PREFS_TEXT_EDITOR_FONT.equals(event.getProperty())) {
                text.setFont((Font) event.getNewValue());
            }
            else if (IThemeManager.CHANGE_CURRENT_THEME.equals(event.getProperty())) {
                ITheme newTheme = (ITheme) event.getOldValue();
                if (newTheme != null) {
                    text.setFont(newTheme.getFontRegistry().get(UIActivator.PREFS_TEXT_EDITOR_FONT));
                }
            }
        }
    };

    public BaseJavaScriptEditor() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        themeManager = PlatformUI.getWorkbench().getThemeManager();

        Composite contents = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        contents.setLayout(gridLayout);

        text = new StyledText(contents, SWT.FULL_SELECTION | SWT.WRAP | SWT.V_SCROLL);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        text.setMargins(5, 5, 5, 5);
        text.addLineStyleListener(new JavaScriptLineStyler());
        text.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                updateCursorLocation();
            }
        });

        createStatusBar(contents);

        ITheme theme = themeManager.getCurrentTheme();
        text.setFont(theme.getFontRegistry().get(UIActivator.PREFS_TEXT_EDITOR_FONT));
        themeManager.addPropertyChangeListener(preferencesChangeListener);

        text.addModifyListener(modifyListener);

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

    public void setText(String text) {
        this.text.removeModifyListener(modifyListener);
        try {
            this.text.setText(text);
            updateCursorLocation();
        } finally {
            this.text.addModifyListener(modifyListener);
        }
    }

    public String getText() {
        return text.getText();
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
     * @see org.eclipse.ui.ISaveablePart#isDirty()
     */
    @Override
    public boolean isDirty() {
        return dirty;
    }

    protected void setDirty(boolean dirty) {
        if (this.dirty != dirty) {
            this.dirty = dirty;
            firePropertyChange(PROP_DIRTY);
        }
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
        themeManager.removePropertyChangeListener(preferencesChangeListener);
        super.dispose();
    }
}
