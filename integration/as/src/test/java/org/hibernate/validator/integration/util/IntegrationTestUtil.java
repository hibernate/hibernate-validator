/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.integration.util;

import java.util.Collection;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;

import org.hibernate.validator.internal.util.Version;

/**
 * Helper functions for creating integration tests with Arquillian and Shrinkwrap.
 *
 * @author Hardy Ferentschik
 */
public class IntegrationTestUtil {
	// if you want to run this test from the IDE make sure that the hibernate-validator-integrationtest module has
	// a dependency to the hibernate-validator jar file since the version string in org.hibernate.validator.util.Version
	// works with byte code enhancement
	private static final String VALIDATOR_VERSION = Version.getVersionString();
	private static final String CUSTOM_BV_JAR_NAME = "dummy-bean-validation-provider.jar";
	private static final String VALIDATION_PROVIDER_SERVICE_FILE_PATH = "META-INF/services/javax.validation.spi.ValidationProvider";

	// prevent instantiation
	private IntegrationTestUtil() {
	}

	/**
	 * @return Returns a jar file containing a custom (dummy) Bean Validation provider including provider, configuration,
	 *         validator and service file
	 */
	public static Archive<?> createCustomBeanValidationProviderJar() {
		return ShrinkWrap.create( JavaArchive.class, CUSTOM_BV_JAR_NAME )
				.addClasses(
						MyValidator.class,
						MyValidatorConfiguration.class,
						MyValidationProvider.class
				)
				.addAsResource(
						"javax.validation.spi.ValidationProvider",
						VALIDATION_PROVIDER_SERVICE_FILE_PATH
				);
	}

	public static Collection<JavaArchive> bundleHibernateValidatorWithDependencies(boolean removeServiceFile) {
		Collection<JavaArchive> hibernateValidatorWithDependencies = DependencyResolvers.use(
				MavenDependencyResolver.class
		)
				// go offline to make sure to get the SNAPSHOT from the current build and not a resolved SNAPSHOT
				// from a remote repo
				.goOffline()
				.artifact( "org.hibernate:hibernate-validator:" + VALIDATOR_VERSION )
				.resolveAs( JavaArchive.class );

		// remove the service file for Hibernate Validator to avoid bootstrapping Hibernate Validator
		if ( removeServiceFile ) {
			for ( JavaArchive archive : hibernateValidatorWithDependencies ) {
				if ( archive.contains( VALIDATION_PROVIDER_SERVICE_FILE_PATH ) ) {
					archive.delete( VALIDATION_PROVIDER_SERVICE_FILE_PATH );
				}
			}
		}

		// add logging classes
		hibernateValidatorWithDependencies.addAll( bundleLoggingDependencies() );
		return hibernateValidatorWithDependencies;
	}

	public static Collection<JavaArchive> bundleLoggingDependencies() {
		return DependencyResolvers.use( MavenDependencyResolver.class )
				.loadMetadataFromPom( "pom.xml" )
				.artifact( "log4j:log4j" )
				.resolveAs( JavaArchive.class );
	}

	public static Collection<JavaArchive> bundleOptionalDependencies() {
		return DependencyResolvers.use( MavenDependencyResolver.class )
				.loadMetadataFromPom( "pom.xml" )
				.artifact( "org.jsoup:jsoup" )
				.artifact( "joda-time:joda-time" )
				.resolveAs( JavaArchive.class );
	}
}


