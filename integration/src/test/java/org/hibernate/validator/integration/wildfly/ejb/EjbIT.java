/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly.ejb;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.hibernate.validator.integration.AbstractArquillianIT;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;

import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.http.ContentType;
import org.testng.annotations.Test;

/**
 * This is a reproducer for WFL-11566, but fixing the problem requires changes in WildFly.
 * Thus it is ignored for now.
 * See HV-1754.
 */
public class EjbIT extends AbstractArquillianIT {
	private static final String WAR_FILE_NAME = EjbIT.class.getSimpleName() + ".war";
	private static final String APPLICATION_PATH = EjbIT.class.getSimpleName();

	@Deployment
	public static WebArchive createTestArchive() throws Exception {
		return buildTestArchive( WAR_FILE_NAME )
				.addClass( EjbJaxRsResource.class )
				.addClass( JaxRsApplication.class )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" )
				.addAsWebInfResource(
						new StringAsset(
								Descriptors.create( org.jboss.shrinkwrap.descriptor.api.webapp31.WebAppDescriptor.class )
										.exportAsString()
						),
						"web.xml"
				);
	}

	@Test(enabled = false)
	@RunAsClient
	public void testRestEasyWorks() {
		given()
				.filter( new ErrorLoggingFilter() )
				.body( "[\"a\", \"b\", \"c\"]" )
				.contentType( ContentType.JSON )
				.post( APPLICATION_PATH + "/put/list/" )
				.then()
				.statusCode( 200 )
				.body( equalTo( "Hello bars a, b, c" ) );
	}

	@Test(enabled = false)
	@RunAsClient
	public void testValidationWorks() {
		given()
				.filter( new ErrorLoggingFilter() )
				.body( "[]" )
				.contentType( ContentType.JSON )
				.post( APPLICATION_PATH + "/put/list/" )
				.then()
				.statusCode( 400 )
				.body( containsString( "must not be empty" ) );
	}
}
