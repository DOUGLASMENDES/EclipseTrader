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
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;

public class HighestPriceFactory extends AbstractProviderFactory {
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
	        return HighestPriceFactory.this;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#getValue(org.eclipse.core.runtime.IAdaptable)
         */
        public IAdaptable getValue(IAdaptable adaptable) {
        	ITodayOHL todayOHL = (ITodayOHL) adaptable.getAdapter(ITodayOHL.class);
        	if (todayOHL != null && todayOHL.getHigh() != null) {
        		final Double value = todayOHL.getHigh();
        		return new IAdaptable() {
                    @SuppressWarnings("unchecked")
                    public Object getAdapter(Class adapter) {
                    	if (adapter.isAssignableFrom(String.class))
                    		return formatter.format(value);
                    	if (adapter.isAssignableFrom(Double.class))
                    		return value;
	                    return null;
                    }

                    @Override
                    public boolean equals(Object obj) {
                    	if (!(obj instanceof IAdaptable))
                    		return false;
                    	Double s = (Double) ((IAdaptable) obj).getAdapter(Double.class);
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

	public HighestPriceFactory() {
		formatter.setGroupingUsed(true);
		formatter.setMinimumIntegerDigits(1);
		formatter.setMinimumFractionDigits(2);
		formatter.setMaximumFractionDigits(4);
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
	    		Double.class,
	    		String.class,
	    	};
    }
}
