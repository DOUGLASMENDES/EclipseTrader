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

package org.eclipsetrader.core.internal.trading;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "param")
public class ParameterElement {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "type")
    private String type;

    @XmlValue
    private String value;

    private static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private static NumberFormat numberFormat = NumberFormat.getInstance();

    public static ParameterElement create(String name, Object value) {
        if (value instanceof Date) {
            return new ParameterElement(name, Date.class.getName(), dateFormat.format(value));
        }
        if (value instanceof Number) {
            return new ParameterElement(name, value.getClass().getName(), numberFormat.format(value));
        }
        if (value instanceof Boolean) {
            return new ParameterElement(name, Boolean.class.getName(), Boolean.TRUE.equals(value) ? "true" : "false");
        }
        if (value instanceof Currency) {
            return new ParameterElement(name, Currency.class.getName(), ((Currency) value).getCurrencyCode());
        }
        return new ParameterElement(name, value.toString());
    }

    public static Object convert(ParameterElement property) {
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

    public ParameterElement() {
    }

    public ParameterElement(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public ParameterElement(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @XmlTransient
    public String getName() {
        return name;
    }

    @XmlTransient
    public String getValue() {
        return value;
    }

    @XmlTransient
    public String getType() {
        return type;
    }
}
