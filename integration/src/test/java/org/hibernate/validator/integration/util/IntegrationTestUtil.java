/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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

	private static final String CONSTRAINT_CONTRIBUTOR_SERVICE_FILE_PATH = "META-INF/services/javax.validation.ConstraintValidator";
	private static final String CONTRIBUTOR_BUNDLE_NAME = "ContributorValidationMessages.properties";
	private static final String OX_BERRY_CUSTOM_VALIDATOR_JAR_NAME = "oxberry.jar";
	private static final String ACME_CUSTOM_VALIDATOR_JAR_NAME = "acme.jar";

	// prevent instantiation
	private IntegrationTestUtil() {
	}

	/**
	 * @return Returns a jar file containing a custom (dummy) Bean Validation provider including provider, configuration,
	 * validator and service file
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

	/**
	 * @return Returns a jar file containing the custom OxBerry constraints bundled for contribution
	 */
	public static Archive<?> createOxBerryConstraintDefinitionContributorJar() {
		return ShrinkWrap.create( JavaArchive.class, OX_BERRY_CUSTOM_VALIDATOR_JAR_NAME )
				.addClasses(
						OxBerryConstraint.class,
						OxBerryConstraintValidator.class
				)
				.addAsResource(
						"javax.validation.ConstraintValidator-OxBerry",
						CONSTRAINT_CONTRIBUTOR_SERVICE_FILE_PATH
				)
				.addAsResource(
						"ContributorValidationMessages.properties-OxBerry",
						CONTRIBUTOR_BUNDLE_NAME
				);
	}

	/**
	 * @return Returns a jar file containing the custom OxBerry constraints bundled for contribution
	 */
	public static Archive<?> createAcmeConstraintDefinitionContributorJar() {
		return ShrinkWrap.create( JavaArchive.class, ACME_CUSTOM_VALIDATOR_JAR_NAME )
				.addClasses(
						AcmeConstraint.class,
						AcmeConstraintValidator.class
				)
				.addAsResource(
						"javax.validation.ConstraintValidator-Acme",
						CONSTRAINT_CONTRIBUTOR_SERVICE_FILE_PATH
				)
				.addAsResource(
						"ContributorValidationMessages.properties-Acme",
						CONTRIBUTOR_BUNDLE_NAME
				);
	}
}
