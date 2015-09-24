/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly.jpa;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import org.apache.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.persistence20.PersistenceDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Tests the usage of HV by JPA, applying a custom validation.xml. Also making sure that the VF is CDI-enabled.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
@RunWith(Arquillian.class)
public class CustomValidatorFactoryInPersistenceUnitIT {

	private static final String WAR_FILE_NAME = CustomValidatorFactoryInPersistenceUnitIT.class.getSimpleName() + ".war";
	private static final Logger log = Logger.getLogger( CustomValidatorFactoryInPersistenceUnitIT.class );

	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create( WebArchive.class, WAR_FILE_NAME )
				.addClasses( Magician.class, ValidMagicianName.class, MagicianService.class )
				.addAsResource( "log4j.properties" )
				.addAsResource( persistenceXml(), "META-INF/persistence.xml" )
				.addAsResource( "validation.xml", "META-INF/validation.xml" )
				.addAsResource( "constraints-magician.xml", "META-INF/validation/constraints-magician.xml" )
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

	@Inject
	private MagicianService magicianService;

	@Test
	public void testValidatorFactoryPassedToPersistenceUnitIsCorrectlyConfigured() throws Exception {
		log.debug( "Running testValidatorFactoryPassedToPersistenceUnitIsCorrectlyConfigured..." );

		try {
			magicianService.storeMagician();
		}
		catch(Exception e) {
			Throwable rootException = getRootException( e );
			assertEquals( ConstraintViolationException.class, rootException.getClass() );

			ConstraintViolationException constraintViolationException = (ConstraintViolationException) rootException;
			assertEquals( 1, constraintViolationException.getConstraintViolations().size() );
			assertEquals( "Invalid magician name", constraintViolationException.getConstraintViolations().iterator().next().getMessage() );
		}

		log.debug( "testValidatorFactoryPassedToPersistenceUnitIsCorrectlyConfigured completed" );
	}

	private Throwable getRootException(Throwable throwable) {
		while ( true ) {
			Throwable cause = throwable.getCause();
			if ( cause == null ) {
				return throwable;
			}
			throwable = cause;
		}
	}
}
