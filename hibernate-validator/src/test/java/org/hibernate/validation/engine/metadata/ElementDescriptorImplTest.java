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
package org.hibernate.validation.engine.metadata;

import java.util.Set;
import javax.validation.ConstraintDescriptor;
import javax.validation.ElementDescriptor;
import javax.validation.Validator;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import org.hibernate.validation.engine.Order;
import org.hibernate.validation.util.TestUtil;


/**
 * @author Hardy Ferentschik
 */
public class ElementDescriptorImplTest {
	/**
	 * HV-95
	 */
	@Test
	public void testElementDescriptorImmutable() {
		Validator validator = TestUtil.getValidator();
		ElementDescriptor elementDescriptor = validator.getConstraintsForClass( Order.class )
				.getConstraintsForProperty( "orderNumber" );
		Set<ConstraintDescriptor<?>> constraintDescriptors = elementDescriptor.getConstraintDescriptors();
		assertTrue( "There should be a ConstraintDescriptor", constraintDescriptors.size() == 1 );
		ConstraintDescriptor<?> descriptor = constraintDescriptors.iterator().next();

		try {
			constraintDescriptors.add( descriptor );
			fail( "Set should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {

		}

		try {
			constraintDescriptors.remove( descriptor );
			fail( "Set should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {

		}
	}
}