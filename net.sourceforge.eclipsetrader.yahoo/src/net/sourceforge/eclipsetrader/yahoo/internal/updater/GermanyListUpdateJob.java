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
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;
import org.w3c.dom.Element;

/**
 * Implements the job that reads the securities list from the web pages.
 */
public class GermanyListUpdateJob extends AbstractListUpdateJob {

	public GermanyListUpdateJob() {
		super(Messages.GermanyListUpdateJob_Name);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.yahoo.internal.AbstractListUpdateJob#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus run(IProgressMonitor monitor) {
		try {
			initialize();

			int size = 1 + 10 + 26 * 6;
			monitor.beginTask(Messages.GermanyListUpdateJob_TaskName, size);
			monitor.setTaskName(Messages.GermanyListUpdateJob_TaskName);
			
			String url = "http://de.biz.yahoo.com/p/de/cpi/index.html"; //$NON-NLS-1$
			parseStocklist(monitor, url);
			monitor.worked(1);
			
			for (char l = '0'; l < '9'; l++) {
				url = "http://de.biz.yahoo.com/p/de/cpi/cpi" + l + "0.html"; //$NON-NLS-1$ //$NON-NLS-2$
				parseStocklist(monitor, url);
				monitor.worked(1);
			}
			
			for (char l = 'a'; l < 'z'; l++) {
				for (int p = 0; p < 6; p++) {
					url = "http://de.biz.yahoo.com/p/de/cpi/cpi" + l + p + ".html"; //$NON-NLS-1$ //$NON-NLS-2$
					parseStocklist(monitor, url);
					monitor.worked(1);
				}
			}
			
			if (!monitor.isCanceled())
				save("securities_de.xml"); //$NON-NLS-1$

		} catch (Exception e) {
        	Status status = new Status(Status.ERROR, YahooPlugin.PLUGIN_ID, -1, e.toString(), e);
            YahooPlugin.getDefault().getLog().log(status);
		} finally {
			monitor.done();
		}
		
		return Status.OK_STATUS;
	}

	private void parseStocklist(IProgressMonitor monitor, String url) throws Exception {
		monitor.subTask(url);

		Parser parser = new Parser(url);
		NodeList list = parser.parse(new HasAttributeFilter("class", "yfnc_tabledata1")); //$NON-NLS-1$ //$NON-NLS-2$
		for (SimpleNodeIterator iter = list.elements(); iter.hasMoreNodes();) {
			TagNode node = (TagNode) iter.nextNode();
			NodeList childs = node.getChildren();

			String name = childs.elementAt(1).getFirstChild().getText();
			String wkn = childs.elementAt(3).getFirstChild().getText();
			String link = ((LinkTag) childs.elementAt(5).getFirstChild()).getLink();
			String code = link.substring(link.indexOf("s=") + 2); //$NON-NLS-1$

			if (map.get(code) == null) {
				Element security = document.createElement("security"); //$NON-NLS-1$
				security.setAttribute("code", code); //$NON-NLS-1$
				security.setAttribute("wkn", wkn); //$NON-NLS-1$
				security.appendChild(document.createTextNode(name));
				map.put(code, security);
			}
		}
	}
}
