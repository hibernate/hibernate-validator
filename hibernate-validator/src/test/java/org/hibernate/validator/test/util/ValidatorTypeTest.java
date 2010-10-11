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
package org.hibernate.validator.test.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintValidator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.Test;

import org.hibernate.validator.util.ValidatorTypeHelper;

/**
 * Tests for message resolution.
 *
 * @author Emmanuel Bernard
 */
public class ValidatorTypeTest {

	@Test
	public void testTypeDiscovery() {
		List<Class<? extends ConstraintValidator<Positive, ?>>> validators =
				new ArrayList<Class<? extends ConstraintValidator<Positive, ?>>>();
		validators.add( PositiveConstraintValidator.class );
		Map<Type, Class<? extends ConstraintValidator<?, ?>>> validatorsTypes = ValidatorTypeHelper
				.getValidatorsTypes( validators );

		assertEquals( validatorsTypes.get( Integer.class ), PositiveConstraintValidator.class );
		assertNull( validatorsTypes.get( String.class ) );
	}
}
