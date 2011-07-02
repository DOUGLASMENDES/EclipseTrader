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

package org.eclipsetrader.repository.local.internal.types;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.views.Column;
import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IDataProviderFactory;

@XmlRootElement(name = "column")
public class ColumnType {

    @XmlValue
    private String name;

    @XmlAttribute(name = "factory")
    @XmlJavaTypeAdapter(DataProviderFactoryAdapter.class)
    private IDataProviderFactory dataProviderFactory;

    public ColumnType() {
    }

    public ColumnType(IColumn column) {
        this.name = column.getName();
        this.dataProviderFactory = column.getDataProviderFactory();
    }

    public IColumn getElement() {
        return new Column(name != null && !name.equals("") ? name : null, dataProviderFactory);
    }
}
