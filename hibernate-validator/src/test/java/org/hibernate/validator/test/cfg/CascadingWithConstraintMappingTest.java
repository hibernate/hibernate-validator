/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.hibernate.validator.test.cfg;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.test.util.TestUtil;

import static java.lang.annotation.ElementType.FIELD;
import static org.hibernate.validator.test.util.TestUtil.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.test.util.TestUtil.assertNumberOfViolations;

public class CascadingWithConstraintMappingTest {

	/**
	 * See HV-433
	 */
	@Test
	public void testCascadedValidationInHierarchyWithConstraintMapping() {
		HibernateValidatorConfiguration config = TestUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping newMapping = new ConstraintMapping();
		newMapping
				.type( Bar.class )
				.property( "string", FIELD )
				.constraint( NotNullDef.class )
				.type( Foo.class )
				.valid( "bar", FIELD );
		config.addMapping( newMapping );
		ValidatorFactory factory = config.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Baz baz = new Baz();
		baz.bar = new Bar();

		Set<ConstraintViolation<Baz>> violations = validator.validate( baz );

		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "may not be null" );
	}

	private static class Bar {
		private String string;
	}

	private static class Foo {
		protected Bar bar;
	}

	private static class Baz extends Foo {
	}
}