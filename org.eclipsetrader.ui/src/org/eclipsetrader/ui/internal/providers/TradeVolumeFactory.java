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

package org.eclipsetrader.ui.internal.providers;

import java.text.NumberFormat;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;

public class TradeVolumeFactory extends AbstractProviderFactory {
	private NumberFormat formatter = NumberFormat.getInstance();

	public class DataProvider implements IDataProvider {

		public DataProvider() {
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#init(org.eclipse.core.runtime.IAdaptable)
         */
        public void init(IAdaptable adaptable) {
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#getFactory()
         */
        public IDataProviderFactory getFactory() {
	        return TradeVolumeFactory.this;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#getValue(org.eclipse.core.runtime.IAdaptable)
         */
        public IAdaptable getValue(IAdaptable adaptable) {
        	ITrade trade = (ITrade) adaptable.getAdapter(ITrade.class);
        	if (trade != null && trade.getVolume() != null) {
        		final Long value = trade.getVolume();
        		return new IAdaptable() {
                    @SuppressWarnings("unchecked")
                    public Object getAdapter(Class adapter) {
                    	if (adapter.isAssignableFrom(String.class))
                    		return formatter.format(value);
                    	if (adapter.isAssignableFrom(Long.class))
                    		return value;
	                    return null;
                    }

                    @Override
                    public boolean equals(Object obj) {
                    	if (!(obj instanceof IAdaptable))
                    		return false;
                    	Long s = (Long) ((IAdaptable) obj).getAdapter(Long.class);
                    	return s == value || (value != null && value.equals(s));
                    }
        		};
        	}
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#dispose()
         */
        public void dispose() {
        }
	}

	public TradeVolumeFactory() {
		formatter.setGroupingUsed(true);
		formatter.setMinimumIntegerDigits(1);
		formatter.setMinimumFractionDigits(0);
		formatter.setMaximumFractionDigits(0);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IDataProviderFactory#createProvider()
	 */
	public IDataProvider createProvider() {
		return new DataProvider();
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IDataProviderFactory#getType()
     */
    @SuppressWarnings("unchecked")
    public Class[] getType() {
	    return new Class[] {
	    		Long.class,
	    		String.class,
	    	};
    }
}
