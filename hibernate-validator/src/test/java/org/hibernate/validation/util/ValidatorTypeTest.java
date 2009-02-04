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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotNull;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import org.hibernate.validation.constraints.composition.FrenchZipcodeConstraintValidator;

/**
 * Tests for message resolution.
 *
 * @author Emmanuel Bernard
 */
public class ValidatorTypeTest {

	@Test
	public void testTypeDiscovery() {
		List<Class<? extends ConstraintValidator<?, ?>>> validators = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		validators.add( FrenchZipcodeConstraintValidator.class );
		Map<Class<?>, Class<? extends ConstraintValidator<? extends Annotation, ?>>> validatorsTypes = ValidatorTypeHelper
				.getValidatorsTypes( validators );
		assertEquals( FrenchZipcodeConstraintValidator.class, validatorsTypes.get( String.class ) );

		Type type = ValidatorTypeHelper
				.extractTypeLoose( TestValidator.class );


		Type type2 = ValidatorTypeHelper
				.extractTypeLoose( TestValidator2.class );
		assertEquals( type, type2 );

	}

	public static class TestValidator implements ConstraintValidator<NotNull, Set<String>> {
		public void initialize(NotNull constraintAnnotation) {
		}

		public boolean isValid(Set<String> object, ConstraintValidatorContext constraintValidatorContext) {
			return false;
		}
	}

	public static class TestValidator2 implements ConstraintValidator<NotNull, Set<String>> {
		public void initialize(NotNull constraintAnnotation) {
		}

		public boolean isValid(Set<String> object, ConstraintValidatorContext constraintValidatorContext) {
			return true;
		}
	}
}