/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.engine.HibernateValidatorEnhancedBean;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
@TestForIssue(jiraKey = "HV-1680")
public class EnhancedBeanAccessorTest {
	@Test
	public void testValidatePropertyWithRedefinedDefaultGroupOnMainEntity() {
		Validator validator = getValidator();
		Foo foo = new Bar();

		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( foo );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Pattern.class ).withProperty( "string" ),
				violationOf( Positive.class ).withProperty( "num" ),
				violationOf( Min.class ).withProperty( "looooong" ),
				violationOf( Length.class ).withProperty( "message" ),
				violationOf( AssertTrue.class ).withProperty( "key" ),
				violationOf( NotEmpty.class ).withProperty( "strings" )
		);
	}

	private static class Bar extends Foo implements HibernateValidatorEnhancedBean {
		@NotEmpty
		private final List<String> strings;

		private Bar() {
			this.strings = Collections.emptyList();
		}

		@Override
		public Object $$_hibernateValidator_getFieldValue(String name) {
			if ( "strings".equals( name ) ) {
				return strings;
			}
			return super.$$_hibernateValidator_getFieldValue( name );
		}
	}

	private static class Foo implements HibernateValidatorEnhancedBean {
		@Pattern(regexp = "[A-Z]")
		private String string;
		@Positive
		private Integer num;
		@Min(100_000L)
		private long looooong;

		public Foo() {
			this( "test", -1, 100L );
		}

		public Foo(final String string, final Integer num, final long looooong) {
			this.string = string;
			this.num = num;
			this.looooong = looooong;
		}

		@Length(min = 100)
		public String getMessage() {
			return "messssssage";
		}

		@AssertTrue
		public boolean getKey() {
			return false;
		}

		@Override
		public Object $$_hibernateValidator_getFieldValue(String name) {
			if ( "string".equals( name ) ) {
				return string;
			}
			if ( "num".equals( name ) ) {
				return num;
			}
			if ( "looooong".equals( name ) ) {
				return looooong;
			}
			throw new IllegalArgumentException( "No such property as '" + name + "'" );
		}

		@Override
		public Object $$_hibernateValidator_getGetterValue(String name) {
			if ( "getKey".equals( name ) ) {
				return getKey();
			}
			if ( "getMessage".equals( name ) ) {
				return getMessage();
			}
			throw new IllegalArgumentException( "No such property as '" + name + "'" );
		}
	}
}
