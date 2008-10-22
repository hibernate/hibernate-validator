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

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * @author Hardy Ferentschik
 */
public class ReflectionHelperTest {
	@Test
	public void testGetIndexedValueFormMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		Object testObject = new Object();
		String key = "key";
		map.put( key, testObject );

		Object value = ReflectionHelper.getIndexedValue( map, key );
		assertEquals( "We should be able to retrieve the indexed object", testObject, value );

		// try to get to the value by integer index
	    value = ReflectionHelper.getIndexedValue( map, "0" );
		assertEquals( "We should be able to retrieve the indexed object", testObject, value );

		value = ReflectionHelper.getIndexedValue( map, "foo" );
		assertNull("A non existent index should return the null value", value);

		value = ReflectionHelper.getIndexedValue( map, "2" );
		assertNull("A non existent index should return the null value", value);
	}

	@Test
	public void testGetIndexedValueForList() {
		List<Object> list = new ArrayList<Object>();
		Object testObject = new Object();
		list.add( testObject );

		Object value = ReflectionHelper.getIndexedValue( list, "0" );
		assertEquals( "We should be able to retrieve the indexed object", testObject, value );

		value = ReflectionHelper.getIndexedValue( list, "2" );
		assertNull("A non existent index should return the null value", value);
	}

	@Test
	public void testGetIndexedValueForNull() {
		Object value = ReflectionHelper.getIndexedValue( null, "0" );
		assertNull( value );
	}
}
