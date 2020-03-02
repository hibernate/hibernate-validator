/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.spi.properties.ConstrainableExecutable;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;
import org.hibernate.validator.testutil.ConstraintViolationAssert;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class GetterPropertySelectionStrategyTest {

	@Test
	public void testGetterPropertySelectionStrategy() throws Exception {
		GetterPropertySelectionStrategy strategy = new FooGetterPropertySelectionStrategy();

		ConstrainableExecutableImpl fooMethod = new ConstrainableExecutableImpl( Foo.class.getDeclaredMethod( "fooMethod" ) );
		ConstrainableExecutableImpl getMethod = new ConstrainableExecutableImpl( Foo.class.getDeclaredMethod( "getMethod" ) );

		assertThat( strategy.getProperty( fooMethod ) ).isPresent();
		assertThat( strategy.getProperty( getMethod ) ).isNotPresent();

		assertThat( strategy.getProperty( fooMethod ).get() ).isEqualTo( "method" );
	}

	@Test
	public void testConfigureGetterPropertySelectionStrategy() throws Exception {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.getterPropertySelectionStrategy( new FooGetterPropertySelectionStrategy() )
				.buildValidatorFactory()
				.getValidator();
		Set<ConstraintViolation<Foo>> violations = validator.validate( new Foo() );

		ConstraintViolationAssert.assertThat( violations ).containsOnlyViolations(
				violationOf( Min.class )
		);
	}

	@Test
	public void testConfigureGetterPropertySelectionStrategyWithProperty() throws Exception {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addProperty( HibernateValidatorConfiguration.GETTER_PROPERTY_SELECTION_STRATEGY_CLASSNAME, FooGetterPropertySelectionStrategy.class.getName() )
				.buildValidatorFactory()
				.getValidator();
		Set<ConstraintViolation<Foo>> violations = validator.validate( new Foo() );

		ConstraintViolationAssert.assertThat( violations ).containsOnlyViolations(
				violationOf( Min.class )
		);
	}

	@Test
	public void testNoPrefixGetterPropertySelectionStrategy() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.getterPropertySelectionStrategy( new NoPrefixGetterPropertySelectionStrategy() )
				.buildValidatorFactory()
				.getValidator();
		Set<ConstraintViolation<NoPrefixFoo>> violations = validator.validate( new NoPrefixFoo() );

		ConstraintViolationAssert.assertThat( violations ).containsOnlyViolations(
				violationOf( Min.class ).withProperty( "test" ),
				violationOf( NotBlank.class ).withProperty( "name" )
		);
	}

	@Test
	public void testGetterAndFieldWithSamePropertyNameButDifferentTypes() {
		Validator validator = ValidatorUtil.getValidator();

		Set<ConstraintViolation<Bar>> violations = validator.validate( new Bar() );

		ConstraintViolationAssert.assertThat( violations ).containsOnlyViolations(
				violationOf( NotEmpty.class ).withProperty( "tags" ),
				violationOf( Size.class ).withProperty( "tags" )
		);
	}

	@Test
	public void testStrangePropertyNames() {
		class StrangeProeprties {
			@AssertTrue
			public boolean isHasGet() {
				return false;
			}

			@AssertTrue
			public boolean hasIsGet() {
				return false;
			}

			@NotNull
			public String getHasIs() {
				return null;
			}
		}

		Validator validator = ValidatorUtil.getValidator();

		Set<ConstraintViolation<StrangeProeprties>> violations = validator.validate( new StrangeProeprties() );

		ConstraintViolationAssert.assertThat( violations ).containsOnlyViolations(
				violationOf( AssertTrue.class ).withProperty( "hasGet" ),
				violationOf( AssertTrue.class ).withProperty( "isGet" ),
				violationOf( NotNull.class ).withProperty( "hasIs" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-622")
	public void testIsGetterMethod() throws Exception {
		class Bar {
			@NotNull
			public String getBar() {
				return null;
			}

			@NotEmpty
			public String getBar(String param) {
				return null;
			}
		}
		Validator validator = ValidatorUtil.getValidator();

		Set<ConstraintViolation<Bar>> violations = validator.validate( new Bar() );

		ConstraintViolationAssert.assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withProperty( "bar" )
		);
	}

	private static class Bar {

		@Size(min = 10, max = 100)
		private final String tags = "";

		@NotEmpty
		public List<String> getTags() {
			return Arrays.stream( tags.split( "," ) )
					.filter( tag -> !tag.trim().isEmpty() )
					.collect( Collectors.toList() );
		}
	}

	private static class Foo {

		@Min(10)
		public int fooMethod() {
			return 1;
		}

		@Max(-10)
		public int getMethod() {
			return 1;
		}
	}

	private static class NoPrefixFoo {

		@Min(10)
		public int test() {
			return 1;
		}

		@NotBlank
		public String name() {
			return "";
		}

		@NotBlank
		public String getName() {
			return "";
		}

		@AssertTrue
		public boolean isTest() {
			return false;
		}

		@Max(-10)
		public int getTest() {
			return 1;
		}
	}

	public static class NoPrefixGetterPropertySelectionStrategy implements GetterPropertySelectionStrategy {

		@Override
		public Optional<String> getProperty(ConstrainableExecutable executable) {
			if ( executable.getReturnType() == void.class
					|| executable.getParameterTypes().length > 0
					|| executable.getName().startsWith( "is" )
					|| executable.getName().startsWith( "get" ) ) {
				return Optional.empty();
			}

			return Optional.of( executable.getName() );
		}

		@Override
		public Set<String> getGetterMethodNameCandidates(String propertyName) {
			return Collections.singleton( propertyName );
		}
	}

	public static class FooGetterPropertySelectionStrategy implements GetterPropertySelectionStrategy {

		@Override
		public Optional<String> getProperty(ConstrainableExecutable executable) {
			if ( executable.getParameterTypes().length > 0
					|| !executable.getName().startsWith( "foo" ) ) {
				return Optional.empty();
			}

			char[] chars = executable.getName().substring( 3 ).toCharArray();
			chars[0] = Character.toLowerCase( chars[0] );
			return Optional.of( new String( chars ) );
		}

		@Override
		public Set<String> getGetterMethodNameCandidates(String propertyName) {
			return Collections.singleton( "foo" + Character.toUpperCase( propertyName.charAt( 0 ) ) + propertyName.substring( 1 ) );
		}
	}

	private static class ConstrainableExecutableImpl implements ConstrainableExecutable {

		private final Method method;

		private ConstrainableExecutableImpl(Method method) {
			this.method = method;
		}

		@Override
		public Class<?> getReturnType() {
			return method.getReturnType();
		}

		@Override
		public String getName() {
			return method.getName();
		}

		@Override
		public Class<?>[] getParameterTypes() {
			return method.getParameterTypes();
		}
	}
}
