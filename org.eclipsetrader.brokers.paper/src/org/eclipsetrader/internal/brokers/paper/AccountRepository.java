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

package org.eclipsetrader.internal.brokers.paper;

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
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.trading.IAccount;

public class AccountRepository {

    private static AccountRepository instance;
    private List<Account> accounts = new ArrayList<Account>();

    public AccountRepository() {
        instance = this;
    }

    public static AccountRepository getInstance() {
        return instance;
    }

    public void add(Account account) {
        accounts.add(account);
    }

    public void remove(Account account) {
        accounts.remove(account);
    }

    public int size() {
        return accounts.size();
    }

    public IAccount[] getAccounts() {
        return accounts.toArray(new Account[accounts.size()]);
    }

    void load(File file) throws JAXBException {
        if (!file.exists()) {
            return;
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(Account[].class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                Activator.log(status);
                return true;
            }
        });
        JAXBElement<Account[]> element = unmarshaller.unmarshal(new StreamSource(file), Account[].class);
        if (element != null) {
            accounts.addAll(Arrays.asList(element.getValue()));
        }
    }

    void save(File file) throws JAXBException, IOException {
        if (file.exists()) {
            file.delete();
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(Account[].class);
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

        Account[] elements = accounts.toArray(new Account[accounts.size()]);
        JAXBElement<Account[]> element = new JAXBElement<Account[]>(new QName("list"), Account[].class, elements);
        marshaller.marshal(element, new FileWriter(file));
    }
}
