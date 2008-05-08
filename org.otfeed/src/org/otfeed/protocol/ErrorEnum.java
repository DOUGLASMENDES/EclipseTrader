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

package org.otfeed.protocol;

/**
 * Enumerates well-known OpenTick error codes.
 */
public enum ErrorEnum {

	/** Incorrect login (username / password). */
	BAD_LOGIN 			(1001),

	/** You are not logged in. */
	NOT_LOGGED_IN 		(1002),

	/** Requested data does not exist. */
	NO_DATA 			(1003),

	/** Invalid cancelId. */
	INVALID_CANCEL_ID 	(1004),

	/** Invalid interval type or value of the request for historical data. */
	INVALID_INTERVAL 	(1005),

	/** You do not have a license to request real-time data from the specified exchange. */
	NO_LICENSE 			(1006),

	/** Your symbol limit is exceeded. */
	LIMIT_EXCEEDED 		(1007),

	/** You have requested this tick stream already. */
	DUPLICATE_REQUEST 	(1008),

	/** Your account is inactive. */
	INACTIVE_ACCOUNT 	(1009),

	/** You are logged in already. */
	LOGGED_IN 			(1010),

	/** Parameters of the request are incorrect. */
	BAD_REQUEST 		(1011),

	/** You are not subscribed to a historical data package. */
	NO_HIST_PACKAGE 	(1012),


	/**
	 * API errors.
	 */

	/** System error. */
	E_SYSTEM 			(2000),

	/** Server error. */
	E_SYSTEM_SERVER_ERROR (2002),

	/** Cannot connect error. */
	E_CANNOT_CONNECT (2003),

	/** Opentick error. */
	E_OPENTICK (3000),

	/* 
	 * org.otfeed errors 
	 */

	/** Otfeed client was shutdown (normal exit) */
	E_OTFEED_OK(5000),

	/** Otfeed client experienced an internal error */
	E_OTFEED_INTERNAL (5001),

	/** Request has been cancelled */
	E_OTFEED_CANCELLED (5002);

	public final int code;
	
	private ErrorEnum(int code) {
		this.code = code;
	}
}
