/*
 *  Copyright 2003-2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.commons.collections.keyvalue;

import java.util.Map;

import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.Unmodifiable;

/**
 * A {@link java.util.Map.Entry Map.Entry} that throws
 * UnsupportedOperationException when <code>setValue</code> is called.
 *
 * @since Commons Collections 3.0
 * @version $Revision: 405927 $ $Date: 2006-05-12 23:57:03 +0100 (Fri, 12 May 2006) $
 * 
 * @author Stephen Colebourne
 */
public final class UnmodifiableMapEntry extends AbstractMapEntry implements Unmodifiable {

    /**
     * Constructs a new entry with the specified key and given value.
     *
     * @param key  the key for the entry, may be null
     * @param value  the value for the entry, may be null
     */
    public UnmodifiableMapEntry(final Object key, final Object value) {
        super(key, value);
    }

    /**
     * Constructs a new entry from the specified <code>KeyValue</code>.
     *
     * @param pair  the pair to copy, must not be null
     * @throws NullPointerException if the entry is null
     */
    public UnmodifiableMapEntry(final KeyValue pair) {
        super(pair.getKey(), pair.getValue());
    }

    /**
     * Constructs a new entry from the specified <code>Map.Entry</code>.
     *
     * @param entry  the entry to copy, must not be null
     * @throws NullPointerException if the entry is null
     */
    public UnmodifiableMapEntry(final Map.Entry entry) {
        super(entry.getKey(), entry.getValue());
    }

    /**
     * Throws UnsupportedOperationException.
     * 
     * @param value  the new value
     * @return the previous value
     * @throws UnsupportedOperationException always
     */
    public Object setValue(Object value) {
        throw new UnsupportedOperationException("setValue() is not supported");
    }

}
