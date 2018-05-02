/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.function.Predicate;

import javax.validation.Valid;
import javax.validation.constraints.Min;
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
		// Cases where no needed annotation is present:
		// 1. Object doesn't have any needed annotations
		assertFalse( predicate.test( Object.class ) );
		// 2. Simple class with no annotations
		assertFalse( predicate.test( SimpleClassWithNoAnnotations.class ) );
		// 3. Class that implements a lot of interfaces but there's no need annotations on it
		assertFalse( predicate.test( ImplementsALotOfInterfacesButNonAreAnnotated.class ) );

		// Cases where annotation could be found somewhere:
		// 1. A constraint annotation on an interface
		assertTrue( predicate.test( InterfaceWithConstraintAnnotation.class ) );
		// 2. Class that doesn't have any own annotations but implements an interface with one
		assertTrue( predicate.test( NoAnnotationsOfItsOwnButImplementsAnnotatedInterface.class ) );
		// 3. Class with deeper hierarchy and it's own annotation
		assertTrue( predicate.test( ExtendsCleanClassButHasOwnAnnotation.class ) );

		// 4. Interface with @Valid and class that implements it
		assertTrue( predicate.test( ImplementsInterfaceWithValidAnnotation.class ) );
		assertTrue( predicate.test( InterfaceWithValidAnnotation.class ) );

		// 5. Interface with constraint and class that implements it
		assertTrue( predicate.test( InterfaceWithConstraintAnnotation.class ) );
		assertTrue( predicate.test( ImplementsInterfaceWithConstraintAnnotation.class ) );

		// 6. Interface @ValidateOnExecution with and class that implements it
		assertTrue( predicate.test( ImplementsInterfaceWithValidateOnExecutionAnnotation.class ) );
		assertTrue( predicate.test( InterfaceWithValidateOnExecutionAnnotation.class ) );

		// 7. Class with annotated field:
		assertTrue( predicate.test( AnnotatedField.class ) );

		// 8. Class with annotated constructor
		assertTrue( predicate.test( AnnotatedConstructor.class ) );
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

		public void doNothing() {
		}
	}

	private static class NoAnnotationsOfItsOwnButImplementsAnnotatedInterface extends SimpleClassWithNoAnnotations implements InterfaceWithConstraintAnnotation {

		@Override
		public int bar() {
			return 0;
		}

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
		public int bar() {
			return 0;
		}
	}

	private static class ImplementsALotOfInterfacesButNonAreAnnotated implements Serializable, Predicate<Integer>, Comparable<ImplementsALotOfInterfacesButNonAreAnnotated> {

		@Override
		public int compareTo(ImplementsALotOfInterfacesButNonAreAnnotated o) {
			return 0;
		}

		@Override
		public boolean test(Integer integer) {
			return false;
		}
	}

	private static class ExtendsCleanClassButHasOwnAnnotation extends ImplementsALotOfInterfacesButNonAreAnnotated {

		@ValidateOnExecution
		public int bar() {
			return 0;
		}
	}
}
