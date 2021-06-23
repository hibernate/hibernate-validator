/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.tracking;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.PredefinedScopeHibernateValidator;
import org.hibernate.validator.PredefinedScopeHibernateValidatorFactory;
import org.hibernate.validator.internal.engine.PredefinedScopeValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.ValidatorFactoryScopedContext;
import org.hibernate.validator.internal.engine.tracking.ProcessedBeansTrackingStrategy;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * An example of beans with cascading constraints, some cycle and others do not.
 *
 * A -> B ---> C ------> F -> G <-
 * |    ^      |         ^    ^  |
 * |    |      |         |    |  |
 * |    -- D <--         |    |  |
 * --------------------> E -------
 *
 * A, B, C, D, E, F, and G are beans that get validated.
 *
 * An arrow, ->, indicates a cascading constraint.
 *
 * The following are the properties with cascading constraints:
 * A.b
 *  .e
 * B.c
 * C.d
 *  .f
 * D.b
 * E.f
 *  .g
 * F.g
 *
 * @author Gail Badner
 *
 */

public class ProcessedBeansTrackingCyclesNoCyclesTest {

	@Test
	public void testTrackingEnabled() {

		final ValidatorFactoryScopedContext validatorFactoryScopedContext = getValidatorFactoryScopedContext();
		final ProcessedBeansTrackingStrategy processedBeansTrackingStrategy =
				validatorFactoryScopedContext.getProcessedBeansTrackingStrategy();
		assertTrue( processedBeansTrackingStrategy.isEnabledForBean(
				A.class,
				true
		) );
		assertTrue( processedBeansTrackingStrategy.isEnabledForBean(
				B.class,
				true
		) );
		assertTrue( processedBeansTrackingStrategy.isEnabledForBean(
				C.class,
				true
		) );
		assertTrue( processedBeansTrackingStrategy.isEnabledForBean(
				D.class,
				true
		) );
		assertFalse( processedBeansTrackingStrategy.isEnabledForBean(
				E.class,
				true
		) );
		assertFalse( processedBeansTrackingStrategy.isEnabledForBean(
				F.class,
				true
		) );
		assertFalse( processedBeansTrackingStrategy.isEnabledForBean(
				G.class,
				false
		) );
	}

	@Test
	public void testValidate() {
		final A a = new A();
		final B b = new B();
		final C c = new C();
		final D d = new D();
		final E e = new E();
		final F f = new F();
		final G g = new G();

		a.b = b;
		a.e = e;
		b.c = c;
		c.d = d;
		d.b = b;
		e.f = f;
		e.g = g;
		e.gAnother = g;
		f.g = g;

		final Validator validator = getValidator();
		final Set<ConstraintViolation<A>> violationsA = validator.validate( a );
		final Set<ConstraintViolation<B>> violationsB = validator.validate( b );
		final Set<ConstraintViolation<C>> violationsC = validator.validate( c );
		final Set<ConstraintViolation<D>> violationsD = validator.validate( d );
		final Set<ConstraintViolation<E>> violationsE = validator.validate( e );
		final Set<ConstraintViolation<F>> violationsF = validator.validate( f );
		final Set<ConstraintViolation<G>> violationsG = validator.validate( g );
	}

	private Validator getValidator() {
		return getValidatorFactory().getValidator();
	}

	private PredefinedScopeHibernateValidatorFactory getValidatorFactory() {
		return Validation.byProvider( PredefinedScopeHibernateValidator.class )
				.configure()
				.builtinConstraints( new HashSet<>( Arrays.asList( NotNull.class.getName() ) ) )
				.initializeBeanMetaData( new HashSet<>( Arrays.asList(
						A.class, B.class, C.class, D.class, E.class, F.class, G.class
				) ) )
				.buildValidatorFactory().unwrap( PredefinedScopeHibernateValidatorFactory.class );
	}

	private ValidatorFactoryScopedContext getValidatorFactoryScopedContext() {
		return ( (PredefinedScopeValidatorFactoryImpl) getValidatorFactory() ).getValidatorFactoryScopedContext();
	}

	private static class A {

		private String description;

		@Valid
		private B b;

		@Valid
		private E e;
	}

	private static class B {
		@Valid
		private String description;

		@Valid
		private C c;
	}

	private static class C {

		private String description;

		@Valid
		private D d;

		@Valid
		private F f;
	}

	private static class D {

		private String description;

		@Valid
		private B b;
	}

	private static class E {

		private String description;

		@Valid
		private F f;

		@Valid
		private G g;

		@Valid
		private G gAnother;
	}

	private static class F {

		private String description;

		@Valid
		private G g;
	}

	private static class G {

		private String description;
	}
}
