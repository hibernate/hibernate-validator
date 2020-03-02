/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cfg;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.cfg.defs.AssertTrueDef;
import org.hibernate.validator.cfg.defs.MinDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.spi.cfg.ConstraintMappingContributor;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidationXmlTestHelper;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

/**
 * Tests the contribution of constraint mappings via a contributor configured in {@code META-INF/validation.xml}.
 *
 * @author Gunnar Morling
 */
public class ConstraintMappingContributorConfiguredInValidationXmlTest {

	@Test
	@TestForIssue(jiraKey = "HV-955")
	public void shouldApplyConstraintMappingsFromContributorsConfiguredInValidationXml() {
		runWithCustomValidationXml( "constraint-mapping-contributors-validation.xml", new Runnable() {

			@Override
			public void run() {
				Validator validator = ValidatorUtil.getValidator();

				Set<? extends ConstraintViolation<?>> violations = validator.validate( new Marathon() );
				assertThat( violations ).containsOnlyViolations(
						violationOf( NotNull.class ).withProperty( "name" ),
						violationOf( Min.class ).withProperty( "numberOfHelpers" )
				);

				violations = validator.validate( new Runner() );
				assertThat( violations ).containsOnlyViolations(
						violationOf( AssertTrue.class ).withProperty( "paidEntryFee" )
				);
			}
		} );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1545")
	public void shouldApplyConstraintMappingsFromContributorsConfiguredInValidationXmlWithDeprecatedProperty() {
		runWithCustomValidationXml( "deprecated-constraint-mapping-contributor-validation.xml", new Runnable() {

			@Override
			public void run() {
				Validator validator = ValidatorUtil.getValidator();

				Set<? extends ConstraintViolation<?>> violations = validator.validate( new Marathon() );
				assertThat( violations ).containsOnlyViolations(
						violationOf( NotNull.class ).withProperty( "name" ),
						violationOf( Min.class ).withProperty( "numberOfHelpers" )
				);

				violations = validator.validate( new Runner() );
				assertThat( violations ).containsOnlyViolations(
						violationOf( AssertTrue.class ).withProperty( "paidEntryFee" )
				);
			}
		} );
	}

	private void runWithCustomValidationXml(String validationXmlName, Runnable runnable) {
		new ValidationXmlTestHelper( ConstraintMappingContributorConfiguredInValidationXmlTest.class ).
			runWithCustomValidationXml( validationXmlName, runnable );
	}

	public static class MyConstraintMappingContributor1 implements ConstraintMappingContributor {

		@Override
		public void createConstraintMappings(ConstraintMappingBuilder builder) {
			builder.addConstraintMapping()
				.type( Marathon.class )
					.getter( "name" )
						.constraint( new NotNullDef() )
					.field( "numberOfHelpers" )
						.constraint( new MinDef().value( 1 ) );
		}
	}

	public static class MyConstraintMappingContributor2 implements ConstraintMappingContributor {

		@Override
		public void createConstraintMappings(ConstraintMappingBuilder builder) {
			builder.addConstraintMapping()
				.type( Runner.class )
					.field( "paidEntryFee" )
						.constraint( new AssertTrueDef() );
		}
	}
}
