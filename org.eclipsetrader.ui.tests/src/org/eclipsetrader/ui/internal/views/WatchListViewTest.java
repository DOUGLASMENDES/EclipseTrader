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

package org.eclipsetrader.ui.internal.views;

import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.WorkbenchPart;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;
import org.eclipsetrader.core.views.WatchList;
import org.eclipsetrader.core.views.WatchListColumn;
import org.eclipsetrader.core.views.WatchListElement;
import org.eclipsetrader.ui.internal.TestUIActivator;
import org.eclipsetrader.ui.internal.TestWorkbenchPartSite;

public class WatchListViewTest extends TestCase {

    private Shell shell;
    TestUIActivator activator;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        Display display = Display.getCurrent();
        shell = new Shell(display);

        activator = new TestUIActivator();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        shell.dispose();
        while (Display.getCurrent().readAndDispatch()) {
        }
        activator.dispose();
    }

    public void testOpenWithoutView() throws Exception {
        WatchListView part = new WatchListView();
        part.createPartControl(shell);
        assertNull(part.getViewer());
    }

    public void testCreateEmptyView() throws Exception {
        WatchList watchList = new WatchList("Test", new WatchListColumn[0]);

        WatchListView part = new WatchListView();
        getField(WatchListView.class, "watchList").set(part, watchList);
        getField(WorkbenchPart.class, "partSite").set(part, new TestWorkbenchPartSite(shell));
        part.createPartControl(shell);

        assertEquals(0, part.getTable().getColumnCount());
        assertEquals(0, part.getTable().getItemCount());
    }

    public void testCreateColumns() throws Exception {
        WatchListView part = new WatchListView();
        TableViewer viewer = part.createViewer(shell);
        WatchListViewColumn[] columns = new WatchListViewColumn[] {
            new WatchListViewColumn("Column1", new DataProviderFactory("test.id1", "Test1")),
        };
        part.createColumns(viewer, columns);
        assertEquals(1, viewer.getTable().getColumnCount());
        assertEquals("Column1", viewer.getTable().getColumn(0).getText());
    }

    public void testAddColumns() throws Exception {
        WatchListView part = new WatchListView();
        TableViewer viewer = part.createViewer(shell);
        WatchListViewColumn[] columns = new WatchListViewColumn[] {
            new WatchListViewColumn("Column1", new DataProviderFactory("test.id1", "Test1")),
        };
        part.createColumns(viewer, columns);
        assertEquals(1, viewer.getTable().getColumnCount());
        columns = new WatchListViewColumn[] {
            new WatchListViewColumn("Column1", new DataProviderFactory("test.id1", "Test1")),
            new WatchListViewColumn("Column2", new DataProviderFactory("test.id2", "Test2")),
        };
        part.createColumns(viewer, columns);
        assertEquals(2, viewer.getTable().getColumnCount());
        assertEquals("Column1", viewer.getTable().getColumn(0).getText());
        assertEquals("Column2", viewer.getTable().getColumn(1).getText());
    }

    public void testRemoveColumns() throws Exception {
        WatchListView part = new WatchListView();
        TableViewer viewer = part.createViewer(shell);
        WatchListViewColumn[] columns = new WatchListViewColumn[] {
            new WatchListViewColumn("Column1", new DataProviderFactory("test.id1", "Test1")),
            new WatchListViewColumn("Column2", new DataProviderFactory("test.id2", "Test2")),
        };
        part.createColumns(viewer, columns);
        assertEquals(2, viewer.getTable().getColumnCount());
        columns = new WatchListViewColumn[] {
            new WatchListViewColumn("Column1", new DataProviderFactory("test.id1", "Test1")),
        };
        part.createColumns(viewer, columns);
        assertEquals(1, viewer.getTable().getColumnCount());
        assertEquals("Column1", viewer.getTable().getColumn(0).getText());
    }

    public void testCompareDoubleValues() throws Exception {
        IAdaptable v1 = new IAdaptable() {

            @Override
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
                if (adapter.isAssignableFrom(Double.class)) {
                    return new Double(10.5);
                }
                return null;
            }
        };
        IAdaptable v2 = new IAdaptable() {

            @Override
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
                if (adapter.isAssignableFrom(Double.class)) {
                    return new Double(10.6);
                }
                return null;
            }
        };
        WatchListView part = new WatchListView();
        assertEquals(-1, part.compareValues(v1, v2));
    }

    public void testCompareLongValues() throws Exception {
        IAdaptable v1 = new IAdaptable() {

            @Override
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
                if (adapter.isAssignableFrom(Long.class)) {
                    return new Long(100000);
                }
                return null;
            }
        };
        IAdaptable v2 = new IAdaptable() {

            @Override
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
                if (adapter.isAssignableFrom(Long.class)) {
                    return new Long(110000);
                }
                return null;
            }
        };
        WatchListView part = new WatchListView();
        assertEquals(-1, part.compareValues(v1, v2));
    }

    public void testCompareStringValues() throws Exception {
        IAdaptable v1 = new IAdaptable() {

            @Override
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
                if (adapter.isAssignableFrom(String.class)) {
                    return "A";
                }
                return null;
            }
        };
        IAdaptable v2 = new IAdaptable() {

            @Override
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
                if (adapter.isAssignableFrom(String.class)) {
                    return "B";
                }
                return null;
            }
        };
        WatchListView part = new WatchListView();
        assertEquals(-1, part.compareValues(v1, v2));
    }

    public void testCompareValueWithNull() throws Exception {
        IAdaptable v1 = new IAdaptable() {

            @Override
            @SuppressWarnings({
                "unchecked", "rawtypes"
            })
            public Object getAdapter(Class adapter) {
                if (adapter.isAssignableFrom(Double.class)) {
                    return new Double(10.5);
                }
                return null;
            }
        };
        WatchListView part = new WatchListView();
        assertEquals(1, part.compareValues(v1, null));
    }

    public void testCompareNullWithValue() throws Exception {
        IAdaptable v2 = new IAdaptable() {

            @Override
            @SuppressWarnings({
                "unchecked", "rawtypes"
            })
            public Object getAdapter(Class adapter) {
                if (adapter.isAssignableFrom(Double.class)) {
                    return new Double(10.5);
                }
                return null;
            }
        };
        WatchListView part = new WatchListView();
        assertEquals(-1, part.compareValues(null, v2));
    }

    public void testCompareNullValues() throws Exception {
        WatchListView part = new WatchListView();
        assertEquals(0, part.compareValues(null, null));
    }

    public void testBuildColumns() throws Exception {
        WatchList watchList = new WatchList("Test", new WatchListColumn[] {
            new WatchListColumn("Col1", new DataProviderFactory("test.id1", "Test1")),
            new WatchListColumn("Col2", new DataProviderFactory("test.id2", "Test2"))
        });

        WatchListView part = new WatchListView();
        getField(WatchListView.class, "watchList").set(part, watchList);
        getField(WorkbenchPart.class, "partSite").set(part, new TestWorkbenchPartSite(shell));
        part.createPartControl(shell);

        WatchListViewColumn[] columns = part.getColumns();
        assertEquals(2, columns.length);
        assertEquals("Col1", columns[0].getName());
        assertEquals("Col2", columns[1].getName());
    }

    public void testBuildItems() throws Exception {
        WatchList watchList = new WatchList("Test", new WatchListColumn[0]);
        watchList.setItems(new WatchListElement[] {
            new WatchListElement(new Security("Test1", null)),
            new WatchListElement(new Security("Test2", null)),
        });

        WatchListView part = new WatchListView();
        getField(WatchListView.class, "watchList").set(part, watchList);
        getField(WorkbenchPart.class, "partSite").set(part, new TestWorkbenchPartSite(shell));
        part.createPartControl(shell);

        assertEquals(2, part.getItemsMap().size());
    }

    public void testAggregateItemsWithSameSecurity() throws Exception {
        Security security = new Security("Test1", null);

        WatchList watchList = new WatchList("Test", new WatchListColumn[0]);
        watchList.setItems(new WatchListElement[] {
            new WatchListElement(security), new WatchListElement(security),
        });

        WatchListView part = new WatchListView();
        getField(WatchListView.class, "watchList").set(part, watchList);
        getField(WorkbenchPart.class, "partSite").set(part, new TestWorkbenchPartSite(shell));
        part.createPartControl(shell);

        assertEquals(1, part.getItemsMap().size());
        assertEquals(2, part.getItemsMap().get(security).size());
    }

    public void testNotifyAddedElement() throws Exception {
        WatchListElement element1 = new WatchListElement(new Security("Test1", null));
        WatchListElement element2 = new WatchListElement(new Security("Test2", null));

        WatchList watchList = new WatchList("Test", new WatchListColumn[0]);
        watchList.setItems(new WatchListElement[] {
            element1, element2
        });

        WatchListView part = new WatchListView();
        getField(WatchListView.class, "watchList").set(part, watchList);
        getField(WorkbenchPart.class, "partSite").set(part, new TestWorkbenchPartSite(shell));
        part.createPartControl(shell);

        assertEquals(2, part.getItemsMap().size());

        WatchListElement element3 = new WatchListElement(new Security("Test3", null));
        watchList.setItems(new WatchListElement[] {
            element1, element2, element3
        });

        assertEquals(3, part.getItemsMap().size());
    }

    public void testNotifyRemovedElement() throws Exception {
        WatchListElement element1 = new WatchListElement(new Security("Test1", null));
        WatchListElement element2 = new WatchListElement(new Security("Test2", null));

        WatchList watchList = new WatchList("Test", new WatchListColumn[0]);
        watchList.setItems(new WatchListElement[] {
            element1, element2
        });

        WatchListView part = new WatchListView();
        getField(WatchListView.class, "watchList").set(part, watchList);
        getField(WorkbenchPart.class, "partSite").set(part, new TestWorkbenchPartSite(shell));
        part.createPartControl(shell);

        assertEquals(2, part.getItemsMap().size());

        watchList.setItems(new WatchListElement[] {
            element1
        });

        assertEquals(1, part.getItemsMap().size());
    }

    public void testCallInitOnInitialization() throws Exception {
        DataProvider provider = new DataProvider();
        WatchList watchList = new WatchList("Test", new WatchListColumn[] {
            new WatchListColumn("col1", new DataProviderFactory("col1", "Col 1", provider)),
        });
        watchList.setItems(new WatchListElement[] {
            new WatchListElement(new Security("Test1", null))
        });

        WatchListView part = new WatchListView();
        getField(WatchListView.class, "watchList").set(part, watchList);
        getField(WorkbenchPart.class, "partSite").set(part, new TestWorkbenchPartSite(shell));
        part.createPartControl(shell);

        assertEquals(1, part.getItemsMap().size());
        assertTrue(provider.isInitCalled());
    }

    public void testCallInitOnNewAdditions() throws Exception {
        DataProvider provider = new DataProvider();
        WatchList watchList = new WatchList("Test", new WatchListColumn[] {
            new WatchListColumn("col1", new DataProviderFactory("col1", "Col 1", provider)),
        });

        WatchListView part = new WatchListView();
        getField(WatchListView.class, "watchList").set(part, watchList);
        getField(WorkbenchPart.class, "partSite").set(part, new TestWorkbenchPartSite(shell));
        part.createPartControl(shell);

        assertEquals(0, part.getItemsMap().size());
        assertFalse(provider.isInitCalled());

        part.addItem(new Security("Test 1", null));

        assertEquals(1, part.getItemsMap().size());
        assertTrue(provider.isInitCalled());
    }

    public void testCallDisposeOnRemove() throws Exception {
        DataProvider provider = new DataProvider();
        WatchList watchList = new WatchList("Test", new WatchListColumn[] {
            new WatchListColumn("col1", new DataProviderFactory("col1", "Col 1", provider)),
        });

        WatchListView part = new WatchListView();
        getField(WatchListView.class, "watchList").set(part, watchList);
        getField(WorkbenchPart.class, "partSite").set(part, new TestWorkbenchPartSite(shell));
        part.createPartControl(shell);

        Security security = new Security("Test 1", null);
        part.addItem(security);
        part.removeItem(part.getItemsMap().get(security).iterator().next());

        assertEquals(0, part.getItemsMap().size());
        assertTrue(provider.isDisposed());
    }

    public void testInitializeNewColumns() throws Exception {
        DataProvider provider = new DataProvider();
        WatchList watchList = new WatchList("Test", new WatchListColumn[0]);
        watchList.setItems(new WatchListElement[] {
            new WatchListElement(new Security("Test1", null))
        });

        WatchListView part = new WatchListView();
        getField(WatchListView.class, "watchList").set(part, watchList);
        getField(WorkbenchPart.class, "partSite").set(part, new TestWorkbenchPartSite(shell));
        part.createPartControl(shell);

        part.setColumns(new WatchListViewColumn[] {
            new WatchListViewColumn("col1", new DataProviderFactory("col1", "Col 1", provider))
        });
        assertTrue(provider.isInitCalled());
    }

    public void testDisposeRemovedColumns() throws Exception {
        DataProvider provider = new DataProvider();
        WatchList watchList = new WatchList("Test", new WatchListColumn[0]);
        watchList.setItems(new WatchListElement[] {
            new WatchListElement(new Security("Test1", null))
        });

        WatchListView part = new WatchListView();
        getField(WatchListView.class, "watchList").set(part, watchList);
        getField(WorkbenchPart.class, "partSite").set(part, new TestWorkbenchPartSite(shell));
        part.createPartControl(shell);
        part.setColumns(new WatchListViewColumn[] {
            new WatchListViewColumn("col1", new DataProviderFactory("col1", "Col 1", provider))
        });

        part.setColumns(new WatchListViewColumn[0]);
        assertTrue(provider.isDisposed());
    }

    @SuppressWarnings("unchecked")
    private Field getField(Class clazz, String name) {
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(name)) {
                fields[i].setAccessible(true);
                return fields[i];
            }
        }
        return null;
    }

    private class DataProviderFactory implements IDataProviderFactory {

        private String id;
        private String name;
        private IDataProvider provider;

        public DataProviderFactory(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public DataProviderFactory(String id, String name, IDataProvider provider) {
            this.id = id;
            this.name = name;
            this.provider = provider;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#createProvider()
         */
        @Override
        public IDataProvider createProvider() {
            return provider;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#getId()
         */
        @Override
        public String getId() {
            return id;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#getName()
         */
        @Override
        public String getName() {
            return name;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#getType()
         */
        @Override
        @SuppressWarnings("unchecked")
        public Class[] getType() {
            return new Class[] {
                String.class
            };
        }
    }

    private class DataProvider implements IDataProvider {

        private boolean disposed;
        private boolean initCalled;

        public DataProvider() {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#init(org.eclipse.core.runtime.IAdaptable)
         */
        @Override
        public void init(IAdaptable adaptable) {
            initCalled = true;
        }

        public boolean isInitCalled() {
            return initCalled;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#dispose()
         */
        @Override
        public void dispose() {
            disposed = true;
        }

        public boolean isDisposed() {
            return disposed;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#getFactory()
         */
        @Override
        public IDataProviderFactory getFactory() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#getValue(org.eclipse.core.runtime.IAdaptable)
         */
        @Override
        public IAdaptable getValue(IAdaptable adaptable) {
            return null;
        }
    }
}
