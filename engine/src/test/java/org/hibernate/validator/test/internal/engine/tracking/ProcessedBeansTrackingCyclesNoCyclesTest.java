/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.tracking;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

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
		assertThat( validator.validate( a ) ).isEmpty();
		assertThat( validator.validate( b ) ).isEmpty();
		assertThat( validator.validate( c ) ).isEmpty();
		assertThat( validator.validate( d ) ).isEmpty();
		assertThat( validator.validate( e ) ).isEmpty();
		assertThat( validator.validate( f ) ).isEmpty();
		assertThat( validator.validate( g ) ).isEmpty();
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
