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

import org.otfeed.event.ICompletionDelegate;
import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTError;

/**
 * Common delegate implements all delegate intefaces
 * of the org.otfeed API. Hence, it can be passed
 * as <code>xxxDelegate</code> paramater to any command
 * object.
 * <p/>
 * This object delegates actual output of the data
 * to the {@link #getDataWriter() dataWriter} object, that defaults
 * to {@link SimpleDataWriter}.
 * <p/>
 * Arbitrary id string can be (optionally) associated with
 * this object, allowing to use multiple instancies of
 * this object with a single {@link #getDataWriter() dataWriter}. 
 * In this case id strings help to identify the source
 * of the record in the <code>dataWriter</code>.
 *
 * @param <T> delegate type (the type of event object).
 */
public class CommonDelegate<T> 
		implements IDataDelegate<T>, ICompletionDelegate {

	/**
	 * Creates new CommonDelegate, with no <code>id</code>,
	 * and <code>null</code> dataWriter.
	 *
	 */
	public CommonDelegate() { }
	
	/**
	 * Creates new CommonDelegate with the given 
	 * <code>id</code> string.
	 * 
	 * @param id arbitrary id string. Identifies
	 * this listener. 
	 */
	public CommonDelegate(String id) {
		setId(id);
	}
	
	/**
	 * Creates new CommonDelegate with the given id string, and
	 * given output destination.
	 * 
	 * @param id id string.
	 * @param writer output destination.
	 */
	public CommonDelegate(String id, IDataWriter writer) {
		setId(id);
		setDataWriter(writer);
	}

	/**
	 * Creates new CommonDelegate with no id, and given 
	 * output destination.
	 * 
	 * @param writer destination.
	 */
	public CommonDelegate(IDataWriter writer) {
		setDataWriter(writer);
	}

	private String id = null;
	
	/**
	 * Arbitrary string, identifying this listener.
	 * If set, it will be passed to the output writer.
	 * This allows to use a single output writer for multiple
	 * listener instencies, and still be able to distinguish
	 * which listener produced a particular record.
	 *
	 * @return id string.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Sets id string.
	 * 
	 * @param val id string.
	 */
	public void setId(String val) {
		id = val;
	}
	
	private IDataWriter writer = new SimpleDataWriter();
	
	/**
	 * Ouptut destination.
	 * 
	 * @return output writer.
	 */
	public IDataWriter getDataWriter() { 
		return writer; 
	}
	
	/**
	 * Sets output writer.
	 * 
	 * @param val writer.
	 */
	public void setDataWriter(IDataWriter val) {
		writer = val;
	}
	
	public void onData(T data) {
		writer.writeData(id, data);
	}

	public void onDataEnd(OTError error) {
		if(error != null) {
			System.err.println("ERROR: " + error);
		}
		writer.close();
	}
}
