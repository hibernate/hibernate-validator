/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.testng.annotations.Test;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.NullDef;
import org.hibernate.validator.test.constraints.Object;
import org.hibernate.validator.testutil.ValidatorUtil;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Unit test for {@link org.hibernate.validator.cfg.ConstraintMapping} et al.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintMappingWithAnnotationProcessingOptionsTest {

	@Test
	public void testIgnoreAllAnnotationsOnType() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Foo.class ).ignoreAllAnnotations();

		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );
		assertFalse( validator.getConstraintsForClass( Foo.class ).isBeanConstrained() );
	}

	@Test
	public void testIgnoreClassConstraints() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Fu.class ).ignoreAnnotations();

		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );
		assertFalse( validator.getConstraintsForClass( Fu.class ).isBeanConstrained() );
	}

	@Test
	public void testIgnoreAnnotationsOnProperty() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Foo.class )
				.property( "property", FIELD )
				.ignoreAnnotations();

		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );
		assertFalse( validator.getConstraintsForClass( Foo.class ).isBeanConstrained() );
	}

	@Test
	public void testIgnoreAnnotationsRespectsFieldVsGetterAccess() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Foo.class )
				.property( "property", METHOD )
				.ignoreAnnotations();

		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );
		assertTrue( validator.getConstraintsForClass( Foo.class ).isBeanConstrained() );
	}

	@Test
	public void testConvertNotNullToNull() {
		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<Bar>> violations = validator.validate( new Bar() );
		assertCorrectConstraintTypes( violations, NotNull.class );

		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Bar.class )
				.property( "property", FIELD )
				.ignoreAnnotations()
				.constraint( new NullDef() );

		validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );
		violations = validator.validate( new Bar() );

		assertNumberOfViolations( violations, 0 );
	}

	private static class Foo {
		@NotNull
		private String property;

		public String getProperty() {
			return property;
		}
	}

	private static class Bar {
		@NotNull
		private String property;
	}

	@Fighters
	private static class Fu {
	}

	@Constraint(validatedBy = { FightersValidator.class })
	@Target({ TYPE })
	@Retention(RUNTIME)
	@Documented
	public @interface Fighters {
		public abstract String message() default "fu fighters";

		public abstract Class<?>[] groups() default { };

		public abstract Class<? extends Payload>[] payload() default { };
	}

	public class FightersValidator implements ConstraintValidator<Fighters, Object> {
		public void initialize(Fighters annotation) {
		}

		public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
			return true;
		}
	}
}
