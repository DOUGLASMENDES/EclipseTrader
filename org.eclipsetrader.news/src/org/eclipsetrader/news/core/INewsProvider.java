/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.news.core;

/**
 * Interface for news data provider plugins.
 *
 * @since 1.0
 */
public interface INewsProvider {

    /**
     * Gets the unique plugin id.
     *
     * @return the plugin id.
     */
    public String getId();

    /**
     * Gets the broker's display name.
     *
     * @return the name.
     */
    public String getName();

    /**
     * Gets a possibly empty array of headlines.
     *
     * @return the headlines.
     */
    public IHeadLine[] getHeadLines();

    /**
     * Starts the news feed.
     */
    public void start();

    /**
     * Stops the news feed.
     */
    public void stop();

    /**
     * Force an immediate update.
     */
    public void refresh();
}
