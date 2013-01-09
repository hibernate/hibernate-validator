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
package org.hibernate.validator.test.internal.engine.traversableresolver;

import java.util.Set;
import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.engine.resolver.DefaultTraversableResolver;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.testng.Assert.assertTrue;


/**
 * See HV-305
 *
 * @author Hardy Ferentschik
 */
public class JpaTraversableResolverTest {
	private Validator validator;

	@BeforeTest
	public void setUp() {
		Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.traversableResolver( new DefaultTraversableResolver() );
		validator = configuration.buildValidatorFactory().getValidator();
	}

	@Test
	@TestForIssue(jiraKey = "HV-305")
	public void testWithBooks() {
		Author author = new Author();
		author.books.add( new Book() );
		Set<ConstraintViolation<Author>> results = validator.validate( author );
		assertTrue( results.isEmpty() );
	}

	@Test
	@TestForIssue(jiraKey = "HV-305")
	public void testWithoutBooks() {
		Author author = new Author();

		// If the "books" collection is empty, everything works as expected.
		Set<ConstraintViolation<Author>> results = validator.validate( author );
		assertTrue( results.isEmpty() );
	}
}


