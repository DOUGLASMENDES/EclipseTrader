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

package org.eclipsetrader.yahoo.internal.news;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "category")
public class Category {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "handler")
    private String handler;

    @XmlElement(name = "page")
    private List<Page> pages;

    public Category() {
    }

    @XmlTransient
    public String getName() {
        return name;
    }

    @XmlTransient
    public String getId() {
        return id;
    }

    @XmlTransient
    public String getHandler() {
        return handler;
    }

    @XmlTransient
    public Page[] getPages() {
        if (pages != null) {
            return pages.toArray(new Page[pages.size()]);
        }
        return new Page[0];
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name;
    }
}
