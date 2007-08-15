//$Id: $
package org.hibernate.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hibernate Vaildator version
 *
 * @author Emmanuel Bernard
 */
public class Version {
	public static final String VERSION = "3.0.0.GA";
	private static Log log = LogFactory.getLog( Version.class );

	static {
		log.info( "Hibernate Validator " + VERSION );
	}

	public static void touch() {
	}
}