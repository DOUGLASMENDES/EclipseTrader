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

package org.eclipsetrader.ui.internal.ats.monitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipsetrader.core.ats.ITradingSystem;
import org.eclipsetrader.core.ats.ITradingSystemService;
import org.eclipsetrader.ui.internal.ats.ViewColumn;
import org.eclipsetrader.ui.internal.ats.ViewItem;

public class TradingSystemsViewModel extends TreeStructureAdvisor implements IObservableFactory {

    private final List<TradingSystemItem> list = new ArrayList<TradingSystemItem>();
    private final WritableList root = new WritableList(list, TradingSystemItem.class);

    private final List<ViewColumn> dataProviders = new ArrayList<ViewColumn>();
    private final WritableList columns = new WritableList(dataProviders, ViewColumn.class);

    public TradingSystemsViewModel(ITradingSystemService tradingSystemService) {
        for (ITradingSystem tradingSystem : tradingSystemService.getTradeSystems()) {
            root.add(new TradingSystemItem(this, tradingSystem));
        }
    }

    public WritableList getObservableDataProviders() {
        return columns;
    }

    public List<ViewColumn> getDataProviders() {
        return dataProviders;
    }

    public void setDataProviders(List<ViewColumn> list) {
        for (int i = 0; i < list.size(); i++) {
            int index = columns.indexOf(list.get(i));
            if (index != i) {
                if (index != -1) {
                    columns.remove(index);
                }
                columns.add(i, list.get(i));
            }
            else {
                dataProviders.get(index).setName(list.get(index).getName());
            }
        }
        for (ViewColumn column : new ArrayList<ViewColumn>(dataProviders)) {
            if (!list.contains(column)) {
                columns.remove(column);
            }
        }
        updateAll();
    }

    public List<TradingSystemItem> getList() {
        return list;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.observable.masterdetail.IObservableFactory#createObservable(java.lang.Object)
     */
    @Override
    public IObservable createObservable(Object target) {
        if (target == this) {
            return Observables.unmodifiableObservableList(root);
        }
        if (target instanceof ViewItem) {
            ObservableList list = ((ViewItem) target).getItems();
            if (list != null) {
                return Observables.unmodifiableObservableList(list);
            }
        }
        return null;
    }

    public void updateAll() {
        for (TradingSystemItem tradingSystemItem : list) {
            for (TradingSystemInstrumentItem instrumentItem : tradingSystemItem.getList()) {
                updateValues(instrumentItem);
            }
        }
    }

    public void updateValues(TradingSystemInstrumentItem instrumentItem) {
        for (ViewColumn column : dataProviders) {
            IAdaptable adaptable = column.getDataProvider().getValue(instrumentItem);
            if (adaptable != null) {
                Object propertyValue = adaptable.getAdapter(String.class);
                if (propertyValue == null) {
                    propertyValue = adaptable.getAdapter(Number.class);
                }
                instrumentItem.putValue(column.getDataProviderFactory().getId(), propertyValue);
            }
        }
    }
}
