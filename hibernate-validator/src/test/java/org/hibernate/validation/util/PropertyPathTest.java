// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.util;

import java.util.Iterator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class PropertyPathTest {

	@Test
	public void testParsing() {
		String property = "order[3].deliveryAddress.addressline[1]";
		PropertyPath path = new PropertyPath( property );
		Iterator<PropertyPath.PathElement> propIter = path.iterator();

		assertTrue( propIter.hasNext() );

		PropertyPath.PathElement elem = propIter.next();
		assertTrue( propIter.hasNext() );
		assertEquals( "order", elem.value() );
		assertTrue( elem.isIndexed() );
		assertEquals( "3", elem.getIndex() );
		assertEquals( property, path.getOriginalProperty() );

		assertTrue( propIter.hasNext() );
		elem = propIter.next();
		assertEquals( "deliveryAddress", elem.value() );
		assertFalse( elem.isIndexed() );
		assertEquals( null, elem.getIndex() );

		assertTrue( propIter.hasNext() );
		elem = propIter.next();
		assertEquals( "addressline", elem.value() );
		assertTrue( elem.isIndexed() );
		assertEquals( "1", elem.getIndex() );

		assertFalse( propIter.hasNext() );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNull() {
		new PropertyPath( null );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testUnbalancedBraces() {
		new PropertyPath( "foo[.bar" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testIndexInMiddleOfProperty() {
		new PropertyPath( "f[1]oo.bar" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testTrailingPathSeperator() {
		new PropertyPath( "foo.bar." );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testLeadingPathSeperator() {
		new PropertyPath( ".foo.bar" );
	}

	@Test
	public void testEmptyString() {
		PropertyPath path = new PropertyPath( "" );
		assertFalse( path.iterator().hasNext() );
	}
}
