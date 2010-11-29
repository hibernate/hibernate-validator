/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.engine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.testng.annotations.Test;

import org.hibernate.validator.engine.PathImpl;
import org.hibernate.validator.test.util.TestUtil;

import static org.hibernate.validator.test.util.TestUtil.assertCorrectPropertyPaths;
import static org.hibernate.validator.test.util.TestUtil.assertNumberOfViolations;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * @author Hardy Ferentschik
 */
public class PathImplTest {

	@Test
	public void testParsing() {
		String property = "orders[3].deliveryAddress.addressline[1]";
		Path path = PathImpl.createPathFromString( property );
		Iterator<Path.Node> propIter = path.iterator();

		assertTrue( propIter.hasNext() );
		Path.Node elem = propIter.next();
		assertEquals( elem.getName(), "orders" );
		assertFalse( elem.isInIterable() );

		assertTrue( propIter.hasNext() );
		elem = propIter.next();
		assertEquals( elem.getName(), "deliveryAddress" );
		assertTrue( elem.isInIterable() );
		assertEquals( elem.getIndex(), new Integer( 3 ) );

		assertTrue( propIter.hasNext() );
		elem = propIter.next();
		assertEquals( elem.getName(), "addressline" );
		assertFalse( elem.isInIterable() );

		assertTrue( propIter.hasNext() );
		elem = propIter.next();
		assertEquals( elem.getName(), null );
		assertTrue( elem.isInIterable() );
		assertEquals( elem.getIndex(), new Integer( 1 ) );

		assertFalse( propIter.hasNext() );

		assertEquals( path.toString(), property );
	}

	@Test
	public void testParseMapBasedProperty() {
		String property = "order[foo].deliveryAddress";
		Path path = PathImpl.createPathFromString( property );
		Iterator<Path.Node> propIter = path.iterator();

		assertTrue( propIter.hasNext() );
		Path.Node elem = propIter.next();
		assertEquals( "order", elem.getName() );
		assertFalse( elem.isInIterable() );

		assertTrue( propIter.hasNext() );
		elem = propIter.next();
		assertEquals( "deliveryAddress", elem.getName() );
		assertTrue( elem.isInIterable() );
		assertEquals( "foo", elem.getKey() );

		assertFalse( propIter.hasNext() );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNull() {
		PathImpl.createPathFromString( null );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testUnbalancedBraces() {
		PathImpl.createPathFromString( "foo[.bar" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testIndexInMiddleOfProperty() {
		PathImpl.createPathFromString( "f[1]oo.bar" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testTrailingPathSeparator() {
		PathImpl.createPathFromString( "foo.bar." );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testLeadingPathSeparator() {
		PathImpl.createPathFromString( ".foo.bar" );
	}

	@Test
	public void testEmptyString() {
		Path path = PathImpl.createPathFromString( "" );
		assertTrue( path.iterator().hasNext() );
	}

	@Test
	public void testNonStringMapKey() {
		Validator validator = TestUtil.getValidator();
		Container container = new Container();
		Long id = 0l;
		container.addItem( id, new Item( null) );
		Set<ConstraintViolation<Container>> constraintViolations = validator.validate( container );
		assertNumberOfViolations( constraintViolations, 1 );
		ConstraintViolation<Container> violation = constraintViolations.iterator().next();
		Path path = violation.getPropertyPath();
		Iterator<Path.Node> iter = path.iterator();
		iter.next();
		Path.Node node = iter.next();
		assertNotNull(node);
		assertTrue( node.isInIterable() );
		assertEquals( node.getKey(), id  );
	}

	class Container {
		@Valid
		Map<Long, Item> store = new HashMap<Long, Item>();

		public void addItem(Long id, Item item) {
			store.put( id, item );
		}
	}

	class Item {
		@NotNull
		String id;

		Item(String id) {
			this.id = id;
		}
	}
}
