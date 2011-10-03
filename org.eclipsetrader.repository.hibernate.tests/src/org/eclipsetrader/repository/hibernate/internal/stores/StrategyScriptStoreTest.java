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

package org.eclipsetrader.repository.hibernate.internal.stores;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipsetrader.core.IScript;
import org.eclipsetrader.core.Script;
import org.eclipsetrader.core.ats.IScriptStrategy;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.repository.hibernate.HibernateRepository;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

public class StrategyScriptStoreTest extends TestCase {

    private HibernateRepository repository;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        buildRepositoryInstance();
    }

    private void buildRepositoryInstance() {
        Properties properties = new Properties();
        properties.put("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
        properties.put("hibernate.connection.url", "jdbc:hsqldb:mem:testdb");
        properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.cache.provider_class", "org.hibernate.cache.HashtableCacheProvider");
        repository = new HibernateRepository("tests", "Tests Repository", properties) {

            @Override
            protected AnnotationConfiguration buildConfiguration() {
                AnnotationConfiguration cfg = super.buildConfiguration();
                new SchemaExport(cfg).create(true, true);
                return cfg;
            }

            /* (non-Javadoc)
             * @see org.eclipsetrader.repository.hibernate.HibernateRepository#startUp(org.eclipse.core.runtime.IProgressMonitor)
             */
            @Override
            public void startUp(IProgressMonitor monitor) {
                super.startUp(monitor);
                super.initializeWatchListsCollections();
            }

            @Override
            public IStatus runInRepository(IRepositoryRunnable runnable, ISchedulingRule rule, IProgressMonitor monitor) {
                Transaction transaction = getSession().beginTransaction();
                try {
                    runnable.run(monitor);
                    transaction.commit();
                } catch (Exception e) {
                    try {
                        transaction.rollback();
                    } catch (Exception e1) {
                    }
                    throw new RuntimeException(e);
                }
                return Status.OK_STATUS;
            }
        };
        repository.startUp(null);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        repository.shutDown(null);
    }

    public void testFetchInstrumentsProperty() throws Exception {
        final Security security1 = new Security("Microsoft", null);
        final Security security2 = new Security("Apple", null);

        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                IStore store = repository.createObject();
                store.putProperties(security1.getStoreProperties(), monitor);
                security1.setStore(store);

                store = repository.createObject();
                store.putProperties(security2.getStoreProperties(), monitor);
                security2.setStore(store);

                return Status.OK_STATUS;
            }
        }, null);

        final List<StrategyScriptProperties> properties = new ArrayList<StrategyScriptProperties>();
        properties.add(StrategyScriptProperties.create(StrategyScriptStore.K_INSTRUMENT, security1));
        properties.add(StrategyScriptProperties.create(StrategyScriptStore.K_INSTRUMENT, security2));

        StrategyScriptStore store = new StrategyScriptStore();
        store.properties = properties;

        IStoreProperties storeProperties = store.fetchProperties(new NullProgressMonitor());
        ISecurity[] value = (ISecurity[]) storeProperties.getProperty(IScriptStrategy.PROP_INSTRUMENTS);
        assertNotNull(value);
        assertEquals(2, value.length);
    }

    public void testPutInstrumentsProperty() throws Exception {
        final Security security1 = new Security("Microsoft", null);
        final Security security2 = new Security("Apple", null);

        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                IStore store = repository.createObject();
                store.putProperties(security1.getStoreProperties(), monitor);
                security1.setStore(store);

                store = repository.createObject();
                store.putProperties(security2.getStoreProperties(), monitor);
                security2.setStore(store);

                return Status.OK_STATUS;
            }
        }, null);

        StoreProperties storeProperties = new StoreProperties();
        storeProperties.setProperty(IScriptStrategy.PROP_INSTRUMENTS, new ISecurity[] {
            security1, security2
        });

        StrategyScriptStore store = new StrategyScriptStore();
        store.setRepository(repository);

        store.putProperties(storeProperties, new NullProgressMonitor());

        assertNotNull(store.instrumentsData);
        assertEquals(2, store.instrumentsData.length);
    }

    public void testFetchBarsProperty() throws Exception {
        final List<StrategyScriptProperties> properties = new ArrayList<StrategyScriptProperties>();
        properties.add(StrategyScriptProperties.create(StrategyScriptStore.K_BARS, TimeSpan.minutes(5)));
        properties.add(StrategyScriptProperties.create(StrategyScriptStore.K_BARS, TimeSpan.days(1)));

        StrategyScriptStore store = new StrategyScriptStore();
        store.properties = properties;

        IStoreProperties storeProperties = store.fetchProperties(new NullProgressMonitor());
        TimeSpan[] value = (TimeSpan[]) storeProperties.getProperty(IScriptStrategy.PROP_BARS_TIMESPAN);
        assertNotNull(value);
        assertEquals(2, value.length);
    }

    public void testPutBarsProperty() throws Exception {
        StoreProperties storeProperties = new StoreProperties();
        storeProperties.setProperty(IScriptStrategy.PROP_BARS_TIMESPAN, new TimeSpan[] {
            TimeSpan.minutes(5), TimeSpan.days(1)
        });

        StrategyScriptStore store = new StrategyScriptStore();
        store.setRepository(repository);

        store.putProperties(storeProperties, new NullProgressMonitor());

        assertNotNull(store.barsData);
        assertEquals(2, store.barsData.length);
    }

    public void testFetchIncludesProperty() throws Exception {
        final Script script = new Script("Common Functions");

        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                IStore store = repository.createObject();
                store.putProperties(script.getStoreProperties(), monitor);
                script.setStore(store);
                return Status.OK_STATUS;
            }
        }, null);

        final List<StrategyScriptProperties> properties = new ArrayList<StrategyScriptProperties>();
        properties.add(StrategyScriptProperties.create(StrategyScriptStore.K_INCLUDE, script));

        StrategyScriptStore store = new StrategyScriptStore();
        store.properties = properties;

        IStoreProperties storeProperties = store.fetchProperties(new NullProgressMonitor());
        IScript[] value = (IScript[]) storeProperties.getProperty(IScriptStrategy.PROP_INCLUDES);
        assertNotNull(value);
        assertEquals(1, value.length);
    }

    public void testPutIncludesProperty() throws Exception {
        final Script script = new Script("Common Functions");

        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                IStore store = repository.createObject();
                store.putProperties(script.getStoreProperties(), monitor);
                script.setStore(store);
                return Status.OK_STATUS;
            }
        }, null);

        StoreProperties storeProperties = new StoreProperties();
        storeProperties.setProperty(IScriptStrategy.PROP_INCLUDES, new IScript[] {
            script
        });

        StrategyScriptStore store = new StrategyScriptStore();
        store.setRepository(repository);

        store.putProperties(storeProperties, new NullProgressMonitor());

        assertNotNull(store.includesData);
        assertEquals(1, store.includesData.length);
    }
}
