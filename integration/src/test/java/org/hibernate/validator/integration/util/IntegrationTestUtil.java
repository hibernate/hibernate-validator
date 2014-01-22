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

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Helper functions for creating integration tests with Arquillian and Shrinkwrap.
 *
 * @author Hardy Ferentschik
 */
public class IntegrationTestUtil {
	// if you want to run this test from the IDE make sure that the hibernate-validator-integrationtest module has
	// a dependency to the hibernate-validator jar file since the version string in org.hibernate.validator.util.Version
	// works with byte code enhancement
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
}
