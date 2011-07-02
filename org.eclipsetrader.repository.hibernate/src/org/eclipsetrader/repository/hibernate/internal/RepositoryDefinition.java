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

package org.eclipsetrader.repository.hibernate.internal;

import java.util.Properties;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "repository")
public class RepositoryDefinition {

    @XmlAttribute(name = "schema")
    private String schema;

    @XmlElement(name = "name")
    private String label;

    @XmlElement(name = "driver")
    private String databaseDriver;

    @XmlElement(name = "dialect")
    private String dialect;

    @XmlElement(name = "url")
    private String url;

    @XmlElement(name = "user")
    private String user;

    @XmlElement(name = "password")
    private String password;

    public RepositoryDefinition() {
    }

    public RepositoryDefinition(String schema, String label, String databaseDriver, String dialect, String url, String user, String password) {
        this.schema = schema;
        this.label = label;
        this.databaseDriver = databaseDriver;
        this.dialect = dialect;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @XmlTransient
    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @XmlTransient
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @XmlTransient
    public String getDatabaseDriver() {
        return databaseDriver;
    }

    public void setDatabaseDriver(String databaseDriver) {
        this.databaseDriver = databaseDriver;
    }

    @XmlTransient
    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    @XmlTransient
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlTransient
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @XmlTransient
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.connection.driver_class", databaseDriver);
        properties.put("hibernate.dialect", dialect);
        if (url != null && !"".equals(url)) {
            properties.put("hibernate.connection.url", url);
        }
        if (user != null && !"".equals(user)) {
            properties.put("hibernate.connection.username", user);
        }
        if (password != null && !"".equals(password)) {
            properties.put("hibernate.connection.password", password);
        }
        return properties;
    }
}
