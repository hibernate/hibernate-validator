/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly;

import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.hibernate.validator.integration.util.IntegrationTestUtil;
import org.hibernate.validator.testutil.TestForIssue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class ConstraintDefinitionContributorIT {
	private static final String WAR_FILE_NAME = ConstraintDefinitionContributorIT.class.getSimpleName() + ".war";

	@Inject
	private Validator validator;


	@Deployment
	public static WebArchive createTestArchive() throws Exception {
		return ShrinkWrap
				.create( WebArchive.class, WAR_FILE_NAME )
				.addClass( TestEntity.class )
				.addAsLibrary(
						IntegrationTestUtil.createAcmeConstraintDefinitionContributorJar()
								.as( JavaArchive.class )
				)
				.addAsLibrary(
						IntegrationTestUtil.createOxBerryConstraintDefinitionContributorJar()
								.as( JavaArchive.class )
				)
				.addAsResource( "log4j.properties" )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
	}


	@Test
	@TestForIssue(jiraKey = "HV-953")
	public void testConstraintContributionsGetDiscovered() throws Exception {
		TestEntity testEntity = new TestEntity( "foo" );
		Set<ConstraintViolation<TestEntity>> constraintViolations = validator.validate( testEntity );
		assertEquals( "There should be two constraint violations", 2, constraintViolations.size() );

		Set<String> messages = new HashSet<String>();
		for ( ConstraintViolation<TestEntity> constraintViolation : constraintViolations ) {
			messages.add( constraintViolation.getMessage() );
		}

		assertTrue( "Expected message 'acme' not in set - " + messages, messages.contains( "acme" ) );
		assertTrue( "Expected message 'oxberry' not in set - " + messages, messages.contains( "oxberry" ) );
	}
}
