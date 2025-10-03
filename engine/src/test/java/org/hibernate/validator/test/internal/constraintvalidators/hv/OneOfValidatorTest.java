/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;


import static org.easymock.EasyMock.mock;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.OneOfDef;
import org.hibernate.validator.constraints.OneOf;
import org.hibernate.validator.internal.constraintvalidators.hv.OneOfValidator;
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
		OneOf annotation = createOneOf( false, new String[] { "Value1", "Value2" }, null, null, null, null, null );
		validator.initialize( annotation );
		assertTrue( validator.isValid( "Value1", context ), "Exact case-sensitive match should return true." );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testIsValidCaseSensitiveMismatchShouldReturnFalse() {
		OneOf annotation = createOneOf( false, new String[] { "Value1", "Value2" }, null, null, null, null, null );
		validator.initialize( annotation );
		assertFalse( validator.isValid( "value1", context ), "Case-sensitive mismatch should return false." );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testIsValidIgnoreCaseMatchShouldReturnTrue() {
		OneOf annotation = createOneOf( true, new String[] { "Value1", "Value2" }, null, null, null, null, null );
		validator.initialize( annotation );
		assertTrue( validator.isValid( "value1", context ), "Ignore-case match should return true." );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testIsValidIgnoreCaseMismatchShouldReturnFalse() {
		OneOf annotation = createOneOf( true, new String[] { "Value1", "Value2" }, null, null, null, null, null );
		validator.initialize( annotation );
		assertFalse( validator.isValid( "invalid", context ), "Ignore-case mismatch should return false." );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testInitializeEnumClassShouldAcceptEnumValues() {
		OneOf annotation = createOneOf( false, new String[] { }, null, null, null, null, TestEnum.class );
		validator.initialize( annotation );
		assertTrue( validator.isValid( "ONE", context ), "Enum constant 'ONE' should be valid." );
		assertFalse( validator.isValid( "FOUR", context ), "'FOUR' should not be valid as it's not in the enum." );
	}


	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testAllowedIntegersMatchShouldReturnTrue() {
		OneOf annotation = createOneOf( false, null, new int[] { 1, 2, 3 }, null, null, null, null );
		validator.initialize( annotation );
		assertTrue( validator.isValid( "1", context ), "Integer value '1' should be valid." );
		assertTrue( validator.isValid( 1, context ), "Integer value 1 should be valid." );
		assertFalse( validator.isValid( 4, context ), "Integer value '4' should be invalid." );
		assertFalse( validator.isValid( "4", context ), "Integer value '4' should be invalid." );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testAllowedLongsMatchShouldReturnTrue() {
		OneOf annotation = createOneOf( false, null, null, new long[] { 100L, 200L, 300L }, null, null, null );
		validator.initialize( annotation );
		assertTrue( validator.isValid( "100", context ), "Long value '100' should be valid." );
		assertTrue( validator.isValid( 100L, context ), "Long value 100L should be valid." );
		assertFalse( validator.isValid( 400L, context ), "Long value '400' should be invalid." );
		assertFalse( validator.isValid( "400", context ), "Long value '400' should be invalid." );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testAllowedFloatsMatchShouldReturnTrue() {
		OneOf annotation = createOneOf( false, null, null, null, new float[] { 1.1f, 2.2f, 3.3f }, null, null );
		validator.initialize( annotation );
		assertTrue( validator.isValid( "1.1", context ), "Float value '1.1' should be valid." );
		assertTrue( validator.isValid( 1.1f, context ), "Float value 1.1f should be valid." );
		assertFalse( validator.isValid( 4.4f, context ), "Float value '4.4' should be invalid." );
		assertFalse( validator.isValid( "4.4", context ), "Float value '4.4' should be invalid." );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testAllowedDoublesMatchShouldReturnTrue() {
		OneOf annotation = createOneOf( false, null, null, null, null, new double[] { 1.11, 2.22, 3.33 }, null );
		validator.initialize( annotation );
		assertTrue( validator.isValid( "1.11", context ), "Double value '1.11' should be valid." );
		assertTrue( validator.isValid( 1.11, context ), "Double value 1.11 should be valid." );
		assertFalse( validator.isValid( 4.44, context ), "Double value '4.44' should be invalid." );
		assertFalse( validator.isValid( "4.44", context ), "Double value '4.44' should be invalid." );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testValidDto() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		OneOfDto dto = new OneOfDto();
		dto.setOneOfString( "value1" );
		dto.setOneOfInteger( 100 );
		dto.setOneOfLong( 1000L );
		dto.setOneOfDouble( 1.5 );
		dto.setOneOfFloat( 0.1f );
		dto.setOneOfEnum( TestEnum.ONE );
		dto.setOneOfIgnoreCaseString( "enabled" );

		Set<ConstraintViolation<OneOfDto>> violations = validator.validate( dto );
		assertNoViolations( violations, "The DTO should be valid" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testInvalidDto() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		OneOfDto dto = new OneOfDto();
		dto.setOneOfString( "invalid" );
		dto.setOneOfInteger( 400 );
		dto.setOneOfLong( 4000L );
		dto.setOneOfDouble( 4.5 );
		dto.setOneOfFloat( 0.4f );
		dto.setOneOfEnum( TestEnum.valueOf( "TWO" ) );
		dto.setOneOfIgnoreCaseString( "invalid" );

		Set<ConstraintViolation<OneOfDto>> violations = validator.validate( dto );
		assertFalse( violations.isEmpty(), "The DTO should be invalid" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testProgrammaticDefinitionWithString() throws Exception {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( MyClassString.class )
				.field( "myValue" )
				.constraint( new OneOfDef().allowedValues( new String[] { "value1", "value2" } ) );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<MyClassString>> constraintViolations = validator.validate( new MyClassString( "value1" ) );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( new MyClassString( "invalid" ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( OneOf.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testProgrammaticDefinitionWithInt() throws Exception {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( MyClassInt.class )
				.field( "myValue" )
				.constraint( new OneOfDef().allowedIntegers( new int[] { 1, 2, 3 } ) );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<MyClassInt>> constraintViolations = validator.validate( new MyClassInt( 1 ) );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( new MyClassInt( 4 ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( OneOf.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testProgrammaticDefinitionWithLong() throws Exception {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( MyClassLong.class )
				.field( "myValue" )
				.constraint( new OneOfDef().allowedLongs( new long[] { 100L, 200L, 300L } ) );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<MyClassLong>> constraintViolations = validator.validate( new MyClassLong( 100L ) );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( new MyClassLong( 400L ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( OneOf.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testProgrammaticDefinitionWithFloat() throws Exception {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( MyClassFloat.class )
				.field( "myValue" )
				.constraint( new OneOfDef().allowedFloats( new float[] { 1.1f, 2.2f, 3.3f } ) );


		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<MyClassFloat>> constraintViolations = validator.validate( new MyClassFloat( 1.1f ) );
		assertNoViolations( constraintViolations );


		constraintViolations = validator.validate( new MyClassFloat( 4.4f ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( OneOf.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-2073")
	public void testProgrammaticDefinitionWithDouble() throws Exception {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( MyClassDouble.class )
				.field( "myValue" )
				.constraint( new OneOfDef().allowedDoubles( new double[] { 1.11, 2.22, 3.33 } ) );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<MyClassDouble>> constraintViolations = validator.validate( new MyClassDouble( 1.11 ) );
		assertNoViolations( constraintViolations );


		constraintViolations = validator.validate( new MyClassDouble( 4.44 ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( OneOf.class )
		);
	}

	private HibernateValidatorConfiguration getConfiguration(Class<HibernateValidator> validatorClass) {
		return (HibernateValidatorConfiguration) jakarta.validation.Validation.byProvider( validatorClass ).configure();
	}

	private static class MyClassString {

		@SuppressWarnings("unused")
		private String myValue;

		public MyClassString(String myValue) {
			this.myValue = myValue;
		}
	}

	private static class MyClassInt {

		@SuppressWarnings("unused")
		private int myValue;

		public MyClassInt(int myValue) {
			this.myValue = myValue;
		}
	}

	private static class MyClassLong {

		@SuppressWarnings("unused")
		private long myValue;

		public MyClassLong(long myValue) {
			this.myValue = myValue;
		}
	}

	private static class MyClassFloat {

		@SuppressWarnings("unused")
		private float myValue;

		public MyClassFloat(float myValue) {
			this.myValue = myValue;
		}
	}

	private static class MyClassDouble {

		@SuppressWarnings("unused")
		private double myValue;

		public MyClassDouble(double myValue) {
			this.myValue = myValue;
		}
	}

	private static class MyClassEnum {

		@SuppressWarnings("unused")
		private TestEnum myValue;

		public MyClassEnum(TestEnum myValue) {
			this.myValue = myValue;
		}
	}

	private enum TestEnum {
		ONE, TWO, THREE
	}

	private OneOf createOneOf(boolean ignoreCase, String[] allowedValues, int[] allowedInts, long[] allowedLongs, float[] allowedFloats, double[] allowDoubles, Class<? extends Enum<?>> enumClass) {
		return new OneOf() {
			@Override
			public String[] allowedValues() {
				return allowedValues;
			}

			@Override
			public int[] allowedIntegers() {
				return allowedInts;
			}

			@Override
			public long[] allowedLongs() {
				return allowedLongs;
			}

			@Override
			public float[] allowedFloats() {
				return allowedFloats;
			}

			@Override
			public double[] allowedDoubles() {
				return allowDoubles;
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

	public static class OneOfDto {

		@OneOf(allowedValues = { "value1", "value2", "value3" })
		private String oneOfString;

		@OneOf(allowedIntegers = { 100, 200, 300 })
		private Integer oneOfInteger;

		@OneOf(allowedLongs = { 1000L, 2000L, 3000L })
		private Long oneOfLong;

		@OneOf(allowedDoubles = { 1.5, 2.5, 3.5 })
		private Double oneOfDouble;

		@OneOf(allowedFloats = { 0.1f, 0.2f, 0.3f })
		private Float oneOfFloat;

		@OneOf(enumClass = TestEnum.class)
		private TestEnum oneOfEnum;

		@OneOf(allowedValues = { "enabled", "disabled" }, ignoreCase = true)
		private String oneOfIgnoreCaseString;

		public String getOneOfString() {
			return oneOfString;
		}

		public void setOneOfString(String oneOfString) {
			this.oneOfString = oneOfString;
		}

		public Integer getOneOfInteger() {
			return oneOfInteger;
		}

		public void setOneOfInteger(Integer oneOfInteger) {
			this.oneOfInteger = oneOfInteger;
		}

		public Long getOneOfLong() {
			return oneOfLong;
		}

		public void setOneOfLong(Long oneOfLong) {
			this.oneOfLong = oneOfLong;
		}

		public Double getOneOfDouble() {
			return oneOfDouble;
		}

		public void setOneOfDouble(Double oneOfDouble) {
			this.oneOfDouble = oneOfDouble;
		}


		public Float getOneOfFloat() {
			return oneOfFloat;
		}

		public void setOneOfFloat(Float oneOfFloat) {
			this.oneOfFloat = oneOfFloat;
		}

		public TestEnum getOneOfEnum() {
			return oneOfEnum;
		}

		public void setOneOfEnum(TestEnum oneOfEnum) {
			this.oneOfEnum = oneOfEnum;
		}

		public String getOneOfIgnoreCaseString() {
			return oneOfIgnoreCaseString;
		}

		public void setOneOfIgnoreCaseString(String oneOfIgnoreCaseString) {
			this.oneOfIgnoreCaseString = oneOfIgnoreCaseString;
		}
	}
}
