/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.charts;

import org.eclipse.swt.graphics.Point;

public class PixelTools {

	/**
	 * Determine if the point (x,y) lies on the polyline defined by the given pointArray parameter.
	 *
	 * @param x - the reference point x position
	 * @param y - the reference point y position
	 * @param pointArray - the polyline array of (x,y) values
	 * @return true if the reference point is on the line, false otherwise
	 */
	public static boolean isPointOnLine(int x, int y, int[] pointArray) {
		for (int i = 3; i < pointArray.length; i += 2) {
			if (isPointOnLine(x, y, pointArray[i - 3], pointArray[i - 2], pointArray[i - 1], pointArray[i]))
				return true;
		}
		return false;
	}

	/**
	 * Determine if the point (x,y) lies on the polyline defined by the given pointArray parameter.
	 *
	 * @param x - the reference point x position
	 * @param y - the reference point y position
	 * @param pointArray - the polyline array of (x,y) values
	 * @return true if the reference point is on the line, false otherwise
	 */
	public static boolean isPointOnLine(int x, int y, Point[] pointArray) {
		for (int i = 1; i < pointArray.length; i++) {
			if (isPointOnLine(x, y, pointArray[i - 1].x, pointArray[i - 1].y, pointArray[i].x, pointArray[i].y))
				return true;
		}
		return false;
	}

	/**
	 * Determine if the point (x,y) lies on the line (x1,y1)-(x2,y2).
	 * <p>This is the Bresenham's Algorithm for approximating lines, modified to compare the calculated point
	 * against the reference point instead of drawing. This may not be the most efficent way for doing this
	 * job but it works and it is very precise.</p>
	 *
	 * @param x - the reference point x position
	 * @param y - the reference point y position
	 * @param x1 - the starting point x position
	 * @param y1 - the starting point y position
	 * @param x2 - the ending point x position
	 * @param y2 - the ending point y position
	 * @return true if the reference point is on the line, false otherwise
	 */
	public static boolean isPointOnLine(int x, int y, int x1, int y1, int x2, int y2) {
		int d; /* Decision variable */
		int dx, dy; /* Dx and Dy values for the line */
		int Eincr, NEincr; /* Decision variable increments */
		int yincr; /* Increment for y values */
		int tmp; /* Counters etc. */

		dx = Math.abs(x2 - x1);
		dy = Math.abs(y2 - y1);
		if (dy <= dx) {
			/*
			 * We have a line with slope (|1/2| >= slope) (ie: includes horizontal
			 * lines). Ensure that we are always scan converting the line from left to
			 * right to ensure that we produce the same line from P1 to P0 as the line
			 * from P0 to P1.
			 */
			if (x2 < x1) {
				tmp = x2;
				x2 = x1;
				x1 = tmp; /* Swap X coordinates */
				tmp = y2;
				y2 = y1;
				y1 = tmp; /* Swap Y coordinates */
			}
			if (y2 > y1)
				yincr = 1;
			else
				yincr = -1;
			d = 2 * dy - dx; /* Initial decision variable value */
			Eincr = 2 * dy; /* Increment to move to E pixel */
			NEincr = 2 * (dy - dx); /* Increment to move to NE pixel */

			/* Draw the first point at (x1,y1) */
			if (Math.abs(x - x1) <= 1 && Math.abs(y - y1) <= 1)
				return true;
			// putPixel(x1,y1,color);

			/* Incrementally determine the positions of the remaining pixels */
			for (x1++; x1 <= x2; x1++) {
				if (d < 0)
					d += Eincr; /* Choose the Eastern Pixel */
				else {
					d += NEincr; /* Choose the North Eastern Pixel */
					y1 += yincr; /* (or SE pixel for dx/dy < 0!) */
				}

				/* Draw the point */
				if (Math.abs(x - x1) <= 1 && Math.abs(y - y1) <= 1)
					return true;
				// putPixel(x1,y1,color);
			}
		}
		else {
			/*
			 * We have a line with slope (|1/2| < slope) (ie: includes vertical
			 * lines). We must swap our x and y coordinates for this. Ensure that we
			 * are always scan converting the line from left to right to ensure that
			 * we produce the same line from P1 to P0 as the line from P0 to P1.
			 */
			if (y2 < y1) {
				tmp = x2;
				x2 = x1;
				x1 = tmp; /* Swap X coordinates */
				tmp = y2;
				y2 = y1;
				y1 = tmp; /* Swap Y coordinates */
			}
			if (x2 > x1)
				yincr = 1;
			else
				yincr = -1;
			d = 2 * dx - dy; /* Initial decision variable value */
			Eincr = 2 * dx; /* Increment to move to E pixel */
			NEincr = 2 * (dx - dy); /* Increment to move to NE pixel */

			/* Draw the first point at (x1,y1) */
			if (Math.abs(x - x1) <= 1 && Math.abs(y - y1) <= 1)
				return true;
			// putPixel(x1,y1,color);

			/* Incrementally determine the positions of the remaining pixels */
			for (y1++; y1 <= y2; y1++) {
				if (d < 0)
					d += Eincr; /* Choose the Eastern Pixel */
				else {
					d += NEincr; /* Choose the North Eastern Pixel */
					x1 += yincr; /* (or SE pixel for dx/dy < 0!) */
				}

				/* Draw the point */
				if (Math.abs(x - x1) <= 1 && Math.abs(y - y1) <= 1)
					return true;
				// putPixel(x1,y1,color);
			}
		}

		return false;
	}
}
