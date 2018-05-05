/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.aspectj.validation.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.aspectj.validation.Validate;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class AspectValidationTest {

	@Test
	public void testValidationOfMethodParameters() throws Exception {
		Foo foo = new Foo( "", 1 );
		assertThatThrownBy( () -> foo.doNoting( null ) )
				.isInstanceOf( ConstraintViolationException.class )
				.hasMessageContaining( "must not be null" );
	}

	@Test
	public void testValidationOnMethodReturnValue() throws Exception {
		Foo foo = new Foo( null, 1 );
		assertThatThrownBy( () -> foo.getString() )
				.isInstanceOf( ConstraintViolationException.class )
				.hasMessageContaining( "must not be null" );
	}

	@Test
	public void testValidationOnConstructorParameters() throws Exception {
		assertThatThrownBy( () -> new Foo( 1 ) )
				.isInstanceOf( ConstraintViolationException.class )
				.hasMessageContaining( "must be greater than or equal to 10" );
	}

	@Test
	public void testValidationOnConstructorReturnValue() throws Exception {
		assertThatThrownBy( () -> new Foo( "" ) )
				.isInstanceOf( ConstraintViolationException.class )
				.hasMessageContaining( "size must be between 100 and 1000" );
	}

	public static class Foo {

		@Size(min = 100, max = 1000)
		private final String string;
		private final int number;

		public Foo(String string, int number) {
			this.string = string;
			this.number = number;
		}

		@Validate
		public Foo(@Min(10) int number) {
			this.number = number;
			this.string = Integer.toString( number );
		}

		@Validate
		@Valid
		public Foo(String string) {
			this.number = 10;
			this.string = string;
		}

		@NotNull
		@Validate
		public String getString() {
			return string;
		}

		@Validate
		public void doNoting(@NotNull String justString) {

		}

		public int getNumber() {
			return number;
		}
	}
}
