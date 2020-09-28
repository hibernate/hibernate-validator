/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

import javax.inject.Inject;

import org.hibernate.validator.integration.AbstractArquillianIT;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.persistence20.PersistenceDescriptor;
import org.testng.annotations.Test;

import jakarta.validation.ConstraintViolationException;

/**
 * Tests the usage of HV by JPA, applying a custom validation.xml. Also making sure that the VF is CDI-enabled.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class CustomValidatorFactoryInPersistenceUnitIT extends AbstractArquillianIT {

	private static final String WAR_FILE_NAME = CustomValidatorFactoryInPersistenceUnitIT.class.getSimpleName() + ".war";

	@Deployment
	public static Archive<?> createTestArchive() {
		return buildTestArchive( WAR_FILE_NAME )
				.addClasses( Magician.class, ValidMagicianName.class, MagicianService.class, Wand.class, WandConstraintMappingContributor.class )
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
		try {
			magicianService.storeMagician();
			fail( "Expected exception wasn't raised" );
		}
		catch (Exception e) {
			Throwable rootException = getRootException( e );
			assertThat( rootException ).isExactlyInstanceOf( ConstraintViolationException.class );

			ConstraintViolationException constraintViolationException = (ConstraintViolationException) rootException;
			assertThat( constraintViolationException.getConstraintViolations() ).hasSize( 1 );
			assertThat( constraintViolationException.getConstraintViolations().iterator().next().getMessage() )
					.isEqualTo( "Invalid magician name" );
		}
	}

	/**
	 * Makes sure the HV added via our module ZIP is used instead the one coming with WF by relying on functionality not
	 * present in the WF-provided HV
	 */
	// TODO How to make that work reliably also after a HV upgrade within WF?
	@Test
	public void testValidatorFactoryPassedToPersistenceUnitIsContributedFromPortableExtensionOfCurrentModuleZip() throws Exception {
		try {
			magicianService.storeWand();
			fail( "Expected exception wasn't raised" );
		}
		catch (Exception e) {
			Throwable rootException = getRootException( e );
			assertThat( rootException ).isExactlyInstanceOf( ConstraintViolationException.class );

			ConstraintViolationException constraintViolationException = (ConstraintViolationException) rootException;
			assertThat( constraintViolationException.getConstraintViolations() ).hasSize( 1 );
			assertThat( constraintViolationException.getConstraintViolations().iterator().next().getMessage() )
					.isEqualTo( "size must be between 5 and 2147483647" );
		}
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
