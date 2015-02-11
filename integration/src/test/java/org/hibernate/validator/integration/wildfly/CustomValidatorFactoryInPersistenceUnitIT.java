/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.persistence20.PersistenceDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.hibernate.validator.integration.util.IntegrationTestUtil;
import org.hibernate.validator.integration.util.MyValidator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the integration of Hibernate Validator in Wildfly
 *
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class CustomValidatorFactoryInPersistenceUnitIT {

	private static final String WAR_FILE_NAME = CustomValidatorFactoryInPersistenceUnitIT.class.getSimpleName() + ".war";
	private static final Logger log = Logger.getLogger( CustomValidatorFactoryInPersistenceUnitIT.class );

	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create( WebArchive.class, WAR_FILE_NAME )
				.addClasses( User.class )
				.addAsLibrary( IntegrationTestUtil.createCustomBeanValidationProviderJar()
								.as( JavaArchive.class )
								.addAsManifestResource( EmptyAsset.INSTANCE, "beans.xml" ) )
				.addAsResource( "log4j.properties" )
				.addAsResource( persistenceXml(), "META-INF/persistence.xml" )
				.addAsResource( "validation.xml", "META-INF/validation.xml" )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	private static Asset persistenceXml() {
		String persistenceXml = Descriptors.create( PersistenceDescriptor.class )
				.version( "2.0" )
				.createPersistenceUnit()
					.name( "default" )
					.jtaDataSource( "java:jboss/datasources/ExampleDS" )
					.getOrCreateProperties()
						.createProperty().name( "hibernate.hbm2ddl.auto" ).value( "create-drop" ).up()
						.createProperty().name( "hibernate.validator.apply_to_ddl" ).value( "false" ).up()
					.up()
				.up()
				.exportAsString();
		return new StringAsset( persistenceXml );
	}

	@PersistenceContext
	EntityManager em;

	@Test
	public void testValidatorFactoryPassedToPersistenceUnit() throws Exception {
		log.debug( "Running testValidatorFactoryPassedToPersistenceUnit..." );
		Map<String, Object> properties = em.getEntityManagerFactory().getProperties();

		ValidatorFactory factory = (ValidatorFactory) properties.get( "javax.persistence.validation.factory" );
		assertNotNull( "The validator factory should be contained in the EM properties", factory );

		Validator validator = factory.getValidator();

		// Asserting the validator type as the VF is the wrapper type used within WildFly (LazyValidatorFactory)
		assertTrue(
				"The custom validator implementation as retrieved from the default provider configured in META-INF/validation.xml should be used but actually "
						+ validator + " is used",
				validator instanceof MyValidator
		);

		log.debug( "testValidatorFactoryPassedToPersistenceUnit completed" );
	}
}
