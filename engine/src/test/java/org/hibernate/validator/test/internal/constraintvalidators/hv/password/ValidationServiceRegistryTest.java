/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.password;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertTrue;

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

import org.testng.annotations.Test;

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
		assertEquals( result.score(), 3 );
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
		assertEquals( weak.estimate( "x".toCharArray() ).score(), 1 );

		PasswordStrengthEstimator strong = resolver.resolve(
				PasswordStrengthEstimator.class, "strong", BeanRetrieval.ANY ).get();
		assertEquals( strong.estimate( "x".toCharArray() ).score(), 5 );

		assertNotSame( weak, strong );
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

		assertEquals( all.size(), 2 );
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

		assertEquals( named.size(), 2 );
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
		assertEquals( resolved.estimate( "x".toCharArray() ).score(), 7 );
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
		assertEquals( resolved.size(), 2 );
		assertEquals( resolved.get( 0 ).estimate( "x".toCharArray() ).score(), 1 );
		assertEquals( resolved.get( 1 ).estimate( "x".toCharArray() ).score(), 2 );
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

	@Test(expectedExceptions = ValidationException.class)
	public void builtinRetrievalFailsForUnregisteredType() {
		HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class );

		factory.getBeanResolver().resolve( PasswordStrengthEstimator.class, BeanRetrieval.BUILTIN );
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

	@Test(expectedExceptions = ValidationException.class)
	public void unresolvedBeanThrowsValidationException() {
		HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class );

		factory.getBeanResolver().resolve( PasswordStrengthEstimator.class, BeanRetrieval.ANY );
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
