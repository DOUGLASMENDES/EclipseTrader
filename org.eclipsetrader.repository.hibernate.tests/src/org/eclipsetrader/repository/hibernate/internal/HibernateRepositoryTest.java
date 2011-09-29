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

package org.eclipsetrader.repository.hibernate.internal;

import java.util.Properties;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipsetrader.core.Script;
import org.eclipsetrader.core.ats.IScriptStrategy;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.core.views.Column;
import org.eclipsetrader.core.views.Holding;
import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IHolding;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.repository.hibernate.HibernateRepository;
import org.eclipsetrader.repository.hibernate.internal.stores.ScriptStore;
import org.eclipsetrader.repository.hibernate.internal.stores.SecurityStore;
import org.eclipsetrader.repository.hibernate.internal.stores.StrategyScriptStore;
import org.eclipsetrader.repository.hibernate.internal.stores.WatchListStore;
import org.eclipsetrader.repository.hibernate.internal.types.IdentifierType;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

public class HibernateRepositoryTest extends TestCase {

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

    public void testCreateSecurity() throws Exception {
        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                createSecurity("Microsoft Corp.", null);
                return Status.OK_STATUS;
            }
        }, null);
        assertEquals(1, repository.getSession().createCriteria(SecurityStore.class).list().size());
        assertEquals(0, repository.getSession().createCriteria(IdentifierType.class).list().size());
        assertEquals(1, repository.fetchObjects(null).length);
    }

    public void testCreateSecurityWithIdentifier() throws Exception {
        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                createSecurity("Microsoft Corp.", new FeedIdentifier("MSFT", null));
                return Status.OK_STATUS;
            }
        }, null);
        assertEquals(1, repository.getSession().createCriteria(SecurityStore.class).list().size());
        assertEquals(1, repository.getSession().createCriteria(IdentifierType.class).list().size());
        assertEquals(1, repository.fetchObjects(null).length);
    }

    public void testDeleteSecurity() throws Exception {
        final IStore repositoryStore = repository.createObject();
        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                StoreProperties storeProperties = new StoreProperties();
                storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, ISecurity.class.getName());
                storeProperties.setProperty(IPropertyConstants.NAME, "Microsoft Corp.");
                storeProperties.setProperty(IPropertyConstants.IDENTIFIER, new FeedIdentifier("MSFT", null));
                repositoryStore.putProperties(storeProperties, null);
                return Status.OK_STATUS;
            }
        }, null);
        assertEquals(1, repository.getSession().createCriteria(SecurityStore.class).list().size());
        assertEquals(1, repository.getSession().createCriteria(IdentifierType.class).list().size());
        assertEquals(1, repository.fetchObjects(null).length);
        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                repositoryStore.delete(monitor);
                return Status.OK_STATUS;
            }
        }, null);
        assertEquals(0, repository.getSession().createCriteria(SecurityStore.class).list().size());
        assertEquals(1, repository.getSession().createCriteria(IdentifierType.class).list().size());
        assertEquals(0, repository.fetchObjects(null).length);
    }

    public void testCreateWatchList() throws Exception {
        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                StoreProperties storeProperties = new StoreProperties();
                storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IWatchList.class.getName());
                storeProperties.setProperty(IPropertyConstants.NAME, "Sample");
                IStore repositoryStore = repository.createObject();
                repositoryStore.putProperties(storeProperties, null);
                return Status.OK_STATUS;
            }
        }, null);
        assertEquals(1, repository.getSession().createCriteria(WatchListStore.class).list().size());
        assertEquals(1, repository.fetchObjects(null).length);
    }

    public void testCreateWatchListWithColumns() throws Exception {
        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                StoreProperties storeProperties = new StoreProperties();
                storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IWatchList.class.getName());
                storeProperties.setProperty(IPropertyConstants.NAME, "Sample");
                storeProperties.setProperty(IPropertyConstants.COLUMNS, new IColumn[] {
                    new Column("C1", null), new Column("C2", null),
                });
                IStore repositoryStore = repository.createObject();
                repositoryStore.putProperties(storeProperties, null);
                return Status.OK_STATUS;
            }
        }, null);
        assertEquals(1, repository.getSession().createCriteria(WatchListStore.class).list().size());
        assertEquals(1, repository.fetchObjects(null).length);
    }

    public void testCreateWatchListWithSecurities() throws Exception {
        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                IStore store = createSecurity("Microsoft Corp.", new FeedIdentifier("MSFT", null));
                Security security1 = new Security(store, store.fetchProperties(monitor));
                store = createSecurity("Google", new FeedIdentifier("GOOG", null));
                Security security2 = new Security(store, store.fetchProperties(monitor));

                StoreProperties storeProperties = new StoreProperties();
                storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IWatchList.class.getName());
                storeProperties.setProperty(IPropertyConstants.NAME, "Sample");
                storeProperties.setProperty(IPropertyConstants.HOLDINGS, new IHolding[] {
                    new Holding(security1), new Holding(security2),
                });
                IStore repositoryStore = repository.createObject();
                repositoryStore.putProperties(storeProperties, null);
                return Status.OK_STATUS;
            }
        }, null);
        assertEquals(2, repository.getSession().createCriteria(SecurityStore.class).list().size());
        assertEquals(1, repository.getSession().createCriteria(WatchListStore.class).list().size());
        assertEquals(3, repository.fetchObjects(null).length);
    }

    public void testDeleteWatchList() throws Exception {
        final IStore repositoryStore = repository.createObject();
        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                StoreProperties storeProperties = new StoreProperties();
                storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IWatchList.class.getName());
                storeProperties.setProperty(IPropertyConstants.NAME, "Sample");
                repositoryStore.putProperties(storeProperties, null);
                return Status.OK_STATUS;
            }
        }, null);
        assertEquals(1, repository.getSession().createCriteria(WatchListStore.class).list().size());
        assertEquals(1, repository.fetchObjects(null).length);
        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                repositoryStore.delete(monitor);
                return Status.OK_STATUS;
            }
        }, null);
        assertEquals(0, repository.getSession().createCriteria(WatchListStore.class).list().size());
        assertEquals(0, repository.fetchObjects(null).length);
    }

    public void testDeleteWatchListWithSecurities() throws Exception {
        final IStore repositoryStore = repository.createObject();
        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                IStore store = createSecurity("Microsoft Corp.", new FeedIdentifier("MSFT", null));
                Security security1 = new Security(store, store.fetchProperties(monitor));
                store = createSecurity("Google", new FeedIdentifier("GOOG", null));
                Security security2 = new Security(store, store.fetchProperties(monitor));

                StoreProperties storeProperties = new StoreProperties();
                storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IWatchList.class.getName());
                storeProperties.setProperty(IPropertyConstants.NAME, "Sample");
                storeProperties.setProperty(IPropertyConstants.HOLDINGS, new IHolding[] {
                    new Holding(security1), new Holding(security2),
                });
                repositoryStore.putProperties(storeProperties, null);
                return Status.OK_STATUS;
            }
        }, null);
        assertEquals(2, repository.getSession().createCriteria(SecurityStore.class).list().size());
        assertEquals(1, repository.getSession().createCriteria(WatchListStore.class).list().size());
        assertEquals(3, repository.fetchObjects(null).length);
        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                repositoryStore.delete(monitor);
                return Status.OK_STATUS;
            }
        }, null);
        assertEquals(2, repository.getSession().createCriteria(SecurityStore.class).list().size());
        assertEquals(0, repository.getSession().createCriteria(WatchListStore.class).list().size());
        assertEquals(2, repository.fetchObjects(null).length);
    }

    public void testCreateScript() throws Exception {
        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                StoreProperties storeProperties = new StoreProperties();
                storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, Script.class.getName());
                storeProperties.setProperty(IPropertyConstants.NAME, "Test");
                IStore repositoryStore = repository.createObject();
                repositoryStore.putProperties(storeProperties, null);
                return Status.OK_STATUS;
            }
        }, null);

        assertEquals(1, repository.getSession().createCriteria(ScriptStore.class).list().size());
        assertEquals(1, repository.fetchObjects(null).length);
    }

    public void testDeleteScript() throws Exception {
        final IStore repositoryStore = repository.createObject();

        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                StoreProperties storeProperties = new StoreProperties();
                storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, Script.class.getName());
                storeProperties.setProperty(IPropertyConstants.NAME, "Test");
                repositoryStore.putProperties(storeProperties, null);
                return Status.OK_STATUS;
            }
        }, null);

        assertEquals(1, repository.getSession().createCriteria(ScriptStore.class).list().size());
        assertEquals(1, repository.fetchObjects(null).length);

        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                repositoryStore.delete(monitor);
                return Status.OK_STATUS;
            }
        }, null);

        assertEquals(0, repository.getSession().createCriteria(ScriptStore.class).list().size());
        assertEquals(0, repository.fetchObjects(null).length);
    }

    public void testCreateScriptStrategy() throws Exception {
        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                StoreProperties storeProperties = new StoreProperties();
                storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IScriptStrategy.class.getName());
                storeProperties.setProperty(IScriptStrategy.PROP_NAME, "Test");
                IStore repositoryStore = repository.createObject();
                repositoryStore.putProperties(storeProperties, null);
                return Status.OK_STATUS;
            }
        }, null);

        assertEquals(1, repository.getSession().createCriteria(StrategyScriptStore.class).list().size());
        assertEquals(1, repository.fetchObjects(null).length);
    }

    public void testDeleteScriptStrategy() throws Exception {
        final IStore repositoryStore = repository.createObject();

        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                StoreProperties storeProperties = new StoreProperties();
                storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IScriptStrategy.class.getName());
                storeProperties.setProperty(IScriptStrategy.PROP_NAME, "Test");
                repositoryStore.putProperties(storeProperties, null);
                return Status.OK_STATUS;
            }
        }, null);

        assertEquals(1, repository.getSession().createCriteria(StrategyScriptStore.class).list().size());
        assertEquals(1, repository.fetchObjects(null).length);

        repository.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                repositoryStore.delete(monitor);
                return Status.OK_STATUS;
            }
        }, null);

        assertEquals(0, repository.getSession().createCriteria(StrategyScriptStore.class).list().size());
        assertEquals(0, repository.fetchObjects(null).length);
    }

    private IStore createSecurity(String name, IFeedIdentifier identifier) {
        StoreProperties storeProperties = new StoreProperties();
        storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, ISecurity.class.getName());
        storeProperties.setProperty(IPropertyConstants.NAME, name);
        storeProperties.setProperty(IPropertyConstants.IDENTIFIER, identifier);
        IStore repositoryStore = repository.createObject();
        repositoryStore.putProperties(storeProperties, null);
        return repositoryStore;
    }
}
