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

package org.eclipsetrader.repository.hibernate.internal.types;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.eclipsetrader.core.instruments.IUserProperty;
import org.eclipsetrader.core.instruments.UserProperty;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "user_properties")
public class UserPropertyType {

    @Id
    @Column(name = "id", length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @SuppressWarnings("unused")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "value")
    private String defaultValue;

    @Column(name = "required")
    private boolean required;

    public UserPropertyType() {
    }

    public UserPropertyType(IUserProperty userProperty) {
        this.name = userProperty.getName();
        this.defaultValue = userProperty.getDefaultValue();
        this.required = userProperty.isRequired();
    }

    public IUserProperty getProperty() {
        return new UserProperty(name, required, defaultValue);
    }
}
