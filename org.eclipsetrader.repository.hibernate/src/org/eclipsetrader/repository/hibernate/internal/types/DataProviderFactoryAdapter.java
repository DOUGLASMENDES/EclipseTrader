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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;
import org.eclipsetrader.repository.hibernate.internal.Activator;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

public class DataProviderFactoryAdapter implements UserType {

    public static final String PROVIDERS_FACTORY_ID = "org.eclipsetrader.core.providers";

    public class FailsafeDataProviderFactory implements IDataProviderFactory {

        private String id;

        public FailsafeDataProviderFactory(String id) {
            this.id = id;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#createProvider()
         */
        @Override
        public IDataProvider createProvider() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#getId()
         */
        @Override
        public String getId() {
            return id;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#getName()
         */
        @Override
        public String getName() {
            return id;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#getType()
         */
        @Override
        @SuppressWarnings("unchecked")
        public Class[] getType() {
            return new Class[0];
        }
    }

    public DataProviderFactoryAdapter() {
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
     */
    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
     */
    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
     */
    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y || x != null && x.equals(y);
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
     */
    @Override
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#isMutable()
     */
    @Override
    public boolean isMutable() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)
     */
    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        String v = rs.getString(names[0]);
        return v != null ? getFactory(v) : null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
     */
    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        }
        else {
            st.setString(index, ((IDataProviderFactory) value).getId());
        }
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class returnedClass() {
        return IDataProviderFactory.class;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#sqlTypes()
     */
    @Override
    public int[] sqlTypes() {
        return new int[] {
            Types.VARCHAR
        };
    }

    protected IDataProviderFactory getFactory(String id) {
        IConfigurationElement[] configElements = getConfigurationElements();
        if (configElements == null) {
            return null;
        }

        for (int j = 0; j < configElements.length; j++) {
            String strID = configElements[j].getAttribute("id"); //$NON-NLS-1$
            if (id.equals(strID)) {
                try {
                    IDataProviderFactory factory = (IDataProviderFactory) configElements[j].createExecutableExtension("class");
                    return factory;
                } catch (Exception e) {
                    Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Unable to create data provider factory with id " + id, e);
                    Activator.log(status);
                }
                break;
            }
        }

        Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Failed to load provider " + id, null);
        Activator.log(status);

        return new FailsafeDataProviderFactory(id);
    }

    protected IConfigurationElement[] getConfigurationElements() {
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PROVIDERS_FACTORY_ID);
        if (extensionPoint == null) {
            return null;
        }
        return extensionPoint.getConfigurationElements();
    }
}
