/*
  * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation  - initial API and implementation
 *     Marco Maccaferri - added image setter/getter
 */

package org.eclipsetrader.directa.internal.ui;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class StatusLineContributionItem extends ContributionItem {

    private final static int DEFAULT_CHAR_WIDTH = 30;

    private int charWidth;

    private CLabel label;

    /**
     * The composite into which this contribution item has been placed. This
     * will be <code>null</code> if this instance has not yet been
     * initialized.
     */
    private Composite statusLine = null;

    private String text = Util.ZERO_LENGTH_STRING;
    private Image image;

    private int widthHint = -1;
    private int heightHint = -1;

    /**
     * Creates a status line contribution item with the given id.
     *
     * @param id
     *            the contribution item's id, or <code>null</code> if it is to
     *            have no id
     */
    public StatusLineContributionItem(String id) {
        this(id, DEFAULT_CHAR_WIDTH);
    }

    /**
     * Creates a status line contribution item with the given id that displays
     * the given number of characters.
     *
     * @param id
     *            the contribution item's id, or <code>null</code> if it is to
     *            have no id
     * @param charWidth
     *            the number of characters to display
     */
    public StatusLineContributionItem(String id, int charWidth) {
        super(id);
        this.charWidth = charWidth;
        setVisible(false); // no text to start with
    }

    @Override
    public void fill(Composite parent) {
        statusLine = parent;

        Label sep = new Label(parent, SWT.SEPARATOR);
        label = new CLabel(statusLine, SWT.SHADOW_NONE);

        if (widthHint < 0) {
            GC gc = new GC(statusLine);
            gc.setFont(statusLine.getFont());
            FontMetrics fm = gc.getFontMetrics();
            widthHint = fm.getAverageCharWidth() * charWidth;
            heightHint = fm.getHeight();
            gc.dispose();
        }

        StatusLineLayoutData data = new StatusLineLayoutData();
        data.widthHint = widthHint;
        label.setLayoutData(data);
        label.setText(text);
        label.setImage(image);

        data = new StatusLineLayoutData();
        data.heightHint = heightHint;
        sep.setLayoutData(data);
    }

    /**
     * An accessor for the current location of this status line contribution
     * item -- relative to the display.
     *
     * @return The current location of this status line; <code>null</code> if
     *         not yet initialized.
     */
    public Point getDisplayLocation() {
        if (label != null && statusLine != null) {
            return statusLine.toDisplay(label.getLocation());
        }

        return null;
    }

    /**
     * Retrieves the text that is being displayed in the status line.
     *
     * @return the text that is currently being displayed
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text to be displayed in the status line.
     *
     * @param text
     *            the text to be displayed, must not be <code>null</code>
     */
    public void setText(String text) {
        Assert.isNotNull(text);

        this.text = escape(text);

        if (label != null && !label.isDisposed()) {
            label.setText(this.text);
        }

        if (this.text.length() == 0) {
            if (isVisible()) {
                setVisible(false);
                IContributionManager contributionManager = getParent();

                if (contributionManager != null) {
                    contributionManager.update(true);
                }
            }
        }
        else {
            if (!isVisible()) {
                setVisible(true);
                IContributionManager contributionManager = getParent();

                if (contributionManager != null) {
                    contributionManager.update(true);
                }
            }
        }
    }

    /**
     * Sets the image to be displayed in the status line.
     *
     * @param image
     *            the image to be displayed
     */
    public void setImage(Image image) {
        this.image = image;

        if (label != null && !label.isDisposed()) {
            label.setImage(this.image);
        }

        if (!isVisible()) {
            setVisible(true);
            IContributionManager contributionManager = getParent();

            if (contributionManager != null) {
                contributionManager.update(true);
            }
        }
    }

    /**
     * Retrieves the image that is being displayed in the status line.
     *
     * @return the image that is currently being displayed
     */
    public Image getImage() {
        return image;
    }

    private String escape(String text) {
        return Util.replaceAll(text, "&", "&&"); //$NON-NLS-1$//$NON-NLS-2$
    }
}
