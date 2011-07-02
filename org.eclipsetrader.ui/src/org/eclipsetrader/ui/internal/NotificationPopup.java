/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *     Benjamin Pasero - intial API and implementation
 *     Tasktop Technologies - initial API and implementation
 */

package org.eclipsetrader.ui.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipsetrader.ui.INotification;
import org.eclipsetrader.ui.internal.SwtUtil.FadeJob;
import org.eclipsetrader.ui.internal.SwtUtil.IFadeListener;

public class NotificationPopup extends Window {

    private static final int TITLE_HEIGHT = 24;
    private static final String LABEL_NOTIFICATION = "Notification";
    private static final String LABEL_JOB_CLOSE = "Close Notification Job";
    private static final int MAX_WIDTH = 400;
    private static final int MIN_HEIGHT = 100;
    private static final long DEFAULT_DELAY_CLOSE = 8 * 1000;
    private static final int PADDING_EDGE = 5;
    private long delayClose = DEFAULT_DELAY_CLOSE;

    private static final String NOTIFICATIONS_HIDDEN = "more...";
    private static final int NUM_NOTIFICATIONS_TO_DISPLAY = 4;

    protected LocalResourceManager resources;
    private NotificationPopupColors color;
    private final Display display;
    private Shell shell;
    private Region lastUsedRegion;
    private Image lastUsedBgImage;

    private List<? extends INotification> notifications;

    private final Job closeJob = new Job(LABEL_JOB_CLOSE) {

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            if (!display.isDisposed()) {
                display.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        Shell shell = NotificationPopup.this.getShell();
                        if (shell == null || shell.isDisposed()) {
                            return;
                        }

                        if (isMouseOver(shell)) {
                            scheduleAutoClose();
                            return;
                        }

                        NotificationPopup.this.closeFade();
                    }

                });
            }
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }

            return Status.OK_STATUS;
        }
    };

    private final boolean respectDisplayBounds = true;
    private final boolean respectMonitorBounds = true;

    private FadeJob fadeJob;
    private boolean supportsFading;
    private boolean fadingEnabled;

    public NotificationPopup(Display display) {
        this(display, SWT.NO_TRIM | SWT.ON_TOP | SWT.NO_FOCUS);
    }

    public NotificationPopup(Display display, int style) {
        super(new Shell(display));
        setShellStyle(style);

        this.display = display;
        resources = new LocalResourceManager(JFaceResources.getResources());
        initResources();

        closeJob.setSystem(true);
    }

    public boolean isFadingEnabled() {
        return fadingEnabled;
    }

    public void setFadingEnabled(boolean fadingEnabled) {
        this.fadingEnabled = fadingEnabled;
    }

    /**
     * Override to return a customized name. Default is to return the name of the product, specified by the -name (e.g.
     * "Eclipse SDK") command line parameter that's associated with the product ID (e.g. "org.eclipse.sdk.ide"). Strips
     * the trailing "SDK" for any name, since this part of the label is considered visual noise.
     *
     * @return the name to be used in the title of the popup.
     */
    protected String getPopupShellTitle() {
        IProduct product = Platform.getProduct();
        if (product != null) {
            String productName = product.getName();
            String LABEL_SDK = "SDK"; //$NON-NLS-1$
            if (productName.endsWith(LABEL_SDK)) {
                productName = productName.substring(0, productName.length() - LABEL_SDK.length());
            }
            return productName + " " + LABEL_NOTIFICATION; //$NON-NLS-1$
        }
        else {
            return LABEL_NOTIFICATION;
        }
    }

    protected Image getPopupShellImage(int maximumHeight) {
        // always use the launching workbench window
        IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
        if (windows != null && windows.length > 0) {
            IWorkbenchWindow workbenchWindow = windows[0];
            if (workbenchWindow != null && !workbenchWindow.getShell().isDisposed()) {
                Image image = getShell().getImage();
                int diff = Integer.MAX_VALUE;
                if (image != null && image.getBounds().height <= maximumHeight) {
                    diff = maximumHeight - image.getBounds().height;
                }
                else {
                    image = null;
                }

                Image[] images = getShell().getImages();
                if (images != null && images.length > 0) {
                    // find the icon that is closest in size, but not larger than maximumHeight
                    for (Image image2 : images) {
                        int newDiff = maximumHeight - image2.getBounds().height;
                        if (newDiff >= 0 && newDiff <= diff) {
                            diff = newDiff;
                            image = image2;
                        }
                    }
                }

                return image;
            }
        }
        return null;
    }

    /**
     * Override to populate with notifications.
     *
     * @param parent
     */
    protected void createContentArea(Composite parent) {
        int count = 0;
        for (final INotification notification : notifications) {
            Composite notificationComposite = new Composite(parent, SWT.NO_FOCUS);
            notificationComposite.setLayout(new GridLayout(2, false));
            notificationComposite.setBackground(parent.getBackground());

            if (count < NUM_NOTIFICATIONS_TO_DISPLAY) {
                final Label notificationLabelIcon = new Label(notificationComposite, SWT.NO_FOCUS);
                notificationLabelIcon.setBackground(parent.getBackground());
                notificationLabelIcon.setImage(notification.getNotificationImage());

                final Link itemLink = new Link(notificationComposite, SWT.BEGINNING | SWT.NO_FOCUS);
                itemLink.setText(notification.getLabel());
                itemLink.setFont(CommonFonts.BOLD);
                itemLink.setBackground(parent.getBackground());
                itemLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

                String descriptionText = null;
                if (notification.getDescription() != null) {
                    descriptionText = notification.getDescription();
                }
                if (descriptionText != null && !descriptionText.trim().equals("")) { //$NON-NLS-1$
                    Label descriptionLabel = new Label(notificationComposite, SWT.NO_FOCUS | SWT.WRAP);
                    descriptionLabel.setText(descriptionText);
                    descriptionLabel.setBackground(parent.getBackground());
                    descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
                    ((GridData) descriptionLabel.getLayoutData()).widthHint = MAX_WIDTH;
                }
            }
            else {
                int numNotificationsRemain = notifications.size() - count;
                Link remainingHyperlink = new Link(notificationComposite, SWT.NO_FOCUS);
                remainingHyperlink.setBackground(parent.getBackground());

                remainingHyperlink.setText(numNotificationsRemain + " " + NOTIFICATIONS_HIDDEN); //$NON-NLS-1$
                GridDataFactory.fillDefaults().span(2, SWT.DEFAULT).applyTo(remainingHyperlink);
                break;
            }
            count++;
        }
    }

    /**
     * Override to customize the title bar
     */
    protected void createTitleArea(Composite parent) {
        ((GridData) parent.getLayoutData()).heightHint = TITLE_HEIGHT;

        Label titleImageLabel = new Label(parent, SWT.NONE);
        titleImageLabel.setImage(getPopupShellImage(TITLE_HEIGHT));

        Label titleTextLabel = new Label(parent, SWT.NONE);
        titleTextLabel.setText(getPopupShellTitle());
        titleTextLabel.setFont(CommonFonts.BOLD);
        titleTextLabel.setForeground(color.getTitleText());
        titleTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        titleTextLabel.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

        final Label button = new Label(parent, SWT.NONE);
        button.setImage(CommonImages.getImage(CommonImages.NOTIFICATION_CLOSE));
        button.addMouseTrackListener(new MouseTrackAdapter() {

            @Override
            public void mouseEnter(MouseEvent e) {
                button.setImage(CommonImages.getImage(CommonImages.NOTIFICATION_CLOSE_HOVER));
            }

            @Override
            public void mouseExit(MouseEvent e) {
                button.setImage(CommonImages.getImage(CommonImages.NOTIFICATION_CLOSE));
            }
        });
        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseUp(MouseEvent e) {
                close();
                setReturnCode(CANCEL);
            }

        });
    }

    private void initResources() {
        color = new NotificationPopupColors(display, resources);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);

        shell = newShell;
        newShell.setBackground(color.getBorder());
    }

    @Override
    public void create() {
        super.create();
        addRegion(shell);
    }

    private void addRegion(Shell shell) {
        Region region = new Region();
        Point s = shell.getSize();

        /* Add entire Shell */
        region.add(0, 0, s.x, s.y);

        /* Subtract Top-Left Corner */
        region.subtract(0, 0, 5, 1);
        region.subtract(0, 1, 3, 1);
        region.subtract(0, 2, 2, 1);
        region.subtract(0, 3, 1, 1);
        region.subtract(0, 4, 1, 1);

        /* Subtract Top-Right Corner */
        region.subtract(s.x - 5, 0, 5, 1);
        region.subtract(s.x - 3, 1, 3, 1);
        region.subtract(s.x - 2, 2, 2, 1);
        region.subtract(s.x - 1, 3, 1, 1);
        region.subtract(s.x - 1, 4, 1, 1);

        /* Dispose old first */
        if (shell.getRegion() != null) {
            shell.getRegion().dispose();
        }

        /* Apply Region */
        shell.setRegion(region);

        /* Remember to dispose later */
        lastUsedRegion = region;
    }

    private boolean isMouseOver(Shell shell) {
        if (display.isDisposed()) {
            return false;
        }
        return shell.getBounds().contains(display.getCursorLocation());
    }

    @Override
    public int open() {
        if (shell == null || shell.isDisposed()) {
            shell = null;
            create();
        }

        constrainShellSize();
        shell.setLocation(fixupDisplayBounds(shell.getSize(), shell.getLocation()));

        if (isFadingEnabled()) {
            supportsFading = SwtUtil.setAlpha(shell, 0);
        }
        else {
            supportsFading = false;
        }

        shell.layout();
        shell.setVisible(true);

        if (supportsFading) {
            fadeJob = SwtUtil.fadeIn(shell, new IFadeListener() {

                @Override
                public void faded(Shell shell, int alpha) {
                    if (shell.isDisposed()) {
                        return;
                    }

                    if (alpha == 255) {
                        scheduleAutoClose();
                    }
                }
            });
        }
        else {
            scheduleAutoClose();
        }

        return Window.OK;
    }

    protected void scheduleAutoClose() {
        if (delayClose > 0) {
            closeJob.schedule(delayClose);
        }
    }

    @Override
    protected Control createContents(Composite parent) {
        ((GridLayout) parent.getLayout()).marginWidth = 1;
        ((GridLayout) parent.getLayout()).marginHeight = 1;

        /* Outer Composite holding the controls */
        final Composite outerCircle = new Composite(parent, SWT.NO_FOCUS);
        outerCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;

        outerCircle.setLayout(layout);

        /* Title area containing label and close button */
        final Composite titleCircle = new Composite(outerCircle, SWT.NO_FOCUS);
        titleCircle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        titleCircle.setBackgroundMode(SWT.INHERIT_FORCE);

        layout = new GridLayout(4, false);
        layout.marginWidth = 3;
        layout.marginHeight = 0;
        layout.verticalSpacing = 5;
        layout.horizontalSpacing = 3;

        titleCircle.setLayout(layout);
        titleCircle.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                Rectangle clArea = titleCircle.getClientArea();
                lastUsedBgImage = new Image(titleCircle.getDisplay(), clArea.width, clArea.height);
                GC gc = new GC(lastUsedBgImage);

                /* Gradient */
                drawGradient(gc, clArea);

                /* Fix Region Shape */
                fixRegion(gc, clArea);

                gc.dispose();

                Image oldBGImage = titleCircle.getBackgroundImage();
                titleCircle.setBackgroundImage(lastUsedBgImage);

                if (oldBGImage != null) {
                    oldBGImage.dispose();
                }
            }

            private void drawGradient(GC gc, Rectangle clArea) {
                gc.setForeground(color.getGradientBegin());
                gc.setBackground(color.getGradientEnd());
                gc.fillGradientRectangle(clArea.x, clArea.y, clArea.width, clArea.height, true);
            }

            private void fixRegion(GC gc, Rectangle clArea) {
                gc.setForeground(color.getBorder());

                /* Fill Top Left */
                gc.drawPoint(2, 0);
                gc.drawPoint(3, 0);
                gc.drawPoint(1, 1);
                gc.drawPoint(0, 2);
                gc.drawPoint(0, 3);

                /* Fill Top Right */
                gc.drawPoint(clArea.width - 4, 0);
                gc.drawPoint(clArea.width - 3, 0);
                gc.drawPoint(clArea.width - 2, 1);
                gc.drawPoint(clArea.width - 1, 2);
                gc.drawPoint(clArea.width - 1, 3);
            }
        });

        /* Create Title Area */
        createTitleArea(titleCircle);

        /* Outer composite to hold content controlls */
        Composite outerContentCircle = new Composite(outerCircle, SWT.NONE);

        layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;

        outerContentCircle.setLayout(layout);
        outerContentCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        outerContentCircle.setBackground(outerCircle.getBackground());

        /* Middle composite to show a 1px black line around the content controls */
        Composite middleContentCircle = new Composite(outerContentCircle, SWT.NO_FOCUS);

        layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginTop = 1;

        middleContentCircle.setLayout(layout);
        middleContentCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        middleContentCircle.setBackground(color.getBorder());

        /* Inner composite containing the content controls */
        Composite innerContentCircle = new Composite(middleContentCircle, SWT.NO_FOCUS);
        innerContentCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 5;

        innerContentCircle.setLayout(layout);

        ((GridLayout) innerContentCircle.getLayout()).marginLeft = 5;
        ((GridLayout) innerContentCircle.getLayout()).marginRight = 2;
        innerContentCircle.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));

        /* Content Area */
        createContentArea(innerContentCircle);

        return outerCircle;
    }

    @Override
    protected void initializeBounds() {
        Rectangle clArea = getPrimaryClientArea();
        Point initialSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        int height = Math.max(initialSize.y, MIN_HEIGHT);
        int width = initialSize.x; // Math.min(initialSize.x, MAX_WIDTH);

        Point size = new Point(width, height);
        shell.setLocation(clArea.width + clArea.x - size.x - PADDING_EDGE, clArea.height + clArea.y - size.y - PADDING_EDGE);
        shell.setSize(size);
    }

    private Rectangle getPrimaryClientArea() {
        Monitor primaryMonitor = shell.getDisplay().getPrimaryMonitor();
        return primaryMonitor != null ? primaryMonitor.getClientArea() : shell.getDisplay().getClientArea();
    }

    public void closeFade() {
        if (fadeJob != null) {
            fadeJob.cancelAndWait(false);
        }
        if (supportsFading) {
            fadeJob = SwtUtil.fadeOut(getShell(), new IFadeListener() {

                @Override
                public void faded(Shell shell, int alpha) {
                    if (!shell.isDisposed()) {
                        if (alpha == 0) {
                            shell.close();
                        }
                        else if (isMouseOver(shell)) {
                            if (fadeJob != null) {
                                fadeJob.cancelAndWait(false);
                            }
                            fadeJob = SwtUtil.fastFadeIn(shell, new IFadeListener() {

                                @Override
                                public void faded(Shell shell, int alpha) {
                                    if (shell.isDisposed()) {
                                        return;
                                    }

                                    if (alpha == 255) {
                                        scheduleAutoClose();
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
        else {
            shell.close();
        }
    }

    @Override
    public boolean close() {
        resources.dispose();
        if (lastUsedRegion != null) {
            lastUsedRegion.dispose();
        }
        if (lastUsedBgImage != null && !lastUsedBgImage.isDisposed()) {
            lastUsedBgImage.dispose();
        }
        return super.close();
    }

    public long getDelayClose() {
        return delayClose;
    }

    public void setDelayClose(long delayClose) {
        this.delayClose = delayClose;
    }

    private Point fixupDisplayBounds(Point tipSize, Point location) {
        if (respectDisplayBounds || respectMonitorBounds) {
            Rectangle bounds;
            Point rightBounds = new Point(tipSize.x + location.x, tipSize.y + location.y);

            if (respectMonitorBounds) {
                bounds = shell.getDisplay().getPrimaryMonitor().getBounds();
            }
            else {
                bounds = getPrimaryClientArea();
            }

            if (!(bounds.contains(location) && bounds.contains(rightBounds))) {
                if (rightBounds.x > bounds.x + bounds.width) {
                    location.x -= rightBounds.x - (bounds.x + bounds.width);
                }

                if (rightBounds.y > bounds.y + bounds.height) {
                    location.y -= rightBounds.y - (bounds.y + bounds.height);
                }

                if (location.x < bounds.x) {
                    location.x = bounds.x;
                }

                if (location.y < bounds.y) {
                    location.y = bounds.y;
                }
            }
        }

        return location;
    }

    public void setContents(Collection<? extends INotification> notifications) {
        this.notifications = new ArrayList<INotification>(notifications);
    }

    public List<? extends INotification> getNotifications() {
        return new ArrayList<INotification>(notifications);
    }
}
