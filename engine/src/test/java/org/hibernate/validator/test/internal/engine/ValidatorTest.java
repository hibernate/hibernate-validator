/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.GroupSequence;
import jakarta.validation.Payload;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.executable.ExecutableValidator;
import jakarta.validation.metadata.BeanDescriptor;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.hibernate.validator.testutil.CountValidationCalls;
import org.hibernate.validator.testutil.CountValidationCallsValidator;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Guillaume Smet
 */
public class ValidatorTest {
	@Test
	@TestForIssue(jiraKey = "HV-429")
	public void testValidatePropertyWithRedefinedDefaultGroupOnMainEntity() {
		Validator validator = getValidator();
		A testInstance = new A();
		testInstance.c = new C( "aaa" );

		Set<ConstraintViolation<A>> constraintViolations = validator.validateProperty( testInstance, "c.id" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Pattern.class )
						.withPropertyPath( pathWith()
								.property( "c" )
								.property( "id" )
						)
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-429")
	public void testValidatePropertyWithRedefinedDefaultGroupOnSuperClass() {
		Validator validator = getValidator();
		A testInstance = new A();
		testInstance.d = new D( "aa" );

		Set<ConstraintViolation<A>> constraintViolations = validator.validateProperty( testInstance, "d.e" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Pattern.class )
						.withPropertyPath( pathWith()
								.property( "d" )
								.property( "e" )
						)
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-429")
	public void testValidateValueWithRedefinedDefaultGroupOnMainEntity() {
		Validator validator = getValidator();
		Set<ConstraintViolation<A>> constraintViolations = validator.validateValue( A.class, "c.id", "aaa" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Pattern.class )
						.withPropertyPath( pathWith()
								.property( "c" )
								.property( "id" )
						)
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-429")
	public void testValidateValueWithRedefinedDefaultGroupOnSuperClass() {
		Validator validator = getValidator();
		Set<ConstraintViolation<A>> constraintViolations = validator.validateValue( A.class, "d.e", "aa" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Pattern.class )
						.withPropertyPath( pathWith()
								.property( "d" )
								.property( "e" )
						)
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-376")
	public void testValidatePropertyWithCurrencySymbol() {
		Validator validator = getValidator();
		Ticket testInstance = new Ticket();
		Set<ConstraintViolation<Ticket>> constraintViolations = validator.validateProperty( testInstance, "€price" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class ).withProperty( "€price" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-376")
	public void testValidateValueWithCurrencySymbol() {
		Validator validator = getValidator();
		Ticket testInstance = new Ticket();
		Set<ConstraintViolation<Ticket>> constraintViolations = validator.validateValue(
				Ticket.class, "€price", testInstance.€price
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class ).withProperty( "€price" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-208")
	public void testPropertyPathDoesNotStartWithLeadingDot() {
		Validator validator = getValidator();
		A testInstance = new A();
		Set<ConstraintViolation<A>> constraintViolations = validator.validate( testInstance );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class ).withProperty( "b" )
		);
	}

	@Test
	public void testIsBeanConstrained() {
		Validator validator = getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( B.class );
		assertTrue( beanDescriptor.isBeanConstrained() );
	}

	@Test
	@TestForIssue(jiraKey = "HV-466")
	public void testValidateInterfaceConstraintsAreValidatedOneTime() {
		CountValidationCallsValidator.init();
		Set<ConstraintViolation<H>> constraintViolations = getValidator().validate( new H() );

		assertNoViolations( constraintViolations );
		assertEquals( CountValidationCallsValidator.getNumberOfValidationCall(), 2 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-466")
	public void testValidatePropertyInterfaceConstraintsAreValidatedOneTime() {
		CountValidationCallsValidator.init();
		Set<ConstraintViolation<H>> constraintViolations = getValidator().validateProperty( new H(), "foo" );

		assertNoViolations( constraintViolations );
		assertEquals( CountValidationCallsValidator.getNumberOfValidationCall(), 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-466")
	public void testValidateValueInterfaceConstraintsAreValidatedOneTime() {
		CountValidationCallsValidator.init();
		Set<ConstraintViolation<H>> constraintViolations = getValidator().validateValue( H.class, "foo", null );

		assertNoViolations( constraintViolations );
		assertEquals( CountValidationCallsValidator.getNumberOfValidationCall(), 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-468")
	public void testPropertyPath() {
		Validator validator = getValidator();
		Foo foo = new Foo();
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( foo );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "bar" )
								.property( "alwaysNull", true, null, 0, Collection.class, 0 )
						)
		);
	}

	@Test
	public void testConstraintDefinedOnEntityNotFollowingBeanNotation() {
		CountValidationCallsValidator.init();
		Set<ConstraintViolation<NotFollowingJavaBeanNotation>> constraintViolations = getValidator().validate( new NotFollowingJavaBeanNotation() );

		// validating the whole entity
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
		);

		// using validateProperty (which is used by JSF) to validate explicitly
		constraintViolations = getValidator().validateProperty( new NotFollowingJavaBeanNotation(), "m_foo" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class ).withProperty( "m_foo" )
		);

		// using validateProperty (which is used by JSF) to validate explicitly (no violation, because there is no
		// property foo Validator knows about
		constraintViolations = getValidator().validateProperty( new NotFollowingJavaBeanNotation(), "foo" );
		assertNoViolations( constraintViolations );
	}

	@Test(expectedExceptions = ValidationException.class)
	public void testUnwrapToImplementationCausesValidationException() {
		Validator validator = getValidator();
		validator.unwrap( ValidatorImpl.class );
	}

	@Test(expectedExceptions = ValidationException.class)
	public void testUnwrapToExecutableValidatorCausesValidationException() {
		Validator validator = getValidator();
		validator.unwrap( ExecutableValidator.class );
	}

	@Test
	public void testUnwrapToPublicTypesSucceeds() {
		Validator validator = getValidator();

		Validator asValidator = validator.unwrap( Validator.class );
		assertSame( asValidator, validator );

		Object asObject = validator.unwrap( Object.class );
		assertSame( asObject, validator );
	}

	@Test
	@TestForIssue(jiraKey = "HV-596")
	public void testValidateValueWithNestedPath() {
		Validator validator = getValidator();
		Set<ConstraintViolation<X>> constraintViolations = validator.validateValue( X.class, "list[0].foo", null );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "list" )
								.property( "foo", true, null, 0 )
						)
		);

		Set<ConstraintViolation<K>> constraintViolationsK = validator.validateValue( K.class, "foo.bar", null );
		assertThat( constraintViolationsK ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "foo" )
								.property( "bar" )
						)
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1002")
	public void testValidatePropertyWithNestedPath() {
		Validator validator = getValidator();
		X someX = new X();
		someX.addZ( new Z() );
		Set<ConstraintViolation<X>> constraintViolationsX = validator.validateProperty( someX, "list[0].foo" );
		assertThat( constraintViolationsX ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "list" )
								.property( "foo", true, null, 0, null, null )
						)
		);

		I someI = new I();
		someI.putJ( "bar", new J() );
		Set<ConstraintViolation<I>> constraintViolationsI = validator.validateProperty( someI, "map[bar].foo" );
		assertThat( constraintViolationsI ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.property( "foo", true, "bar", null, null, null )
						)
		);

		K someK = new K();
		someK.foo = new L<>();
		Set<ConstraintViolation<K>> constraintViolationsK = validator.validateProperty( someK, "foo.bar" );
		assertThat( constraintViolationsK ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "foo" )
								.property( "bar" )
						)
		);

		constraintViolationsK = validator.validateProperty( someK, "foo.genericProperty" );
		assertThat( constraintViolationsK ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "foo" )
								.property( "genericProperty" )
						)
		);
	}

	@Test(expectedExceptions = { ValidationException.class }, expectedExceptionsMessageRegExp = "^HV000195:.*")
	@TestForIssue(jiraKey = "HV-1002")
	public void testValidatePropertyWithNestedPathAndNullPropertyInTheWay() {
		Validator validator = getValidator();
		X someX = new X();
		validator.validateProperty( someX, "list[0].foo" );
	}

	@Test(expectedExceptions = { IllegalArgumentException.class }, expectedExceptionsMessageRegExp = "^HV000039:.*")
	@TestForIssue(jiraKey = "HV-1002")
	public void testValidatePropertyWithNestedPathAndMissingValid() {
		Validator validator = getValidator();
		M someM = new M();
		validator.validateProperty( someM, "foo.baz" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1341")
	public void testValidatePropertyInPresenceOfClassLevelConstraint() {
		Validator validator = getValidator();

		Set<ConstraintViolation<WithClassLevelConstraint>> violations = validator.validateValue( WithClassLevelConstraint.class, "someProperty", Integer.valueOf( -1 ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Min.class ).withProperty( "someProperty" )
		);
	}

	class A {
		@NotNull
		String b;

		@Valid
		C c;

		@Valid
		D d;
	}

	class B {
		private boolean b;

		@AssertTrue
		public boolean hasB() {
			return b;
		}
	}

	@GroupSequence({ TestGroup.class, C.class })
	class C {
		@Pattern(regexp = "[0-9]+", groups = TestGroup.class)
		@Length(min = 2)
		String id;

		C(String id) {
			this.id = id;
		}
	}

	@GroupSequence({ TestGroup.class, E.class })
	class E {
		String e;

		E(String e) {
			this.e = e;
		}

		@Pattern(regexp = "[0-9]+", groups = TestGroup.class)
		public String getE() {
			return e;
		}
	}

	class D extends E {
		D(String e) {
			super( e );
		}

		@Override
		@Length(min = 2)
		public String getE() {
			return super.getE();
		}
	}

	class Ticket {
		@NotNull
		Float €price;
	}

	interface TestGroup {
	}

	interface F {
		@CountValidationCalls
		String getFoo();

		@CountValidationCalls
		String getBar();
	}

	class G implements F {
		@Override
		public String getFoo() {
			return null;
		}

		@Override
		public String getBar() {
			return null;
		}
	}

	class H extends G implements F {
	}

	class Foo {
		@Valid
		private final Collection<Bar> bar;

		public Foo() {
			bar = new ArrayList<>();
			bar.add( new Bar() );
		}
	}

	class Bar {
		@NotNull
		String alwaysNull;
	}

	class NotFollowingJavaBeanNotation {
		@NotNull
		String m_foo;

		public String getFoo() {
			return m_foo;
		}
	}

	class I {
		@Valid
		Map<String, J> map = new HashMap<>();

		public void putJ(String key, J j) {
			map.put( key, j );
		}
	}

	class J {
		@NotNull
		String foo;
	}

	class K {
		@Valid
		L<String> foo;
	}

	class L<T> {
		@NotNull
		String bar;

		@NotNull
		T genericProperty;
	}

	class M {
		N foo;

		@NotNull
		String bar;
	}

	class N {
		@NotNull
		String baz;
	}

	class X {
		@Valid
		List<Z> list = new ArrayList<>();

		public void addZ(Z z) {
			list.add( z );
		}
	}

	class Z {
		@NotNull
		String foo;
	}

	@ClassLevelConstraint
	class WithClassLevelConstraint {

		@Min(1)
		Integer someProperty;
	}

	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Repeatable(ClassLevelConstraint.List.class)
	@Constraint( validatedBy = ClassLevelConstraint.Validator.class )
	public @interface ClassLevelConstraint {

		String message() default "Invalid order shipment date must be after order date";
		Class<?>[] groups() default {};
		Class<? extends Payload>[] payload() default {};

		@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
		@Retention(RUNTIME)
		@interface List {
			ClassLevelConstraint[] value();
		}

		class Validator implements ConstraintValidator<ClassLevelConstraint, WithClassLevelConstraint> {

			@Override
			public boolean isValid(WithClassLevelConstraint value, ConstraintValidatorContext context) {
				return false;
			}
		}
	}
}
