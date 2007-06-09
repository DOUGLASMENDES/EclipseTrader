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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sourceforge.eclipsetrader.yahoo.YahooPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class AbstractListUpdateJob extends Job {
	DocumentBuilder builder;

	Document document;

	Element root;

	Map<String,Element> map;

	public AbstractListUpdateJob(String name) {
		super(name);
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public abstract IStatus run(IProgressMonitor monitor);

	protected void initialize() throws Exception {
		builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		document = builder.getDOMImplementation().createDocument(null, null, null);
		root = document.createElement("data"); //$NON-NLS-1$
		document.appendChild(root);
		map = new HashMap<String,Element>();
	}
	
	protected void save(String fileName) throws Exception {
		List<Element> nodes = new ArrayList<Element>(map.values());
		Collections.sort(nodes, new Comparator<Element>() {
			public int compare(Element arg0, Element arg1) {
				return arg0.getFirstChild().getNodeValue().compareTo(arg1.getFirstChild().getNodeValue());
			}
		});
		for (Iterator iter = nodes.iterator(); iter.hasNext();)
			root.appendChild((Element) iter.next());

		TransformerFactory factory = TransformerFactory.newInstance();
		try {
			factory.setAttribute("indent-number", new Integer(4)); //$NON-NLS-1$
		} catch (Exception e) {
		}
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		transformer.setOutputProperty("{http\u003a//xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
		DOMSource source = new DOMSource(document);

    	File file = YahooPlugin.getDefault().getStateLocation().append(fileName).toFile(); //$NON-NLS-1$
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		StreamResult result = new StreamResult(out);
		transformer.transform(source, result);
		out.flush();
		out.close();
	}
}
