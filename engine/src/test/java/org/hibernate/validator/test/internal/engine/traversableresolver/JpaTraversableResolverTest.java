/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.traversableresolver;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import jakarta.validation.Configuration;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.internal.engine.resolver.JPATraversableResolver;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * See HV-305
 *
 * @author Hardy Ferentschik
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JpaTraversableResolverTest {
	private Validator validator;

	@BeforeAll
	public void setUp() {
		Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.traversableResolver( new JPATraversableResolver() );
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
