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

package org.eclipsetrader.ui;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

public class DatabindingTestCase extends TestCase {

    public DatabindingTestCase() {
    }

    public DatabindingTestCase(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#run(junit.framework.TestResult)
     */
    @Override
    public void run(final TestResult result) {
        Display display = Display.getDefault();
        Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {

            @Override
            public void run() {
                DatabindingTestCase.super.run(result);
            }
        });
    }

    public void testEmpty() throws Exception {
        // To keep JUnit happy
    }
}
