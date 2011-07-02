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
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.IUserProperties;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.repository.hibernate.internal.Activator;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class SecurityType implements UserType {

    private static IRepositoryService repositoryService;

    public class FailsafeSecurity implements ISecurity, IStoreObject, IStore {

        private URI uri;

        public FailsafeSecurity(URI uri) {
            this.uri = uri;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.instruments.ISecurity#getIdentifier()
         */
        @Override
        public IFeedIdentifier getIdentifier() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.instruments.ISecurity#getName()
         */
        @Override
        public String getName() {
            return uri.toString();
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.instruments.ISecurity#getProperties()
         */
        @Override
        public IUserProperties getProperties() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
            if (adapter.isAssignableFrom(getClass())) {
                return this;
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#getStore()
         */
        @Override
        public IStore getStore() {
            return this;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#getStoreProperties()
         */
        @Override
        public IStoreProperties getStoreProperties() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#setStore(org.eclipsetrader.core.repositories.IStore)
         */
        @Override
        public void setStore(IStore store) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#setStoreProperties(org.eclipsetrader.core.repositories.IStoreProperties)
         */
        @Override
        public void setStoreProperties(IStoreProperties storeProperties) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public void delete(IProgressMonitor monitor) throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStoreProperties fetchProperties(IProgressMonitor monitor) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStore[] fetchChilds(IProgressMonitor monitor) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#createChild()
         */
        @Override
        public IStore createChild() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#getRepository()
         */
        @Override
        public IRepository getRepository() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#toURI()
         */
        @Override
        public URI toURI() {
            return uri;
        }
    }

    public SecurityType() {
    }

    public static void setRepositoryService(IRepositoryService service) {
        SecurityType.repositoryService = service;
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
        if (v == null) {
            return null;
        }

        ISecurity security;
        try {
            URI uri = new URI(v);
            if (repositoryService == null) {
                try {
                    BundleContext context = Activator.getDefault().getBundle().getBundleContext();
                    ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
                    repositoryService = (IRepositoryService) context.getService(serviceReference);
                    context.ungetService(serviceReference);
                } catch (Exception e) {
                    Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading repository service", e);
                    Activator.log(status);
                }
            }

            security = repositoryService.getSecurityFromURI(uri);
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

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
     */
    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        }
        else {
            IStoreObject storeObject = (IStoreObject) ((IAdaptable) value).getAdapter(IStoreObject.class);
            st.setString(index, storeObject.getStore().toURI().toString());
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
        return ISecurity.class;
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
}
