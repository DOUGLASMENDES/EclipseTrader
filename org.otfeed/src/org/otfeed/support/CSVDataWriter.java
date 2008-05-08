/**
 * Copyright 2007 Mike Kroutikov.
 *
 * This program is free software; you can redistribute it and/or modify
 *   it under the terms of the Lesser GNU General Public License as 
 *   published by the Free Software Foundation; either version 3 of
 *   the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   Lesser GNU General Public License for more details.
 *
 *   You should have received a copy of the Lesser GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.otfeed.support;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * Class that provides CSV formatting for Java POJO beans.
 * <p/>
 * Simplest usage is:
 * <pre>
 * IDataWriter writer = new CSVDataWriter(OTTrade.class);
 * </pre>
 * which creates a writer to output 
 * {@link org.otfeed.event.OTTrade} properties in a CSV 
 * format. Note that only objects of one class (used in constructor)
 * can be written. Attempts to write a different object will
 * yield a runtime exception.
 * <p/>
 * More advanced usage is:
 * <pre>
 * List<String> propertiesList = new LinkedList<String>();
 * propertiesList.add("timestamp");
 * propertiesList.add("openPrice");
 * propertiesList.add("closePrice");
 * propertiesList.add("volume");
 * IDataWriter writer = new CSVDatWriter(OOHLC.class, propertiesList);
 * </pre>
 * which creates a writer that outputs only listed properties of 
 * {@link org.otfeed.event.OTOHLC} object.
 * <p/>
 * Typically, this object will be used in conjunction with
 * {@link org.otfeed.support.CommonDelegate CommonListener} or another class that implements an appropriate
 * event listener using {@link IDataWriter} as the event sink.
 */
public class CSVDataWriter implements IDataWriter {
	
	private static List<Prop> buildPropertyList(Class<?> cls, List<String> customPropertyList) {

		Map<String,Prop> allProperties = introspect(cls);
		
		List<Prop> prop = new LinkedList<Prop>();
		
		if(customPropertyList != null) {
			// have custom list: include only properties
			// listed, in the order they are listed
			if(customPropertyList.size()== 0) {
				throw new IllegalArgumentException("invalid property list: must contain at least one property name: [" + customPropertyList + "]");
			}
			for(String name : customPropertyList) {
				if(name.length() == 0) {
					throw new IllegalArgumentException("bad property name (can not have zero length!)");
				}
				Prop p = allProperties.get(name);
				if(p == null) {
					throw new IllegalArgumentException("requested property [" + name + "] not found. Following properties are available: " + allProperties.keySet());
				}
				prop.add(p);
			}
		} else {
			// add all properties (in alphabetical order)
			for(String name : allProperties.keySet()) {
				prop.add(allProperties.get(name));
			}
		}
		
		return prop;
	}
	
	/**
	 * Creates new CSVDataWriter to write listed properties of
	 * a given class.
	 * 
	 * @param cls type of the objects to be written.
	 * @param list list of property names. This is useful
	 *      if you want to get control over which properties
	 *      are included. It allows to skip some properties,
	 *      specify the exact order of properties in the CSV 
	 *      line, or output a single property more than once.
	 */
	public CSVDataWriter(Class<?> cls, List<String> list) {
		dataClass = cls;
		propList = buildPropertyList(cls, list);
	}
	
	/**
	 * Creates new CSVDataWriter to write objects of
	 * a given class. Order of the properties is not well-defined
	 * (actually depends on the JVM implementation, apparently).
	 * Therefore, it switching {@link #isHeaders() headers} property
	 * to OFF is not recommended.
	 * <p/>
	 * If you need full control over which properties are written out,
	 * and in what order, use {@link #CSVDataWriter(Class, List)}
	 * constructor.
	 * 
	 * @param cls type of the objects to be written.
	 */
	public CSVDataWriter(Class<?> cls) {
		this(cls, null);
	}

	private String delimeter = ", ";
	
	/**
	 * Delimeter, used to separate properties.
	 * <p/>
	 * Default value is ", ". Re-set this is you
	 * do not want blank character to follow comma.
	 * 
	 * @return delimeter string.
	 */
	public String getDelimeter() { 
		return delimeter; 
	}
	
	/**
	 * Sets delimeter.
	 * 
	 * @param val delimeter string.
	 */
	public void setDelimeter(String val) { 
		delimeter = val;
	}

	private Map<Class<?>,IFormat<Object>> customFormatters
		= new HashMap<Class<?>,IFormat<Object>>();

	/**
	 * Allows to customize how properties are being formatted.
	 * One particularly useful case is properties of
	 * java.util.Date type.
	 * <p/>
	 * Following code illustrates use of custom property 
	 * format:
	 * <pre>
	 * CSVDataWriter writer = ...;
	 * writer.getCustomPropertyFormatter().put(Date.class, new DateFormat("MM/dd/yyyy"));
	 * </pre>
	 *  
	 * @return map of custom formatters.
	 */
	public Map<Class<?>,IFormat<Object>> getCustomPropertyFormatter() {
		return customFormatters;
	}

	/**
	 * Sets dictionary of custom property formatters.
	 * 
	 * @param val formatters dictionary.
	 */
	public void setCustomPropertyFormatter(Map<Class<?>,IFormat<Object>> val) {
		customFormatters = val;
	}
	
	
	private boolean needHeaders = true;

	/**
	 * Determines whether CVS output strats with list of properties.
	 * Default value is <code>true</code>.
	 * 
	 * @return headers flag.
	 */
	public boolean isHeaders() {
		return needHeaders;
	}
	
	/**
	 * Sets headers flag.
	 * 
	 * @param val headers flag value.
	 */
	public void setHeaders(Boolean val) {
		needHeaders = val;
	}
	
	private PrintWriter out = new PrintWriter(System.out, true);
	/**
	 * Determines the output destination.
	 * Default destination is System.out.
	 * 
	 * @return output destination.
	 */
	public PrintWriter getPrintWriter() {
		return out;
	}
	
	/**
	 * Sets output destination.
	 * 
	 * @param val output destination.
	 */
	public void setPrintWriter(PrintWriter val) {
		out = val;
	}

	private static class Prop {
		public final String name;
		public final Method method;

		private Prop(String name, Method method) {
			this.name = name;
			this.method = method;
		}
	}

	private Class<?> dataClass;
	private List<Prop> propList;
	
	private static String normalizeName(String name) {
		if(name.startsWith("get")) {
			name = name.substring(3);
		} else if(name.startsWith("is")) {
			name = name.substring(2);
		}

		if(name.length() > 1 && Character.isLowerCase(name.charAt(1))) {
			return name.substring(0, 1).toLowerCase() + name.substring(1);
		} else {
			return name;
		}
	}

	private static boolean isReadableProperty(Method method) {
		String name = method.getName();

		if(method.getParameterTypes().length > 0) return false;

		if(name.length() > 3 && name.startsWith("get")) {
			return true;
		} else if(name.length() > 2 && name.startsWith("is")) {
			// FIXME: check for boolean class on output??
			return true;
		}

		return false;
	}

	/**
	 * Returns dictionary of named readable properties.
	 * 
	 * @param cls object class.
	 * @return dictinary of named properties.
	 */
	private static Map<String,Prop> introspect(Class<?> cls) {

		Method[] method = cls.getDeclaredMethods();

		Map<String,Prop> map = new TreeMap<String,Prop>();
		for(int i = 0; i < method.length; i++) {
			if(!isReadableProperty(method[i])) continue;

			String name = normalizeName(method[i].getName());
			map.put(name, new Prop(name, method[i]));
		}

		return map;
	}
	
	private String header(Class<?> cls, List<Prop> prop, String delimeter) {

		StringBuffer out = new StringBuffer();
		for(Prop p : prop) {
			if(out.length() > 0) out.append(delimeter);
			out.append(p.name);
		}

		return out.toString();
	}

	private String format(Object obj, List<Prop> prop, String delimeter) {
		StringBuffer out = new StringBuffer();
		for(Prop p : prop) {
			if(out.length() > 0) out.append(delimeter);
			try {
				Object val = p.method.invoke(obj);
				IFormat<Object> fmt = customFormatters.get(val.getClass());
				if(fmt != null) {
					out.append(fmt.format(val));
				} else {
					out.append(val);
				}
			} catch(Exception ex) {
				throw new AssertionError();
			}
		}

		return out.toString();
	}

	private boolean doneHeaders = false;
	
	public void writeData(String id, Object data) {
		
		if(data == null) {
			throw new NullPointerException("null data not allowed");
		}

		if(!data.getClass().isAssignableFrom(dataClass)) {
			throw new IllegalStateException("incompatible object type: expected " + dataClass + ", received " + data.getClass());
		}

		if(needHeaders && !doneHeaders) {
			doneHeaders = true;
			if(id != null) out.print("id" + delimeter);
			out.println(header(data.getClass(), propList, delimeter));
		}

		if(id != null) out.print(id + delimeter);
		out.println(format(data, propList, delimeter));
	}
	
	/**
	 * Closes the writer stream.
	 */
	public void close() {
		out.flush(); // FIXME: maybe we should close() it?
	}
}
