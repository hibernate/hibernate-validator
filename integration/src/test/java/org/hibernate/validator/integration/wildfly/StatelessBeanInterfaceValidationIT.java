/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.hibernate.validator.integration.wildfly.statelessbean.Greeter;
import org.hibernate.validator.integration.wildfly.statelessbean.StatelessBean;
import org.hibernate.validator.integration.wildfly.statelessbean.StatelessBeanInterface;
import org.hibernate.validator.testutil.TestForIssue;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Davide D'Alto
 */
@TestForIssue(jiraKey = "HV-994")
@RunWith(Arquillian.class)
public class StatelessBeanInterfaceValidationIT {

	private static final String CONTEXT_ROOT = "HV994";
	private static final String WAR_FILE_NAME = StatelessBeanInterfaceValidationIT.class.getSimpleName() + ".war";

	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap.create( WebArchive.class, WAR_FILE_NAME )
				.addClasses( StatelessBean.class, StatelessBeanInterface.class, Greeter.class )
				.addAsWebInfResource( webXml(), "web.xml" )
				.addAsWebInfResource( jbossWebXml(), "jboss-web.xml" )
				.addAsWebResource( "StatelessBeanInterfaceValidationIT-index.xhtml", "/index.xhtml" );
	}

	private static Asset webXml() {
		String webXml = Descriptors.create( WebAppDescriptor.class )
				.displayName( "HV-994 Test" )
				.createWelcomeFileList()
					.welcomeFile( "index.xhtml" ).up()
				.createServlet()
					.servletName( "Faces Servlet" )
					.servletClass( "javax.faces.webapp.FacesServlet" ).up()
				.createServletMapping()
					.servletName( "Faces Servlet" )
					.urlPattern( "*.xhtml" ).up()
				.exportAsString();
		return new StringAsset( webXml );
	}

	private static Asset jbossWebXml() {
		String webXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><jboss-web><context-root>/" + CONTEXT_ROOT + "</context-root></jboss-web>";
		return new StringAsset( webXml );
	}

	@Inject
	private Greeter bean;

	@Test
	public void shouldBeAbleToValidateStatelessBeanFromInterface() {
		try {
			bean.getMessage();
			fail( "Expected a @NotNull validation error, see " + StatelessBeanInterface.class.getSimpleName() + "#lookup(String)" );
		}
		catch (Exception e) {
			assertEquals( ConstraintViolationException.class, e.getCause().getClass() );

			ConstraintViolationException violationException = (ConstraintViolationException) e.getCause();
			int size = violationException.getConstraintViolations().size();
			assertEquals( 1, size );

			ConstraintViolation<?> constraintViolation = violationException.getConstraintViolations().iterator().next();
			assertEquals( "{javax.validation.constraints.NotNull.message}", constraintViolation.getConstraintDescriptor().getMessageTemplate() );
			assertEquals( "lookup.arg0", constraintViolation.getPropertyPath().toString() );
		}
	}
}
