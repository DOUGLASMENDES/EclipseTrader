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

package org.eclipsetrader.ui.internal.trading;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.trading.IAlertService;
import org.eclipsetrader.ui.internal.trading.WatchlistAlertDecorator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class WatchlistAlertDecoratorTest extends TestCase {

    BundleContext context;
    ServiceReference serviceReference;
    IAlertService alertService;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        context = EasyMock.createNiceMock(BundleContext.class);
        serviceReference = EasyMock.createNiceMock(ServiceReference.class);
        alertService = EasyMock.createNiceMock(IAlertService.class);
        EasyMock.expect(context.getServiceReference(IAlertService.class.getName())).andStubReturn(serviceReference);
        EasyMock.expect(context.getService(serviceReference)).andStubReturn(alertService);
    }

    public void testDecorateAlwaysReturnsReadyFlag() throws Exception {
        MyDecoration decoration = new MyDecoration();
        Security security = new Security("Test", null);

        EasyMock.expect(alertService.hasTriggeredAlerts(security)).andStubReturn(false);
        EasyMock.replay(context, serviceReference, alertService);

        WatchlistAlertDecorator decorator = new MyAlertDecorator();
        decorator.decorate(security, decoration);

        assertEquals("*", decoration.prefix);
    }

    public void testDecorateBackgroundColor() throws Exception {
        MyDecoration decoration = new MyDecoration();
        Security security = new Security("Test", null);

        EasyMock.expect(alertService.hasTriggeredAlerts(security)).andStubReturn(true);
        EasyMock.replay(context, serviceReference, alertService);

        WatchlistAlertDecorator decorator = new MyAlertDecorator();
        decorator.decorate(security, decoration);

        assertNotNull(decoration.background);
    }

    class MyAlertDecorator extends WatchlistAlertDecorator {

        MyAlertDecorator() {
            super(context);
        }
    }

    class MyDecoration implements IDecoration {

        String prefix;
        Color background;

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IDecoration#addOverlay(org.eclipse.jface.resource.ImageDescriptor, int)
         */
        @Override
        public void addOverlay(ImageDescriptor overlay, int quadrant) {
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IDecoration#addOverlay(org.eclipse.jface.resource.ImageDescriptor)
         */
        @Override
        public void addOverlay(ImageDescriptor overlay) {
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IDecoration#addPrefix(java.lang.String)
         */
        @Override
        public void addPrefix(String prefix) {
            this.prefix = prefix;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IDecoration#addSuffix(java.lang.String)
         */
        @Override
        public void addSuffix(String suffix) {
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IDecoration#getDecorationContext()
         */
        @Override
        public IDecorationContext getDecorationContext() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IDecoration#setBackgroundColor(org.eclipse.swt.graphics.Color)
         */
        @Override
        public void setBackgroundColor(Color color) {
            background = color;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IDecoration#setFont(org.eclipse.swt.graphics.Font)
         */
        @Override
        public void setFont(Font font) {
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IDecoration#setForegroundColor(org.eclipse.swt.graphics.Color)
         */
        @Override
        public void setForegroundColor(Color color) {
        }
    }
}
