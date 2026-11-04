/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.password;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.validation.Validation;
import jakarta.validation.ValidationException;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.bean.BeanHolder;
import org.hibernate.validator.bean.BeanReference;
import org.hibernate.validator.bean.BeanResolver;
import org.hibernate.validator.bean.BeanRetrieval;
import org.hibernate.validator.spi.password.PasswordStrengthEstimator;
import org.hibernate.validator.spi.password.PasswordStrengthResult;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

import org.junit.jupiter.api.Test;

public class ValidationServiceRegistryTest {

	@Test
	public void registeredBeanIsRetrievable() {
		PasswordStrengthEstimator estimator = password -> new PasswordStrengthResult() {
			@Override
			public int score() {
				return 3;
			}

			@Override
			public String feedback() {
				return null;
			}
		};

		HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addBeanConfigurer( context -> context.define(
						PasswordStrengthEstimator.class,
						BeanReference.ofInstance( estimator ) ) )
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class );

		PasswordStrengthEstimator retrieved = factory.getBeanResolver()
				.resolve( PasswordStrengthEstimator.class, BeanRetrieval.ANY ).get();
		assertNotNull( retrieved );

		PasswordStrengthResult result = retrieved.estimate( "test".toCharArray() );
		assertEquals( 3, result.score() );
	}

	@Test
	public void scriptEvaluatorFactoryIsAccessibleAsBean() {
		HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class );

		ScriptEvaluatorFactory scriptFactory = factory.getBeanResolver()
				.resolve( ScriptEvaluatorFactory.class, BeanRetrieval.ANY ).get();
		assertNotNull( scriptFactory );
	}

	@Test
	public void namedBeanResolution() {
		PasswordStrengthEstimator estimatorA = password -> PasswordStrengthResult.simple( 1, "weak" );
		PasswordStrengthEstimator estimatorB = password -> PasswordStrengthResult.simple( 5, "strong" );

		HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addBeanConfigurer( context -> {
					context.define( PasswordStrengthEstimator.class, "weak",
							BeanReference.ofInstance( estimatorA ) );
					context.define( PasswordStrengthEstimator.class, "strong",
							BeanReference.ofInstance( estimatorB ) );
				} )
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class );

		BeanResolver resolver = factory.getBeanResolver();

		PasswordStrengthEstimator weak = resolver.resolve(
				PasswordStrengthEstimator.class, "weak", BeanRetrieval.ANY ).get();
		assertEquals( 1, weak.estimate( "x".toCharArray() ).score() );

		PasswordStrengthEstimator strong = resolver.resolve(
				PasswordStrengthEstimator.class, "strong", BeanRetrieval.ANY ).get();
		assertEquals( 5, strong.estimate( "x".toCharArray() ).score() );

		assertNotSame( strong, weak );
	}

	@Test
	public void allConfiguredForRole() {
		PasswordStrengthEstimator unnamed = password -> PasswordStrengthResult.simple( 1, null );
		PasswordStrengthEstimator named = password -> PasswordStrengthResult.simple( 2, null );

		HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addBeanConfigurer( context -> {
					context.define( PasswordStrengthEstimator.class,
							BeanReference.ofInstance( unnamed ) );
					context.define( PasswordStrengthEstimator.class, "named",
							BeanReference.ofInstance( named ) );
				} )
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class );

		List<BeanReference<PasswordStrengthEstimator>> all =
				factory.getBeanResolver().allConfiguredForRole( PasswordStrengthEstimator.class );

		assertEquals( 2, all.size() );
	}

	@Test
	public void namedConfiguredForRole() {
		PasswordStrengthEstimator estimatorA = password -> PasswordStrengthResult.simple( 1, null );
		PasswordStrengthEstimator estimatorB = password -> PasswordStrengthResult.simple( 2, null );

		HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addBeanConfigurer( context -> {
					context.define( PasswordStrengthEstimator.class, "alpha",
							BeanReference.ofInstance( estimatorA ) );
					context.define( PasswordStrengthEstimator.class, "beta",
							BeanReference.ofInstance( estimatorB ) );
				} )
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class );

		Map<String, BeanReference<PasswordStrengthEstimator>> named =
				factory.getBeanResolver().namedConfiguredForRole( PasswordStrengthEstimator.class );

		assertEquals( 2, named.size() );
		assertTrue( named.containsKey( "alpha" ) );
		assertTrue( named.containsKey( "beta" ) );
	}

	@Test
	public void resolveByBeanReference() {
		PasswordStrengthEstimator estimator = password -> PasswordStrengthResult.simple( 7, null );

		HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addBeanConfigurer( context -> context.define(
						PasswordStrengthEstimator.class,
						BeanReference.ofInstance( estimator ) ) )
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class );

		BeanReference<PasswordStrengthEstimator> ref = BeanReference.of(
				PasswordStrengthEstimator.class, BeanRetrieval.ANY );
		PasswordStrengthEstimator resolved = factory.getBeanResolver().resolve( ref ).get();
		assertEquals( 7, resolved.estimate( "x".toCharArray() ).score() );
	}

	@Test
	public void resolveByBeanReferenceList() {
		PasswordStrengthEstimator estimatorA = password -> PasswordStrengthResult.simple( 1, null );
		PasswordStrengthEstimator estimatorB = password -> PasswordStrengthResult.simple( 2, null );

		HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addBeanConfigurer( context -> {
					context.define( PasswordStrengthEstimator.class, "a",
							BeanReference.ofInstance( estimatorA ) );
					context.define( PasswordStrengthEstimator.class, "b",
							BeanReference.ofInstance( estimatorB ) );
				} )
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class );

		BeanResolver resolver = factory.getBeanResolver();
		List<BeanReference<PasswordStrengthEstimator>> refs = Arrays.asList(
				BeanReference.of( PasswordStrengthEstimator.class, "a", BeanRetrieval.ANY ),
				BeanReference.of( PasswordStrengthEstimator.class, "b", BeanRetrieval.ANY )
		);

		BeanHolder<List<PasswordStrengthEstimator>> holder = resolver.resolve( refs );
		List<PasswordStrengthEstimator> resolved = holder.get();
		assertEquals( 2, resolved.size() );
		assertEquals( 1, resolved.get( 0 ).estimate( "x".toCharArray() ).score() );
		assertEquals( 2, resolved.get( 1 ).estimate( "x".toCharArray() ).score() );
	}

	@Test
	public void builtinRetrievalFindsConfiguredBean() {
		HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class );

		ScriptEvaluatorFactory resolved = factory.getBeanResolver()
				.resolve( ScriptEvaluatorFactory.class, BeanRetrieval.BUILTIN ).get();
		assertNotNull( resolved );
	}

	@Test
	public void builtinRetrievalFailsForUnregisteredType() {
		assertThatThrownBy( () -> {
			HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
					.configure()
					.buildValidatorFactory()
					.unwrap( HibernateValidatorFactory.class );

			factory.getBeanResolver().resolve( PasswordStrengthEstimator.class, BeanRetrieval.BUILTIN );
		} ).isInstanceOf( ValidationException.class );
	}

	@Test
	public void constructorRetrievalInstantiatesViaReflection() {
		HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class );

		ReflectiveBean resolved = factory.getBeanResolver()
				.resolve( ReflectiveBean.class, BeanRetrieval.CONSTRUCTOR ).get();
		assertNotNull( resolved );
	}

	@Test
	public void unresolvedBeanThrowsValidationException() {
		assertThatThrownBy( () -> {
			HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
					.configure()
					.buildValidatorFactory()
					.unwrap( HibernateValidatorFactory.class );

			factory.getBeanResolver().resolve( PasswordStrengthEstimator.class, BeanRetrieval.ANY );
		} ).isInstanceOf( ValidationException.class );
	}

	@Test
	public void emptyAllConfiguredForRoleReturnsEmptyList() {
		HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class );

		List<BeanReference<PasswordStrengthEstimator>> all =
				factory.getBeanResolver().allConfiguredForRole( PasswordStrengthEstimator.class );
		assertTrue( all.isEmpty() );
	}

	@Test
	public void emptyNamedConfiguredForRoleReturnsEmptyMap() {
		HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class );

		Map<String, BeanReference<PasswordStrengthEstimator>> named =
				factory.getBeanResolver().namedConfiguredForRole( PasswordStrengthEstimator.class );
		assertTrue( named.isEmpty() );
	}

	public static class ReflectiveBean {
		public ReflectiveBean() {
		}
	}
}
