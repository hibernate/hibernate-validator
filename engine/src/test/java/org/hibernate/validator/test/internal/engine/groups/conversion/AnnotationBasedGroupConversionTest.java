/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.groups.conversion;

import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import org.testng.annotations.BeforeMethod;

/**
 * Integrative test for annotation based group conversion.
 *
 * @author Hardy Ferentschik
 */
public class AnnotationBasedGroupConversionTest extends AbstractGroupConversionTest {
	@Override
	@BeforeMethod
	public void setupValidator() {
		validator = getValidator();
	}
}
