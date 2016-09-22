/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Asserts that the validation interceptor picks up a {@code Validator} provided by the application and uses it for
 * validation.
 *
 * @author Gunnar Morling
 */
@RunWith(Arquillian.class)
public class MethodValidationWithCustomValidatorIT {

	private static final String WAR_FILE_NAME = MethodValidationWithCustomValidatorIT.class
			.getSimpleName() + ".war";

	public static class MyService {

		public void doSomething(@NotNull String param) {
		}
	}

	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap.create( WebArchive.class, WAR_FILE_NAME )
				.addClasses( MyValidator.class )
				.addAsWebInfResource( "jboss-deployment-structure.xml", "jboss-deployment-structure.xml" )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Inject
	private MyService myService;

	@Inject
	private MyValidator validator;

	@Test
	public void shouldUseApplicationProvidedValidatorForMethodValidation() {
		assertEquals( 0, validator.getForExecutablesInvocationCount() );
		myService.doSomething( "foobar" );
		assertEquals(
				"MyValidator#forExecutable() should have been invoked once.",
				1,
				validator.getForExecutablesInvocationCount()
		);
	}
}
