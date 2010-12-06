/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.testng.annotations.Test;

import org.hibernate.validator.util.LazyValidatorFactory;

import static org.testng.Assert.assertEquals;

/**
 * @author Emmanuel Bernard
 */
public class LazyValidatorFactoryTest {
	/**
	 * Simple test that makes sure this class works.
	 * The lazy feature is not tested per se
	 * nor is the fact that the default provider is forced to Hibernate Validator
	 */
	@Test
	public void testLazyValidatorFactory() {
		LazyValidatorFactory factory = new LazyValidatorFactory();
		Validator validator = factory.getValidator();
		assertEquals( 1, validator.validate( new A() ).size() );

		factory = new LazyValidatorFactory( Validation.byDefaultProvider().configure() );
		validator = factory.getValidator();
		assertEquals( 1, validator.validate( new A() ).size() );
	}

	public static class A {
		@NotNull
		String b;
	}
}
