// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validation.constraints.validatorcontext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import org.hibernate.validation.util.TestUtil;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorContextTest {

	@Test
	public void testNoCustomization() {
		Validator validator = TestUtil.getValidator();

		DummyValidator.disableDefaultError( false );
		DummyValidator.setErrorMessages( null );

		DummyBean bean = new DummyBean( "foobar" );

		Set<ConstraintViolation<DummyBean>> constraintViolations = validator.validate( bean );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong message", "dummy message", constraintViolation.getMessage() );
	}

	/**
	 * @todo Is this the right behaviour? The spec is not quite clear about this.
	 */
	@Test
	public void testDisableDefaultErrorWithoutCustomError() {
		Validator validator = TestUtil.getValidator();

		DummyValidator.disableDefaultError( true );
		Map<String, String> errors = new HashMap<String, String>();
		DummyValidator.setErrorMessages( errors );

		DummyBean bean = new DummyBean( "foobar" );

		Set<ConstraintViolation<DummyBean>> constraintViolations = validator.validate( bean );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
	}

	@Test
	public void testDisableDefaultErrorWithCustomErrors() {
		Validator validator = TestUtil.getValidator();

		DummyValidator.disableDefaultError( true );
		Map<String, String> errors = new HashMap<String, String>();
		errors.put( "message1", "property1" );
		DummyValidator.setErrorMessages( errors );

		DummyBean bean = new DummyBean( "foobar" );

		Set<ConstraintViolation<DummyBean>> constraintViolations = validator.validate( bean );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		ConstraintViolation constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong message", "message1", constraintViolation.getMessage() );
		assertEquals( "Wrong property", "property1", constraintViolation.getPropertyPath() );
	}

	@Test
	public void testNestedValidation() {
		Validator validator = TestUtil.getValidator();

		DummyValidator.disableDefaultError( false );
		DummyValidator.setErrorMessages( null );

		DummyBean bean = new DummyBean( "foo" );
		bean.setNestedDummy( new DummyBean( "bar" ) );

		Set<ConstraintViolation<DummyBean>> constraintViolations = validator.validate( bean );
		assertEquals( "Wrong number of constraints", 2, constraintViolations.size() );
		boolean validatedNestedBean = false;
		for ( ConstraintViolation<DummyBean> violation : constraintViolations ) {

			if ( violation.getPropertyPath().equals( "value" ) ) {
				assertEquals( "Wrong message", "dummy message", violation.getMessage() );
			}
			else if ( violation.getPropertyPath().equals( "nestedDummy.value" ) ) {
				assertEquals( "Wrong message", "dummy message", violation.getMessage() );
				validatedNestedBean = true;
			}
			else {
				fail( "Wrong property " + violation.getMessage() );
			}
		}
		assertTrue( validatedNestedBean );
	}
}
