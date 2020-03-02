/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cfg;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.UniqueElementsDef;
import org.hibernate.validator.constraints.UniqueElements;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

/**
 * @author Guillaume Smet
 */
public class UniqueElementsDefTest {

	@Test
	@TestForIssue(jiraKey = "HV-1466")
	public void testUniqueElementsDef() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );

		final ConstraintMapping programmaticMapping = configuration.createConstraintMapping();
		programmaticMapping.type( Library.class )
				.field( "books" ).constraint( new UniqueElementsDef() );
		configuration.addMapping( programmaticMapping );

		Validator validator = configuration.buildValidatorFactory().getValidator();
		Set<ConstraintViolation<Library>> violations = validator.validate( new Library(
				Arrays.asList( "A Prayer for Owen Meany", "The Cider House Rules", "The Cider House Rules" ) ) );

		assertThat( violations ).containsOnlyViolations(
				violationOf( UniqueElements.class ).withProperty( "books" )
		);
	}

	@SuppressWarnings("unused")
	private static class Library {

		private final List<String> books;

		public Library(List<String> books) {
			this.books = books;
		}
	}
}
