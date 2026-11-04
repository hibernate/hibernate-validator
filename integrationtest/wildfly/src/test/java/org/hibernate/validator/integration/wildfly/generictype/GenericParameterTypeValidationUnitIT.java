/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.wildfly.generictype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.integration.AbstractArquillianIT;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.Test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

@TestForIssue(jiraKey = "HV-978")
public class GenericParameterTypeValidationUnitIT extends AbstractArquillianIT {
	private static final String WAR_FILE_NAME = GenericParameterTypeValidationUnitIT.class.getSimpleName() + ".war";

	@Deployment
	public static WebArchive deployment() {
		return buildTestArchive( WAR_FILE_NAME )
				.addAsWebInfResource( BEANS_XML, "beans.xml" )
				.addPackage( GenericParameterTypeValidationUnitIT.class.getPackage() );
	}

	@Inject
	private StringInterface bean;

	@Inject
	private RetailBillingService billingService;

	@Test
	public void validation_on_generic_arg() {
		assertThatThrownBy( () -> bean.genericArg( null ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> {
					Set<ConstraintViolation<?>> violations = ( (ConstraintViolationException) e ).getConstraintViolations();
					assertThat( violations ).as( "Unexpected number of constraint violations" ).hasSize( 1 );
					ConstraintViolation<?> constraintViolation = violations.iterator().next();
					assertThat( constraintViolation.getConstraintDescriptor().getAnnotation().annotationType() )
							.as( "Unexpected constraint type" )
							.isEqualTo( NotNull.class );
				} );
	}

	@Test
	public void return_value_constraint_added_in_subtype_is_considered() {
		assertThatThrownBy( () -> billingService.getBillingAmount( "some order" ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> {
					Set<ConstraintViolation<?>> violations = ( (ConstraintViolationException) e ).getConstraintViolations();
					assertThat( violations ).as( "Unexpected number of constraint violations" ).hasSize( 1 );
					ConstraintViolation<?> constraintViolation = violations.iterator().next();
					assertThat( constraintViolation.getConstraintDescriptor().getAnnotation().annotationType() )
							.as( "Unexpected constraint type" )
							.isEqualTo( Min.class );
				} );
	}
}
