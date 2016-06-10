/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
import org.hibernate.validator.testutils.ValidatorUtil;

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


