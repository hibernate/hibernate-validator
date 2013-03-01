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
package org.hibernate.validator.test.internal.engine.methodvalidation.xml;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Configuration;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.CrossParameterDescriptor;
import javax.validation.metadata.MethodDescriptor;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Hardy Ferentschik
 */
public class IgnoreCrossParameterConstraintTest {

	// used by test framework
	public IgnoreCrossParameterConstraintTest() {
	}

	// constructor used for validation
	@CrossParameterConstraint
	IgnoreCrossParameterConstraintTest(@NotNull String foo, String bar) {
	}

	@CrossParameterConstraint
	public void snafu(@NotNull String foo, String bar) {
	}

	@Test
	@TestForIssue(jiraKey = "HV-734")
	public void testCrossParameterConstraintDefinedOnConstructorAndMethod() {
		Validator validator = ValidatorUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( this.getClass() );

		// check that the test constructor has a cross parameter constraint
		ConstructorDescriptor constructorDescriptor = beanDescriptor.getConstraintsForConstructor(
				String.class,
				String.class
		);
		CrossParameterDescriptor crossParameterDescriptor = constructorDescriptor.getCrossParameterDescriptor();
		assertTrue( crossParameterDescriptor.hasConstraints(), "There should be cross parameter constraints." );

		// check that the test method has a cross parameter constraint
		MethodDescriptor methodDescriptor = beanDescriptor.getConstraintsForMethod(
				"snafu",
				String.class,
				String.class
		);
		crossParameterDescriptor = methodDescriptor.getCrossParameterDescriptor();
		assertTrue( crossParameterDescriptor.hasConstraints(), "There should be cross parameter constraints." );
	}

	@Test
	@TestForIssue(jiraKey = "HV-734")
	public void testCrossParameterConstraintsAreIgnored() {
		Validator validator = getXmlConfiguredValidator( "ignore-annotations-for-cross-parameter-constraints.xml" );
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( this.getClass() );

		// check that the test constructor has no cross parameter constraint
		ConstructorDescriptor constructorDescriptor = beanDescriptor.getConstraintsForConstructor(
				String.class,
				String.class
		);
		CrossParameterDescriptor crossParameterDescriptor = constructorDescriptor.getCrossParameterDescriptor();
		assertFalse( crossParameterDescriptor.hasConstraints(), "There should be no cross parameter constraints." );

		// check that the test method has no cross parameter constraint
		MethodDescriptor methodDescriptor = beanDescriptor.getConstraintsForMethod(
				"snafu",
				String.class,
				String.class
		);
		crossParameterDescriptor = methodDescriptor.getCrossParameterDescriptor();
		assertFalse( crossParameterDescriptor.hasConstraints(), "There should be no cross parameter constraints." );
	}

	private Validator getXmlConfiguredValidator(String fileName) {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping(
				IgnoreAnnotationConfiguredConstructorValidationTest.class.getResourceAsStream( fileName )
		);

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		return validatorFactory.getValidator();
	}


	@Target({ CONSTRUCTOR, METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { CrossParameterConstraintValidator.class })
	@Documented
	public @interface CrossParameterConstraint {
		String message() default "snafu";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	@SupportedValidationTarget(value = ValidationTarget.PARAMETERS)
	public class CrossParameterConstraintValidator implements ConstraintValidator<CrossParameterConstraint, Object[]> {

		@Override
		public void initialize(CrossParameterConstraint constraintAnnotation) {
		}

		@Override
		public boolean isValid(Object[] value, ConstraintValidatorContext context) {
			return false;
		}
	}
}
