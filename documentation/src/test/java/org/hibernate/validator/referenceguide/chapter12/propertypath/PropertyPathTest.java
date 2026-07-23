/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter12.propertypath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

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
		assertThat( node.getName() ).isEqualTo( "apartments" );
		assertThat( node.as( PropertyNode.class ).getValue() ).isSameAs( bobsApartment );

		node = nodeIterator.next();
		assertThat( node.getName() ).isEqualTo( "resident" );
		assertThat( node.as( PropertyNode.class ).getValue() ).isSameAs( bob );

		node = nodeIterator.next();
		assertThat( node.getName() ).isEqualTo( "name" );
		assertThat( node.as( PropertyNode.class ).getValue() ).isEqualTo( "Bob" );
		//end::include[]
		if ( path instanceof org.hibernate.validator.path.Path hvPath ) {
			assertThat( hvPath.getLeafNode().getName() ).isEqualTo( "name" );
			assertThat( hvPath.getLeafNode().as( PropertyNode.class ).getValue() ).isEqualTo( "Bob" );
		}
		else {
			fail( "Unexpected path node type: " + path.getClass().getName() );
		}

		assertThat( nodeIterator.hasNext() ).isFalse();
	}
}
