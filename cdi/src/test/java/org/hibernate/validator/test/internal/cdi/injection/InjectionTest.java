/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.cdi.injection;

import javax.inject.Inject;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.cdi.HibernateValidator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class InjectionTest {

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
	HibernateValidatorFactory hibernateValidatorFactory;

	@Test
	public void testInjectionOfQualifiedBeans() throws Exception {
		assertNotNull( validatorFactory );
		assertNotNull( validator );

		assertEquals( 1, validator.validate( new TestEntity() ).size() );
	}

	@Test
	public void testInjectionOfDefaultBeans() throws Exception {
		assertNotNull( defaultValidatorFactory );
		assertNotNull( defaultValidator );

		assertEquals( 1, defaultValidator.validate( new TestEntity() ).size() );
	}

	@Test
	public void testInjectionOfHibernateValidatorFactory() throws Exception {
		assertNotNull( hibernateValidatorFactory );
		assertEquals( 1, hibernateValidatorFactory.getValidator().validate( new TestEntity() ).size() );
	}

	public static class TestEntity {
		@NotNull
		private String foo;
	}
}
