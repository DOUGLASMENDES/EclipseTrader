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

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		Display display = Display.getCurrent();
		shell = new Shell(display);

		new TestUIActivator();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		shell.dispose();
		while (Display.getCurrent().readAndDispatch());
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
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
            	if (adapter.isAssignableFrom(Double.class))
            		return new Double(10.5);
                return null;
            }
		};
	    IAdaptable v2 = new IAdaptable() {
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
            	if (adapter.isAssignableFrom(Double.class))
            		return new Double(10.6);
                return null;
            }
		};
		WatchListView part = new WatchListView();
	    assertEquals(-1, part.compareValues(v1, v2));
    }

	public void testCompareLongValues() throws Exception {
	    IAdaptable v1 = new IAdaptable() {
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
            	if (adapter.isAssignableFrom(Long.class))
            		return new Long(100000);
                return null;
            }
		};
	    IAdaptable v2 = new IAdaptable() {
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
            	if (adapter.isAssignableFrom(Long.class))
            		return new Long(110000);
                return null;
            }
		};
		WatchListView part = new WatchListView();
	    assertEquals(-1, part.compareValues(v1, v2));
    }

	public void testCompareStringValues() throws Exception {
	    IAdaptable v1 = new IAdaptable() {
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
            	if (adapter.isAssignableFrom(String.class))
            		return "A";
                return null;
            }
		};
	    IAdaptable v2 = new IAdaptable() {
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
            	if (adapter.isAssignableFrom(String.class))
            		return "B";
                return null;
            }
		};
		WatchListView part = new WatchListView();
	    assertEquals(-1, part.compareValues(v1, v2));
    }

	public void testCompareValueWithNull() throws Exception {
	    IAdaptable v1 = new IAdaptable() {
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
            	if (adapter.isAssignableFrom(Double.class))
            		return new Double(10.5);
                return null;
            }
		};
		WatchListView part = new WatchListView();
	    assertEquals(0, part.compareValues(v1, null));
    }

	public void testCompareNullWithValue() throws Exception {
	    IAdaptable v2 = new IAdaptable() {
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
            	if (adapter.isAssignableFrom(Double.class))
            		return new Double(10.5);
                return null;
            }
		};
		WatchListView part = new WatchListView();
	    assertEquals(0, part.compareValues(null, v2));
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
	    		new WatchListElement(security),
	    		new WatchListElement(security),
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
	    watchList.setItems(new WatchListElement[] { element1, element2 });

		WatchListView part = new WatchListView();
        getField(WatchListView.class, "watchList").set(part, watchList);
        getField(WorkbenchPart.class, "partSite").set(part, new TestWorkbenchPartSite(shell));
		part.createPartControl(shell);

		assertEquals(2, part.getItemsMap().size());

	    WatchListElement element3 = new WatchListElement(new Security("Test3", null));
	    watchList.setItems(new WatchListElement[] { element1, element2, element3 });

	    assertEquals(3, part.getItemsMap().size());
    }

	public void testNotifyRemovedElement() throws Exception {
		WatchListElement element1 = new WatchListElement(new Security("Test1", null));
		WatchListElement element2 = new WatchListElement(new Security("Test2", null));

		WatchList watchList = new WatchList("Test", new WatchListColumn[0]);
	    watchList.setItems(new WatchListElement[] { element1, element2 });

		WatchListView part = new WatchListView();
        getField(WatchListView.class, "watchList").set(part, watchList);
        getField(WorkbenchPart.class, "partSite").set(part, new TestWorkbenchPartSite(shell));
		part.createPartControl(shell);

		assertEquals(2, part.getItemsMap().size());

	    watchList.setItems(new WatchListElement[] { element1 });

	    assertEquals(1, part.getItemsMap().size());
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

		public DataProviderFactory(String id, String name) {
	        this.id = id;
	        this.name = name;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#createProvider()
         */
        public IDataProvider createProvider() {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#getId()
         */
        public String getId() {
	        return id;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#getName()
         */
        public String getName() {
	        return name;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#getType()
         */
        @SuppressWarnings("unchecked")
        public Class[] getType() {
            return new Class[] { String.class };
        }
	}
}
