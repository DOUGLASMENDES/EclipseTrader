/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
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
	private Map<String, TestRepository> repositories;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
	    repositories = new HashMap<String, TestRepository>();
	    repositories.put("local", new TestRepository("local"));
	    repositories.put("remote", new TestRepository("remote"));
    }

	public void testSaveSecurity() throws Exception {
	    Security security = new Security("Security", new FeedIdentifier("ID", null));
	    RepositoryService service = new TestRepositoryService();
	    service.saveAdaptable(new ISecurity[] { security });
	    assertEquals(1, repositories.get("local").stores.size());
	    assertEquals(0, repositories.get("remote").stores.size());
	    assertSame(repositories.get("local").stores.get(0), security.getStore());
    }

	public void testMoveSecurity() throws Exception {
	    Security security = new Security("Security", new FeedIdentifier("ID", null));
	    RepositoryService service = new TestRepositoryService();
	    service.saveAdaptable(new ISecurity[] { security });
	    assertEquals(1, repositories.get("local").stores.size());
	    assertEquals(0, repositories.get("remote").stores.size());
	    service.moveAdaptable(new ISecurity[] { security }, repositories.get("remote"));
	    assertEquals(0, repositories.get("local").stores.size());
	    assertEquals(1, repositories.get("remote").stores.size());
	    assertSame(repositories.get("remote").stores.get(0), security.getStore());
	}

	public void testDeleteSecurity() throws Exception {
	    Security security = new Security("Security", new FeedIdentifier("ID", null));
	    RepositoryService service = new TestRepositoryService();
	    service.saveAdaptable(new ISecurity[] { security });
	    assertEquals(1, repositories.get("local").stores.size());
	    service.deleteAdaptable(new ISecurity[] { security });
	    assertEquals(0, repositories.get("local").stores.size());
	    assertNull(security.getStore());
	}

	public void testSaveSecurityAndAddToCollection() throws Exception {
	    Security security = new Security("Security", new FeedIdentifier("ID", null));
	    RepositoryService service = new TestRepositoryService();
	    assertEquals(0, service.getSecurities().length);
	    service.saveAdaptable(new ISecurity[] { security });
	    assertEquals(1, service.getSecurities().length);
	    assertSame(security, service.getSecurities()[0]);
    }

	public void testDeleteSecurityAndRemoveFromCollection() throws Exception {
	    Security security = new Security("Security", new FeedIdentifier("ID", null));
	    RepositoryService service = new TestRepositoryService();
	    service.saveAdaptable(new ISecurity[] { security });
	    assertEquals(1, service.getSecurities().length);
	    service.deleteAdaptable(new ISecurity[] { security });
	    assertEquals(0, service.getSecurities().length);
	}

	public void testMoveSecurityChangesURI() throws Exception {
	    Security security = new Security("Security", new FeedIdentifier("ID", null));
	    RepositoryService service = new TestRepositoryService();
	    service.saveAdaptable(new ISecurity[] { security });
	    assertEquals(new URI("local", "object", "1"), security.getStore().toURI());
	    service.moveAdaptable(new ISecurity[] { security }, repositories.get("remote"));
	    assertEquals(new URI("remote", "object", "1"), security.getStore().toURI());
	}

	public void testSaveSecurityEvent() throws Exception {
	    final Security security = new Security("Security", new FeedIdentifier("ID", null));
	    final RepositoryService service = new TestRepositoryService();
	    service.runInService(new IRepositoryRunnable() {
			public IStatus run(IProgressMonitor monitor) throws Exception {
			    service.saveAdaptable(new ISecurity[] { security });
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
	    final RepositoryService service = new TestRepositoryService();
	    service.saveAdaptable(new ISecurity[] { security });
	    service.runInService(new IRepositoryRunnable() {
			public IStatus run(IProgressMonitor monitor) throws Exception {
			    service.moveAdaptable(new ISecurity[] { security }, repositories.get("remote"));
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
	    final RepositoryService service = new TestRepositoryService();
	    service.saveAdaptable(new ISecurity[] { security });
	    service.runInService(new IRepositoryRunnable() {
			public IStatus run(IProgressMonitor monitor) throws Exception {
			    service.deleteAdaptable(new ISecurity[] { security });
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
	    list.setItems(new IWatchListElement[] { new WatchListElement(security) });
	    RepositoryService service = new TestRepositoryService();
	    service.saveAdaptable(new IAdaptable[] { security, list });
	    assertEquals(2, repositories.get("local").stores.size());
	    service.deleteAdaptable(new IAdaptable[] { security });
	    assertEquals(1, repositories.get("local").stores.size());
	    assertEquals(0, list.getItemCount());
	}

	public void testMoveSecurityWithHistory() throws Exception {
	    Security security = new Security("Security", new FeedIdentifier("ID", null));
	    History history = new History(security, new IOHLC[0]);
	    RepositoryService service = new TestRepositoryService();
	    service.saveAdaptable(new IAdaptable[] { security, history });
	    assertEquals(2, repositories.get("local").stores.size());
	    assertEquals(0, repositories.get("remote").stores.size());
	    service.moveAdaptable(new IAdaptable[] { security }, repositories.get("remote"));
	    assertEquals(0, repositories.get("local").stores.size());
	    assertEquals(2, repositories.get("remote").stores.size());
	    assertTrue(repositories.get("remote").stores.contains(security.getStore()));
	    assertTrue(repositories.get("remote").stores.contains(history.getStore()));
	}

    public class TestRepositoryService extends RepositoryService {

    	public TestRepositoryService() {
    	}

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.internal.repositories.RepositoryService#getRepository(java.lang.String)
         */
        @Override
        public IRepository getRepository(String scheme) {
	        return repositories.get(scheme);
        }
    }

	public class TestRepository implements IRepository {
		private String scheme;
		public List<TestStore> stores;

		public TestRepository(String scheme) {
			this.scheme = scheme;
			this.stores = new ArrayList<TestStore>();
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepository#getSchema()
         */
        public String getSchema() {
	        return scheme;
        }

		/* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepository#canDelete()
         */
        public boolean canDelete() {
	        return true;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepository#canWrite()
         */
        public boolean canWrite() {
	        return true;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepository#createObject()
         */
        public IStore createObject() {
        	try {
	            TestStore store = new TestStore(this, new URI(scheme, "object", String.valueOf(stores.size() + 1)));
	            stores.add(store);
	            return store;
            } catch (URISyntaxException e) {
	            throw new RuntimeException(e);
            }
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepository#fetchObjects(org.eclipse.core.runtime.IProgressMonitor)
         */
        public IStore[] fetchObjects(IProgressMonitor monitor) {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepository#getObject(java.net.URI)
         */
        public IStore getObject(URI uri) {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepository#runInRepository(org.eclipsetrader.core.repositories.IRepositoryRunnable, org.eclipse.core.runtime.IProgressMonitor)
         */
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
        public IStatus runInRepository(IRepositoryRunnable runnable, ISchedulingRule rule, IProgressMonitor monitor) {
	        try {
	            runnable.run(monitor);
            } catch (Exception e) {
	            e.printStackTrace();
            }
	        return Status.OK_STATUS;
        }
	}

	public class TestStore implements IStore {
		private URI uri;
		private TestRepository repository;
		private IStoreProperties properties;

		public TestStore(TestRepository repository, URI uri) {
	        this.repository = repository;
	        this.uri = uri;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
         */
        public void delete(IProgressMonitor monitor) throws CoreException {
        	repository.stores.remove(this);
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
         */
        public IStoreProperties fetchProperties(IProgressMonitor monitor) {
	        return properties;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
         */
        public IStore[] fetchChilds(IProgressMonitor monitor) {
        	List<IStore> l = new ArrayList<IStore>();

        	String type = (String) properties.getProperty(IPropertyConstants.OBJECT_TYPE);
			if (ISecurity.class.getName().equals(type)) {
				for (TestStore store : repository.stores) {
					IStoreProperties storeProperties = store.fetchProperties(monitor);
					if (IHistory.class.getName().equals(storeProperties.getProperty(IPropertyConstants.OBJECT_TYPE))) {
						Security security = (Security) storeProperties.getProperty(IPropertyConstants.SECURITY);
						if (security.getStore().toURI().equals(toURI()))
							l.add(store);
					}
				}
			}

			return l.toArray(new IStore[l.size()]);
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#createChild()
         */
        public IStore createChild() {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#getRepository()
         */
        public IRepository getRepository() {
	        return repository;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
         */
        public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
        	this.properties = properties;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#toURI()
         */
        public URI toURI() {
	        return uri;
        }
	}
}
