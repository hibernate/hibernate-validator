/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

public class RegexpEscapingInterpolationTest {

	private static final String REGEXP_CONTAINING_SPECIAL_CHARACTERS = "[{0-9]{1,160}$";

	@Test
	public void regexpShouldBeEscapedBeforeBeingInjectedIntoMessageDuringInterpolation() throws Exception {
		Validator validator = ValidatorUtil.getValidator();

		Set<ConstraintViolation<PatternWithCharactersUsedForInterpolationEntity>> violations = validator.validate( new PatternWithCharactersUsedForInterpolationEntity( "test" ) );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Pattern.class ).withMessage( "must match \"" + REGEXP_CONTAINING_SPECIAL_CHARACTERS + "\"" )
		);
	}

	private static class PatternWithCharactersUsedForInterpolationEntity {

		@Pattern(regexp = REGEXP_CONTAINING_SPECIAL_CHARACTERS)
		private String field;

		private PatternWithCharactersUsedForInterpolationEntity(String field) {
			this.field = field;
		}
	}
}
