/*
 * Copyright (c) 2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.ats.ui.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.eclipsetrader.ats.ATSPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Bundle;

public class SystemSelectionPage extends WizardSelectionPage {
	protected TreeViewer viewer;

	protected Image folder;

	protected Map<String, Image> imageCache = new HashMap<String, Image>();

	protected LabelProvider labelProvider = new LabelProvider() {

		@Override
		public Image getImage(Object element) {
			Image image = null;
			TreeNode node = (TreeNode) element;

			if (node.getValue() instanceof IConfigurationElement) {
				IConfigurationElement c = (IConfigurationElement) node.getValue();

				String iconName = c.getAttribute("icon");
				if (iconName != null) {
					image = imageCache.get(c.getContributor().getName() + "/" + iconName);
					if (image == null) {
						Bundle bundle = Platform.getBundle(c.getContributor().getName());
						ImageDescriptor descriptor = ImageDescriptor.createFromURL(bundle.getResource(iconName));
						if (descriptor != null) {
							image = descriptor.createImage();
							imageCache.put(c.getContributor().getName() + "/" + iconName, image);
						}
					}

				}
			} else
				image = folder;

			return image;
		}

		@Override
		public String getText(Object element) {
			TreeNode node = (TreeNode) element;

			if (node.getValue() instanceof IConfigurationElement) {
				IConfigurationElement c = (IConfigurationElement) node.getValue();
				String text = c.getAttribute("name");
				return text != null ? text : "";
			}

			return node.getValue().toString();
		}
	};

	protected ViewerComparator comparator = new ViewerComparator() {

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			String s1 = labelProvider.getText(e1);
			String s2 = labelProvider.getText(e2);

			if (!(((TreeNode) e1).getValue() instanceof IConfigurationElement))
				s1 = "0" + s1;
			else
				s1 = "1" + s1;
			if (!(((TreeNode) e2).getValue() instanceof IConfigurationElement))
				s2 = "0" + s2;
			else
				s2 = "1" + s2;

			return s1.compareTo(s2);
		}
	};

	public SystemSelectionPage() {
		super("systemSelection");
		setTitle("Select a Wizard");
		setPageComplete(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		folder = ATSPlugin.getImageDescriptor("icons/full/obj16/folder.png").createImage();

		viewer = new TreeViewer(parent);
		viewer.setLabelProvider(labelProvider);
		viewer.setContentProvider(new TreeNodeContentProvider());
		viewer.setComparator(comparator);
		viewer.setInput(buildInput());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				setSelectedNode(null);
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (!selection.isEmpty()) {
					TreeNode node = (TreeNode) selection.getFirstElement();
					if (node.getValue() instanceof IConfigurationElement) {
						setSelectedNode(new WizardNode((IConfigurationElement) node.getValue()));
					}
				}
				setDescription(getSelectedNode() != null ? getSelectedNode().getWizard().getWindowTitle() : "");
				setPageComplete(getSelectedNode() != null);
			}
		});
		setControl(viewer.getControl());
	}

	@Override
	public void dispose() {
		if (folder != null)
			folder.dispose();
		for (Image image : imageCache.values())
			image.dispose();
		super.dispose();
	}

	protected TreeNode[] buildInput() {
		List<TreeNode> rootNodes = new ArrayList<TreeNode>();

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(ATSPlugin.SYSTEMS_EXTENSION_ID);
		if (extensionPoint != null) {
			IConfigurationElement[] members = extensionPoint.getConfigurationElements();
			for (int c = 0; c < members.length; c++) {
				IConfigurationElement item = members[c];
				if (item.getName().equals("category")) {
					String id = item.getAttribute("id");
					TreeNode parentNode = new TreeNode(item.getAttribute("name"));

					List<TreeNode> childNodes = new ArrayList<TreeNode>();
					for (int i = 0; i < members.length; i++) {
						IConfigurationElement element = members[i];
						if (element.getName().equals("system") && id.equals(element.getAttribute("category"))) {
							TreeNode node = new TreeNode(element);
							node.setParent(parentNode);
							childNodes.add(node);
						}
					}
					if (childNodes.size() != 0)
						parentNode.setChildren(childNodes.toArray(new TreeNode[childNodes.size()]));

					rootNodes.add(parentNode);
				}
			}

			for (int i = 0; i < members.length; i++) {
				IConfigurationElement element = members[i];
				if (element.getName().equals("system") && (element.getAttribute("category") == null || "".equals(element.getAttribute("category"))))
					rootNodes.add(new TreeNode(element));
			}
		}

		return rootNodes.toArray(new TreeNode[rootNodes.size()]);
	}
}
