/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly.generictype;

import java.util.Set;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.hibernate.validator.testutil.TestForIssue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@TestForIssue(jiraKey = "HV-978")
@RunWith(Arquillian.class)
public class GenericParameterTypeValidationUnitIT {
	@Deployment
	public static WebArchive deployment() {
		return ShrinkWrap.create( WebArchive.class )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" )
				.addPackage( GenericParameterTypeValidationUnitIT.class.getPackage() );
	}

	@Inject
	private StringInterface bean;

	@Test
	public void validation_on_generic_arg() {
		try {
			bean.genericArg( null );
			fail( "@NotNull constraint should be violated" );
		}
		catch ( ConstraintViolationException e ) {
			Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			assertTrue( "Unexpected number of constraint violations", violations.size() == 1 );
			ConstraintViolation constraintViolation = violations.iterator().next();
			assertEquals(
					"Unexpected constraint type ",
					NotNull.class,
					constraintViolation.getConstraintDescriptor().getAnnotation().annotationType()
			);
		}
	}
}
