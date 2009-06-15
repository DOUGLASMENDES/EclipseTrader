/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.internal.ui.trading;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.ITradingService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class TradeHandler extends AbstractHandler {
	public static final String PARAM_BROKER = "broker"; //$NON-NLS-1$
	public static final String PARAM_LIMIT_PRICE = "limitPrice"; //$NON-NLS-1$
	public static final String PARAM_SIDE = "side"; //$NON-NLS-1$

	public TradeHandler() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		IWorkbenchSite site = HandlerUtil.getActiveSite(event);

		ITradingService tradingService = null;
		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(ITradingService.class.getName());
		if (serviceReference != null) {
			tradingService = (ITradingService) context.getService(serviceReference);
			context.ungetService(serviceReference);
		}
		if (tradingService == null)
			return null;

		if (selection != null && !selection.isEmpty()) {
			for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
				Object target = iter.next();
				if (target instanceof IAdaptable) {
					OrderDialog dlg = new OrderDialog(site.getShell(), tradingService);
					dlg.setTarget((IAdaptable) target);

					ISecurity security = (ISecurity) ((IAdaptable) target).getAdapter(ISecurity.class);
					dlg.setBroker(tradingService.getBrokerForSecurity(security));

					String brokerId = event.getParameter(PARAM_BROKER);
					if (brokerId != null)
						dlg.setBroker(tradingService.getBroker(brokerId));

					String limitPrice = event.getParameter(PARAM_LIMIT_PRICE);
					if (limitPrice != null)
						dlg.setLimitPrice(Double.parseDouble(limitPrice));

					String side = event.getParameter(PARAM_SIDE);
					if (side != null) {
						if (IOrderSide.Buy.getId().equals(side))
							dlg.setOrderSide(IOrderSide.Buy);
						else if (IOrderSide.Sell.getId().equals(side))
							dlg.setOrderSide(IOrderSide.Sell);
						else if (IOrderSide.BuyCover.getId().equals(side))
							dlg.setOrderSide(IOrderSide.BuyCover);
						else if (IOrderSide.SellShort.getId().equals(side))
							dlg.setOrderSide(IOrderSide.SellShort);
					}

					dlg.open();
				}
			}
		}

		return null;
	}
}
