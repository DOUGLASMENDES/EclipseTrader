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

package org.eclipsetrader.repository.hibernate.internal.ui;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class DatabaseElement {

    private String label;
    private Image icon;
    private String driver;
    private String dialect;

    private String schema;
    private String url;

    public DatabaseElement(IConfigurationElement element) {
        label = element.getAttribute("name");
        driver = element.getAttribute("driver_class");
        dialect = element.getAttribute("dialect");

        schema = element.getAttribute("schema");
        url = element.getAttribute("url");

        if (!"".equals(element.getAttribute("icon"))) {
            String pluginId = element.getContributor().getName();
            ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, element.getAttribute("icon"));
            icon = imageDescriptor.createImage();
        }
    }

    public String getDriver() {
        return driver;
    }

    public String getDialect() {
        return dialect;
    }

    public String getLabel() {
        return label;
    }

    public Image getIcon() {
        return icon;
    }

    public String getSchema() {
        return schema;
    }

    public String getUrl() {
        return url;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return label;
    }
}
