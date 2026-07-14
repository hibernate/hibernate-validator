/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.injection;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.bean.BeanHolder;
import org.hibernate.validator.bean.BeanResolver;
import org.hibernate.validator.bean.BeanRetrieval;
import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.constraints.PasswordStrength;
import org.hibernate.validator.spi.password.PasswordStrengthEstimator;
import org.hibernate.validator.spi.password.PasswordStrengthResult;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.testng.annotations.Test;

/**
 * Tests that {@link BeanResolver} correctly resolves CDI beans via {@code Instance.Handle},
 * including lazy resolution and proper lifecycle through {@link BeanHolder#close()}.
 */
public class BeanResolverCdiTest extends Arquillian {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addAsManifestResource( "beans.xml" );
	}

	@HibernateValidator
	@Inject
	ValidatorFactory validatorFactory;

	@Test
	public void testResolveCdiBean() {
		BeanResolver beanResolver = validatorFactory.unwrap( HibernateValidatorFactory.class )
				.getBeanResolver();

		try ( BeanHolder<MyApplicationScopedService> holder = beanResolver.resolve(
				MyApplicationScopedService.class, BeanRetrieval.BEAN ) ) {
			MyApplicationScopedService service = holder.get();
			assertThat( service ).isNotNull();
			assertThat( service.greet() ).isEqualTo( "hello" );

			assertThat( holder.get() ).isSameAs( service );
		}
	}

	@Test
	public void testResolveDependentScopedBean() {
		BeanResolver beanResolver = validatorFactory.unwrap( HibernateValidatorFactory.class )
				.getBeanResolver();

		MyDependentService first;
		try ( BeanHolder<MyDependentService> holder = beanResolver.resolve(
				MyDependentService.class, BeanRetrieval.BEAN ) ) {
			first = holder.get();
			assertThat( first ).isNotNull();
		}

		try ( BeanHolder<MyDependentService> holder = beanResolver.resolve(
				MyDependentService.class, BeanRetrieval.BEAN ) ) {
			assertThat( holder.get() ).isNotSameAs( first );
		}
	}

	@Test
	public void testPasswordStrengthValidationWithCdiEstimator() {
		Validator validator = validatorFactory.getValidator();

		assertThat( validator.validate( new PasswordBean( "weak" ) ) ).hasSize( 1 );
		assertThat( validator.validate( new PasswordBean( "strong-enough-password" ) ) ).isEmpty();
	}

	@ApplicationScoped
	public static class MyApplicationScopedService {
		public String greet() {
			return "hello";
		}
	}

	@Dependent
	public static class MyDependentService {
	}

	@ApplicationScoped
	public static class LengthBasedEstimator implements PasswordStrengthEstimator {
		@Override
		public PasswordStrengthResult estimate(char[] password) {
			int score = password.length >= 10 ? 4 : 1;
			return PasswordStrengthResult.simple( score, null );
		}
	}

	public static class PasswordBean {
		@PasswordStrength(min = 3)
		private final String password;

		PasswordBean(String password) {
			this.password = password;
		}
	}
}
