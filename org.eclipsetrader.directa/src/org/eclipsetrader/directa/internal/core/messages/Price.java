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

public class Price extends DataMessage {
	public double val_ult;
	public long ora_ult;
	public long qta_ult;
	public double val_prgs;
	public long qta_prgs;
	public double min;
	public double max;
	public long num_contr;

	public Price(byte[] arr, int i, int decade) {
		val_ult = Util.getFloat(arr, i);
		i += 4;
		ora_ult = Util.getDataOra(arr, i, decade);
		i += 4;
		qta_ult = Util.getUlong(arr, i);
		i += 4;
		val_prgs = Util.getFloat(arr, i);
		i += 4;
		qta_prgs = Util.getUlong(arr, i);
		i += 4;
		min = Util.getFloat(arr, i);
		i += 4;
		max = Util.getFloat(arr, i);
		i += 4;
		num_contr = Util.getUlong(arr, i);
	}
}
