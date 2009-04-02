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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class PropertyIteratorTest {

	@Test
	public void testSplit() {
		String property = "order[3].deliveryAddress.addressline[1]";
		PropertyIterator propIter = new PropertyIterator( property );

		assertTrue( propIter.hasNext() );

		propIter.split();
		assertTrue( propIter.hasNext() );
		assertEquals( "order", propIter.getHead() );
		assertTrue( propIter.isIndexed() );
		assertEquals( "3", propIter.getIndex() );
		assertEquals( "deliveryAddress.addressline[1]", propIter.getTail() );
		assertEquals( property, propIter.getOriginalProperty() );

		propIter.split();
		assertTrue( propIter.hasNext() );
		assertEquals( "deliveryAddress", propIter.getHead() );
		assertFalse( propIter.isIndexed() );
		assertEquals( null, propIter.getIndex() );
		assertEquals( "addressline[1]", propIter.getTail() );
		assertEquals( property, propIter.getOriginalProperty() );

		propIter.split();
		assertFalse( propIter.hasNext() );
		assertEquals( "addressline", propIter.getHead() );
		assertTrue( propIter.isIndexed() );
		assertEquals( "1", propIter.getIndex() );
		assertEquals( null, propIter.getTail() );
		assertEquals( property, propIter.getOriginalProperty() );
	}

	@Test
	public void testNull() {
		PropertyIterator propIter = new PropertyIterator( null );
		assertFalse( propIter.hasNext() );

		propIter.split();
		assertFalse( propIter.hasNext() );
		assertEquals( null, propIter.getHead() );
		assertFalse( propIter.isIndexed() );
		assertEquals( null, propIter.getIndex() );
		assertEquals( null, propIter.getTail() );
		assertEquals( null, propIter.getOriginalProperty() );
	}

	@Test
	public void testEmptyString() {
		PropertyIterator propIter = new PropertyIterator( "" );
		assertFalse( propIter.hasNext() );

		propIter.split();
		assertFalse( propIter.hasNext() );
		assertEquals( null, propIter.getHead() );
		assertFalse( propIter.isIndexed() );
		assertEquals( null, propIter.getIndex() );
		assertEquals( null, propIter.getTail() );
		assertEquals( "", propIter.getOriginalProperty() );
	}
}
