/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.tracking;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.PredefinedScopeHibernateValidator;
import org.hibernate.validator.PredefinedScopeHibernateValidatorFactory;
import org.hibernate.validator.internal.engine.PredefinedScopeValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.tracking.ProcessedBeansTrackingStrategy;

import org.testng.annotations.Test;

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
 * An arrow, ->, indicates a cascading constraint.ProcessedBeansTrackingCyclesNoCyclesMapTest
 *
 * The following are the properties with cascading Map constraints:
 * A.bToEs
 * B.cToCs
 * C.dToFs
 * D.bToBs
 * E.fToGs
 *  .gToGs
 * F.gToGs
 *
 * @author Gail Badner
 *
 */

public class ProcessedBeansTrackingCyclesNoCyclesMapTest {

	@Test
	public void testTrackingEnabled() {
		final ProcessedBeansTrackingStrategy processedBeansTrackingStrategy = getProcessedBeansTrackingStrategy();

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

		a.bToEs.put( b, e );
		b.cToCs.put( c, c );
		c.dToFs.put( d, f );
		d.bToBs.put( b, b );
		e.fToGs.put( f, g );
		e.gToGs.put( g, g );

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

	private ProcessedBeansTrackingStrategy getProcessedBeansTrackingStrategy() {
		return ( (PredefinedScopeValidatorFactoryImpl) getValidatorFactory() ).getBeanMetaDataManager().getProcessedBeansTrackingStrategy();
	}

	private static class A {

		private String description;

		private Map<@Valid B, @Valid E> bToEs = new HashMap<>();

	}

	private static class B {
		@Valid
		private String description;

		private Map<@Valid C, @Valid C> cToCs = new HashMap<>();
	}

	private static class C {

		private String description;

		private Map<@Valid D, @Valid F> dToFs = new HashMap<>();
	}

	private static class D {

		private String description;

		private Map<@Valid B, @Valid B> bToBs = new HashMap<>();
	}

	private static class E {

		private String description;

		private Map<@Valid F, G> fToGs = new HashMap<>();

		private Map<@Valid G, @Valid G> gToGs = new HashMap<>();
	}

	private static class F {

		private String description;

		private Map<@Valid G, @Valid G> gToGs = new HashMap<>();
	}

	private static class G {

		private String description;
	}
}
