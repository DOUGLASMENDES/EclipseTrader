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

import java.io.Serializable;

/**
 * This class provides error information.
 */
public final class OTError implements Serializable {
	
	private static final long serialVersionUID = -4500092043902321750L;
	
	private int requestId;
    private int code;
    private String description;

    /**
     * Default constructor.
     */
    public OTError() { }

    /**
     *  Constructor.
     * @param requestId ID of the request which caused the error.
     * @param code 	Error code.
     * @param description Description of the error.
     */
    public OTError(int requestId, int code, String description) {
        this.requestId = requestId;
        this.code = code;
        this.description = description;
    }

    /**
     *
     * @return ID of the request which caused the error.
     */
    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    /**
     *
     * @return Error code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Sets error code.
     * @param code 	Error code.
     */
    public void setCode(int code) {
        this.code = code;
    }

     /**
     *
     * @return Description of the error.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description of the error.
     * @param description Description of the error.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
	public String toString() {
        return "OTError: requestId=" + requestId + ", code=" + code + ", desc=" + description;
    }
}