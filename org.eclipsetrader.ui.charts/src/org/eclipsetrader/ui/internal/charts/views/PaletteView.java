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

package org.eclipsetrader.ui.internal.charts.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.nebula.widgets.pshelf.PShelf;
import org.eclipse.nebula.widgets.pshelf.PShelfItem;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.ui.charts.ChartObjectFactoryTransfer;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class PaletteView extends ViewPart {
	public static final String K_ID = "id"; //$NON-NLS-1$
	public static final String K_NAME = "name"; //$NON-NLS-1$
	public static final String K_DESCRIPTION = "description"; //$NON-NLS-1$
	public static final String K_ICON = "icon"; //$NON-NLS-1$
	public static final String K_CATEGORY = "category"; //$NON-NLS-1$

	private PShelf shelf;

	public PaletteView() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
	    super.init(site, memento);
	    site.setSelectionProvider(new SelectionProvider());
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		shelf = new PShelf(parent, SWT.NONE);

		createItems();

		shelf.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
	            TableViewer viewer = (TableViewer) e.item.getData();
	            if (viewer != null)
	            	updateSiteSelection((IStructuredSelection) viewer.getSelection());
	            else
	            	getViewSite().getSelectionProvider().setSelection(StructuredSelection.EMPTY);
            }
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		shelf.setFocus();
	}

	protected void createItems() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ChartsUIActivator.INDICATORS_EXTENSION_ID);
		IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();

		List<IConfigurationElement> contributionElements = new ArrayList<IConfigurationElement>();

		List<IConfigurationElement> categories = new ArrayList<IConfigurationElement>();
		for (int i = 0; i < configElements.length; i++) {
			if (configElements[i].getName().equals(K_CATEGORY))
				categories.add(configElements[i]);
			else
				contributionElements.add(configElements[i]);
		}

		for (IConfigurationElement categoryElement : categories) {
			PShelfItem shelfItem = new PShelfItem(shelf, SWT.NONE);
			shelfItem.setText(categoryElement.getAttribute(K_NAME));
			String icon = categoryElement.getAttribute(K_ICON);
			if (icon != null) {
				ImageDescriptor imageDescriptor = ChartsUIActivator.imageDescriptorFromPlugin(categoryElement.getContributor().getName(), icon);
				final Image image = imageDescriptor != null ? imageDescriptor.createImage() : null;
				if (image != null) {
					shelfItem.setImage(image);
					shelfItem.addDisposeListener(new DisposeListener() {
                        public void widgetDisposed(DisposeEvent e) {
                        	image.dispose();
                        }
					});
				}
			}
			List<IConfigurationElement> addedElements = createContents(shelfItem, contributionElements.toArray(new IConfigurationElement[contributionElements.size()]), categoryElement.getAttribute(K_ID));
			contributionElements.removeAll(addedElements);
		}

		PShelfItem shelfItem = new PShelfItem(shelf, SWT.NONE);
		shelfItem.setText(Messages.PaletteView_OtherTitle);
		ImageDescriptor imageDescriptor = ChartsUIActivator.imageDescriptorFromPlugin("icons/obj16/blank_obj.gif"); //$NON-NLS-1$
		final Image image = imageDescriptor != null ? imageDescriptor.createImage() : null;
		if (image != null) {
			shelfItem.setImage(image);
			shelfItem.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                	image.dispose();
                }
			});
		}
		createContents(shelfItem, contributionElements.toArray(new IConfigurationElement[contributionElements.size()]), null);
	}

	protected List<IConfigurationElement> createContents(PShelfItem shelfItem, IConfigurationElement[] configElements, String categoryId) {
		shelfItem.getBody().setLayout(new FillLayout());

		final TableViewer viewer = new TableViewer(shelfItem.getBody(), SWT.MULTI | SWT.FULL_SELECTION);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new LabelProvider() {
			private Map<Object, Image> imageMap = new HashMap<Object, Image>();

			@Override
            public Image getImage(Object element) {
				Image image = imageMap.get(element);
				if (image == null) {
	            	IConfigurationElement configurationElement = (IConfigurationElement) element;
					String icon = configurationElement.getAttribute(K_ICON);
					if (icon != null) {
						ImageDescriptor imageDescriptor = ChartsUIActivator.imageDescriptorFromPlugin(configurationElement.getContributor().getName(), icon);
						image = imageDescriptor != null ? imageDescriptor.createImage() : null;
						imageMap.put(element, image);
					}
				}
				return image;
            }

            @Override
            public void dispose() {
            	for (Image image : imageMap.values())
            		image.dispose();
	            super.dispose();
            }

			@Override
            public String getText(Object element) {
            	IConfigurationElement configurationElement = (IConfigurationElement) element;
            	String template = configurationElement.getAttribute(K_DESCRIPTION) != null ? "{0} - {1}" : "{0}"; //$NON-NLS-1$ //$NON-NLS-2$
	            return NLS.bind(template, new Object[] {
	            		configurationElement.getAttribute(K_NAME),
	            		configurationElement.getAttribute(K_DESCRIPTION)
	            	});
            }
		});
		viewer.setSorter(new ViewerSorter());

		shelfItem.setData(viewer);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
            	updateSiteSelection((IStructuredSelection) event.getSelection());
            }
		});

		Transfer[] transferTypes = new Transfer[] {
				ChartObjectFactoryTransfer.getInstance(),
		};
		viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, transferTypes, new DragSourceAdapter() {
            @Override
            public void dragStart(DragSourceEvent event) {
	            event.doit = !viewer.getSelection().isEmpty();
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
	            Object[] selection = ((IStructuredSelection) viewer.getSelection()).toArray();
	            String[] elements = new String[selection.length];
	            for (int i = 0; i < elements.length; i++)
	            	elements[i] = ((IConfigurationElement) selection[i]).getAttribute("id"); //$NON-NLS-1$
	            event.data = elements;
            }
		});

		List<IConfigurationElement> input = new ArrayList<IConfigurationElement>();
		for (int i = 0; i < configElements.length; i++) {
			if (categoryId == null || categoryId.equals(configElements[i].getAttribute(K_CATEGORY)))
				input.add(configElements[i]);
		}
		viewer.setInput(input.toArray());

		return input;
	}

	protected void updateSiteSelection(IStructuredSelection selection) {
    	if (!selection.isEmpty()) {
        	Object[] ar = selection.toArray();
        	Object[] o = new Object[ar.length];
        	for (int i = 0; i < o.length; i++) {
        		try {
        			o[i] = ((IConfigurationElement) ar[i]).createExecutableExtension("class"); //$NON-NLS-1$
        		} catch(Exception e) {
        			// Do nothing
        		}
        	}
        	getViewSite().getSelectionProvider().setSelection(new StructuredSelection(o));
    	}
    	else
        	getViewSite().getSelectionProvider().setSelection(StructuredSelection.EMPTY);
	}
}
