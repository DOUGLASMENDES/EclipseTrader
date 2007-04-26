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

import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.ats.core.runnables.ComponentRunnable;
import net.sourceforge.eclipsetrader.ats.core.runnables.StrategyRunnable;
import net.sourceforge.eclipsetrader.ats.core.runnables.TradingSystemRunnable;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class ContentProvider implements ITreeContentProvider {
	TreeViewer viewer;

	ICollectionObserver collectionObserver = new ICollectionObserver() {

		public void itemAdded(Object o) {
			Object parent = getParent(o);
			if (parent != null) {
				viewer.add(parent, o);
				if (o instanceof TradingSystemRunnable)
					addObservers((TradingSystemRunnable) o);
				if (o instanceof StrategyRunnable)
					addObservers((StrategyRunnable) o);
			}
		}

		public void itemRemoved(Object o) {
			viewer.remove(o);
		}
	};

	Observer observer = new Observer() {

		public void update(Observable o, Object arg) {
			viewer.update(o, null);
		}
	};

	public ContentProvider() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof TradingSystemRunnable)
			return ((TradingSystemRunnable) parentElement).getRunnables();
		else if (parentElement instanceof StrategyRunnable)
			return ((StrategyRunnable) parentElement).getRunnables();
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof StrategyRunnable)
			return ((StrategyRunnable) element).getParent();
		else if (element instanceof ComponentRunnable)
			return ((ComponentRunnable) element).getParent();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof TradingSystemRunnable)
			return ((TradingSystemRunnable) element).getRunnablesCount() != 0;
		else if (element instanceof StrategyRunnable)
			return ((StrategyRunnable) element).getRunnablesCount() != 0;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return (Object[]) inputElement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer) viewer;

		if (oldInput != null) {
			TradingSystemRunnable[] runnables = (TradingSystemRunnable[]) oldInput;
			for (int i = 0; i < runnables.length; i++)
				removeObservers(runnables[i]);
		}

		if (newInput != null) {
			TradingSystemRunnable[] runnables = (TradingSystemRunnable[]) newInput;
			for (int i = 0; i < runnables.length; i++)
				addObservers(runnables[i]);
		}
	}

	public void addObservers(TradingSystemRunnable runnable) {
		runnable.addObserver(observer);
		runnable.addRunnablesObserver(collectionObserver);

		StrategyRunnable[] runnables = runnable.getRunnables();
		for (int i = 0; i < runnables.length; i++)
			addObservers(runnables[i]);
	}

	public void removeObservers(TradingSystemRunnable runnable) {
		runnable.deleteObserver(observer);
		runnable.removeRunnablesObserver(collectionObserver);

		StrategyRunnable[] runnables = runnable.getRunnables();
		for (int i = 0; i < runnables.length; i++)
			removeObservers(runnables[i]);
	}

	protected void addObservers(StrategyRunnable runnable) {
		runnable.getStrategy().addObserver(observer);
		runnable.addRunnablesObserver(collectionObserver);

		ComponentRunnable[] runnables = runnable.getRunnables();
		for (int i = 0; i < runnables.length; i++)
			addObservers(runnables[i]);
	}

	protected void removeObservers(StrategyRunnable runnable) {
		runnable.getStrategy().deleteObserver(observer);
		runnable.removeRunnablesObserver(collectionObserver);

		ComponentRunnable[] runnables = runnable.getRunnables();
		for (int i = 0; i < runnables.length; i++)
			removeObservers(runnables[i]);
	}

	protected void addObservers(ComponentRunnable runnable) {
	}

	protected void removeObservers(ComponentRunnable runnable) {
	}
}
