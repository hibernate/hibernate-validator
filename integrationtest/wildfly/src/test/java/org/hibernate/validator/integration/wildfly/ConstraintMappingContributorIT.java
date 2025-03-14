/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.wildfly;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.integration.AbstractArquillianIT;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;

import org.testng.annotations.Test;

/**
 * Asserts constraints mappings contributed via {@code validation.xml} are applied.
 *
 * @author Gunnar Morling
 */
public class ConstraintMappingContributorIT extends AbstractArquillianIT {

	private static final String WAR_FILE_NAME = ConstraintMappingContributorIT.class
			.getSimpleName() + ".war";

	@Deployment
	public static Archive<?> createTestArchive() {
		return buildTestArchive( WAR_FILE_NAME )
				.addClasses( Broomstick.class, MyConstraintMappingContributor.class )
				.addAsResource( "constraint-mapping-contributor-validation.xml", "META-INF/validation.xml" )
				.addAsWebInfResource( BEANS_XML, "beans.xml" );
	}

	@Inject
	private Validator validator;

	@Test
	public void shouldApplyContributedConstraintMapping() {
		Set<ConstraintViolation<Broomstick>> violations = validator.validate( new Broomstick() );

		assertThat( violations ).hasSize( 1 );
		assertThat( violations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType() ).isEqualTo( NotNull.class );
	}
}
