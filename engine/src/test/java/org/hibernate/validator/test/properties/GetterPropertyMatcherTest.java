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
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.spi.properties.ConstrainableExecutable;
import org.hibernate.validator.spi.properties.GetterPropertyMatcher;
import org.hibernate.validator.testutil.ConstraintViolationAssert;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class GetterPropertyMatcherTest {

	@Test
	public void testGetterPropertyMatcher() throws Exception {
		GetterPropertyMatcher filter = new FooGetterPropertyMatcher();

		ConstrainableExecutableImpl fooMethod = new ConstrainableExecutableImpl( Foo.class.getDeclaredMethod( "fooMethod" ) );
		ConstrainableExecutableImpl getMethod = new ConstrainableExecutableImpl( Foo.class.getDeclaredMethod( "getMethod" ) );

		assertThat( filter.isProperty( fooMethod ) ).isTrue();
		assertThat( filter.isProperty( getMethod ) ).isFalse();

		assertThat( filter.getPropertyName( fooMethod ) ).isEqualTo( "method" );
	}

	@Test
	public void testConfigureGetterPropertyMatcher() throws Exception {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.getterPropertyMatcher( new FooGetterPropertyMatcher() )
				.buildValidatorFactory()
				.getValidator();
		Set<ConstraintViolation<Foo>> violations = validator.validate( new Foo() );

		ConstraintViolationAssert.assertThat( violations ).containsOnlyViolations(
				violationOf( Min.class )
		);
	}

	@Test
	public void testConfigureGetterPropertyMatcherWithProperty() throws Exception {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addProperty( HibernateValidatorConfiguration.GETTER_PROPERTY_MATCHER_CLASSNAME, FooGetterPropertyMatcher.class.getName() )
				.buildValidatorFactory()
				.getValidator();
		Set<ConstraintViolation<Foo>> violations = validator.validate( new Foo() );

		ConstraintViolationAssert.assertThat( violations ).containsOnlyViolations(
				violationOf( Min.class )
		);
	}

	@Test
	public void testNoPrefixGetterPropertyMatcher() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.getterPropertyMatcher( new NoPrefixGetterPropertyMatcher() )
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

	public static class NoPrefixGetterPropertyMatcher implements GetterPropertyMatcher {

		@Override
		public boolean isProperty(ConstrainableExecutable executable) {
			String name = executable.getName();
			return executable.getParameterTypes().length == 0
					&& !name.startsWith( "is" )
					&& !name.startsWith( "get" );
		}

		@Override
		public String getPropertyName(ConstrainableExecutable method) {
			return method.getName();
		}
	}

	public static class FooGetterPropertyMatcher implements GetterPropertyMatcher {

		@Override
		public boolean isProperty(ConstrainableExecutable executable) {
			return executable.getParameterTypes().length == 0
					&& executable.getName().startsWith( "foo" );
		}

		@Override
		public String getPropertyName(ConstrainableExecutable method) {
			char[] chars = method.getName().substring( 3 ).toCharArray();
			chars[0] = Character.toLowerCase( chars[0] );
			return new String( chars );
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
		public Type[] getParameterTypes() {
			return method.getParameterTypes();
		}
	}
}
