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

package org.eclipsetrader.directa.internal.core.messages;

public class AstaApertura extends DataMessage {
	public double val_aper;
	public long ora_aper;
	public long qta_aper;
	public long num_contr;
	public int num_aper;

	public AstaApertura(byte[] arr, int i, int decade) {
		val_aper = Util.getFloat(arr, i);
		i += 4;
		ora_aper = Util.getDataOra(arr, i, decade);
		i += 4;
		qta_aper = Util.getUlong(arr, i);
		i += 4;
		num_contr = Util.getUlong(arr, i);
		i += 4;
		num_aper = Util.getUInt(arr, i);
	}
}
