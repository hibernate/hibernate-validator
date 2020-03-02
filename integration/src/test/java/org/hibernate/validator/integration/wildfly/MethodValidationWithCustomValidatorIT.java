/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.integration.AbstractArquillianIT;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
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
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
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
