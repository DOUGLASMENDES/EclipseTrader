//$Id: $
package org.hibernate.cfg.annotations.reflection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.hibernate.util.StringHelper;

/**
 * @author Emmanuel Bernard
 */
public class XMLContext {
	private static Log log = LogFactory.getLog( XMLContext.class );
	private Default globalDefaults;
	private Map<String, Element> classOverriding = new HashMap<String, Element>();
	private Map<String, Default> defaultsOverriding = new HashMap<String, Default>();
	private List<Element> defaultElements = new ArrayList<Element>();
	private List<String> defaultEntityListeners = new ArrayList<String>();
	private boolean hasContext = false;

	/**
	 * Add a document and return the list of added classes names
	 */
	public List<String> addDocument(Document doc) {
		hasContext = true;
		List<String> addedClasses = new ArrayList<String>();
		Element root = doc.getRootElement();
		//global defaults
		Element metadata = root.element( "persistence-unit-metadata" );
		if ( metadata != null ) {
			if ( globalDefaults == null ) {
				globalDefaults = new Default();
				globalDefaults.setMetadataComplete(
						metadata.element( "xml-mapping-metadata-complete" ) != null ?
								Boolean.TRUE :
								null
				);
				Element defaultElement = metadata.element( "persistence-unit-defaults" );
				if ( defaultElement != null ) {
					Element unitElement = defaultElement.element( "schema" );
					globalDefaults.setSchema( unitElement != null ? unitElement.getTextTrim() : null );
					unitElement = defaultElement.element( "catalog" );
					globalDefaults.setCatalog( unitElement != null ? unitElement.getTextTrim() : null );
					unitElement = defaultElement.element( "access" );
					globalDefaults.setAccess( unitElement != null ? unitElement.getTextTrim() : null );
					unitElement = defaultElement.element( "cascade-persist" );
					globalDefaults.setCascadePersist( unitElement != null ? Boolean.TRUE : null );
					defaultEntityListeners.addAll( addEntityListenerClasses( defaultElement, null, addedClasses ) );
				}
			}
			else {
				log.warn( "Found more than one <persistence-unit-metadata>, subsequent ignored" );
			}
		}

		//entity mapping default
		Default entityMappingDefault = new Default();
		Element unitElement = root.element( "package" );
		String packageName = unitElement != null ? unitElement.getTextTrim() : null;
		entityMappingDefault.setPackageName( packageName );
		unitElement = root.element( "schema" );
		entityMappingDefault.setSchema( unitElement != null ? unitElement.getTextTrim() : null );
		unitElement = root.element( "catalog" );
		entityMappingDefault.setCatalog( unitElement != null ? unitElement.getTextTrim() : null );
		unitElement = root.element( "access" );
		entityMappingDefault.setAccess( unitElement != null ? unitElement.getTextTrim() : null );
		defaultElements.add( root );

		List<Element> entities = (List<Element>) root.elements( "entity" );
		addClass( entities, packageName, entityMappingDefault, addedClasses );

		entities = (List<Element>) root.elements( "mapped-superclass" );
		addClass( entities, packageName, entityMappingDefault, addedClasses );

		entities = (List<Element>) root.elements( "embeddable" );
		addClass( entities, packageName, entityMappingDefault, addedClasses );
		return addedClasses;
	}

	private void addClass(List<Element> entities, String packageName, Default defaults, List<String> addedClasses) {
		for (Element element : entities) {
			String className = buildSafeClassName( element.attributeValue( "class" ), packageName );
			if ( classOverriding.containsKey( className ) ) {
				//maybe switch it to warn?
				throw new IllegalStateException( "Duplicate XML entry for " + className );
			}
			addedClasses.add( className );
			classOverriding.put( className, element );
			Default localDefault = new Default();
			localDefault.override( defaults );
			String metadataCompleteString = element.attributeValue( "metadata-complete" );
			if ( metadataCompleteString != null ) {
				localDefault.setMetadataComplete( Boolean.parseBoolean( metadataCompleteString ) );
			}
			String access = element.attributeValue( "access" );
			if ( access != null ) localDefault.setAccess( access );
			defaultsOverriding.put( className, localDefault );

			log.debug( "Adding XML overriding information for " + className );
			addEntityListenerClasses( element, packageName, addedClasses );
		}
	}

	private List<String> addEntityListenerClasses(Element element, String packageName, List<String> addedClasses) {
		List<String> localAddedClasses = new ArrayList<String>();
		Element listeners = element.element( "entity-listeners" );
		if ( listeners != null ) {
			List<Element> elements = (List<Element>) listeners.elements( "entity-listener" );
			for (Element listener : elements) {
				String listenerClassName = buildSafeClassName( listener.attributeValue( "class" ), packageName );
				if ( classOverriding.containsKey( listenerClassName ) ) {
					//maybe switch it to warn?
					if ( "entity-listener".equals( classOverriding.get( listenerClassName ).getName() ) ) {
						log.info(
								"entity-listener duplication, first event definition will be used: "
										+ listenerClassName
						);
						continue;
					}
					else {
						throw new IllegalStateException( "Duplicate XML entry for " + listenerClassName );
					}
				}
				localAddedClasses.add( listenerClassName );
				classOverriding.put( listenerClassName, listener );
			}
		}
		log.debug( "Adding XML overriding information for listener: " + listeners );
		addedClasses.addAll( localAddedClasses );
		return localAddedClasses;
	}

	public static String buildSafeClassName(String className, String defaultPackageName) {
		if ( className.indexOf( '.' ) < 0 && StringHelper.isNotEmpty( defaultPackageName ) ) {
			className = StringHelper.qualify( defaultPackageName, className );
		}
		return className;
	}

	public static String buildSafeClassName(String className, XMLContext.Default defaults) {
		return buildSafeClassName( className, defaults.getPackageName() );
	}

	public Default getDefault(String className) {
		Default xmlDefault = new Default();
		xmlDefault.override( globalDefaults );
		if ( className != null ) {
			Default entityMappingOverriding = defaultsOverriding.get( className );
			xmlDefault.override( entityMappingOverriding );
		}
		return xmlDefault;
	}

	public Element getXMLTree(String className, String methodName) {
		return classOverriding.get( className );
	}

	public List<Element> getAllDocuments() {
		return defaultElements;
	}

	public boolean hasContext() {
		return hasContext;
	}

	public static class Default {
		private String access;
		private String packageName;
		private String schema;
		private String catalog;
		private Boolean metadataComplete;
		private Boolean cascadePersist;

		public String getAccess() {
			return access;
		}

		protected void setAccess(String access) {
			if ( "FIELD".equals( access ) || "PROPERTY".equals( access ) ) {
				this.access = access.toLowerCase();
			}
			else {
				this.access = access;
			}
		}

		public String getCatalog() {
			return catalog;
		}

		protected void setCatalog(String catalog) {
			this.catalog = catalog;
		}

		public String getPackageName() {
			return packageName;
		}

		protected void setPackageName(String packageName) {
			this.packageName = packageName;
		}

		public String getSchema() {
			return schema;
		}

		protected void setSchema(String schema) {
			this.schema = schema;
		}

		public Boolean getMetadataComplete() {
			return metadataComplete;
		}

		public boolean canUseJavaAnnotations() {
			return metadataComplete == null || !metadataComplete.booleanValue();
		}

		protected void setMetadataComplete(Boolean metadataComplete) {
			this.metadataComplete = metadataComplete;
		}

		public Boolean getCascadePersist() {
			return cascadePersist;
		}

		void setCascadePersist(Boolean cascadePersist) {
			this.cascadePersist = cascadePersist;
		}

		public void override(Default globalDefault) {
			if ( globalDefault != null ) {
				if ( globalDefault.getAccess() != null ) access = globalDefault.getAccess();
				if ( globalDefault.getPackageName() != null ) packageName = globalDefault.getPackageName();
				if ( globalDefault.getSchema() != null ) schema = globalDefault.getSchema();
				if ( globalDefault.getCatalog() != null ) catalog = globalDefault.getCatalog();
				if ( globalDefault.getMetadataComplete() != null ) {
					metadataComplete = globalDefault.getMetadataComplete();
				}
				//TODO fix that in stone if cascade-persist is set already?
				if ( globalDefault.getCascadePersist() != null ) cascadePersist = globalDefault.getCascadePersist();
			}
		}
	}

	public List<String> getDefaultEntityListeners() {
		return defaultEntityListeners;
	}
}
