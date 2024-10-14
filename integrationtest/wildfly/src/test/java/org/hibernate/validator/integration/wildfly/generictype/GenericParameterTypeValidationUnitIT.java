/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.wildfly.generictype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

import java.util.Set;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.integration.AbstractArquillianIT;
import org.hibernate.validator.testutil.TestForIssue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import org.testng.annotations.Test;

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
		try {
			bean.genericArg( null );
			fail( "@NotNull constraint should be violated" );
		}
		catch (ConstraintViolationException e) {
			Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			assertThat( violations ).as( "Unexpected number of constraint violations" ).hasSize( 1 );
			ConstraintViolation<?> constraintViolation = violations.iterator().next();
			assertThat( constraintViolation.getConstraintDescriptor().getAnnotation().annotationType() )
					.as( "Unexpected constraint type" )
					.isEqualTo( NotNull.class );
		}
	}

	@Test
	public void return_value_constraint_added_in_subtype_is_considered() {
		try {
			billingService.getBillingAmount( "some order" );
			fail( "@Min constraint should be violated" );
		}
		catch (ConstraintViolationException e) {
			Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			assertThat( violations ).as( "Unexpected number of constraint violations" ).hasSize( 1 );
			ConstraintViolation<?> constraintViolation = violations.iterator().next();
			assertThat( constraintViolation.getConstraintDescriptor().getAnnotation().annotationType() )
					.as( "Unexpected constraint type" )
					.isEqualTo( Min.class );
		}
	}
}
