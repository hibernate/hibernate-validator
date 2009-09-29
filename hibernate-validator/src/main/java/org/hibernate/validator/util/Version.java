// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,  
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.util;

import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.Logger;

/**
 * @author Hardy Ferentschik
 */
public class Version {
	private static final Logger log = org.hibernate.validator.util.LoggerFactory.make();

	static {
		Class clazz = Version.class;
		String classFileName = clazz.getSimpleName() + ".class";
		String classFilePath = clazz.getCanonicalName().replace( '.', '/' )
				+ ".class";
		String pathToThisClass =
				clazz.getResource( classFileName ).toString();
		String pathToManifest = pathToThisClass.substring( 0, pathToThisClass.indexOf( classFilePath ) - 1 )
				+ "/META-INF/MANIFEST.MF";
		log.trace( "Manifest file {}", pathToManifest );
		Manifest manifest = null;
		String version;
		try {
			manifest = new Manifest( new URL( pathToManifest ).openStream() );
		}
		catch ( Exception e ) {
			log.warn( "Unable to determine version of Hibernate Validator" );
		}
		if ( manifest == null ) {
			version = "?";
		}
		else {
			version = manifest.getMainAttributes().getValue( Attributes.Name.IMPLEMENTATION_VERSION );
		}
		log.info( "Hibernate Validator {}", version );
	}

	public static void touch() {
	}
}
