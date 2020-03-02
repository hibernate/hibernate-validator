/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import javax.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.integration.AbstractArquillianIT;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.validationConfiguration11.ValidationConfigurationDescriptor;
import org.jboss.shrinkwrap.descriptor.api.validationMapping11.ValidationMappingDescriptor;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.testng.annotations.Test;

/**
 * Test for https://hibernate.atlassian.net/browse/HV-1280. To reproduce the issue, the deployment must be done twice
 * (it will only show up during the 2nd deploy), which is why the test is managing the deployment itself via client-side
 * test methods.
 *
 * @author Gunnar Morling
 */
public class JaxpContainedInDeploymentIT extends AbstractArquillianIT {

	private static final String WAR_FILE_NAME = JaxpContainedInDeploymentIT.class.getSimpleName() + ".war";

	@ArquillianResource
	private Deployer deployer;

	@Inject
	private Validator validator;

	@Deployment(name = "jaxpit", managed = false)
	public static Archive<?> createTestArchive() {
		return buildTestArchive( WAR_FILE_NAME )
				.addClass( Camera.class )
				.addAsResource( validationXml(), "META-INF/validation.xml" )
				.addAsResource( mappingXml(), "META-INF/my-mapping.xml" )
				.addAsLibrary( Maven.resolver().resolve( "xerces:xercesImpl:2.9.1" ).withoutTransitivity().asSingleFile() )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	private static Asset validationXml() {
		String validationXml = Descriptors.create( ValidationConfigurationDescriptor.class )
				.version( "1.1" )
				.constraintMapping( "META-INF/my-mapping.xml" )
				.exportAsString();
		return new StringAsset( validationXml );
	}

	private static Asset mappingXml() {
		String mappingXml = Descriptors.create( ValidationMappingDescriptor.class )
				.version( "1.1" )
				.createBean()
					.clazz( Camera.class.getName() )
						.createField()
							.name( "brand" )
							.createConstraint()
								.annotation( "jakarta.validation.constraints.NotNull" )
						.up()
					.up()
				.up()
				.exportAsString();
		return new StringAsset( mappingXml );
	}

	@Test
	@RunAsClient
	public void deploy1() throws Exception {
		deployer.deploy( "jaxpit" );
	}

	@Test(dependsOnMethods = "deploy1")
	public void test1() throws Exception {
		doTest();
	}

	@Test(dependsOnMethods = "test1")
	@RunAsClient
	public void undeploy1() throws Exception {
		deployer.undeploy( "jaxpit" );
	}

	@Test(dependsOnMethods = "undeploy1")
	@RunAsClient
	public void deploy2() throws Exception {
		deployer.deploy( "jaxpit" );
	}

	@Test(dependsOnMethods = "deploy2")
	public void test2() throws Exception {
		doTest();
	}

	@Test(dependsOnMethods = "test2")
	@RunAsClient
	public void undeploy2() throws Exception {
		deployer.undeploy( "jaxpit" );
	}

	private void doTest() {
		Set<ConstraintViolation<Camera>> violations = validator.validate( new Camera() );

		assertThat( violations ).hasSize( 1 );
		assertThat( violations.iterator()
				.next()
				.getConstraintDescriptor()
				.getAnnotation()
				.annotationType()
		).isSameAs( NotNull.class );
	}
}
