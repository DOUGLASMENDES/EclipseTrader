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

import org.eclipsetrader.core.IScript;
import org.eclipsetrader.core.ats.IScriptStrategy;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.repository.local.TestRepositoryService;
import org.eclipsetrader.repository.local.TestScript;
import org.eclipsetrader.repository.local.TestSecurity;
import org.eclipsetrader.repository.local.internal.types.ScriptAdapter;
import org.eclipsetrader.repository.local.internal.types.SecurityAdapter;

public class StrategyScriptStoreTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    private String marshal(StrategyScriptStore object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private StrategyScriptStore unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(StrategyScriptStore.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (StrategyScriptStore) unmarshaller.unmarshal(new StringReader(string));
    }

    public void testMarshalEmpty() throws Exception {
        StrategyScriptStore object = new StrategyScriptStore();
        assertEquals(prefix + "<strategy><bars/><instruments/><includes/></strategy>", marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        StrategyScriptStore object = unmarshal(prefix + "<strategy><bars/><instruments/></strategy>");
        assertNotNull(object);
    }

    public void testMarshalInstruments() throws Exception {
        StoreProperties properties = new StoreProperties();
        properties.setProperty(IScriptStrategy.PROP_INSTRUMENTS, new ISecurity[] {
            new TestSecurity("Test", null, "local:securities#1"),
        });
        StrategyScriptStore object = new StrategyScriptStore();
        object.putProperties(properties, null);
        assertEquals(prefix + "<strategy><bars/><instruments><security>local:securities#1</security></instruments><includes/></strategy>", marshal(object));
    }

    public void testUnmarshalInstruments() throws Exception {
        TestRepositoryService repositoryService = new TestRepositoryService();
        repositoryService.saveAdaptable(new ISecurity[] {
            new TestSecurity("Test", null, "local:securities#1")
        });
        SecurityAdapter.setRepositoryService(repositoryService);
        StrategyScriptStore object = unmarshal(prefix + "<strategy><bars/><instruments><security>local:securities#1</security></instruments></strategy>");
        IStoreProperties properties = object.fetchProperties(null);
        ISecurity[] elements = (ISecurity[]) properties.getProperty(IScriptStrategy.PROP_INSTRUMENTS);
        assertEquals(1, elements.length);
        assertEquals("Test", elements[0].getName());
    }

    public void testMarshalBars() throws Exception {
        StoreProperties properties = new StoreProperties();
        properties.setProperty(IScriptStrategy.PROP_BARS_TIMESPAN, new TimeSpan[] {
            TimeSpan.minutes(5), TimeSpan.days(1),
        });
        StrategyScriptStore object = new StrategyScriptStore();
        object.putProperties(properties, null);
        assertEquals(prefix + "<strategy><bars><timeSpan>5min</timeSpan><timeSpan>1d</timeSpan></bars><instruments/><includes/></strategy>", marshal(object));
    }

    public void testUnmarshalBars() throws Exception {
        StrategyScriptStore object = unmarshal(prefix + "<strategy><bars><timeSpan>5min</timeSpan><timeSpan>1d</timeSpan></bars><instruments/></strategy>");
        IStoreProperties properties = object.fetchProperties(null);
        TimeSpan[] elements = (TimeSpan[]) properties.getProperty(IScriptStrategy.PROP_BARS_TIMESPAN);
        assertEquals(2, elements.length);
        assertEquals(TimeSpan.minutes(5), elements[0]);
        assertEquals(TimeSpan.days(1), elements[1]);
    }

    public void testMarshalIncludes() throws Exception {
        StoreProperties properties = new StoreProperties();
        properties.setProperty(IScriptStrategy.PROP_INCLUDES, new IScript[] {
            new TestScript("Common Functions", new URI("local://scripts#1"))
        });
        StrategyScriptStore object = new StrategyScriptStore();
        object.putProperties(properties, null);
        assertEquals(prefix + "<strategy><bars/><instruments/><includes><script>local://scripts#1</script></includes></strategy>", marshal(object));
    }

    public void testUnmarshalIncludes() throws Exception {
        TestRepositoryService repositoryService = new TestRepositoryService();
        repositoryService.saveAdaptable(new IScript[] {
            new TestScript("Common Functions", new URI("local://scripts#1"))
        });
        ScriptAdapter.setRepositoryService(repositoryService);
        StrategyScriptStore object = unmarshal(prefix + "<strategy><bars/><instruments/><includes><script>local://scripts#1</script></includes></strategy>");
        IStoreProperties properties = object.fetchProperties(null);
        IScript[] elements = (IScript[]) properties.getProperty(IScriptStrategy.PROP_INCLUDES);
        assertEquals(1, elements.length);
        assertEquals("Common Functions", elements[0].getName());
    }
}
