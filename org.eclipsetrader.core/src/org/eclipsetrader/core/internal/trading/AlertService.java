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

package org.eclipsetrader.core.internal.trading;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.trading.AlertEvent;
import org.eclipsetrader.core.trading.IAlert;
import org.eclipsetrader.core.trading.IAlertListener;
import org.eclipsetrader.core.trading.IAlertService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class AlertService implements IAlertService {

    MarketPricingEnvironment pricingEnvironment;

    Map<ISecurity, List<IAlert>> map = new HashMap<ISecurity, List<IAlert>>();
    Map<ISecurity, List<IAlert>> triggeredMap = new HashMap<ISecurity, List<IAlert>>();

    ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    private IPricingListener pricingListener = new IPricingListener() {

        @Override
        public void pricingUpdate(PricingEvent event) {
            doPricingUpdate(event);
        }
    };

    public AlertService() {
    }

    public void startUp() throws Exception {
        BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IMarketService.class.getName());
        pricingEnvironment = new MarketPricingEnvironment((IMarketService) context.getService(serviceReference));
        context.ungetService(serviceReference);

        load(CoreActivator.getDefault().getStateLocation().append("alerts.xml").toFile());

        for (ISecurity instrument : map.keySet()) {
            pricingEnvironment.addSecurity(instrument);

            ITrade trade = pricingEnvironment.getTrade(instrument);
            IQuote quote = pricingEnvironment.getQuote(instrument);

            for (IAlert alert : map.get(instrument)) {
                alert.setInitialValues(trade, quote);
            }
        }

        pricingEnvironment.addPricingListener(pricingListener);
    }

    void load(File file) throws JAXBException {
        if (!file.exists()) {
            return;
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(InstrumentElement[].class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                Status status = new Status(IStatus.WARNING, CoreActivator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                CoreActivator.log(status);
                return true;
            }
        });
        JAXBElement<InstrumentElement[]> element = unmarshaller.unmarshal(new StreamSource(file), InstrumentElement[].class);
        if (element == null) {
            return;
        }

        for (InstrumentElement ie : element.getValue()) {
            ISecurity instrument = ie.getInstrument();

            List<IAlert> list = new ArrayList<IAlert>();

            AlertElement[] alerts = ie.getAlerts();
            for (int ii = 0; ii < alerts.length; ii++) {
                Map<String, Object> parameters = new HashMap<String, Object>();
                for (ParameterElement param : alerts[ii].getParameters()) {
                    parameters.put(param.getName(), ParameterElement.convert(param));
                }

                IAlert alert = alerts[ii].getAlert();
                if (alert != null) {
                    alert.setParameters(parameters);
                    list.add(alert);
                }
            }

            map.put(instrument, list);
        }
    }

    protected synchronized void doPricingUpdate(PricingEvent event) {
        List<IAlert> set = map.get(event.getSecurity());
        if (set == null) {
            return;
        }

        List<IAlert> list = new ArrayList<IAlert>();

        List<IAlert> triggeredList;
        synchronized (triggeredMap) {
            triggeredList = triggeredMap.get(event.getSecurity());
            if (triggeredList == null) {
                triggeredList = new ArrayList<IAlert>();
                triggeredMap.put(event.getSecurity(), triggeredList);
            }
        }

        for (IAlert alert : set) {
            if (triggeredList.contains(alert)) {
                continue;
            }
            for (PricingDelta delta : event.getDelta()) {
                if (delta.getNewValue() instanceof ITrade) {
                    alert.setTrade((ITrade) delta.getNewValue());
                    if (alert.isTriggered()) {
                        triggeredList.add(alert);
                        list.add(alert);
                        break;
                    }
                }
                if (delta.getNewValue() instanceof IQuote) {
                    alert.setQuote((IQuote) delta.getNewValue());
                    if (alert.isTriggered()) {
                        triggeredList.add(alert);
                        list.add(alert);
                        break;
                    }
                }
            }
        }

        if (list.size() != 0) {
            ITrade trade = pricingEnvironment.getTrade(event.getSecurity());
            IQuote quote = pricingEnvironment.getQuote(event.getSecurity());
            AlertEvent alertEvent = new AlertEvent(event.getSecurity(), trade, quote, list.toArray(new IAlert[list.size()]));
            fireAlertTriggeredEvent(alertEvent);
        }
    }

    protected void fireAlertTriggeredEvent(AlertEvent alertEvent) {
        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((IAlertListener) l[i]).alertTriggered(alertEvent);
            } catch (Throwable t) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error notifying listeners", t); //$NON-NLS-1$
                CoreActivator.log(status);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlertService#addAlertListener(org.eclipsetrader.core.trading.IAlertListener)
     */
    @Override
    public void addAlertListener(org.eclipsetrader.core.trading.IAlertListener l) {
        listeners.add(l);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlertService#removeAlertListener(org.eclipsetrader.core.trading.IAlertListener)
     */
    @Override
    public void removeAlertListener(org.eclipsetrader.core.trading.IAlertListener l) {
        listeners.remove(l);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlertService#resetTrigger(org.eclipsetrader.core.trading.IAlert)
     */
    @Override
    public void resetTrigger(IAlert alert) {
        for (List<IAlert> triggeredList : triggeredMap.values()) {
            triggeredList.remove(alert);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlertService#resetAllTriggers()
     */
    @Override
    public void resetAllTriggers() {
        triggeredMap.clear();
    }

    public void shutDown() throws IllegalStateException, JAXBException, IOException {
        pricingEnvironment.dispose();

        listeners.clear();

        List<InstrumentElement> list = new ArrayList<InstrumentElement>();
        for (ISecurity instrument : map.keySet()) {
            List<AlertElement> alertList = new ArrayList<AlertElement>();
            for (IAlert alert : map.get(instrument)) {
                alertList.add(new AlertElement(alert));
            }

            list.add(new InstrumentElement(instrument, alertList));
        }
        save(CoreActivator.getDefault().getStateLocation().append("alerts.xml").toFile(), list.toArray(new InstrumentElement[list.size()]));
    }

    void save(File file, InstrumentElement[] elements) throws JAXBException, IOException {
        if (file.exists()) {
            file.delete();
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(InstrumentElement[].class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                Status status = new Status(IStatus.WARNING, CoreActivator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                CoreActivator.log(status);
                return true;
            }
        });
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$

        JAXBElement<InstrumentElement[]> element = new JAXBElement<InstrumentElement[]>(new QName("list"), InstrumentElement[].class, elements);
        marshaller.marshal(element, new FileWriter(file));
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlertService#getAlerts(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public IAlert[] getAlerts(ISecurity instrument) {
        List<IAlert> list = map.get(instrument);
        if (list == null) {
            return new IAlert[0];
        }
        return list.toArray(new IAlert[list.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlertService#setAlerts(org.eclipsetrader.core.instruments.ISecurity, org.eclipsetrader.core.trading.IAlert[])
     */
    @Override
    public void setAlerts(ISecurity instrument, IAlert[] alerts) {
        if (!map.containsKey(instrument)) {
            pricingEnvironment.addSecurity(instrument);
        }

        List<IAlert> oldList = map.get(instrument);
        if (oldList != null) {
            ITrade trade = pricingEnvironment.getTrade(instrument);
            IQuote quote = pricingEnvironment.getQuote(instrument);

            for (int i = 0; i < alerts.length; i++) {
                if (!oldList.contains(alerts[i])) {
                    alerts[i].setInitialValues(trade, quote);
                }
            }
        }

        map.put(instrument, new ArrayList<IAlert>(Arrays.asList(alerts)));
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlertService#getTriggeredAlerts(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public IAlert[] getTriggeredAlerts(ISecurity instrument) {
        List<IAlert> list = triggeredMap.get(instrument);
        if (list == null) {
            return new IAlert[0];
        }
        return list.toArray(new IAlert[list.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlertService#hasTriggeredAlerts(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public boolean hasTriggeredAlerts(ISecurity instrument) {
        List<IAlert> list = triggeredMap.get(instrument);
        if (list == null) {
            return false;
        }
        return !list.isEmpty();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAlertService#resetTriggers(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public void resetTriggers(ISecurity instrument) {
        ITrade trade = pricingEnvironment.getTrade(instrument);
        IQuote quote = pricingEnvironment.getQuote(instrument);

        synchronized (triggeredMap) {
            for (IAlert alert : triggeredMap.get(instrument)) {
                alert.setInitialValues(trade, quote);
            }

            triggeredMap.remove(instrument);
        }

        fireAlertTriggeredEvent(new AlertEvent(instrument, trade, quote, new IAlert[0]));
    }
}
