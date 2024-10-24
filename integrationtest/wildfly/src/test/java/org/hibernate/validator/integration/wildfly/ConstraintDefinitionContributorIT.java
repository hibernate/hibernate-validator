/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.wildfly;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.integration.AbstractArquillianIT;
import org.hibernate.validator.integration.util.IntegrationTestUtil;
import org.hibernate.validator.testutil.TestForIssue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintDefinitionContributorIT extends AbstractArquillianIT {
	private static final String WAR_FILE_NAME = ConstraintDefinitionContributorIT.class.getSimpleName() + ".war";

	@Inject
	private Validator validator;


	@Deployment
	public static WebArchive createTestArchive() throws Exception {
		return buildTestArchive( WAR_FILE_NAME )
				.addClass( TestEntity.class )
				.addAsLibrary(
						IntegrationTestUtil.createAcmeConstraintDefinitionContributorJar()
								.as( JavaArchive.class )
				)
				.addAsLibrary(
						IntegrationTestUtil.createOxBerryConstraintDefinitionContributorJar()
								.as( JavaArchive.class )
				)
				.addAsWebInfResource( BEANS_XML, "beans.xml" );
	}


	@Test
	@TestForIssue(jiraKey = "HV-953")
	public void testConstraintContributionsGetDiscovered() throws Exception {
		TestEntity testEntity = new TestEntity( "foo" );
		Set<ConstraintViolation<TestEntity>> constraintViolations = validator.validate( testEntity );
		assertThat( constraintViolations ).as( "There should be two constraint violations" ).hasSize( 2 );

		Set<String> messages = new HashSet<String>();
		for ( ConstraintViolation<TestEntity> constraintViolation : constraintViolations ) {
			messages.add( constraintViolation.getMessage() );
		}

		assertThat( messages ).contains( "acme" );
		assertThat( messages ).contains( "oxberry" );
	}
}
