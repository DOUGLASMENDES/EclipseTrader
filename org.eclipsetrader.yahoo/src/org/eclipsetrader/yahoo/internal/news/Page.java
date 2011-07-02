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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "page")
public class Page {

    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "url")
    private String url;

    @XmlAttribute(name = "id")
    private String id;

    @XmlTransient
    private Category parent;

    public Page() {
    }

    @XmlTransient
    public String getTitle() {
        return title;
    }

    @XmlTransient
    public String getUrl() {
        return url;
    }

    @XmlTransient
    public String getId() {
        return id;
    }

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        this.parent = (Category) parent;
    }

    @XmlTransient
    public Category getParent() {
        return parent;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return title;
    }
}
