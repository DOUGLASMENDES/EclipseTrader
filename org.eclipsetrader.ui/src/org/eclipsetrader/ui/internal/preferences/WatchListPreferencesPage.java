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

package org.eclipsetrader.ui.internal.preferences;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.eclipsetrader.ui.internal.UIActivator;

public class WatchListPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

    private Button alternateBackground;
    private Button enableTickDecorator;
    private ColorSelector positiveTickColor;
    private ColorSelector negativeTickColor;
    private Button drawTickOutline;
    private Button fadeToBackground;

    private ITheme theme;

    public WatchListPreferencesPage() {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(UIActivator.getDefault().getPreferenceStore());

        IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
        theme = themeManager.getCurrentTheme();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);

        alternateBackground = new Button(content, SWT.CHECK);
        alternateBackground.setText("Alternate rows background");
        alternateBackground.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));

        enableTickDecorator = new Button(content, SWT.CHECK);
        enableTickDecorator.setText("Enable highlights");
        enableTickDecorator.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        enableTickDecorator.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateControlsEnablement();
            }
        });

        Label label = new Label(content, SWT.NONE);
        label.setText("Positive highlight");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        ((GridData) label.getLayoutData()).horizontalIndent = convertHorizontalDLUsToPixels(18);
        positiveTickColor = new ColorSelector(content);

        label = new Label(content, SWT.NONE);
        label.setText("Negative highlight");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        ((GridData) label.getLayoutData()).horizontalIndent = convertHorizontalDLUsToPixels(18);
        negativeTickColor = new ColorSelector(content);

        drawTickOutline = new Button(content, SWT.CHECK);
        drawTickOutline.setText("Highlight outline");
        drawTickOutline.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        ((GridData) drawTickOutline.getLayoutData()).horizontalIndent = convertHorizontalDLUsToPixels(18);

        fadeToBackground = new Button(content, SWT.CHECK);
        fadeToBackground.setText("Fade to background");
        fadeToBackground.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        ((GridData) fadeToBackground.getLayoutData()).horizontalIndent = convertHorizontalDLUsToPixels(18);

        IPreferenceStore preferenceStore = getPreferenceStore();
        alternateBackground.setSelection(preferenceStore.getBoolean(UIActivator.PREFS_WATCHLIST_ALTERNATE_BACKGROUND));
        enableTickDecorator.setSelection(preferenceStore.getBoolean(UIActivator.PREFS_WATCHLIST_ENABLE_TICK_DECORATORS));
        positiveTickColor.setColorValue(theme.getColorRegistry().getRGB(UIActivator.PREFS_WATCHLIST_POSITIVE_TICK_COLOR));
        negativeTickColor.setColorValue(theme.getColorRegistry().getRGB(UIActivator.PREFS_WATCHLIST_NEGATIVE_TICK_COLOR));
        drawTickOutline.setSelection(preferenceStore.getBoolean(UIActivator.PREFS_WATCHLIST_DRAW_TICK_OUTLINE));
        fadeToBackground.setSelection(preferenceStore.getBoolean(UIActivator.PREFS_WATCHLIST_FADE_TO_BACKGROUND));

        updateControlsEnablement();

        return content;
    }

    private void updateControlsEnablement() {
        positiveTickColor.setEnabled(enableTickDecorator.getSelection());
        negativeTickColor.setEnabled(enableTickDecorator.getSelection());
        drawTickOutline.setEnabled(enableTickDecorator.getSelection());
        fadeToBackground.setEnabled(enableTickDecorator.getSelection());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        IPreferenceStore preferenceStore = getPreferenceStore();
        ITheme theme = getTheme();

        alternateBackground.setSelection(preferenceStore.getDefaultBoolean(UIActivator.PREFS_WATCHLIST_ALTERNATE_BACKGROUND));
        enableTickDecorator.setSelection(preferenceStore.getDefaultBoolean(UIActivator.PREFS_WATCHLIST_ENABLE_TICK_DECORATORS));
        drawTickOutline.setSelection(preferenceStore.getDefaultBoolean(UIActivator.PREFS_WATCHLIST_DRAW_TICK_OUTLINE));
        fadeToBackground.setSelection(preferenceStore.getDefaultBoolean(UIActivator.PREFS_WATCHLIST_FADE_TO_BACKGROUND));

        positiveTickColor.setColorValue(theme.getColorRegistry().getRGB(UIActivator.PREFS_WATCHLIST_POSITIVE_TICK_COLOR));
        negativeTickColor.setColorValue(theme.getColorRegistry().getRGB(UIActivator.PREFS_WATCHLIST_NEGATIVE_TICK_COLOR));

        updateControlsEnablement();

        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        performApply();
        return super.performOk();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        IPreferenceStore preferenceStore = getPreferenceStore();
        ITheme theme = getTheme();

        preferenceStore.setValue(UIActivator.PREFS_WATCHLIST_ALTERNATE_BACKGROUND, alternateBackground.getSelection());
        preferenceStore.setValue(UIActivator.PREFS_WATCHLIST_ENABLE_TICK_DECORATORS, enableTickDecorator.getSelection());
        preferenceStore.setValue(UIActivator.PREFS_WATCHLIST_DRAW_TICK_OUTLINE, drawTickOutline.getSelection());
        preferenceStore.setValue(UIActivator.PREFS_WATCHLIST_FADE_TO_BACKGROUND, fadeToBackground.getSelection());

        theme.getColorRegistry().put(UIActivator.PREFS_WATCHLIST_POSITIVE_TICK_COLOR, positiveTickColor.getColorValue());
        theme.getColorRegistry().put(UIActivator.PREFS_WATCHLIST_NEGATIVE_TICK_COLOR, negativeTickColor.getColorValue());
    }

    public ITheme getTheme() {
        return theme;
    }

    public void setTheme(ITheme theme) {
        this.theme = theme;
    }
}
