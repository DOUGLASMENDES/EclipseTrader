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

public class FrenchListUpdateJob extends AbstractListUpdateJob {

	public FrenchListUpdateJob() {
		super(Messages.FrenchListUpdateJob_Name);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.yahoo.internal.AbstractListUpdateJob#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus run(IProgressMonitor monitor) {
		try {
			initialize();
			
			String[] url = new String[] {
				"http://fr.finance.yahoo.com/q/cq?s=@SRD_AB.PA", //$NON-NLS-1$
				"http://fr.finance.yahoo.com/q/cq?s=@SRD_CC.PA", //$NON-NLS-1$
				"http://fr.finance.yahoo.com/q/cq?s=@SRD_DE.PA", //$NON-NLS-1$
				"http://fr.finance.yahoo.com/q/cq?s=@SRD_FI.PA", //$NON-NLS-1$
				"http://fr.finance.yahoo.com/q/cq?s=@SRD_JN.PA", //$NON-NLS-1$
				"http://fr.finance.yahoo.com/q/cq?s=@SRD_OR.PA", //$NON-NLS-1$
				"http://fr.finance.yahoo.com/q/cq?s=@SRD_SS.PA", //$NON-NLS-1$
				"http://fr.finance.yahoo.com/q/cq?s=@SRD_TZ.PA", //$NON-NLS-1$
			};

			monitor.beginTask(Messages.FrenchListUpdateJob_TaskName, url.length);
			monitor.setTaskName(Messages.FrenchListUpdateJob_TaskName);

			for (int i = 0; i < url.length && !monitor.isCanceled(); i++) {
				parseStocklist(monitor, url[i]);
				monitor.worked(1);
			}

			if (!monitor.isCanceled())
				save("securities_fr.xml"); //$NON-NLS-1$

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
			String wkn = ((LinkTag) childs.elementAt(1)).getLinkText();
			String link = ((LinkTag) childs.elementAt(1)).getLink();
			String code = link.substring(link.indexOf("s=") + 2); //$NON-NLS-1$

			node = (TagNode) iter.nextNode();
			childs = node.getChildren();
			String name = ""; //$NON-NLS-1$
			if (childs.elementAt(1) != null)
				name = childs.elementAt(1).getText();
			else
				name = childs.elementAt(0).getText();

			for (int i = 2; i < node.getParent().getChildren().size(); i++)
				node = (TagNode) iter.nextNode();

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
