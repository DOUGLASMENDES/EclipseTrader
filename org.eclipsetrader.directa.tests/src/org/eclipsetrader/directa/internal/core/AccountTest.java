/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.directa.internal.core;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.eclipsetrader.core.trading.IPositionListener;
import org.eclipsetrader.core.trading.PositionEvent;

public class AccountTest extends TestCase {

	public void testSetPositions() throws Exception {
		Account account = new Account("ID", null);

		Position position = new Position("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;");
		account.setPositions(new Position[] {
			position
		});

		assertEquals(1, account.positions.size());
		assertSame(position, account.positions.get(0));
	}

	public void testReplacePositions() throws Exception {
		Account account = new Account("ID", null);

		Position position1 = new Position("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;");
		account.setPositions(new Position[] {
			position1
		});

		Position position2 = new Position("4;7;0;;  ;UCG;I;;;200;1.8300;;;;;;;;;;;;015;A;H;;;");
		account.setPositions(new Position[] {
			position2
		});

		assertEquals(1, account.positions.size());
		assertSame(position2, account.positions.get(0));
	}

	public void testRemovePositions() throws Exception {
		Account account = new Account("ID", null);
		account.setPositions(new Position[] {
			new Position("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;")
		});

		account.setPositions(new Position[0]);

		assertEquals(0, account.positions.size());
	}

	public void testFirePositionOpenEvent() throws Exception {
		IPositionListener listener = EasyMock.createMock(IPositionListener.class);
		listener.positionOpened(EasyMock.isA(PositionEvent.class));
		EasyMock.replay(listener);

		Account account = new Account("ID", null);
		account.addPositionListener(listener);

		account.setPositions(new Position[] {
			new Position("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;")
		});

		EasyMock.verify(listener);
	}

	public void testFirePositionUpdateEvent() throws Exception {
		Account account = new Account("ID", null);
		account.setPositions(new Position[] {
			new Position("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;")
		});

		IPositionListener listener = EasyMock.createMock(IPositionListener.class);
		listener.positionChanged(EasyMock.isA(PositionEvent.class));
		EasyMock.replay(listener);

		account.addPositionListener(listener);
		account.setPositions(new Position[] {
			new Position("4;7;0;;  ;UCG;I;;;200;1.8300;;;;;;;;;;;;015;A;H;;;")
		});

		EasyMock.verify(listener);
	}

	public void testFirePositionCloseEvent() throws Exception {
		Account account = new Account("ID", null);
		account.setPositions(new Position[] {
			new Position("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;")
		});

		IPositionListener listener = EasyMock.createMock(IPositionListener.class);
		listener.positionClosed(EasyMock.isA(PositionEvent.class));
		EasyMock.replay(listener);

		account.addPositionListener(listener);
		account.setPositions(new Position[0]);

		EasyMock.verify(listener);
	}
}
