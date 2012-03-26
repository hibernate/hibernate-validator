/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.testng.annotations.Test;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.testutil.ValidatorUtil;

import static java.lang.annotation.ElementType.FIELD;
import static org.testng.Assert.assertFalse;

/**
 * Unit test for {@link org.hibernate.validator.cfg.ConstraintMapping} et al.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintMappingWithAnnotationProcessingOptionsTest {

	@Test
	public void testIgnoreAnnotationsOnType() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Foo.class )
				.ignoreAnnotations();

		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );
		assertFalse( validator.getConstraintsForClass( Foo.class ).isBeanConstrained() );
	}

	@Test
	public void testIgnoreAnnotationsOnProperty() {
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Foo.class )
				.property( "property", FIELD )
				.ignoreAnnotations();

		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( mapping );
		assertFalse( validator.getConstraintsForClass( Foo.class ).isBeanConstrained() );
	}

	private static class Foo {
		@NotNull
		private String property;
	}
}
