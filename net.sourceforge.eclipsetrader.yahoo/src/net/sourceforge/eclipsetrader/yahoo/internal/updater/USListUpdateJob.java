/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.yahoo.internal.updater;

import net.sourceforge.eclipsetrader.yahoo.YahooPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.htmlparser.Parser;
import org.htmlparser.filters.LinkRegexFilter;
import org.htmlparser.filters.LinkStringFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;
import org.w3c.dom.Element;

public class USListUpdateJob extends AbstractListUpdateJob {

	public USListUpdateJob() {
		super(Messages.USListUpdateJob_Name);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.yahoo.internal.AbstractListUpdateJob#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
    @Override
    public IStatus run(IProgressMonitor monitor) {
		try {
			initialize();

			Parser parser = new Parser("http://biz.yahoo.com/p/"); //$NON-NLS-1$
			NodeList list = parser.parse(new LinkRegexFilter("http://biz.yahoo.com/p/\\d")); //$NON-NLS-1$
			
			monitor.beginTask(Messages.USListUpdateJob_TaskName, list.size());
			
			for (SimpleNodeIterator iter = list.elements(); iter.hasMoreNodes() && !monitor.isCanceled();) {
				LinkTag node = (LinkTag) iter.nextNode();
				parseSubpage(monitor, node.getLink());
				monitor.worked(1);
			}

			if (!monitor.isCanceled())
				save("securities.xml"); //$NON-NLS-1$
			
		} catch (Exception e) {
        	Status status = new Status(Status.ERROR, YahooPlugin.PLUGIN_ID, -1, e.toString(), e);
            YahooPlugin.getDefault().getLog().log(status);
		} finally {
			monitor.done();
		}
		
		return Status.OK_STATUS;
	}

	private void parsePage(IProgressMonitor monitor, String url) throws Exception {
		Parser parser = new Parser(url);
		NodeList list = parser.parse(new LinkStringFilter("q?s=")); //$NON-NLS-1$
		for (SimpleNodeIterator iter = list.elements(); iter.hasMoreNodes() && !monitor.isCanceled();) {
			LinkTag node = (LinkTag) iter.nextNode();
			if (node.getLink().equals(url))
				continue;
			String code = node.getFirstChild().getText();
			String name = node.getPreviousSibling().getPreviousSibling().getFirstChild().getText();
			name = name.replaceAll("[\r\n]", " "); //$NON-NLS-1$ //$NON-NLS-2$
			name = name.replaceAll("[ ]{2,}", " "); //$NON-NLS-1$ //$NON-NLS-2$
			System.out.println(code + " - " + name); //$NON-NLS-1$
			if (map.get(code) == null) {
				Element security = document.createElement("security"); //$NON-NLS-1$
				security.setAttribute("code", code); //$NON-NLS-1$
				security.appendChild(document.createTextNode(name));
				map.put(code, security);
			}
		}
	}

	private void parseSubpage(IProgressMonitor monitor, String url) throws Exception {
		Parser parser = new Parser(url);
		NodeList list = parser.parse(new LinkRegexFilter("http://biz.yahoo.com/p/\\d\\d")); //$NON-NLS-1$
		for (SimpleNodeIterator iter = list.elements(); iter.hasMoreNodes() && !monitor.isCanceled();) {
			LinkTag node = (LinkTag) iter.nextNode();
			if (node.getLink().equals(url))
				continue;
			monitor.subTask(node.getLink());
			parsePage(monitor, node.getLink());
		}
	}
}
