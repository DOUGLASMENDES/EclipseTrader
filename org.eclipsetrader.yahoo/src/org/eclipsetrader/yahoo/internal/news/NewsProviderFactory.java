/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.yahoo.internal.news;

import javax.xml.bind.JAXBException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.yahoo.internal.YahooActivator;

public class NewsProviderFactory implements IExecutableExtensionFactory {
	private static NewsProvider provider;

	public NewsProviderFactory() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtensionFactory#create()
	 */
	public Object create() throws CoreException {
		if (provider == null) {
			provider = new NewsProvider();
			try {
	            provider.startUp();
            } catch (JAXBException e) {
				Status status = new Status(Status.WARNING, YahooActivator.PLUGIN_ID, 0, "Error initializing news provider", e);
	            throw new CoreException(status);
            }
		}
		return provider;
	}
}
