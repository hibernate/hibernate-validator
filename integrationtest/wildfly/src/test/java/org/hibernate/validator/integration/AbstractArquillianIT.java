/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration;

import org.junit.jupiter.api.extension.ExtendWith;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

/**
 * Base class for all the JUnit 5 tests using Arquillian.
 *
 * @author Guillaume Smet
 */
@ExtendWith(ArquillianExtension.class)
public abstract class AbstractArquillianIT {

	protected static StringAsset BEANS_XML = new StringAsset( "<beans xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" " +
			"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
			"xsi:schemaLocation=\"https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/beans_3_0.xsd\"\n" +
			"bean-discovery-mode=\"all\"></beans>" );

	public static WebArchive buildTestArchive(String warFileName) {
		PomEquippedResolveStage pom = Maven.resolver().loadPomFromFile( "pom.xml" );
		return ShrinkWrap
				.create( WebArchive.class, warFileName )
				.addClass( AbstractArquillianIT.class )
				.addAsLibraries( pom.resolve( "org.junit.jupiter:junit-jupiter" ).withTransitivity().asFile() )
				.addAsLibraries( pom.resolve( "org.assertj:assertj-core" ).withTransitivity().asFile() )
				.addAsResource( "log4j.properties" );
	}
}
