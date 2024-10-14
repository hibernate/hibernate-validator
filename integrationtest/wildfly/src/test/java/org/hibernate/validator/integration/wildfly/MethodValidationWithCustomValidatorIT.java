/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.wildfly;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.integration.AbstractArquillianIT;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;

import org.testng.annotations.Test;

/**
 * Asserts that the validation interceptor picks up a {@code Validator} provided by the application and uses it for
 * validation.
 *
 * @author Gunnar Morling
 */
public class MethodValidationWithCustomValidatorIT extends AbstractArquillianIT {

	private static final String WAR_FILE_NAME = MethodValidationWithCustomValidatorIT.class
			.getSimpleName() + ".war";

	public static class MyService {

		public void doSomething(@NotNull String param) {
		}
	}

	@Deployment
	public static Archive<?> createTestArchive() {
		return buildTestArchive( WAR_FILE_NAME )
				.addClasses( MyValidator.class )
				.addAsWebInfResource( BEANS_XML, "beans.xml" );
	}

	@Inject
	private MyService myService;

	@Inject
	private MyValidator validator;

	@Test
	public void shouldUseApplicationProvidedValidatorForMethodValidation() {
		assertThat( validator.getForExecutablesInvocationCount() ).isEqualTo( 0 );
		myService.doSomething( "foobar" );
		assertThat( validator.getForExecutablesInvocationCount() )
				.as( "MyValidator#forExecutable() should have been invoked once." )
				.isEqualTo( 1 );
	}
}
