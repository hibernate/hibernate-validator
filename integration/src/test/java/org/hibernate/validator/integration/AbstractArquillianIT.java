/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration;

import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * Base class for all the TestNG tests using Arquillian
 * .
 * @author Guillaume Smet
 */
public abstract class AbstractArquillianIT extends Arquillian {

	public static WebArchive buildTestArchive(String warFileName) {
		return ShrinkWrap
				.create( WebArchive.class, warFileName )
				.addClass( AbstractArquillianIT.class )
				.addAsLibraries(
						Maven.resolver().resolve( "org.testng:testng:6.8" ).withTransitivity().asFile()
				)
				.addAsLibraries(
						Maven.resolver().resolve( "org.assertj:assertj-core:3.5.2" ).withTransitivity().asFile()
				)
				.addAsResource( "log4j.properties" );
	}

}
