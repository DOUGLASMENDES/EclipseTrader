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

package org.eclipsetrader.core.internal.repositories;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.History;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.RepositoryResourceDelta;
import org.eclipsetrader.core.views.IWatchListColumn;
import org.eclipsetrader.core.views.IWatchListElement;
import org.eclipsetrader.core.views.WatchList;
import org.eclipsetrader.core.views.WatchListElement;

public class RepositoryServiceTest extends TestCase {

    private Map<String, RepositoryMock> repositories;

    public class RepositoryServiceMock extends RepositoryService {

        public RepositoryServiceMock() {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.internal.repositories.RepositoryService#getRepository(java.lang.String)
         */
        @Override
        public IRepository getRepository(String scheme) {
            return repositories.get(scheme);
        }
    }

    public class RepositoryMock implements IRepository {

        private String scheme;
        public List<StoreMock> stores;

        public RepositoryMock(String scheme) {
            this.scheme = scheme;
            this.stores = new ArrayList<StoreMock>();
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepository#getSchema()
         */
        @Override
        public String getSchema() {
            return scheme;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepository#canDelete()
         */
        @Override
        public boolean canDelete() {
            return true;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepository#canWrite()
         */
        @Override
        public boolean canWrite() {
            return true;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepository#createObject()
         */
        @Override
        public IStore createObject() {
            try {
                StoreMock store = new StoreMock(this, new URI(scheme, "object", String.valueOf(stores.size() + 1)));
                stores.add(store);
                return store;
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepository#fetchObjects(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStore[] fetchObjects(IProgressMonitor monitor) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepository#getObject(java.net.URI)
         */
        @Override
        public IStore getObject(URI uri) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepository#runInRepository(org.eclipsetrader.core.repositories.IRepositoryRunnable, org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStatus runInRepository(IRepositoryRunnable runnable, IProgressMonitor monitor) {
            try {
                runnable.run(monitor);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Status.OK_STATUS;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepository#runInRepository(org.eclipsetrader.core.repositories.IRepositoryRunnable, org.eclipse.core.runtime.jobs.ISchedulingRule, org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStatus runInRepository(IRepositoryRunnable runnable, ISchedulingRule rule, IProgressMonitor monitor) {
            try {
                runnable.run(monitor);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Status.OK_STATUS;
        }
    }

    public class StoreMock implements IStore {

        private URI uri;
        private RepositoryMock repository;
        private IStoreProperties properties;

        public StoreMock(RepositoryMock repository, URI uri) {
            this.repository = repository;
            this.uri = uri;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public void delete(IProgressMonitor monitor) throws CoreException {
            repository.stores.remove(this);
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStoreProperties fetchProperties(IProgressMonitor monitor) {
            return properties;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStore[] fetchChilds(IProgressMonitor monitor) {
            List<IStore> l = new ArrayList<IStore>();

            String type = (String) properties.getProperty(IPropertyConstants.OBJECT_TYPE);
            if (ISecurity.class.getName().equals(type)) {
                for (StoreMock store : repository.stores) {
                    IStoreProperties storeProperties = store.fetchProperties(monitor);
                    if (IHistory.class.getName().equals(storeProperties.getProperty(IPropertyConstants.OBJECT_TYPE))) {
                        Security security = (Security) storeProperties.getProperty(IPropertyConstants.SECURITY);
                        if (security.getStore().toURI().equals(toURI())) {
                            l.add(store);
                        }
                    }
                }
            }

            return l.toArray(new IStore[l.size()]);
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#createChild()
         */
        @Override
        public IStore createChild() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#getRepository()
         */
        @Override
        public IRepository getRepository() {
            return repository;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
            this.properties = properties;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#toURI()
         */
        @Override
        public URI toURI() {
            return uri;
        }
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        repositories = new HashMap<String, RepositoryMock>();
        repositories.put("local", new RepositoryMock("local"));
        repositories.put("remote", new RepositoryMock("remote"));
    }

    public void testSaveSecurity() throws Exception {
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new ISecurity[] {
            security
        });
        assertEquals(1, repositories.get("local").stores.size());
        assertEquals(0, repositories.get("remote").stores.size());
        assertSame(repositories.get("local").stores.get(0), security.getStore());
    }

    public void testMoveSecurity() throws Exception {
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new ISecurity[] {
            security
        });
        assertEquals(1, repositories.get("local").stores.size());
        assertEquals(0, repositories.get("remote").stores.size());
        service.moveAdaptable(new ISecurity[] {
            security
        }, repositories.get("remote"));
        assertEquals(0, repositories.get("local").stores.size());
        assertEquals(1, repositories.get("remote").stores.size());
        assertSame(repositories.get("remote").stores.get(0), security.getStore());
    }

    public void testDeleteSecurity() throws Exception {
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new ISecurity[] {
            security
        });
        assertEquals(1, repositories.get("local").stores.size());
        service.deleteAdaptable(new ISecurity[] {
            security
        });
        assertEquals(0, repositories.get("local").stores.size());
        assertNull(security.getStore());
    }

    public void testSaveSecurityAndAddToCollection() throws Exception {
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        RepositoryService service = new RepositoryServiceMock();
        assertEquals(0, service.getSecurities().length);
        service.saveAdaptable(new ISecurity[] {
            security
        });
        assertEquals(1, service.getSecurities().length);
        assertSame(security, service.getSecurities()[0]);
    }

    public void testDeleteSecurityAndRemoveFromCollection() throws Exception {
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new ISecurity[] {
            security
        });
        assertEquals(1, service.getSecurities().length);
        service.deleteAdaptable(new ISecurity[] {
            security
        });
        assertEquals(0, service.getSecurities().length);
    }

    public void testMoveSecurityChangesURI() throws Exception {
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new ISecurity[] {
            security
        });
        assertEquals(new URI("local", "object", "1"), security.getStore().toURI());
        service.moveAdaptable(new ISecurity[] {
            security
        }, repositories.get("remote"));
        assertEquals(new URI("remote", "object", "1"), security.getStore().toURI());
    }

    public void testSaveSecurityEvent() throws Exception {
        final Security security = new Security("Security", new FeedIdentifier("ID", null));
        final RepositoryService service = new RepositoryServiceMock();
        service.runInService(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                service.saveAdaptable(new ISecurity[] {
                    security
                });
                return Status.OK_STATUS;
            }
        }, null);
        RepositoryResourceDelta[] deltas = service.getDeltas();
        assertEquals(1, deltas.length);
        assertSame(security, deltas[0].getResource());
        assertSame(repositories.get("local"), deltas[0].getMovedTo());
    }

    public void testMoveSecurityEvent() throws Exception {
        final Security security = new Security("Security", new FeedIdentifier("ID", null));
        final RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new ISecurity[] {
            security
        });
        service.runInService(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                service.moveAdaptable(new ISecurity[] {
                    security
                }, repositories.get("remote"));
                return Status.OK_STATUS;
            }
        }, null);
        RepositoryResourceDelta[] deltas = service.getDeltas();
        assertEquals(1, deltas.length);
        assertSame(security, deltas[0].getResource());
        assertSame(repositories.get("local"), deltas[0].getMovedFrom());
        assertSame(repositories.get("remote"), deltas[0].getMovedTo());
    }

    public void testDeleteSecurityEvent() throws Exception {
        final Security security = new Security("Security", new FeedIdentifier("ID", null));
        final RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new ISecurity[] {
            security
        });
        service.runInService(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                service.deleteAdaptable(new ISecurity[] {
                    security
                });
                return Status.OK_STATUS;
            }
        }, null);
        RepositoryResourceDelta[] deltas = service.getDeltas();
        assertEquals(1, deltas.length);
        assertSame(security, deltas[0].getResource());
        assertSame(repositories.get("local"), deltas[0].getMovedFrom());
        assertNull(deltas[0].getMovedTo());
    }

    public void testDeleteSecurityRemovesFromWatchList() throws Exception {
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        WatchList list = new WatchList("List", new IWatchListColumn[0]);
        list.setItems(new IWatchListElement[] {
            new WatchListElement(security)
        });
        RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new IAdaptable[] {
                security, list
        });
        assertEquals(2, repositories.get("local").stores.size());
        service.deleteAdaptable(new IAdaptable[] {
            security
        });
        assertEquals(1, repositories.get("local").stores.size());
        assertEquals(0, list.getItemCount());
    }

    public void testMoveSecurityWithHistory() throws Exception {
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        History history = new History(security, new IOHLC[0]);

        RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new IAdaptable[] {
                security, history
        });

        service.moveAdaptable(new IAdaptable[] {
            security
        }, repositories.get("remote"));

        assertEquals(0, repositories.get("local").stores.size());
        assertEquals(2, repositories.get("remote").stores.size());
    }

    public void testGetHistoryForSecurity() throws Exception {
        Security security = new Security("Security", new FeedIdentifier("ID", null));
        History history = new History(security, new IOHLC[0]);

        RepositoryService service = new RepositoryServiceMock();
        service.saveAdaptable(new IAdaptable[] {
                security, history
        });

        assertNotNull(service.getHistoryFor(security));
    }
}
