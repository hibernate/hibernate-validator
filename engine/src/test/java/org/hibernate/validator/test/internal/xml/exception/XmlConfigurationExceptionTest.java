/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml.exception;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.Test;

import javax.validation.Configuration;
import javax.validation.ValidationException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
public class XmlConfigurationExceptionTest {

	@Test
	@TestForIssue(jiraKey = "HV-620")
	public void testMissingAnnotationParameter() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlConfigurationExceptionTest.class.getResourceAsStream( "hv-620-mapping.xml" ) );

		try {
			configuration.buildValidatorFactory();
			fail();
		}
		catch ( ValidationException e ) {
			assertTrue( e.getMessage().startsWith( "HV000012" ) );
			Throwable cause = e.getCause();
			assertEquals(
					cause.getMessage(),
					"HV000085: No value provided for parameter 'regexp' of annotation @Pattern.",
					"Wrong error message"
			);
		}
	}
}
