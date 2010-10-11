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

import java.util.Iterator;
import javax.validation.Path;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

import org.hibernate.validator.engine.PathImpl;

/**
 * @author Hardy Ferentschik
 */
public class PathImplTest {

	@Test
	public void testParsing() {
		String property = "order[3].deliveryAddress.addressline[1]";
		Path path = PathImpl.createPathFromString( property );
		Iterator<Path.Node> propIter = path.iterator();

		assertTrue( propIter.hasNext() );
		Path.Node elem = propIter.next();
		assertEquals( "order", elem.getName() );
		assertTrue( elem.isInIterable() );
		assertEquals( new Integer( 3 ), elem.getIndex() );

		assertTrue( propIter.hasNext() );
		elem = propIter.next();
		assertEquals( "deliveryAddress", elem.getName() );
		assertFalse( elem.isInIterable() );
		assertEquals( null, elem.getIndex() );

		assertTrue( propIter.hasNext() );
		elem = propIter.next();
		assertEquals( "addressline", elem.getName() );
		assertTrue( elem.isInIterable() );
		assertEquals( new Integer( 1 ), elem.getIndex() );

		assertFalse( propIter.hasNext() );
	}

	@Test
	public void testParseMapBasedProperty() {
		String property = "order[foo].deliveryAddress";
		Path path = PathImpl.createPathFromString( property );
		Iterator<Path.Node> propIter = path.iterator();

		assertTrue( propIter.hasNext() );
		Path.Node elem = propIter.next();
		assertEquals( "order", elem.getName() );
		assertTrue( elem.isInIterable() );
		assertEquals( "foo", elem.getKey() );

		assertTrue( propIter.hasNext() );
		elem = propIter.next();
		assertEquals( "deliveryAddress", elem.getName() );
		assertFalse( elem.isInIterable() );
		assertEquals( null, elem.getIndex() );

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
	public void testTrailingPathSeperator() {
		PathImpl.createPathFromString( "foo.bar." );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testLeadingPathSeperator() {
		PathImpl.createPathFromString( ".foo.bar" );
	}

	@Test
	public void testEmptyString() {
		Path path = PathImpl.createPathFromString( "" );
		assertTrue( path.iterator().hasNext() );
	}
}
