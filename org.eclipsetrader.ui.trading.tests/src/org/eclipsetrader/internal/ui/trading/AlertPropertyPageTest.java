/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.internal.ui.trading;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.trading.IAlert;
import org.eclipsetrader.core.trading.IAlertService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("unchecked")
public class AlertPropertyPageTest extends TestCase {
	Shell shell;
	ImageRegistry imageRegistry;

	BundleContext context;
	ServiceReference serviceReference;
	IAlertService alertService;

	Security security;
	IAdaptable element;
	PropertyPage[] propertyPages;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		shell = new Shell(Display.getDefault());
		imageRegistry = new ImageRegistry(Display.getDefault());

		security = new Security("Test", null);
		element = new IAdaptable() {
			public Object getAdapter(Class adapter) {
				if (adapter.isAssignableFrom(security.getClass()))
					return security;
				return null;
			}
		};

		context = EasyMock.createNiceMock(BundleContext.class);
		serviceReference = EasyMock.createNiceMock(ServiceReference.class);
		alertService = EasyMock.createNiceMock(IAlertService.class);
		EasyMock.expect(context.getServiceReference(IAlertService.class.getName())).andStubReturn(serviceReference);
		EasyMock.expect(context.getService(serviceReference)).andStubReturn(alertService);
		EasyMock.expect(alertService.getAlerts(security)).andStubReturn(new IAlert[0]);

		EasyMock.replay(context, serviceReference, alertService);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		imageRegistry.dispose();
		shell.dispose();
	}

	public void testCreateContents() throws Exception {
		AlertPropertyPage page = new MyAlertPropertyPage();
		page.createContents(shell);
	}

	public void testFillTabsFromSelection() throws Exception {
		AlertPropertyPage page = new MyAlertPropertyPage();
		page.createContents(shell);

		PropertyPage property = new PropertyPage() {
			@Override
			protected Control createContents(Composite parent) {
				return new Composite(parent, SWT.NONE);
			}
		};
		property.setTitle("Page1");

		page.propertyPages = new PropertyPage[] {
			property
		};

		page.createTabbedPages();

		assertEquals(1, page.folder.getItemCount());
		assertEquals("Page1", page.folder.getItem(0).getText());
	}

	class MyAlertPropertyPage extends AlertPropertyPage {

		MyAlertPropertyPage() {
			super(context);
			setElement(element);
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.internal.ui.trading.AlertPropertyPage#getImageRegistry()
		 */
		@Override
		ImageRegistry getImageRegistry() {
			return imageRegistry;
		}
	}
}
