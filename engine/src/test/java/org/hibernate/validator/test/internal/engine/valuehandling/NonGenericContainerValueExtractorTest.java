/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.UnwrapByDefault;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.testutils.CandidateForTck;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Guillaume Smet
 */
@CandidateForTck
public class NonGenericContainerValueExtractorTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValueExtractor( new FooContainerValueExtractor() )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test
	public void testValueExtraction() {
		Set<ConstraintViolation<Bean>> constraintViolations = validator.validate( Bean.valid() );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( Bean.nullFoo() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( "<map value>", true, "key", null, Map.class, 1 )
						)
		);

		// check that cascaded validation is working correctly
		constraintViolations = validator.validate( Bean.invalid() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.property( "foo", true, "key", null, Map.class, 1 )
								.property( "property" )
						),
				violationOf( Min.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.property( "foo", true, "key", null, Map.class, 1 )
								.property( "optionalInt" )
						),
				violationOf( Max.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.property( "foo", true, "key", null, Map.class, 1 )
								.property( "bar" )
								.property( "optionalLong" )
						)
		);
	}

	@UnwrapByDefault
	private static class FooContainerValueExtractor implements ValueExtractor<@ExtractedValue(type = Foo.class) FooContainer> {

		@Override
		public void extractValues(FooContainer originalValue, ValueReceiver receiver) {
			receiver.value( null, originalValue.get() );
		}
	}

	private static class Bean {

		private final Map<String, @NotNull @Valid FooContainer> map = new HashMap<>();

		private Bean(FooContainer container) {
			map.put( "key", container );
		}

		private static Bean valid() {
			return new Bean( FooContainer.valid() );
		}

		private static Bean invalid() {
			return new Bean( FooContainer.invalid() );
		}

		private static Bean nullFoo() {
			return new Bean( new FooContainer( null ) );
		}
	}

	private static class FooContainer {

		@Valid
		private final Foo foo;

		private FooContainer(Foo foo) {
			this.foo = foo;
		}

		public Foo get() {
			return foo;
		}

		private static FooContainer valid() {
			return new FooContainer( Foo.valid() );
		}

		private static FooContainer invalid() {
			return new FooContainer( Foo.invalid() );
		}
	}

	private static class Foo {

		@NotBlank
		private final String property;

		@Min(value = 5)
		private final OptionalInt optionalInt;

		@Valid
		private Bar bar;

		private Foo(String property, OptionalInt optionalInt, Bar bar) {
			this.property = property;
			this.optionalInt = optionalInt;
			this.bar = bar;
		}

		private static Foo valid() {
			return new Foo(
					"value",
					OptionalInt.of( 6 ),
					new Bar( OptionalLong.of( 6 ) )
			);
		}

		private static Foo invalid() {
			return new Foo(
					"",
					OptionalInt.of( 4 ),
					new Bar( OptionalLong.of( 18 ) )
			);
		}
	}

	private static class Bar {

		@Max(value = 16)
		private final OptionalLong optionalLong;

		private Bar(OptionalLong optionalLong) {
			this.optionalLong = optionalLong;
		}
	}
}
