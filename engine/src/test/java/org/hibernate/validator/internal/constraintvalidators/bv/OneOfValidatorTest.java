/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import static org.easymock.EasyMock.mock;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import org.hibernate.validator.constraints.OneOf;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OneOfValidatorTest {

	private ConstraintValidatorContext context;
	private OneOfValidator validator;

	@BeforeMethod
	public void setUp() {
		validator = new OneOfValidator();
		context = mock( ConstraintValidatorContext.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testIsValidNullValueShouldReturnTrue() {
		assertTrue( validator.isValid( null, context ), "Null value should be considered valid." );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testIsValidCaseSensitiveMatchShouldReturnTrue() {
		OneOf annotation = createOneOf( false, new String[] { "Value1", "Value2" }, null );

		validator.initialize( annotation );

		assertTrue( validator.isValid( "Value1", context ), "Exact case-sensitive match should return true." );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testIsValidCaseSensitiveMismatchShouldReturnFalse() {
		OneOf annotation = createOneOf( false, new String[] { "Value1", "Value2" }, null );

		validator.initialize( annotation );

		assertFalse( validator.isValid( "value1", context ), "Case-sensitive mismatch should return false." );
	}

	@Test
	public void testIsValidIgnoreCaseMatchShouldReturnTrue() {
		OneOf annotation = createOneOf( true, new String[] { "Value1", "Value2" }, null );

		validator.initialize( annotation );

		assertTrue( validator.isValid( "value1", context ), "Ignore-case match should return true." );
	}

	@Test
	public void testIsValidIgnoreCaseMismatchShouldReturnFalse() {
		OneOf annotation = createOneOf( true, new String[] { "Value1", "Value2" }, null );

		validator.initialize( annotation );

		assertFalse( validator.isValid( "invalid", context ), "Ignore-case mismatch should return false." );
	}

	@Test
	public void testInitializeEnumClassShouldAcceptEnumValues() {
		OneOf annotation = createOneOf( false, new String[] { }, TestEnum.class );

		validator.initialize( annotation );

		assertTrue( validator.isValid( "ONE", context ), "Enum constant 'ONE' should be valid." );
		assertFalse( validator.isValid( "TWO", context ), "'TWO' should not be valid as it's not in the enum." );
	}

	private OneOf createOneOf(boolean ignoreCase, String[] allowedValues, Class<? extends Enum<?>> enumClass) {
		return new OneOf() {
			@Override
			public String[] allowedValues() {
				return allowedValues;
			}

			@Override
			public Class<? extends Enum<?>> enumClass() {
				return enumClass;
			}

			@Override
			public boolean ignoreCase() {
				return ignoreCase;
			}

			@Override
			public String message() {
				return "";
			}

			@Override
			public Class<?>[] groups() {
				return new Class[0];
			}

			@Override
			public Class<? extends Payload>[] payload() {
				return new Class[0];
			}

			@Override
			public Class<OneOf> annotationType() {
				return OneOf.class;
			}
		};
	}

	private enum TestEnum {
		ONE, THREE
	}

}
