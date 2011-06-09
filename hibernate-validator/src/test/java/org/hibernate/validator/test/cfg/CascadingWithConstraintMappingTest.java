/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.hibernate.validator.test.cfg;

import java.lang.reflect.Method;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.testng.annotations.Test;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.method.MethodConstraintViolation;
import org.hibernate.validator.method.MethodValidator;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.hibernate.validator.util.ReflectionHelper;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

public class CascadingWithConstraintMappingTest {
	@Test(description = "HV-433")
	public void testProgrammaticCascadingValidationFieldAccess() {
		ConstraintMapping newMapping = new ConstraintMapping();
		newMapping
				.type( C.class )
				.property( "string", FIELD )
				.constraint( new NotNullDef() )
				.type( A.class )
				.property( "c", FIELD )
				.valid();
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( newMapping );

		B b = new B();
		b.c = new C();

		Set<ConstraintViolation<B>> violations = validator.validate( b );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "may not be null" );
	}

	@Test(description = "HV-433")
	public void testProgrammaticCascadingValidationMethodAccess() {
		ConstraintMapping newMapping = new ConstraintMapping();
		newMapping
				.type( C.class )
				.property( "string", METHOD )
				.constraint( new NotNullDef() )
				.type( A.class )
				.property( "c", METHOD )
				.valid();
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( newMapping );

		B b = new B();
		b.c = new C();

		Set<ConstraintViolation<B>> violations = validator.validate( b );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "may not be null" );
	}

	@Test(description = "HV-433")
	public void testProgrammaticCascadingMethodValidation() {
		ConstraintMapping newMapping = new ConstraintMapping();
		newMapping
				.type( C.class )
				.property( "string", METHOD )
				.constraint( new NotNullDef() )
				.type( A.class )
				.property( "c", METHOD )
				.valid();
		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( newMapping );
		MethodValidator methodValidator = validator.unwrap( MethodValidator.class );

		B b = new B();
		b.c = new C();
		Method method = ReflectionHelper.getMethod( B.class, "getC" );

		Set<MethodConstraintViolation<B>> violations = methodValidator.validateReturnValue(
				b, method, b.getC()
		);

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "may not be null" );
	}

	private static class A {
		protected C c;

		public C getC() {
			return c;
		}
	}

	private static class B extends A {
	}

	private static class C {
		private String string;

		public String getString() {
			return string;
		}
	}
}
