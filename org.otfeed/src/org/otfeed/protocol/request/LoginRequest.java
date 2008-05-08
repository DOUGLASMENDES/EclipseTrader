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

package org.otfeed.protocol.request;


import org.otfeed.event.OTError;
import org.otfeed.protocol.CommandEnum;
import org.otfeed.protocol.VersionEnum;
import org.otfeed.protocol.OSEnum;
import org.otfeed.protocol.PlatformEnum;

import java.nio.ByteBuffer;

/**
 * Request to login. Its the only one that does not have session part.
 */
public class LoginRequest extends AbstractRequest {

	private final String username;
	private final String password;

	public String getUsername() { return username; }
	public String getPassword() { return password; }

	/**
	 * Represents response to the login request.
	 * Could be either re-direct or success.
	 */
	public LoginRequest(int requestId,
			String u,
			String p) {

		super(CommandEnum.LOGIN, requestId, null);

		Check.notNull(u,  "username");
		Check.notNull(p,  "password");

		username = u;
		password = p;
	}

	public static class Response {

		public final String sessionId;
		public final boolean redirectFlag;
		public final String redirectHost;
		public final int redirectPort;

		public Response(ByteBuffer in) {
            		sessionId = Util.readString(in, 64);
            		redirectFlag = Util.readBoolean(in);
			redirectHost = Util.readString(in, 64);
            		redirectPort = in.getShort();
		}

		@Override
		public String toString() {
			return "Response: sessionId=" + sessionId
			+ ", redirectFlag=" + redirectFlag
			+ ", redirectHost=" + redirectHost
			+ ", redirectPort=" + redirectPort;
		}
	}

	@Override
	public void writeRequest(ByteBuffer out) {
		super.writeRequest(out);

		out.putShort((short) VersionEnum.CURRENT_VERSION.code);
		out.put((byte) OSEnum.UNKNOWN.code);
		out.put((byte) PlatformEnum.JAVA.code);
		Util.writeString(out, "", 16); // platform password .. skip
		out.put(new byte[6]);  // mac address... skip for now
		Util.writeString(out, username, 64);
		Util.writeString(out, password, 64);
	}

	public void handleError(OTError error) {
	}
}
