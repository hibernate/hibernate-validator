/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly;

import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Asserts constraints mappings contributed via {@code validation.xml} are applied.
 *
 * @author Gunnar Morling
 */
@RunWith(Arquillian.class)
public class ConstraintMappingContributorIT {

	private static final String WAR_FILE_NAME = ConstraintMappingContributorIT.class
			.getSimpleName() + ".war";

	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap.create( WebArchive.class, WAR_FILE_NAME )
				.addClasses( Broomstick.class, MyConstraintMappingContributor.class )
				.addAsResource( "constraint-mapping-contributor-validation.xml", "META-INF/validation.xml" )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Inject
	private Validator validator;

	@Test
	public void shouldApplyContributedConstraintMapping() {
		Set<ConstraintViolation<Broomstick>> violations = validator.validate( new Broomstick() );

		assertEquals( 1, violations.size() );
		assertEquals(
				NotNull.class,
				violations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType()
		);
	}
}
