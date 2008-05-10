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

package org.eclipsetrader.news.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipsetrader.core.instruments.ISecurity;

public class NewsDecoratorAdapter implements IAdapterFactory {
	private ImageDescriptor unreadedDescriptor;
	private ImageDescriptor readedDescriptor;

	private class NewsDecorator extends LabelProvider implements ILabelDecorator {
		private Map<Image, Image[]> decoratedImages = new HashMap<Image, Image[]>();

		public NewsDecorator() {
        }

		/* (non-Javadoc)
         * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
         */
        @Override
        public void dispose() {
        	for (Image[] images : decoratedImages.values()) {
        		for (int i = 0; i < images.length; i++)
        			images[i].dispose();
        	}
	        super.dispose();
        }

		/* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
         */
        public Image decorateImage(Image image, Object element) {
        	if (element instanceof IAdaptable)
        		element = ((IAdaptable) element).getAdapter(ISecurity.class);
        	if (element instanceof ISecurity) {
        		Image[] images = decoratedImages.get(image);
        		if (images == null) {
        			images = new Image[] {
        				new DecorationOverlayIcon(image, unreadedDescriptor, IDecoration.BOTTOM_LEFT).createImage(),
        				new DecorationOverlayIcon(image, readedDescriptor, IDecoration.BOTTOM_LEFT).createImage(),
        			};
        			decoratedImages.put(image, images);
        		}
        		return images[0];
        	}
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
         */
        public String decorateText(String text, Object element) {
	        return null;
        }
	};

	private NewsDecorator labelDecorator = new NewsDecorator();

	public NewsDecoratorAdapter() {
		if (Activator.getDefault() != null) {
			unreadedDescriptor = Activator.getDefault().getImageRegistry().getDescriptor("unreaded_ovr");
			readedDescriptor = Activator.getDefault().getImageRegistry().getDescriptor("readed_ovr");
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
    @SuppressWarnings("unchecked")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
    	if (adaptableObject instanceof IAdaptable)
    		adaptableObject = ((IAdaptable) adaptableObject).getAdapter(ISecurity.class);
    	if (adaptableObject instanceof ISecurity) {
    		if (adapterType.isAssignableFrom(labelDecorator.getClass()))
    			return labelDecorator;
    	}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
    @SuppressWarnings("unchecked")
	public Class[] getAdapterList() {
	    return new Class[] {
	    		ILabelDecorator.class,
	    	};
	}
}
