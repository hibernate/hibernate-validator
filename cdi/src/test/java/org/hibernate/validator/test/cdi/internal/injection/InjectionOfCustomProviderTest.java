/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal.injection;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.hibernate.validator.test.cdi.internal.injection.MyValidationProvider.MyValidator;
import org.hibernate.validator.test.cdi.internal.injection.MyValidationProvider.MyValidatorFactory;
import org.hibernate.validator.testutil.TestForIssue;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;

/**
 * Tests the injection of validator and validator factory if the default provider is not Hibernate Validator.
 *
 * @author Gunnar Morling
 */
@TestForIssue(jiraKey = "HV-858")
public class InjectionOfCustomProviderTest extends Arquillian {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addClass( MyValidationProvider.class )
				.addAsResource(
						InjectionOfCustomProviderTest.class.getResource( "validation.xml" ),
						"META-INF/validation.xml"
				)
				.add(
						new StringAsset( "org.hibernate.validator.test.cdi.internal.injection.MyValidationProvider" ),
						"META-INF/services/jakarta.validation.spi.ValidationProvider"
				);
	}

	@Inject
	ValidatorFactory defaultValidatorFactory;

	@Inject
	Validator defaultValidator;

	@Inject
	MyValidatorFactory myValidatorFactory;

	@Inject
	MyValidator myValidator;

	@Inject
	@HibernateValidator
	ValidatorFactory hibernateValidatorFactory;

	@Inject
	@HibernateValidator
	Validator hibernateValidator;

	@Inject
	@HibernateValidator
	HibernateValidatorFactory hibernateValidatorSpecificFactory;

	@Test
	public void testInjectionOfDefaultFactory() throws Exception {
		assertThat( defaultValidatorFactory ).isNotNull();
		assertThat( defaultValidatorFactory ).isInstanceOf( MyValidatorFactory.class );
		assertThat( defaultValidatorFactory.unwrap( MyValidatorFactory.class ) ).isInstanceOf( MyValidatorFactory.class );

		assertThat( myValidatorFactory ).isNotNull();
	}

	@Test
	public void testInjectionOfDefaultValidator() throws Exception {
		assertThat( defaultValidator ).isNotNull();
		assertThat( defaultValidator ).isInstanceOf( MyValidator.class );
		assertThat( defaultValidator.forExecutables() ).isNotNull();

		assertThat( myValidator ).isNotNull();
		assertThat( myValidator.forExecutables() ).isNotNull();

		assertThat( defaultValidator.validate( new TestEntity() ) ).hasSize( 1 );
		assertThat( myValidator.validate( new TestEntity() ) ).hasSize( 1 );
	}

	@Test
	public void testInjectionOfHibernateFactory() throws Exception {
		assertThat( hibernateValidatorFactory ).isNotNull();
		assertThat( hibernateValidatorSpecificFactory ).isNotNull();

		assertThat( hibernateValidatorFactory.getValidator() ).isExactlyInstanceOf( ValidatorImpl.class );
		assertThat( hibernateValidatorSpecificFactory.getValidator() ).isExactlyInstanceOf( ValidatorImpl.class );
	}

	@Test
	public void testInjectionOfHibernateValidator() throws Exception {
		assertThat( hibernateValidator ).isNotNull();
		assertThat( hibernateValidator.forExecutables() ).isNotNull();

		assertThat( hibernateValidator.unwrap( Validator.class ) ).isNotNull();

		assertThat( hibernateValidator.validate( new TestEntity() ) ).hasSize( 1 );
	}

	public static class TestEntity {
		@NotNull
		private String foo;
	}
}
