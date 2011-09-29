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

package org.eclipsetrader.repository.local;

import junit.framework.TestCase;

import org.eclipsetrader.core.Script;
import org.eclipsetrader.core.ats.ScriptStrategy;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.views.IWatchListColumn;
import org.eclipsetrader.core.views.WatchList;
import org.eclipsetrader.repository.local.internal.ScriptsCollection;
import org.eclipsetrader.repository.local.internal.SecurityCollection;
import org.eclipsetrader.repository.local.internal.StrategiesCollection;
import org.eclipsetrader.repository.local.internal.WatchListCollection;

public class LocalRepositoryTest extends TestCase {

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        new SecurityCollection();
        new WatchListCollection();
    }

    public void testCreateSecurityObject() throws Exception {
        LocalRepository repository = new LocalRepository(null);

        Security security = new Security("Test", null);

        IStore store = repository.createObject();
        store.putProperties(security.getStoreProperties(), null);

        assertEquals(1, repository.fetchObjects(null).length);
        assertEquals(1, SecurityCollection.getInstance().getAll().length);
    }

    public void testCreateWatchListObject() throws Exception {
        LocalRepository repository = new LocalRepository(null);

        WatchList watchlist = new WatchList("Test", new IWatchListColumn[0]);

        IStore store = repository.createObject();
        store.putProperties(watchlist.getStoreProperties(), null);

        assertEquals(1, repository.fetchObjects(null).length);
        assertEquals(1, WatchListCollection.getInstance().getAll().length);
    }

    public void testGetObjectFromURI() throws Exception {
        LocalRepository repository = new LocalRepository(null);

        Security security = new Security("Test", null);

        IStore store = repository.createObject();
        store.putProperties(security.getStoreProperties(), null);

        assertNotNull(store.toURI());
        assertNotNull(repository.getObject(store.toURI()));
    }

    public void testCreateScriptObject() throws Exception {
        LocalRepository repository = new LocalRepository(null);

        Script script = new Script("Test");

        IStore store = repository.createObject();
        store.putProperties(script.getStoreProperties(), null);

        assertEquals(1, repository.fetchObjects(null).length);
        assertEquals(1, ScriptsCollection.getInstance().getAll().length);
    }

    public void testCreateScriptStrategyObject() throws Exception {
        LocalRepository repository = new LocalRepository(null);

        ScriptStrategy script = new ScriptStrategy();
        script.setName("Test");

        IStore store = repository.createObject();
        store.putProperties(script.getStoreProperties(), null);

        assertEquals(1, repository.fetchObjects(null).length);
        assertEquals(1, StrategiesCollection.getInstance().getAll().length);
    }
}
