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

package org.eclipsetrader.directa.internal.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.eclipsetrader.core.Cash;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.trading.IPositionListener;
import org.eclipsetrader.core.trading.ITransaction;
import org.eclipsetrader.core.trading.PositionEvent;
import org.eclipsetrader.directa.internal.Activator;

public class Account implements IAccount {

    String id;
    String name;
    File file;

    List<Position> positions = new ArrayList<Position>();
    ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    public Account(String id, File file) {
        this.id = id;
        this.file = file;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getDescription()
     */
    @Override
    public String getDescription() {
        return name != null ? name : id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getBalance()
     */
    @Override
    public Cash getBalance() {
        // TODO
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#addPositionListener(org.eclipsetrader.core.trading.IPositionListener)
     */
    @Override
    public void addPositionListener(IPositionListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#removePositionListener(org.eclipsetrader.core.trading.IPositionListener)
     */
    @Override
    public void removePositionListener(IPositionListener listener) {
        listeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getPositions()
     */
    @Override
    public IPosition[] getPositions() {
        synchronized (positions) {
            return positions.toArray(new IPosition[positions.size()]);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getTransactions()
     */
    @Override
    public ITransaction[] getTransactions() {
        return new ITransaction[0];
    }

    public Position getPositionFor(ISecurity security) {
        synchronized (positions) {
            for (Position p : positions) {
                if (p.getSecurity().equals(security)) {
                    return p;
                }
            }
        }
        return null;
    }

    public void updatePosition(OrderMonitor monitor) {
        Position newPosition;
        if (monitor.getOrder().getSide() == IOrderSide.Sell || monitor.getOrder().getSide() == IOrderSide.SellShort) {
            newPosition = new Position(monitor.getOrder().getSecurity(), -monitor.getFilledQuantity(), monitor.getAveragePrice());
        }
        else {
            newPosition = new Position(monitor.getOrder().getSecurity(), monitor.getFilledQuantity(), monitor.getAveragePrice());
        }

        Position existingPosition = getPositionFor(monitor.getOrder().getSecurity());
        if (existingPosition == null) {
            synchronized (positions) {
                positions.add(newPosition);
            }
            firePositionOpenedEvent(newPosition);
        }
        else {
            existingPosition.setQuantity(existingPosition.getQuantity() + newPosition.getQuantity());
            if (existingPosition.getQuantity() == 0) {
                synchronized (positions) {
                    positions.remove(existingPosition);
                }
                firePositionClosedEvent(existingPosition);
            }
            else {
                firePositionUpdateEvent(existingPosition);
            }
        }
    }

    public void updatePosition(Position newPosition) {
        Position existingPosition = getPositionFor(newPosition.getSecurity());
        if (existingPosition == null) {
            synchronized (positions) {
                positions.add(newPosition);
            }
            firePositionOpenedEvent(newPosition);
        }
        else {
            synchronized (positions) {
                positions.remove(existingPosition);
                positions.add(newPosition);
            }
            firePositionUpdateEvent(newPosition);
        }
    }

    public void setPositions(Position[] newPositions) {
        for (int i = 0; i < newPositions.length; i++) {
            updatePosition(newPositions[i]);
        }

        Position[] currentPositions;
        synchronized (positions) {
            currentPositions = positions.toArray(new Position[positions.size()]);
        }
        for (int i = 0; i < currentPositions.length; i++) {
            boolean doRemove = true;
            for (int j = 0; j < newPositions.length; j++) {
                if (newPositions[j].getSecurity().equals(currentPositions[i].getSecurity())) {
                    doRemove = false;
                    break;
                }
            }
            if (doRemove) {
                synchronized (positions) {
                    positions.remove(currentPositions[i]);
                }
                firePositionClosedEvent(currentPositions[i]);
            }
        }
    }

    public void firePositionOpenedEvent(Position p) {
        PositionEvent event = new PositionEvent(this, p);

        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((IPositionListener) l[i]).positionOpened(event);
            } catch (Throwable t) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error running listener", t); //$NON-NLS-1$
                Activator.log(status);
            }
        }
    }

    public void firePositionUpdateEvent(Position p) {
        PositionEvent event = new PositionEvent(this, p);

        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((IPositionListener) l[i]).positionChanged(event);
            } catch (Throwable t) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error running listener", t); //$NON-NLS-1$
                Activator.log(status);
            }
        }
    }

    public void firePositionClosedEvent(Position p) {
        PositionEvent event = new PositionEvent(this, p);

        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((IPositionListener) l[i]).positionClosed(event);
            } catch (Throwable t) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error running listener", t); //$NON-NLS-1$
                Activator.log(status);
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 11 * id.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Account)) {
            return false;
        }
        return id.equals(((Account) obj).id);
    }

    public void load() {
        if (file == null || !file.exists()) {
            return;
        }

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Position[].class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(new ValidationEventHandler() {

                @Override
                public boolean handleEvent(ValidationEvent event) {
                    Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                    Activator.log(status);
                    return true;
                }
            });
            JAXBElement<Position[]> element = unmarshaller.unmarshal(new StreamSource(file), Position[].class);
            if (element != null) {
                positions.addAll(Arrays.asList(element.getValue()));
            }
        } catch (JAXBException e) {
            Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error loading positions", e); //$NON-NLS-1$
            Activator.log(status);
        }
    }

    public void save() throws JAXBException, IOException {
        if (file.exists()) {
            file.delete();
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(Position[].class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                Activator.log(status);
                return true;
            }
        });
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$

        Position[] elements = positions.toArray(new Position[positions.size()]);
        JAXBElement<Position[]> element = new JAXBElement<Position[]>(new QName("list"), Position[].class, elements); //$NON-NLS-1$
        marshaller.marshal(element, new FileWriter(file));
    }
}
