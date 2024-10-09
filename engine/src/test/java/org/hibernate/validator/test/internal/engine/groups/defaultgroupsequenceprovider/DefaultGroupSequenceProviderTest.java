/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.defaultgroupsequenceprovider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.GroupDefinitionException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class DefaultGroupSequenceProviderTest {

	private static Validator validator;

	@BeforeClass
	public static void init() {
		validator = getValidator();
	}

	@Test(
			expectedExceptions = GroupDefinitionException.class,
			expectedExceptionsMessageRegExp = ".* must be part of the redefined default group sequence."
	)
	public void testNullProviderDefaultGroupSequence() {
		validator.validate( new A() );
	}

	@Test(
			expectedExceptions = GroupDefinitionException.class,
			expectedExceptionsMessageRegExp = ".* must be part of the redefined default group sequence."
	)
	public void testNotValidProviderDefaultGroupSequenceDefinition() {
		validator.validate( new B() );
	}

	@Test(
			expectedExceptions = GroupDefinitionException.class,
			expectedExceptionsMessageRegExp = "HV[0-9]*: The default group sequence provider defined for .* has the wrong type"
	)
	public void testDefinitionOfDefaultGroupSequenceProviderWithWrongType() {
		validator.validate( new D() );
	}

	@Test
	public void testValidateUserProviderDefaultGroupSequence() {
		User user = new User( "$password" );
		Set<ConstraintViolation<User>> violations = validator.validate( user );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Pattern.class ).withMessage( "must match \"\\w+\"" )
		);

		User admin = new User( "short", true );
		violations = validator.validate( admin );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Length.class ).withMessage( "length must be between 10 and 20" )
		);
	}

	@SuppressWarnings("removal")
	@Deprecated(forRemoval = true)
	@Test
	public void testValidateUserProviderDefaultGroupSequenceDeprecated() {
		DeprecatedUser user = new DeprecatedUser( "$password" );
		Set<ConstraintViolation<DeprecatedUser>> violations = validator.validate( user );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Pattern.class ).withMessage( "must match \"\\w+\"" )
		);

		DeprecatedUser admin = new DeprecatedUser( "short", true );
		violations = validator.validate( admin );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Length.class ).withMessage( "length must be between 10 and 20" )
		);
	}

	@Test
	public void testValidatePropertyUserProviderDefaultGroupSequence() {
		User user = new User( "$password" );
		Set<ConstraintViolation<User>> violations = validator.validateProperty( user, "password" );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Pattern.class ).withMessage( "must match \"\\w+\"" )
		);

		User admin = new User( "short", true );
		violations = validator.validateProperty( admin, "password" );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Length.class ).withMessage( "length must be between 10 and 20" )
		);
	}

	@Test
	public void testValidateValueUserProviderDefaultGroupSequence() {
		Set<ConstraintViolation<User>> violations = validator.validateValue(
				User.class, "password", "$password"
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( Pattern.class ).withMessage( "must match \"\\w+\"" )
		);
	}

	@Test
	public void testValidateReturnValueProviderDefaultGroupSequence() throws NoSuchMethodException {
		C c = new CImpl();
		Method fooMethod = C.class.getDeclaredMethod( "foo", String.class );

		Set<ConstraintViolation<C>> violations = validator.forExecutables().validateReturnValue(
				c, fooMethod, c.foo( null )
		);
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withMessage( "must not be null" )
		);

		violations = validator.forExecutables().validateReturnValue( c, fooMethod, c.foo( "foo" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Length.class ).withMessage( "length must be between 10 and 20" )
		);
	}

	@Test
	public void testGenericGroupSequenceProvider() {
		GenericBean1 bean1 = new GenericBean1();

		assertThat( validator.validate( bean1 ) ).containsOnlyViolations(
				violationOf( NotNull.class )
		);

		GenericGroupSequenceProvider.key.set( false );
		assertNoViolations( validator.validate( bean1 ) );

		GenericBean2 bean2 = new GenericBean2( "" );

		assertThat( validator.validate( bean2 ) ).containsOnlyViolations(
				violationOf( Positive.class )
		);

		GenericGroupSequenceProvider.key.set( true );
		assertThat( validator.validate( bean2 ) ).containsOnlyViolations(
				violationOf( Negative.class )
		);

		// validation stopped on the first (default) group validation.
		assertThat( validator.validate( new GenericBean2( null ) ) ).containsOnlyViolations(
				violationOf( NotNull.class )
		);

		assertThat( validator.validateValue( GenericBean2.class, "number", 0 ) ).containsOnlyViolations(
				violationOf( Negative.class )
		);
	}

	@Test
	public void testNothingImplemented() {
		assertThatThrownBy( () -> validator.validate( new DummyBean() ) )
				.isInstanceOf( GroupDefinitionException.class );
	}

	@GroupSequenceProvider(NullGroupSequenceProvider.class)
	private static class A {
		@NotNull
		String c;

		@NotNull(groups = TestGroup.class)
		String d;
	}

	@GroupSequenceProvider(InvalidGroupSequenceProvider.class)
	private static class B {
	}

	private interface C {

		@NotNull(message = "must not be null")
		@Length(min = 10, max = 20, groups = TestGroup.class, message = "length must be between {min} and {max}")
		String foo(String param);
	}

	@GroupSequenceProvider(MethodGroupSequenceProvider.class)
	private static class CImpl implements C {

		@Override
		public String foo(String param) {
			return param;
		}
	}

	@GroupSequenceProvider(NullGroupSequenceProvider.class)
	private static class D {
	}

	private interface TestGroup {
	}

	public static class MethodGroupSequenceProvider implements DefaultGroupSequenceProvider<CImpl> {

		@Override
		public List<Class<?>> getValidationGroups(Class<?> klass, CImpl object) {
			return Arrays.<Class<?>>asList( TestGroup.class, CImpl.class );
		}
	}

	public static class NullGroupSequenceProvider implements DefaultGroupSequenceProvider<A> {

		@Override
		public List<Class<?>> getValidationGroups(Class<?> klass, A object) {
			return null;
		}
	}

	public static class InvalidGroupSequenceProvider implements DefaultGroupSequenceProvider<B> {

		@Override
		public List<Class<?>> getValidationGroups(Class<?> klass, B object) {
			List<Class<?>> defaultGroupSequence = new ArrayList<Class<?>>();
			defaultGroupSequence.add( TestGroup.class );

			return defaultGroupSequence;
		}
	}

	/*
	 * The idea is that the same group sequence provider can be applied to different, possibly unrelated, beans.
	 * Because of that the sequence provider implementation does not know the bean type,
	 * and passed in object value may be {@code null}, hence it wouldn't be possible to obtain the type info from the instance.
	 * {@code null} instance can be passed in case of validating values, or on the startup when the defaults are initialized.
	 */
	public static class GenericGroupSequenceProvider implements DefaultGroupSequenceProvider<Object> {

		public static final AtomicBoolean key = new AtomicBoolean( true );

		@Override
		public List<Class<?>> getValidationGroups(Class<?> clazz, Object object) {
			if ( key.get() ) {
				return List.of( clazz, GenericGroup1.class );
			}
			else {
				return List.of( clazz, GenericGroup2.class );
			}
		}
	}

	interface GenericGroup1 {
	}

	interface GenericGroup2 {
	}

	@GroupSequenceProvider(GenericGroupSequenceProvider.class)
	private static class GenericBean1 {
		@NotNull(groups = GenericGroup1.class)
		@Null(groups = GenericGroup2.class)
		String string;
	}

	@GroupSequenceProvider(GenericGroupSequenceProvider.class)
	private static class GenericBean2 {

		private String string;

		public GenericBean2(String string) {
			this.string = string;
		}

		@Negative(groups = GenericGroup1.class)
		@Positive(groups = GenericGroup2.class)
		public int getNumber() {
			return 0;
		}

		@NotNull
		public String getString() {
			return string;
		}
	}

	public static class DummyGroupSequenceProvider implements DefaultGroupSequenceProvider<Object> {

	}

	@GroupSequenceProvider(DummyGroupSequenceProvider.class)
	private static class DummyBean {
	}
}
