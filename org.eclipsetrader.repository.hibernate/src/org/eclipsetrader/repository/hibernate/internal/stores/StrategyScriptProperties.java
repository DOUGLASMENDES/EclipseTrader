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

package org.eclipsetrader.repository.hibernate.internal.stores;

import java.net.URI;
import java.net.URISyntaxException;
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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.IScript;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.repository.hibernate.internal.Activator;
import org.eclipsetrader.repository.hibernate.internal.types.FailsafeScript;
import org.eclipsetrader.repository.hibernate.internal.types.FailsafeSecurity;
import org.hibernate.HibernateException;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@Entity
@Table(name = "strategies_properties")
public class StrategyScriptProperties {

    private static IRepositoryService repositoryService;

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
    @Index(name = "strategies_id_fkey")
    @SuppressWarnings("unused")
    private StrategyScriptStore parent;

    private static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private static NumberFormat numberFormat = NumberFormat.getInstance();

    public static StrategyScriptProperties create(String name, Object value) {
        if (value instanceof Date) {
            return new StrategyScriptProperties(name, Date.class.getName(), dateFormat.format(value));
        }
        if (value instanceof Number) {
            return new StrategyScriptProperties(name, value.getClass().getName(), numberFormat.format(value));
        }
        if (value instanceof Boolean) {
            return new StrategyScriptProperties(name, Boolean.class.getName(), Boolean.TRUE.equals(value) ? "true" : "false");
        }
        if (value instanceof Currency) {
            return new StrategyScriptProperties(name, Currency.class.getName(), ((Currency) value).getCurrencyCode());
        }
        if (value instanceof TimeSpan) {
            return new StrategyScriptProperties(name, TimeSpan.class.getName(), ((TimeSpan) value).toString());
        }
        if (value instanceof ISecurity) {
            IStoreObject store = (IStoreObject) ((ISecurity) value).getAdapter(IStoreObject.class);
            return new StrategyScriptProperties(name, ISecurity.class.getName(), store.getStore().toURI().toString());
        }
        if (value instanceof IScript) {
            IStoreObject store = (IStoreObject) ((IScript) value).getAdapter(IStoreObject.class);
            return new StrategyScriptProperties(name, IScript.class.getName(), store.getStore().toURI().toString());
        }
        return new StrategyScriptProperties(name, value.toString());
    }

    @SuppressWarnings({
        "rawtypes", "unchecked"
    })
    public static Object convert(StrategyScriptProperties property) {
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
            if (TimeSpan.class.getName().equals(property.getType())) {
                return TimeSpan.fromString(property.getValue());
            }
            if (ISecurity.class.getName().equals(property.getType())) {
                ISecurity security = null;
                try {
                    URI uri = new URI(property.getValue());
                    try {
                        BundleContext context = Activator.getDefault().getBundle().getBundleContext();
                        ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
                        repositoryService = (IRepositoryService) context.getService(serviceReference);
                        security = repositoryService.getSecurityFromURI(uri);
                        context.ungetService(serviceReference);
                    } catch (Exception e) {
                        Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading repository service", e);
                        Activator.log(status);
                    }

                    if (security == null) {
                        Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Failed to load security " + uri.toString(), null);
                        Activator.log(status);
                        return new FailsafeSecurity(uri);
                    }

                    return security;
                } catch (URISyntaxException e) {
                    throw new HibernateException(e);
                }
            }
            if (IScript.class.getName().equals(property.getType())) {
                IScript script = null;
                try {
                    URI uri = new URI(property.getValue());
                    try {
                        BundleContext context = Activator.getDefault().getBundle().getBundleContext();
                        ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
                        repositoryService = (IRepositoryService) context.getService(serviceReference);
                        script = (IScript) repositoryService.getObjectFromURI(uri);
                        context.ungetService(serviceReference);
                    } catch (Exception e) {
                        Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading repository service", e);
                        Activator.log(status);
                    }

                    if (script == null) {
                        Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Failed to load script " + uri.toString(), null);
                        Activator.log(status);
                        return new FailsafeScript(uri);
                    }

                    return script;
                } catch (URISyntaxException e) {
                    throw new HibernateException(e);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return property.getValue();
    }

    public StrategyScriptProperties() {
    }

    public StrategyScriptProperties(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public StrategyScriptProperties(String name, String type, String value) {
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

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StrategyScriptProperties)) {
            return false;
        }
        StrategyScriptProperties other = (StrategyScriptProperties) obj;
        if (!name.equals(other.name)) {
            return false;
        }
        if (!type.equals(other.type)) {
            return false;
        }
        if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 7 * name.hashCode() + 11 * type.hashCode() + 31 * value.hashCode();
    }
}
