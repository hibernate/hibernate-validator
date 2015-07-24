/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cfg;

import org.hibernate.validator.cfg.defs.AssertTrueDef;
import org.hibernate.validator.cfg.defs.MinDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.spi.cfg.ConstraintMappingContributor;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidationXmlTestHelper;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Set;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;

/**
 * Tests the contribution of constraint mappings via a contributor configured in {@code META-INF/validation.xml}.
 *
 * @author Gunnar Morling
 */
public class ConstraintMappingContributorConfiguredInValidationXmlTest {

	@Test
	@TestForIssue(jiraKey = "HV-955")
	public void shouldApplyConstraintMappingsFromContributorConfiguredInValidationXml() {
		runWithCustomValidationXml( "constraint-mapping-contributor-validation.xml", new Runnable() {

			@Override
			public void run() {
				Validator validator = ValidatorUtil.getValidator();

				Set<? extends ConstraintViolation<?>> violations = validator.validate( new Marathon() );
				assertCorrectConstraintTypes( violations, NotNull.class, Min.class );

				violations = validator.validate( new Runner() );
				assertCorrectConstraintTypes( violations, AssertTrue.class );
			}
		} );

	}

	private void runWithCustomValidationXml(String validationXmlName, Runnable runnable) {
		new ValidationXmlTestHelper( ConstraintMappingContributorConfiguredInValidationXmlTest.class ).
			runWithCustomValidationXml( validationXmlName, runnable );
	}

	public static class MyConstraintMappingContributor implements ConstraintMappingContributor {

		@Override
		public void createConstraintMappings(ConstraintMappingBuilder builder) {
			builder.addConstraintMapping()
				.type( Marathon.class )
					.property( "name", METHOD )
						.constraint( new NotNullDef() )
					.property( "numberOfHelpers", FIELD )
						.constraint( new MinDef().value( 1 ) );

			builder.addConstraintMapping()
				.type( Runner.class )
					.property( "paidEntryFee", FIELD )
						.constraint( new AssertTrueDef() );
		}
	}
}
