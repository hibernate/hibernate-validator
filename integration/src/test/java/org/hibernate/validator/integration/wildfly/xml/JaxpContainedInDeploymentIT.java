/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.validationConfiguration11.ValidationConfigurationDescriptor;
import org.jboss.shrinkwrap.descriptor.api.validationMapping11.ValidationMappingDescriptor;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for https://hibernate.atlassian.net/browse/HV-1280. To reproduce the issue, the deployment must be done twice
 * (it will only show up during the 2nd deploy), which is why the test is managing the deployment itself via client-side
 * test methods.
 *
 * @author Gunnar Morling
 */
@RunWith(Arquillian.class)
public class JaxpContainedInDeploymentIT {

	private static final String WAR_FILE_NAME = JaxpContainedInDeploymentIT.class.getSimpleName() + ".war";

	@ArquillianResource
	private Deployer deployer;

	@Inject
	private Validator validator;

	@Deployment(name = "jaxpit", managed = false)
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create( WebArchive.class, WAR_FILE_NAME )
				.addClass( Camera.class )
				.addAsResource( validationXml(), "META-INF/validation.xml" )
				.addAsResource( mappingXml(), "META-INF/my-mapping.xml" )
				.addAsLibrary( Maven.resolver().resolve( "xerces:xercesImpl:2.9.1" ).withoutTransitivity().asSingleFile() )
				.addAsResource( "log4j.properties" )
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
								.annotation( "javax.validation.constraints.NotNull" )
						.up()
					.up()
				.up()
				.exportAsString();
		return new StringAsset( mappingXml );
	}

	@Test
	@RunAsClient
	@InSequence(0)
	public void deploy1() throws Exception {
		deployer.deploy( "jaxpit" );
	}

	@Test
	@InSequence(1)
	public void test1() throws Exception {
		doTest();
	}

	@Test
	@RunAsClient
	@InSequence(2)
	public void undeploy1() throws Exception {
		deployer.undeploy( "jaxpit" );
	}

	@Test
	@RunAsClient
	@InSequence(3)
	public void deploy2() throws Exception {
		deployer.deploy( "jaxpit" );
	}

	@Test
	@InSequence(4)
	public void test2() throws Exception {
		doTest();
	}

	@Test
	@RunAsClient
	@InSequence(5)
	public void undeploy2() throws Exception {
		deployer.undeploy( "jaxpit" );
	}

	private void doTest() {
		Set<ConstraintViolation<Camera>> violations = validator.validate( new Camera() );

		assertEquals( 1, violations.size() );
		assertSame( NotNull.class, violations.iterator()
				.next()
				.getConstraintDescriptor()
				.getAnnotation()
				.annotationType() );
	}
}
