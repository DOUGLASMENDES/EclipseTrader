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
 *   
 *   Derived from code developed by Opentick Corporation, http://www.opentick.com.
 */

package org.otfeed.event;

import static org.otfeed.event.IdentityUtil.equalsTo;
import static org.otfeed.event.IdentityUtil.safeCompare;
import static org.otfeed.event.IdentityUtil.safeHashCode;

import java.io.Serializable;

/**
 * Encapsulates hostname and port pair.
 */
public final class OTHost implements Comparable<OTHost>, Serializable {

	private static final long serialVersionUID = -5913623386353359260L;

	private String host;
	private int port;

	/** 
	 * Default constructor
	 */
	public OTHost() { }

	/**
	 * Constructor with parameter initialization
	 *
	 * @param host server host
	 * @param port server port
	 */
	public OTHost(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	/**
	 * Constructor that parses string in the form "host:port"
	 * 
	 * @param address
	 */
	public OTHost(String address) {
		int index = address.indexOf(':');
		if(index < 0) {
			throw new IllegalArgumentException("address string parsing error: colon not find");
		}
		this.host = address.substring(0, index);
		this.port = Integer.parseInt(address.substring(index + 1));
	}

	/**
	 * Sets host name.
	 *
	 * @param host host name.
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Returns host value.
	 *
	 * @return host value.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Sets port value;
	 *
	 * @param port port value.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Returns port value.
	 *
	 * @return port value.
	 */
	public int getPort() {
		return port;
	}

	@Override
	public String toString() {
		return host + ":" + port;
	}

    @Override
	public int hashCode() {
        return safeHashCode(host) 
    		+ 3 * safeHashCode(port); 
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTHost other) {
		int rc;

		if((rc = safeCompare(host, other.host)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(port, other.port)) != 0) {
			return rc;
		}
		
		return 0;
	}
}