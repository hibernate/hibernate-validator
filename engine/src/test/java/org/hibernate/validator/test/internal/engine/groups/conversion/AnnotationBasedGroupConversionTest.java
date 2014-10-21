/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.conversion;

import org.testng.annotations.BeforeMethod;

import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

/**
 * Integrative test for annotation based group conversion.
 *
 * @author Hardy Ferentschik
 */
public class AnnotationBasedGroupConversionTest extends AbstractGroupConversionTest {
	@BeforeMethod
	public void setupValidator() {
		validator = getValidator();
	}
}
