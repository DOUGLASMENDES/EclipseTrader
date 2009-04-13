//$Id$
package org.hibernate.cfg;

import org.hibernate.MappingException;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;

public class PropertyPreloadedData implements PropertyData {
	private final String defaultAccess;

	private final String propertyName;

	private final XClass returnedClass;

	public PropertyPreloadedData(String defaultAccess, String propertyName, XClass returnedClass) {
		this.defaultAccess = defaultAccess;
		this.propertyName = propertyName;
		this.returnedClass = returnedClass;
	}

	public String getDefaultAccess() throws MappingException {
		return defaultAccess;
	}

	public String getPropertyName() throws MappingException {
		return propertyName;
	}

	public XClass getClassOrElement() throws MappingException {
		return getPropertyClass();
	}

	public XClass getPropertyClass() throws MappingException {
		return returnedClass;
	}

	public String getClassOrElementName() throws MappingException {
		return getTypeName();
	}

	public String getTypeName() throws MappingException {
		return returnedClass == null ? null : returnedClass.getName();
	}

	public XProperty getProperty() {
		return null; //instead of UnsupportedOperationException
	}
}
