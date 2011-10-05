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

package org.eclipsetrader.core.ats.engines;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

public class EnhancedWrapFactory extends WrapFactory {

    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public class NativeMapAdapter extends NativeJavaObject {

        private static final long serialVersionUID = 3722239924401421834L;

        public NativeMapAdapter(Scriptable scope, Object javaObject, Class staticType) {
            super(scope, javaObject, staticType);
        }

        private Map getMap() {
            return (Map) javaObject;
        }

        @Override
        public void delete(String name) {
            try {
                getMap().remove(name);
            } catch (RuntimeException e) {
                Context.throwAsScriptRuntimeEx(e);
            }
        }

        @Override
        public Object get(String name, Scriptable start) {
            Object value = super.get(name, start);
            if (value != Scriptable.NOT_FOUND) {
                return value;
            }
            return getMap().get(name);
        }

        @Override
        public String getClassName() {
            return "NativeMapAdapter";
        }

        @Override
        public Object[] getIds() {
            return getMap().keySet().toArray();
        }

        @Override
        public boolean has(String name, Scriptable start) {
            return getMap().containsKey(name) || super.has(name, start);
        }

        @Override
        public void put(String name, Scriptable start, Object value) {
            try {
                getMap().put(name, Context.jsToJava(value, ScriptRuntime.ObjectClass));
            } catch (RuntimeException e) {
                Context.throwAsScriptRuntimeEx(e);
            }
        }

        @Override
        public String toString() {
            return javaObject.toString();
        }
    }

    public EnhancedWrapFactory() {
        setJavaPrimitiveWrap(false);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class staticType) {
        if (javaObject instanceof Map) {
            return new NativeMapAdapter(scope, javaObject, staticType);
        }
        else {
            return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
        }
    }
}
