/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class SuccessfulClassInheritanceMethodValidationTest extends Arquillian {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addClass( MI6.class )
				.addClass( SecretServiceBase.class )
				.addAsManifestResource( "beans.xml" );
	}

	@Inject
	MI6 mi6;

	@Test
	public void testOverriddenExecutionTypeIsConsidered() {
		assertThatThrownBy( () -> mi6.whisper( null ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> assertThat(
						( (ConstraintViolationException) e ).getConstraintViolations()
								.iterator()
								.next()
								.getConstraintDescriptor()
								.getAnnotation()
								.annotationType() )
						.isEqualTo( NotNull.class ) );
	}
}
