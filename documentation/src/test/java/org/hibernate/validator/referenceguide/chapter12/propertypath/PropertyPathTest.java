/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter12.propertypath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.Iterator;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.path.PropertyNode;

import org.junit.Test;

/**
 * @author Gunnar Morling
 *
 */
public class PropertyPathTest {

	@Test
	public void testPropertyNodeGetValueForSet() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.failFast( true )
				.buildValidatorFactory()
				.getValidator();

		//tag::include[]
		Building building = new Building();

		// Assume the name of the person violates a @Size constraint
		Person bob = new Person( "Bob" );
		Apartment bobsApartment = new Apartment( bob );
		building.getApartments().add( bobsApartment );

		Set<ConstraintViolation<Building>> constraintViolations = validator.validate( building );

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getName(), "apartments" );
		assertSame( node.as( PropertyNode.class ).getValue(), bobsApartment );

		node = nodeIterator.next();
		assertEquals( node.getName(), "resident" );
		assertSame( node.as( PropertyNode.class ).getValue(), bob );

		node = nodeIterator.next();
		assertEquals( node.getName(), "name" );
		assertEquals( node.as( PropertyNode.class ).getValue(), "Bob" );
		//end::include[]

		assertFalse( nodeIterator.hasNext() );
	}
}
