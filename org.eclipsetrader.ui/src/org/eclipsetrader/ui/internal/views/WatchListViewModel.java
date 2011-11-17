/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.feed.IPricingEnvironment;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.core.views.IWatchListColumn;
import org.eclipsetrader.core.views.IWatchListElement;
import org.eclipsetrader.core.views.WatchList;
import org.eclipsetrader.core.views.WatchListColumn;
import org.eclipsetrader.core.views.WatchListElement;
import org.eclipsetrader.ui.internal.providers.GainValue;

public class WatchListViewModel implements IAdaptable {

    public static final String PROP_NAME = "name";
    public static final String PROP_DIRTY = "dirty";

    private String name;
    private final WatchList watchList;
    private final IPricingEnvironment pricingEnvironment;

    private final List<WatchListViewColumn> columns = new ArrayList<WatchListViewColumn>();
    private final WritableList observableColumns = new WritableList(columns, WatchListViewColumn.class);

    private final List<WatchListViewItem> items = new ArrayList<WatchListViewItem>();
    private final WritableList observableItems = new WritableList(items, WatchListViewItem.class);

    private WatchListViewModelTotalsItem totalsItem;

    private NumberFormat formatter = NumberFormat.getInstance();
    private NumberFormat percentageFormatter = NumberFormat.getInstance();
    private Color positiveColor = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
    private Color negativeColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);

    private boolean dirty;

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private final IPricingListener pricingListener = new IPricingListener() {

        @Override
        public void pricingUpdate(PricingEvent event) {
            doPricingUpdate(event);
        }
    };

    private final PropertyChangeListener holdingChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            WatchListViewItem viewItem = (WatchListViewItem) evt.getSource();
            updateValues(viewItem);
            changeSupport.firePropertyChange(PROP_DIRTY, dirty, dirty = true);
        }
    };

    public WatchListViewModel(WatchList watchList, IPricingEnvironment pricingEnvironment) {
        this.name = watchList.getName();
        this.watchList = watchList;
        this.pricingEnvironment = pricingEnvironment;

        formatter.setGroupingUsed(true);
        formatter.setMinimumIntegerDigits(1);
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(2);

        percentageFormatter.setGroupingUsed(true);
        percentageFormatter.setMinimumIntegerDigits(1);
        percentageFormatter.setMinimumFractionDigits(2);
        percentageFormatter.setMaximumFractionDigits(2);

        IWatchListColumn[] columns = watchList.getColumns();
        for (int i = 0; i < columns.length; i++) {
            observableColumns.add(new WatchListViewColumn(columns[i]));
        }

        IWatchListElement[] elements = watchList.getItems();
        for (int i = 0; i < elements.length; i++) {
            WatchListViewItem viewItem = new WatchListViewItem(this, elements[i]);
            viewItem.setTrade(pricingEnvironment.getTrade(elements[i].getSecurity()));
            viewItem.setQuote(pricingEnvironment.getQuote(elements[i].getSecurity()));
            viewItem.setLastClose(pricingEnvironment.getLastClose(elements[i].getSecurity()));
            viewItem.setTodayOHL(pricingEnvironment.getTodayOHL(elements[i].getSecurity()));
            viewItem.setBook(pricingEnvironment.getBook(elements[i].getSecurity()));
            viewItem.addPropertyChangeListener(WatchListViewItem.PROP_QUANTITY, holdingChangeListener);
            viewItem.addPropertyChangeListener(WatchListViewItem.PROP_PRICE, holdingChangeListener);
            observableItems.add(viewItem);
        }

        totalsItem = new WatchListViewModelTotalsItem();

        update();

        observableItems.addListChangeListener(new IListChangeListener() {

            @Override
            public void handleListChange(ListChangeEvent event) {
                event.diff.accept(new ListDiffVisitor() {

                    @Override
                    public void handleAdd(int index, Object element) {
                        WatchListViewItem viewItem = (WatchListViewItem) element;
                        init(viewItem);
                        viewItem.addPropertyChangeListener(WatchListViewItem.PROP_QUANTITY, holdingChangeListener);
                        viewItem.addPropertyChangeListener(WatchListViewItem.PROP_PRICE, holdingChangeListener);
                        changeSupport.firePropertyChange(PROP_DIRTY, dirty, dirty = true);
                    }

                    @Override
                    public void handleRemove(int index, Object element) {
                        WatchListViewItem viewItem = (WatchListViewItem) element;
                        viewItem.removePropertyChangeListener(WatchListViewItem.PROP_QUANTITY, holdingChangeListener);
                        viewItem.removePropertyChangeListener(WatchListViewItem.PROP_PRICE, holdingChangeListener);
                        changeSupport.firePropertyChange(PROP_DIRTY, dirty, dirty = true);
                    }
                });
            }
        });

        observableColumns.addListChangeListener(new IListChangeListener() {

            @Override
            public void handleListChange(ListChangeEvent event) {
                event.diff.accept(new ListDiffVisitor() {

                    @Override
                    public void handleAdd(int index, Object element) {
                        WatchListViewColumn column = (WatchListViewColumn) element;
                        for (WatchListViewItem viewItem : items) {
                            column.getDataProvider().init(viewItem);
                        }
                        changeSupport.firePropertyChange(PROP_DIRTY, dirty, dirty = true);
                    }

                    @Override
                    public void handleRemove(int index, Object element) {
                        WatchListViewColumn column = (WatchListViewColumn) element;
                        column.getDataProvider().dispose();
                        changeSupport.firePropertyChange(PROP_DIRTY, dirty, dirty = true);
                    }
                });
            }
        });

        pricingEnvironment.addPricingListener(pricingListener);
    }

    public void init() {
        WatchListViewColumn[] column = columns.toArray(new WatchListViewColumn[columns.size()]);
        for (int i = 0; i < column.length; i++) {
            for (WatchListViewItem viewItem : items) {
                column[i].getDataProvider().init(viewItem);
            }
        }
    }

    private void init(WatchListViewItem viewItem) {
        WatchListViewColumn[] column = columns.toArray(new WatchListViewColumn[columns.size()]);
        for (int i = 0; i < column.length; i++) {
            column[i].getDataProvider().init(viewItem);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (this.name != null && name != null && this.name.equals(name)) {
            return;
        }
        changeSupport.firePropertyChange(PROP_NAME, name, this.name = name);
        changeSupport.firePropertyChange(PROP_DIRTY, this.dirty, this.dirty = true);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        changeSupport.firePropertyChange(PROP_DIRTY, this.dirty, this.dirty = dirty);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public void add(ISecurity security) {
        WatchListViewItem viewItem = new WatchListViewItem(this, new WatchListElement(security, null, null, null));

        viewItem.setLastClose(pricingEnvironment.getLastClose(security));
        viewItem.setQuote(pricingEnvironment.getQuote(security));
        viewItem.setTrade(pricingEnvironment.getTrade(security));
        viewItem.setTodayOHL(pricingEnvironment.getTodayOHL(security));
        updateValues(viewItem);

        observableItems.add(viewItem);
    }

    public void commit() {
        watchList.setName(name);

        IWatchListColumn[] c = new IWatchListColumn[columns.size()];
        for (int i = 0; i < c.length; i++) {
            WatchListViewColumn viewColumn = columns.get(i);
            c[i] = viewColumn.getColumn();
            if (c[i] != null) {
                c[i].setName(viewColumn.getName());
            }
            else {
                c[i] = new WatchListColumn(viewColumn.getName(), viewColumn.getDataProviderFactory());
            }
        }
        watchList.setColumns(c);

        List<IWatchListElement> e = new ArrayList<IWatchListElement>();
        for (WatchListViewItem viewItem : items) {
            IWatchListElement element = viewItem.getElement();
            element.setPosition(viewItem.getPosition());
            element.setPurchasePrice(viewItem.getPurchasePrice());
            e.add(element);
        }
        watchList.setItems(e.toArray(new IWatchListElement[e.size()]));
    }

    public void dispose() {
        pricingEnvironment.removePricingListener(pricingListener);
    }

    public IWatchList getWatchList() {
        return watchList;
    }

    public List<WatchListViewColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<WatchListViewColumn> list) {
        int i;

        for (i = observableColumns.size() - 1; i >= 0; i--) {
            if (!list.contains(observableColumns.get(i))) {
                observableColumns.remove(i);
            }
        }

        for (i = 0; i < list.size(); i++) {
            if (!observableColumns.contains(list.get(i))) {
                observableColumns.add(i, list.get(i));
            }
        }

        for (i = 0; i < list.size(); i++) {
            int index = observableColumns.indexOf(list.get(i));
            columns.get(index).setName(list.get(i).getName());
            if (index != i) {
                observableColumns.move(index, i);
                i = -1;
            }
        }

        update();
    }

    public WritableList getObservableColumns() {
        return observableColumns;
    }

    public WritableList getObservableItems() {
        return observableItems;
    }

    protected void doPricingUpdate(PricingEvent event) {
        PricingDelta[] delta = event.getDelta();

        double purchaseValue = 0.0;
        double marketValue = 0.0;

        for (WatchListViewItem viewItem : items) {
            if (viewItem.getSecurity() == event.getSecurity()) {
                for (int i = 0; i < delta.length; i++) {
                    viewItem.setPriceData(delta[i].getNewValue());
                }
                updateValues(viewItem);
            }
            GainValue gainValue = (GainValue) viewItem.getValue("org.eclipsetrader.ui.providers.gain");
            if (gainValue != null) {
                purchaseValue += gainValue.getPurchaseValue();
                marketValue += gainValue.getMarketValue();
            }
        }

        if (purchaseValue != 0.0) {
            Double value = marketValue - purchaseValue;
            Double percentage = value / purchaseValue * 100.0;
            String text = (value > 0 ? "+" : "") + formatter.format(value) + " (" + (value > 0 ? "+" : "") + percentageFormatter.format(percentage) + "%)";
            Color color = value != 0 ? value > 0 ? positiveColor : negativeColor : null;
            totalsItem.putValue("org.eclipsetrader.ui.providers.gain", new GainValue(value, purchaseValue, marketValue, text, color));
        }
    }

    public void update() {
        for (WatchListViewItem viewItem : items) {
            updateValues(viewItem);
        }
    }

    private void updateValues(WatchListViewItem item) {
        WatchListViewColumn[] column = columns.toArray(new WatchListViewColumn[columns.size()]);
        for (int i = 0; i < column.length; i++) {
            IAdaptable value = column[i].getDataProvider().getValue(item);
            item.putValue(column[i].getId(), value);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(pricingEnvironment.getClass())) {
            return pricingEnvironment;
        }
        return null;
    }
}
