package org.eclipsetrader.ui.internal.views;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipsetrader.core.views.IColumn;

/**
 * Interface implemented by watch list viewer presentations.
 *
 * @since 1.0
 */
public interface IWatchListViewerPresentation {

	/**
	 * Gets the structured viewer used by the receiver.
	 *
	 * @return the structured viewer.
	 */
	public StructuredViewer getViewer();

	/**
	 * Updates the displayed columns.
	 *
	 * @param columns the new columns to display.
	 */
	public void updateColumns(IColumn[] columns);

	/**
	 * Disposes the receiver and the associated resources.
	 */
	public void dispose();
}
