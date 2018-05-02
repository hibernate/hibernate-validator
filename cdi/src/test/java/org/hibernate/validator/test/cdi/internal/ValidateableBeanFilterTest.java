/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

import org.hibernate.validator.cdi.internal.ValidateableBeanFilter;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class ValidateableBeanFilterTest {

	private Predicate<Class<?>> predicate;

	@BeforeMethod
	public void setUp() {
		this.predicate = new ValidateableBeanFilter();
	}

	@Test
	public void test() {
		// Cases where no required annotations are present:
		// 1. Object doesn't have any required annotations
		assertFalse( predicate.test( Object.class ) );
		// 2. Simple class with no annotations
		assertFalse( predicate.test( SimpleClassWithNoAnnotations.class ) );
		// 3. Class that implements a lot of interfaces but there's no required annotations on it
		assertFalse( predicate.test( ImplementsALotOfInterfacesButNoneAreAnnotated.class ) );

		// Cases where annotation could be found somewhere:
		// 1. A constraint annotation on an interface
		assertTrue( predicate.test( InterfaceWithConstraintAnnotation.class ) );
		// 2. Class that doesn't have any own annotations but implements an interface with one
		assertTrue( predicate.test( NoAnnotationsOfItsOwnButImplementsAnnotatedInterface.class ) );
		// 3. Class with deeper hierarchy and its own annotation
		assertTrue( predicate.test( ExtendsCleanClassButHasOwnAnnotation.class ) );

		// 4. Interface with @Valid and class that implements it
		assertTrue( predicate.test( ImplementsInterfaceWithValidAnnotation.class ) );
		assertTrue( predicate.test( InterfaceWithValidAnnotation.class ) );

		// 5. Interface with constraint and class that implements it
		assertTrue( predicate.test( InterfaceWithConstraintAnnotation.class ) );
		assertTrue( predicate.test( ImplementsInterfaceWithConstraintAnnotation.class ) );

		// 6. Interface with @ValidateOnExecution and class that implements it
		assertTrue( predicate.test( ImplementsInterfaceWithValidateOnExecutionAnnotation.class ) );
		assertTrue( predicate.test( InterfaceWithValidateOnExecutionAnnotation.class ) );

		// 7. Class with annotated field
		assertTrue( predicate.test( AnnotatedField.class ) );

		// 8. Class with annotated constructor
		assertTrue( predicate.test( AnnotatedConstructor.class ) );

		// 9. Annotated type arguments
		assertTrue( predicate.test( TypeArgumentField.class ) );
		assertTrue( predicate.test( TypeArgumentReturnValue.class ) );
		assertTrue( predicate.test( TypeArgumentMethodParameter.class ) );
		assertTrue( predicate.test( TypeArgumentConstructorParameter.class ) );
		assertTrue( predicate.test( NestedTypeArgumentField.class ) );
		assertTrue( predicate.test( DeepNestedAnnotatedMethodParameter.class ) );

		// 10. Custom user constraint:
		assertTrue( predicate.test( BeanWithCustomConstraintOnParameter.class ) );
		assertTrue( predicate.test( BeanWithCustomConstraintOnField.class ) );
		assertTrue( predicate.test( BeanWithCustomConstraintOnReturnValue.class ) );
	}

	@SuppressWarnings("unused")
	private static class AnnotatedMethodParameter {
		void doSomething(@NotNull String string) {
		}
	}

	private static class DeepNestedAnnotatedMethodParameter {
		@SuppressWarnings("unused")
		void doSomething(List<Map<String, List<Optional<Map<String, @NotNull String>>>>> strings) {
		}
	}

	private static class TypeArgumentField {
		@SuppressWarnings("unused")
		private List<@NotNull String> strings;
	}

	private static class NestedTypeArgumentField {
		@SuppressWarnings("unused")
		private List<Map<String, @NotNull String>> strings;
	}

	private static class TypeArgumentReturnValue {
		@SuppressWarnings("unused")
		List<@NotNull String> strings() {
			return null;
		}
	}

	private static class TypeArgumentMethodParameter {
		@SuppressWarnings("unused")
		void strings(List<@NotNull String> strings) {
		}
	}

	private static class TypeArgumentConstructorParameter {
		@SuppressWarnings("unused")
		TypeArgumentConstructorParameter(List<@NotNull String> strings) {
		}
	}

	private static class AnnotatedConstructor {
		@Valid
		public AnnotatedConstructor() {
		}
	}

	private static class AnnotatedField {
		@Min(10)
		private int num;
	}

	private static class SimpleClassWithNoAnnotations {

		@SuppressWarnings("unused")
		public void doNothing() {
		}
	}

	private static class NoAnnotationsOfItsOwnButImplementsAnnotatedInterface extends SimpleClassWithNoAnnotations implements InterfaceWithConstraintAnnotation {

		@Override
		public int bar() {
			return 0;
		}

		@SuppressWarnings("unused")
		public void doSomething(int foo) {
		}
	}

	interface InterfaceWithConstraintAnnotation {

		@Min(10)
		int bar();
	}

	private static class ImplementsInterfaceWithConstraintAnnotation implements InterfaceWithConstraintAnnotation {

		@Override
		public int bar() {
			return 0;
		}
	}

	interface InterfaceWithValidAnnotation {

		@Valid
		SimpleClassWithNoAnnotations bar();
	}

	private static class ImplementsInterfaceWithValidAnnotation implements InterfaceWithValidAnnotation {

		@Override
		public SimpleClassWithNoAnnotations bar() {
			return null;
		}
	}

	@ValidateOnExecution(type = ExecutableType.NON_GETTER_METHODS)
	interface InterfaceWithValidateOnExecutionAnnotation {

		int bar();
	}

	private static class ImplementsInterfaceWithValidateOnExecutionAnnotation implements InterfaceWithValidateOnExecutionAnnotation {
		@Override
		public int bar() {
			return 0;
		}
	}

	private static class ImplementsALotOfInterfacesButNoneAreAnnotated implements Serializable, Predicate<Integer>, Comparable<ImplementsALotOfInterfacesButNoneAreAnnotated> {

		@Override
		public int compareTo(ImplementsALotOfInterfacesButNoneAreAnnotated o) {
			return 0;
		}

		@Override
		public boolean test(Integer integer) {
			return false;
		}
	}

	private static class ExtendsCleanClassButHasOwnAnnotation extends ImplementsALotOfInterfacesButNoneAreAnnotated {

		@ValidateOnExecution
		public int bar() {
			return 0;
		}
	}

	/**
	 * This class and {@link ValidNumber} is taken from Felix tests, where the issue was initially
	 * discovered.
	 */
	public static class BeanWithCustomConstraintOnParameter {

		public void doDefault(@ValidNumber String number) {
		}
	}

	public static class BeanWithCustomConstraintOnField {
		private @ValidNumber String number;
	}


	public static class BeanWithCustomConstraintOnReturnValue {
		@ValidNumber
		String number() {
			return null;
		}
	}

	@Documented
	@Target({ ANNOTATION_TYPE, METHOD, FIELD, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	@Constraint(validatedBy = {})
	@Size(min = 3, message = "Must be 3 at least")
	@Pattern(regexp = "[0-9]*")
	@NotNull(message = "Cannot be null")
	public @interface ValidNumber {

		String message() default "invalid number";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};

		String value() default "";
	}
}
