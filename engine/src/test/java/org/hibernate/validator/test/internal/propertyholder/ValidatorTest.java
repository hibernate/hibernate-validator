/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.propertyholder;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

import org.hibernate.validator.internal.engine.ValidatorImpl;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class ValidatorTest {
	@Test
	public void testSimplePropertyHolder() {
		ValidatorImpl validator = (ValidatorImpl) getValidator();

		Map<String, Object> address = new HashMap<>();
		address.put( "street", "str" );
		address.put( "buildingNumber", -1L );

		Map<String, Object> user = new HashMap<>();
		user.put( "name", "jhon" );
		user.put( "email", "not a mail" );
		user.put( "address", address );

		Set<ConstraintViolation<Map>> constraintViolations = validator.validatePropertyHolder( user, "user" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class ).withProperty( "name" ),
				violationOf( Email.class ).withProperty( "email" )
		);
	}
}
