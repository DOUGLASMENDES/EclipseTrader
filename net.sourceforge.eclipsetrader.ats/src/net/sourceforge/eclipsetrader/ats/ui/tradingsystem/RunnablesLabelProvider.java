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

package net.sourceforge.eclipsetrader.ats.ui.tradingsystem;

import net.sourceforge.eclipsetrader.ats.ATSPlugin;
import net.sourceforge.eclipsetrader.ats.core.runnables.ComponentRunnable;
import net.sourceforge.eclipsetrader.ats.core.runnables.StrategyRunnable;
import net.sourceforge.eclipsetrader.ats.core.runnables.TradingSystemRunnable;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class RunnablesLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider {
	Font boldFont;

	ILabelProvider[] columnLabelProviders = new ILabelProvider[0];

	ImageDescriptor decorator = ImageDescriptor.createFromURL(ATSPlugin.getDefault().getBundle().getEntry("icons/full/obj16/bullet_error.png"));

	public RunnablesLabelProvider() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		if (element instanceof TradingSystemRunnable)
			return ATSPlugin.getDefault().getImageRegistry().get(ATSPlugin.TRADING_SYSTEM_ICON);
		if (element instanceof StrategyRunnable)
			return ATSPlugin.getDefault().getImageRegistry().get(ATSPlugin.STRATEGY_ICON);
		if (element instanceof ComponentRunnable)
			return ATSPlugin.getDefault().getImageRegistry().get(ATSPlugin.SECURITY_ICON);

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof TradingSystemRunnable)
			return ((TradingSystemRunnable) element).getTradingSystem().getName();
		else if (element instanceof StrategyRunnable) {
			StrategyRunnable runnable = (StrategyRunnable) element;
			if (runnable.getStrategy().getName() == null)
				return ATSPlugin.getStrategyPluginName(runnable.getStrategy().getPluginId());
			return runnable.getStrategy().getName();
		} else if (element instanceof ComponentRunnable)
			return ((ComponentRunnable) element).getSecurity().toString();
		return element.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0)
			return getImage(element);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		if (columnIndex == 0)
			return getText(element);

		columnIndex--;
		if (columnIndex >= 0 && columnIndex < columnLabelProviders.length)
			return columnLabelProviders[columnIndex].getText(element);

		return "";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
	 */
	public Font getFont(Object element) {
		if (element instanceof TradingSystemRunnable)
			return boldFont;
		return null;
	}

	public ILabelProvider[] getColumnLabelProviders() {
		return columnLabelProviders;
	}

	public void setColumnLabelProviders(ILabelProvider[] columnLabelProviders) {
		this.columnLabelProviders = columnLabelProviders;
	}
}
