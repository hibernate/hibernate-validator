/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.cdi;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.testutil.TestForIssue;

import static org.junit.Assert.assertNotNull;

/**
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class QualifiedInjectionUnitIT {
	private static final String WAR_FILE_NAME = QualifiedInjectionUnitIT.class.getSimpleName() + ".war";

	@Inject
	@HibernateValidator
	private ValidatorFactory validatorFactory;

	@Inject
	@HibernateValidator
	private Validator validator;

	@Inject
	TestEntity testEntity;

	@Deployment
	public static WebArchive createTestArchive() throws Exception {
		return ShrinkWrap
				.create( WebArchive.class, WAR_FILE_NAME )
				.addAsResource( "log4j.properties" )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Test
	public void testQualifiedValidatorFactoryAndValidatorInjectable() {
		assertNotNull( "The validator factory should have been injected", validatorFactory );
		assertNotNull( "The validator should have been injected", validator );
	}

	@Test
	@TestForIssue(jiraKey = "HV-787")
	public void testInjectionIntoBeanWithPassivatingScope() throws Exception {
		assertNotNull( testEntity );
		assertNotNull( testEntity.getValidatorFactory() );
		assertNotNull( testEntity.getValidator() );
	}

	@SessionScoped
	@SuppressWarnings("serial")
	public static class TestEntity implements Serializable {

		@Inject
		@HibernateValidator
		private ValidatorFactory validatorFactory;

		@Inject
		@HibernateValidator
		private Validator validator;


		public ValidatorFactory getValidatorFactory() {
			return validatorFactory;
		}

		public Validator getValidator() {
			return validator;
		}
	}
}
