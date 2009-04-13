//$Id: $
package org.hibernate.cfg;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.util.DTDEntityResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author Emmanuel Bernard
 */
public class EJB3DTDEntityResolver extends DTDEntityResolver {
	public static final EntityResolver INSTANCE = new EJB3DTDEntityResolver();

	private static final Log log = LogFactory.getLog( EJB3DTDEntityResolver.class );

	boolean resolved = false;

	public boolean isResolved() {
		return resolved;
	}

	public InputSource resolveEntity(String publicId, String systemId) {
		InputSource is = super.resolveEntity( publicId, systemId );
		if ( is == null ) {
			if ( systemId != null ) {
				if ( systemId.endsWith( "orm_1_0.xsd" ) ) {
					log.debug(
							"recognized EJB3 ORM namespace; attempting to resolve on classpath under org/hibernate/ejb"
					);
					String path = "org/hibernate/ejb/" + "orm_1_0.xsd";
					InputStream dtdStream = resolveInHibernateNamespace( path );
					if ( dtdStream == null ) {
						log.debug( "unable to locate [" + systemId + "] on classpath" );
					}
					else {
						log.debug( "located [" + systemId + "] in classpath" );
						InputSource source = new InputSource( dtdStream );
						source.setPublicId( publicId );
						source.setSystemId( systemId );
						resolved = false;
						return source;
					}
				}
				else if ( systemId.endsWith( "persistence_1_0.xsd" ) ) {
					log.debug(
							"recognized EJB3 ORM namespace; attempting to resolve on classpath under org/hibernate/ejb"
					);
					String path = "org/hibernate/ejb/" + "persistence_1_0.xsd";
					InputStream dtdStream = resolveInHibernateNamespace( path );
					if ( dtdStream == null ) {
						log.debug( "unable to locate [" + systemId + "] on classpath" );
					}
					else {
						log.debug( "located [" + systemId + "] in classpath" );
						InputSource source = new InputSource( dtdStream );
						source.setPublicId( publicId );
						source.setSystemId( systemId );
						resolved = true;
						return source;
					}
				}
			}
		}
		else {
			resolved = true;
			return is;
		}
		//use the default behavior
		return null;
	}
}
