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

package net.sourceforge.eclipsetrader.news.views;

import java.text.SimpleDateFormat;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.NewsItem;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class NewsLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider, IFontProvider {
    SimpleDateFormat dateTimeFormat = CorePlugin.getDateTimeFormat();
    Color addedBackground;
    Color addedForeground;
    Font addedFont;
    Color readedBackground;
    Color readedForeground;
    Font readedFont;

	public NewsLabelProvider() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		NewsItem newsItem = (NewsItem)element;
		
		switch(columnIndex) {
			case 0:
				return dateTimeFormat.format(newsItem.getDate());
			case 1: {
		        String title = newsItem.getTitle();
		        title = title.replaceAll("&agrave;", "à"); //$NON-NLS-1$ //$NON-NLS-2$
		        title = title.replaceAll("&egrave;", "è"); //$NON-NLS-1$ //$NON-NLS-2$
		        title = title.replaceAll("&eacute;", "é"); //$NON-NLS-1$ //$NON-NLS-2$
		        title = title.replaceAll("&igrave;", "ì"); //$NON-NLS-1$ //$NON-NLS-2$
		        title = title.replaceAll("&ograve;", "ò"); //$NON-NLS-1$ //$NON-NLS-2$
		        title = title.replaceAll("&pgrave;", "ù"); //$NON-NLS-1$ //$NON-NLS-2$
		        return title;
			}
			case 2: {
		        String text = ""; //$NON-NLS-1$
		        Object[] o = newsItem.getSecurities().toArray();
		        for (int i = 0; i < o.length; i++) {
		            if (!text.equals("")) //$NON-NLS-1$
		                text += ", "; //$NON-NLS-1$
		            text += ((Security)o[i]).getDescription();
		        }
		        return text;
			}
			case 3:
				return newsItem.getSource();
		}
		
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
     */
    public Color getBackground(Object element) {
		NewsItem newsItem = (NewsItem)element;

		if (newsItem.isReaded())
        	return readedBackground;
        else if (newsItem.isRecent())
        	return addedBackground;
	    
		return null;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
     */
    public Color getForeground(Object element) {
		NewsItem newsItem = (NewsItem)element;

		if (newsItem.isReaded())
        	return readedForeground;
        else if (newsItem.isRecent())
        	return addedForeground;
	    
		return null;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
     */
    public Font getFont(Object element) {
		NewsItem newsItem = (NewsItem)element;

		if (newsItem.isReaded())
        	return readedFont;
        else if (newsItem.isRecent())
        	return addedFont;
	    
		return null;
    }

	public void setAddedBackground(Color addedBackground) {
    	this.addedBackground = addedBackground;
    }

	public void setAddedFont(Font addedFont) {
    	this.addedFont = addedFont;
    }

	public void setAddedForeground(Color addedForeground) {
    	this.addedForeground = addedForeground;
    }

	public void setReadedBackground(Color readedBackground) {
    	this.readedBackground = readedBackground;
    }

	public void setReadedFont(Font readedFont) {
    	this.readedFont = readedFont;
    }

	public void setReadedForeground(Color readedForeground) {
    	this.readedForeground = readedForeground;
    }
}
