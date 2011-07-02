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

package org.eclipsetrader.yahoo.internal.news;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

public class CategoryTest extends TestCase {

    public void testParseNewsFeedFile() throws Exception {
        File file = new File("../org.eclipsetrader.yahoo/data/news_feeds.xml");

        JAXBContext jaxbContext = JAXBContext.newInstance(Category[].class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<Category[]> element = unmarshaller.unmarshal(new StreamSource(file), Category[].class);

        assertNotNull(element);
        assertTrue(element.getValue().length != 0);
    }
}
