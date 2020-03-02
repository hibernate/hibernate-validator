/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal.injection;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.cdi.HibernateValidator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;

/**
 * Tests the case where {@code @Default}-scoped beans for validator and validator factory have already been registered
 * by another component and only the {@code @HibernateValidator}-scoped beans must be registered.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class InjectionWithExternallyProvidedDefaultBeansTest extends Arquillian {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addAsManifestResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@HibernateValidator
	@Inject
	ValidatorFactory validatorFactory;

	@HibernateValidator
	@Inject
	Validator validator;

	@Inject
	ValidatorFactory defaultValidatorFactory;

	@Inject
	Validator defaultValidator;

	@Inject
	@Any
	HibernateValidatorFactory hibernateValidatorFactory;

	@Test
	public void testInjectionOfQualifiedBeans() throws Exception {
		assertThat( validatorFactory ).isNotNull();
		assertThat( validator ).isNotNull();

		assertThat( validator.validate( new TestEntity() ) ).hasSize( 1 );
	}

	@Test
	public void testInjectionOfDefaultBeans() throws Exception {
		assertThat( defaultValidatorFactory ).isNotNull();
		assertThat( defaultValidator ).isNotNull();

		assertThat( defaultValidator.validate( new TestEntity() ) ).hasSize( 1 );
	}

	@Test
	public void testInjectionOfHibernateValidatorFactory() throws Exception {
		assertThat( hibernateValidatorFactory ).isNotNull();
		assertThat( hibernateValidatorFactory.getValidator().validate( new TestEntity() ) ).hasSize( 1 );
	}

	public static class TestEntity {
		@NotNull
		private String foo;
	}

	@ApplicationScoped
	public static class ProducerBean {

		@Produces
		ValidatorFactory produceDefaultValidatorFactory() {
			return Validation.buildDefaultValidatorFactory();
		}

		@Produces
		Validator produceDefaultValidator() {
			return Validation.buildDefaultValidatorFactory().getValidator();
		}
	}
}
