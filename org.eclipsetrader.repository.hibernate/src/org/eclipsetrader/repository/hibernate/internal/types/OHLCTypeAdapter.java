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
import java.util.Date;

import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

public class OHLCTypeAdapter implements UserType {

    public OHLCTypeAdapter() {
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
        Date date = rs.getDate(names[0]);
        if (rs.wasNull()) {
            date = null;
        }

        double dv = rs.getDouble(names[1]);
        Double open = rs.wasNull() ? null : new Double(dv);
        dv = rs.getDouble(names[1]);
        Double high = rs.wasNull() ? null : new Double(dv);
        dv = rs.getDouble(names[1]);
        Double low = rs.wasNull() ? null : new Double(dv);
        dv = rs.getDouble(names[1]);
        Double close = rs.wasNull() ? null : new Double(dv);

        long lv = rs.getLong(names[1]);
        Long volume = rs.wasNull() ? null : new Long(lv);

        return date == null && open == null && high == null && low == null && close == null && volume == null ? null : new OHLC(date, open, high, low, close, volume);
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
     */
    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index++, Types.DATE);
            st.setNull(index++, Types.DOUBLE);
            st.setNull(index++, Types.DOUBLE);
            st.setNull(index++, Types.DOUBLE);
            st.setNull(index++, Types.DOUBLE);
            st.setNull(index++, Types.INTEGER);
        }
        else {
            IOHLC ohlc = (IOHLC) value;
            if (ohlc.getDate() == null) {
                st.setNull(index++, Types.DATE);
            }
            st.setDate(index++, new java.sql.Date(ohlc.getDate().getTime()));
            if (ohlc.getOpen() == null) {
                st.setNull(index++, Types.DOUBLE);
            }
            st.setDouble(index++, ohlc.getOpen());
            if (ohlc.getHigh() == null) {
                st.setNull(index++, Types.DOUBLE);
            }
            st.setDouble(index++, ohlc.getHigh());
            if (ohlc.getLow() == null) {
                st.setNull(index++, Types.DOUBLE);
            }
            st.setDouble(index++, ohlc.getLow());
            if (ohlc.getClose() == null) {
                st.setNull(index++, Types.DOUBLE);
            }
            st.setDouble(index++, ohlc.getClose());
            if (ohlc.getVolume() == null) {
                st.setNull(index++, Types.INTEGER);
            }
            st.setLong(index++, ohlc.getVolume());
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
        return IOHLC.class;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#sqlTypes()
     */
    @Override
    public int[] sqlTypes() {
        return new int[] {
                Types.DATE,
                Types.DOUBLE,
                Types.DOUBLE,
                Types.DOUBLE,
                Types.DOUBLE,
                Types.INTEGER
        };
    }

}
