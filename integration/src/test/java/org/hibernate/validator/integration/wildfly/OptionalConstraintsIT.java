/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Future;

import org.hibernate.validator.constraints.SafeHtml;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Asserts that the constraints based on the JodaTime and JSoup server modules can be used.
 *
 * @author Gunnar Morling
 */
@RunWith(Arquillian.class)
public class OptionalConstraintsIT {

	private static final String WAR_FILE_NAME = OptionalConstraintsIT.class
			.getSimpleName() + ".war";

	public static class Shipment {

		@Future
		public DateTime deliveryDate = new DateTime( 2014, 10, 21, 0, 0, 0, 0 );
	}

	public static class Item {

		@SafeHtml
		public String descriptionHtml = "<script>Cross-site scripting https://en.wikipedia.org/wiki/42<script/>";
	}

	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap.create( WebArchive.class, WAR_FILE_NAME )
				.addAsWebInfResource( "jboss-deployment-structure-optional-constraints.xml", "jboss-deployment-structure.xml" )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Inject
	private Validator validator;

	@Test
	public void canUseJodaTimeConstraintValidator() {
		Set<ConstraintViolation<Shipment>> violations = validator.validate( new Shipment() );

		assertEquals( 1, violations.size() );
		assertEquals(
				Future.class,
				violations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType()
		);
	}

	@Test
	public void canUseJsoupBasedConstraint() {
		Set<ConstraintViolation<Item>> violations = validator.validate( new Item() );

		assertEquals( 1, violations.size() );
		assertEquals(
				SafeHtml.class,
				violations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType()
		);
	}
}
