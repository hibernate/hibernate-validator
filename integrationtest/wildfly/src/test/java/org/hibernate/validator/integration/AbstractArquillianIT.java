/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration;

import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

/**
 * Base class for all the TestNG tests using Arquillian.
 *
 * @author Guillaume Smet
 */
public abstract class AbstractArquillianIT extends Arquillian {

	protected static StringAsset BEANS_XML = new StringAsset( "<beans xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" " +
			"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
			"xsi:schemaLocation=\"https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/beans_3_0.xsd\"\n" +
			"bean-discovery-mode=\"all\"></beans>" );

	public static WebArchive buildTestArchive(String warFileName) {
		PomEquippedResolveStage pom = Maven.resolver().loadPomFromFile( "pom.xml" );
		return ShrinkWrap
				.create( WebArchive.class, warFileName )
				.addClass( AbstractArquillianIT.class )
				.addAsLibraries( pom.resolve( "org.testng:testng" ).withTransitivity().asFile() )
				.addAsLibraries( pom.resolve( "org.assertj:assertj-core" ).withTransitivity().asFile() )
				.addAsResource( "log4j.properties" );
	}
}
