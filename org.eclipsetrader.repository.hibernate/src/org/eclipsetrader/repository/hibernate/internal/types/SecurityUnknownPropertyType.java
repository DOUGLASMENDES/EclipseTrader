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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.eclipsetrader.repository.hibernate.internal.stores.SecurityStore;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "securities_other_properties")
public class SecurityUnknownPropertyType {

    @Id
    @Column(name = "id", length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @SuppressWarnings("unused")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "value")
    private String value;

    @ManyToOne
    @SuppressWarnings("unused")
    private SecurityStore security;

    private static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private static NumberFormat numberFormat = NumberFormat.getInstance();

    public static SecurityUnknownPropertyType create(SecurityStore security, String name, Object value) {
        if (value instanceof Date) {
            return new SecurityUnknownPropertyType(security, name, Date.class.getName(), dateFormat.format(value));
        }
        if (value instanceof Number) {
            return new SecurityUnknownPropertyType(security, name, value.getClass().getName(), numberFormat.format(value));
        }
        if (value instanceof Boolean) {
            return new SecurityUnknownPropertyType(security, name, Boolean.class.getName(), Boolean.TRUE.equals(value) ? "true" : "false");
        }
        if (value instanceof Currency) {
            return new SecurityUnknownPropertyType(security, name, Currency.class.getName(), ((Currency) value).getCurrencyCode());
        }
        return new SecurityUnknownPropertyType(security, name, value.toString());
    }

    public static Object convert(SecurityUnknownPropertyType property) {
        try {
            if (Date.class.getName().equals(property.getType())) {
                return dateFormat.parse(property.getValue());
            }
            if (Double.class.getName().equals(property.getType())) {
                return numberFormat.parse(property.getValue()).doubleValue();
            }
            if (Float.class.getName().equals(property.getType())) {
                return numberFormat.parse(property.getValue()).floatValue();
            }
            if (Integer.class.getName().equals(property.getType())) {
                return numberFormat.parse(property.getValue()).intValue();
            }
            if (Long.class.getName().equals(property.getType())) {
                return numberFormat.parse(property.getValue()).longValue();
            }
            if (Byte.class.getName().equals(property.getType())) {
                return numberFormat.parse(property.getValue()).byteValue();
            }
            if (Short.class.getName().equals(property.getType())) {
                return numberFormat.parse(property.getValue()).shortValue();
            }
            if (Boolean.class.getName().equals(property.getType())) {
                return "true".equals(property.getValue());
            }
            if (Currency.class.getName().equals(property.getType())) {
                return Currency.getInstance(property.getValue());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return property.getValue();
    }

    public SecurityUnknownPropertyType() {
    }

    public SecurityUnknownPropertyType(SecurityStore security, String name, String value) {
        this.security = security;
        this.name = name;
        this.value = value;
    }

    public SecurityUnknownPropertyType(SecurityStore security, String name, String type, String value) {
        this.security = security;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}
