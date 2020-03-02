/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.sequence;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.GroupSequence;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
@TestForIssue( jiraKey = "HV-1715")
public class SequenceTest {

	@Test
	public void groupSequenceOfGroupSequences() {
		Validator validator = ValidatorUtil.getValidator();

		Set<ConstraintViolation<Foo>> violations = validator.validate( new Foo( null, "", null ) );

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withProperty( "str1" )
		);
	}

	interface Group1 extends Group11, Group12, Group13, Group14, Group15, Group16, Group17, Group18, Group19 {
	}

	interface Group11 {
	}

	interface Group12 {
	}

	interface Group13 {
	}

	interface Group14 {
	}

	interface Group15 {
	}

	interface Group16 {
	}

	interface Group17 {
	}

	interface Group18 {
	}

	interface Group19 {
	}

	interface Group2 {
	}

	@GroupSequence({ Foo.class, SequenceTest.Group1.class, SequenceTest.Group2.class })
	private static class Foo {
		@NotNull(groups = SequenceTest.Group11.class)
		private String str1;
		@NotNull(groups = SequenceTest.Group12.class)
		private String str2;
		@NotNull(groups = SequenceTest.Group2.class)
		private String str3;

		public Foo(String str1, String str2, String str3) {
			this.str1 = str1;
			this.str2 = str2;
			this.str3 = str3;
		}
	}
}
