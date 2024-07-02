/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integrationtest.java.module.cdi.constraint;

import static org.assertj.core.api.Assertions.assertThat;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.integrationtest.java.module.cdi.model.TestEntity;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.testng.annotations.Test;

public class InjectionTest extends Arquillian {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addAsManifestResource( "beans.xml" );
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
}
