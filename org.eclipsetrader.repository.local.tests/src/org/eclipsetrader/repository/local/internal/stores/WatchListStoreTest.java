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

package org.eclipsetrader.repository.local.internal.stores;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.core.views.Column;
import org.eclipsetrader.core.views.Holding;
import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IHolding;
import org.eclipsetrader.repository.local.TestRepositoryService;
import org.eclipsetrader.repository.local.TestSecurity;
import org.eclipsetrader.repository.local.TestStore;
import org.eclipsetrader.repository.local.internal.types.SecurityAdapter;

public class WatchListStoreTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalEmpty() throws Exception {
        WatchListStore object = new WatchListStore();
        assertEquals(prefix + "<watchlist><columns/><elements/></watchlist>", marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        WatchListStore object = unmarshal(prefix + "<watchlist><elements/></watchlist>");
        assertNotNull(object);
    }

    public void testMarshalName() throws Exception {
        StoreProperties properties = new StoreProperties();
        properties.setProperty(IPropertyConstants.NAME, "Name");
        WatchListStore object = new WatchListStore();
        object.putProperties(properties, null);
        assertEquals(prefix + "<watchlist><name>Name</name><columns/><elements/><properties/></watchlist>", marshal(object));
    }

    public void testUnmarshalName() throws Exception {
        WatchListStore object = unmarshal(prefix + "<watchlist><name>Name</name><elements/></watchlist>");
        IStoreProperties properties = object.fetchProperties(null);
        assertEquals("Name", properties.getProperty(IPropertyConstants.NAME));
    }

    public void testMarshalElements() throws Exception {
        StoreProperties properties = new StoreProperties();
        properties.setProperty(IPropertyConstants.HOLDINGS, new IHolding[] {
            new Holding(new TestSecurity("Test", null, "local:securities#1"), null, null, null),
        });
        WatchListStore object = new WatchListStore();
        object.putProperties(properties, null);
        assertEquals(prefix + "<watchlist><columns/><elements><holding security=\"local:securities#1\"/></elements><properties/></watchlist>", marshal(object));
    }

    public void testUnmarshalElements() throws Exception {
        TestRepositoryService repositoryService = new TestRepositoryService();
        repositoryService.saveAdaptable(new ISecurity[] {
            new TestSecurity("Test", null, "local:securities#1")
        });
        SecurityAdapter.setRepositoryService(repositoryService);
        WatchListStore object = unmarshal(prefix + "<watchlist><elements><holding security=\"local:securities#1\"/></elements></watchlist>");
        IStoreProperties properties = object.fetchProperties(null);
        IHolding[] elements = (IHolding[]) properties.getProperty(IPropertyConstants.HOLDINGS);
        assertEquals(1, elements.length);
        assertEquals("Test", elements[0].getSecurity().getName());
    }

    public void testMarshalColumns() throws Exception {
        StoreProperties properties = new StoreProperties();
        properties.setProperty(IPropertyConstants.COLUMNS, new IColumn[] {
            new Column("Test", null),
        });
        WatchListStore object = new WatchListStore();
        object.putProperties(properties, null);
        assertEquals(prefix + "<watchlist><columns><column>Test</column></columns><elements/><properties/></watchlist>", marshal(object));
    }

    public void testUnmarshalColumns() throws Exception {
        SecurityAdapter.setRepositoryService(new TestRepositoryService());
        WatchListStore object = unmarshal(prefix + "<watchlist><columns><column>Test</column></columns><elements/></watchlist>");
        IStoreProperties properties = object.fetchProperties(null);
        IColumn[] columns = (IColumn[]) properties.getProperty(IPropertyConstants.COLUMNS);
        assertEquals(1, columns.length);
        assertEquals("Test", columns[0].getName());
    }

    public void testMarshalElementsAfterStoreChange() throws Exception {
        TestSecurity security = new TestSecurity("Test", null, "local:securities#1");
        TestRepositoryService repositoryService = new TestRepositoryService();
        repositoryService.saveAdaptable(new ISecurity[] {
            security
        });
        SecurityAdapter.setRepositoryService(repositoryService);
        WatchListStore object = unmarshal(prefix + "<watchlist><elements><holding security=\"local:securities#1\"/></elements></watchlist>");
        security.setStore(new TestStore(null, null, new URI("new", "security", "2")));
        assertEquals(prefix + "<watchlist><columns/><elements><holding security=\"new:security#2\"/></elements></watchlist>", marshal(object));
    }

    public void testMarshalUnknownProperties() throws Exception {
        WatchListStore object = new WatchListStore();
        StoreProperties properties = new StoreProperties();
        properties.setProperty("option-expiration", "2008/08/30");
        object.putProperties(properties, null);
        assertEquals(prefix + "<watchlist><columns/><elements/><properties><property name=\"option-expiration\">2008/08/30</property></properties></watchlist>", marshal(object));
    }

    public void testUnmarshalUnknownProperties() throws Exception {
        WatchListStore object = unmarshal(prefix + "<watchlist><properties><property name=\"option-expiration\">2008/08/30</property></properties></watchlist>");
        IStoreProperties properties = object.fetchProperties(null);
        assertEquals("2008/08/30", properties.getProperty("option-expiration"));
    }

    private String marshal(WatchListStore object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private WatchListStore unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(WatchListStore.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (WatchListStore) unmarshaller.unmarshal(new StringReader(string));
    }
}
