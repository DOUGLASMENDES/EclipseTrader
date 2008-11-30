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

import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.news.internal.repository.HeadLine;

@SuppressWarnings("restriction")
public interface INewsHandler {

	public HeadLine[] parseNewsPages(URL[] url, IProgressMonitor monitor);
}
