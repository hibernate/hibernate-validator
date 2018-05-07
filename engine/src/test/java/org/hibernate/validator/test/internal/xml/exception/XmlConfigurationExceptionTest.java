/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml.exception;

import javax.validation.Configuration;
import javax.validation.ValidationException;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
public class XmlConfigurationExceptionTest {

	@Test
	@TestForIssue(jiraKey = "HV-620")
	public void testMissingAnnotationAttribute() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlConfigurationExceptionTest.class.getResourceAsStream( "hv-620-mapping.xml" ) );

		try {
			configuration.buildValidatorFactory();
			fail();
		}
		catch (ValidationException e) {
			assertTrue( e.getMessage().startsWith( "HV000012" ) );
			Throwable cause = e.getCause();
			assertEquals(
					cause.getMessage(),
					"HV000085: No value provided for attribute 'regexp' of annotation @javax.validation.constraints.Pattern.",
					"Wrong error message"
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1534")
	public void testConstraintOnPropertyDefinedInSuperClass() {
		Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping(
				XmlConfigurationExceptionTest.class.getResourceAsStream(
						"constraint-on-subclass-mapping.xml"
				)
		);
		assertThatThrownBy( () -> configuration.buildValidatorFactory() )
				.isExactlyInstanceOf( ValidationException.class )
				.hasMessage( "HV000106: " + MyClass.class.getName() + " does not define the property id." );
	}

	public static class MyObject {

		private final String id;

		public MyObject(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}
	}

	public static class MyClass extends MyObject {

		public MyClass(String id) {
			super( id );
		}
	}
}
