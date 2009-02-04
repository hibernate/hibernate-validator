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
package org.hibernate.validation.engine;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.Test;

import org.hibernate.validation.eg.MultipleMinMax;
import static org.hibernate.validation.util.TestUtil.assertNumberOfViolations;
import static org.hibernate.validation.util.TestUtil.getValidator;

/**
 * Tests for constraint validator resolution.
 *
 * @author Hardy Ferentschik
 */
public class ValidatorResolutionTest {

	@Test
	public void testValidatorResolutionForMinMax() {
		Validator validator = getValidator();

		MultipleMinMax minMax = new MultipleMinMax( "5", 5 );
		Set<ConstraintViolation<MultipleMinMax>> constraintViolations = validator.validate( minMax );
		assertNumberOfViolations( constraintViolations, 2 );
	}
}