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
package org.hibernate.validation.constraints;

import java.lang.annotation.Annotation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * @author Hardy Ferentschik
 */
public class PatternConstraintTest {
	@Test
	public void testIsValid() {
		PatternConstraint constraint = new PatternConstraint();
		constraint.initialize(
				new Pattern() {

					public String message() {
						return "{validator.pattern}";
					}

					public String[] groups() {
						return new String[0];
					}

					public String regex() {
						return "foobar";
					}

					public int flags() {
						return 0;
					}

					public Class<? extends Annotation> annotationType() {
						return this.getClass();
					}
				}
		);

		assertTrue( constraint.isValid( null, null ) );
		assertFalse( constraint.isValid( "", null ) );
		assertFalse( constraint.isValid( "bla bla", null ) );
		assertFalse( constraint.isValid( "This test is not foobar", null ) );

		try {
			constraint.isValid( new Object(), null );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			// success
		}
	}
}
