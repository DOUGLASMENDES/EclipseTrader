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

import junit.framework.TestCase;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipsetrader.core.views.Column;
import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;

public class TablePresentationTest extends TestCase {
	private Shell shell;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		Display display = Display.getCurrent();
		shell = new Shell(display);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		shell.dispose();
	}

	public void testCreatePresentation() throws Exception {
		TablePresentation presentation = new TablePresentation(shell, null);
		assertEquals(0, presentation.getTable().getColumnCount());
		assertEquals(0, presentation.getTable().getItemCount());
    }

	public void testCreateColumns() throws Exception {
		TablePresentation presentation = new TablePresentation(shell, null);
		IColumn[] columns = new IColumn[] {
				new Column("Column1", new IDataProviderFactory() {
	                public IDataProvider createProvider() {
		                return null;
	                }

	                public String getId() {
		                return "test.id1";
	                }

	                public String getName() {
		                return "Test1";
	                }

	                @SuppressWarnings("unchecked")
	                public Class[] getType() {
		                return new Class[] { String.class };
	                }
				}),
			};
		presentation.updateColumns(columns);
		assertEquals(1, presentation.getTable().getColumnCount());
		assertEquals("Column1", presentation.getTable().getColumn(0).getText());
		assertSame(columns[0], presentation.getTable().getColumn(0).getData());
    }

	public void testAddColumns() throws Exception {
		TablePresentation presentation = new TablePresentation(shell, null);
		IColumn[] columns = new IColumn[] {
				new Column("Column1", new IDataProviderFactory() {
	                public IDataProvider createProvider() {
		                return null;
	                }

	                public String getId() {
		                return "test.id1";
	                }

	                public String getName() {
		                return "Test1";
	                }

	                @SuppressWarnings("unchecked")
	                public Class[] getType() {
		                return new Class[] { String.class };
	                }
				}),
			};
		presentation.updateColumns(columns);
		assertEquals(1, presentation.getTable().getColumnCount());
		columns = new IColumn[] {
				new Column("Column1", new IDataProviderFactory() {
	                public IDataProvider createProvider() {
		                return null;
	                }

	                public String getId() {
		                return "test.id1";
	                }

	                public String getName() {
		                return "Test1";
	                }

	                @SuppressWarnings("unchecked")
	                public Class[] getType() {
		                return new Class[] { String.class };
	                }
				}),
				new Column("Column2", new IDataProviderFactory() {
	                public IDataProvider createProvider() {
		                return null;
	                }

	                public String getId() {
		                return "test.id2";
	                }

	                public String getName() {
		                return "Test2";
	                }

	                @SuppressWarnings("unchecked")
	                public Class[] getType() {
		                return new Class[] { String.class };
	                }
				}),
			};
		presentation.updateColumns(columns);
		assertEquals(2, presentation.getTable().getColumnCount());
		assertEquals("Column1", presentation.getTable().getColumn(0).getText());
		assertSame(columns[0], presentation.getTable().getColumn(0).getData());
		assertEquals("Column2", presentation.getTable().getColumn(1).getText());
		assertSame(columns[1], presentation.getTable().getColumn(1).getData());
    }

	public void testRemoveColumns() throws Exception {
		TablePresentation presentation = new TablePresentation(shell, null);
		IColumn[] columns = new IColumn[] {
				new Column("Column1", new IDataProviderFactory() {
	                public IDataProvider createProvider() {
		                return null;
	                }

	                public String getId() {
		                return "test.id1";
	                }

	                public String getName() {
		                return "Test1";
	                }

	                @SuppressWarnings("unchecked")
	                public Class[] getType() {
		                return new Class[] { String.class };
	                }
				}),
				new Column("Column2", new IDataProviderFactory() {
	                public IDataProvider createProvider() {
		                return null;
	                }

	                public String getId() {
		                return "test.id2";
	                }

	                public String getName() {
		                return "Test2";
	                }

	                @SuppressWarnings("unchecked")
	                public Class[] getType() {
		                return new Class[] { String.class };
	                }
				}),
			};
		presentation.updateColumns(columns);
		assertEquals(2, presentation.getTable().getColumnCount());
		columns = new IColumn[] {
				new Column("Column1", new IDataProviderFactory() {
	                public IDataProvider createProvider() {
		                return null;
	                }

	                public String getId() {
		                return "test.id1";
	                }

	                public String getName() {
		                return "Test1";
	                }

	                @SuppressWarnings("unchecked")
	                public Class[] getType() {
		                return new Class[] { String.class };
	                }
				}),
			};
		presentation.updateColumns(columns);
		assertEquals(1, presentation.getTable().getColumnCount());
		assertEquals("Column1", presentation.getTable().getColumn(0).getText());
		assertSame(columns[0], presentation.getTable().getColumn(0).getData());
    }

	public void testCompareDoubleValues() throws Exception {
	    IAdaptable[] v1 = new IAdaptable[] {
	    		new IAdaptable() {
	                @SuppressWarnings("unchecked")
                    public Object getAdapter(Class adapter) {
	                	if (adapter.isAssignableFrom(Double.class))
	                		return new Double(10.5);
	                    return null;
                    }
	    		},
	    	};
	    IAdaptable[] v2 = new IAdaptable[] {
	    		new IAdaptable() {
	                @SuppressWarnings("unchecked")
                    public Object getAdapter(Class adapter) {
	                	if (adapter.isAssignableFrom(Double.class))
	                		return new Double(10.6);
	                    return null;
                    }
	    		},
	    	};
		TablePresentation presentation = new TablePresentation(shell, null);
	    assertEquals(-1, presentation.compareValues(v1, v2, 0));
    }

	public void testCompareLongValues() throws Exception {
	    IAdaptable[] v1 = new IAdaptable[] {
	    		new IAdaptable() {
	                @SuppressWarnings("unchecked")
                    public Object getAdapter(Class adapter) {
	                	if (adapter.isAssignableFrom(Long.class))
	                		return new Long(100000);
	                    return null;
                    }
	    		},
	    	};
	    IAdaptable[] v2 = new IAdaptable[] {
	    		new IAdaptable() {
	                @SuppressWarnings("unchecked")
                    public Object getAdapter(Class adapter) {
	                	if (adapter.isAssignableFrom(Long.class))
	                		return new Long(110000);
	                    return null;
                    }
	    		},
	    	};
		TablePresentation presentation = new TablePresentation(shell, null);
	    assertEquals(-1, presentation.compareValues(v1, v2, 0));
    }

	public void testCompareStringValues() throws Exception {
	    IAdaptable[] v1 = new IAdaptable[] {
	    		new IAdaptable() {
	                @SuppressWarnings("unchecked")
                    public Object getAdapter(Class adapter) {
	                	if (adapter.isAssignableFrom(String.class))
	                		return "A";
	                    return null;
                    }
	    		},
	    	};
	    IAdaptable[] v2 = new IAdaptable[] {
	    		new IAdaptable() {
	                @SuppressWarnings("unchecked")
                    public Object getAdapter(Class adapter) {
	                	if (adapter.isAssignableFrom(String.class))
	                		return "B";
	                    return null;
                    }
	    		},
	    	};
		TablePresentation presentation = new TablePresentation(shell, null);
	    assertEquals(-1, presentation.compareValues(v1, v2, 0));
    }

	public void testCompareValueWithNull() throws Exception {
	    IAdaptable[] v1 = new IAdaptable[] {
	    		new IAdaptable() {
	                @SuppressWarnings("unchecked")
                    public Object getAdapter(Class adapter) {
	                	if (adapter.isAssignableFrom(Double.class))
	                		return new Double(10.5);
	                    return null;
                    }
	    		},
	    	};
	    IAdaptable[] v2 = new IAdaptable[] { null };
		TablePresentation presentation = new TablePresentation(shell, null);
	    assertEquals(0, presentation.compareValues(v1, v2, 0));
    }

	public void testCompareNullWithValue() throws Exception {
	    IAdaptable[] v1 = new IAdaptable[] { null };
	    IAdaptable[] v2 = new IAdaptable[] {
	    		new IAdaptable() {
	                @SuppressWarnings("unchecked")
                    public Object getAdapter(Class adapter) {
	                	if (adapter.isAssignableFrom(Double.class))
	                		return new Double(10.5);
	                    return null;
                    }
	    		},
	    	};
		TablePresentation presentation = new TablePresentation(shell, null);
	    assertEquals(0, presentation.compareValues(v1, v2, 0));
    }

	public void testCompareNullValues() throws Exception {
	    IAdaptable[] v1 = new IAdaptable[] { null };
	    IAdaptable[] v2 = new IAdaptable[] { null };
		TablePresentation presentation = new TablePresentation(shell, null);
	    assertEquals(0, presentation.compareValues(v1, v2, 0));
    }
}
