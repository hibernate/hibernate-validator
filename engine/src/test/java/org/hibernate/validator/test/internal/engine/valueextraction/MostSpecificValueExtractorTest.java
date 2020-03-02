/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.test.internal.engine.valueextraction.model.Entity1;
import org.hibernate.validator.test.internal.engine.valueextraction.model.Entity2;
import org.hibernate.validator.test.internal.engine.valueextraction.model.IWrapper11;
import org.hibernate.validator.test.internal.engine.valueextraction.model.IWrapper111;
import org.hibernate.validator.test.internal.engine.valueextraction.model.IWrapper21;
import org.hibernate.validator.test.internal.engine.valueextraction.model.IWrapper211;
import org.hibernate.validator.test.internal.engine.valueextraction.model.IWrapper212;
import org.hibernate.validator.test.internal.engine.valueextraction.model.IWrapper22;
import org.hibernate.validator.test.internal.engine.valueextraction.model.IWrapper221;
import org.hibernate.validator.test.internal.engine.valueextraction.model.Wrapper1;
import org.hibernate.validator.test.internal.engine.valueextraction.model.Wrapper2;
import org.hibernate.validator.testutil.ConstraintViolationAssert;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.CandidateForTck;
import org.testng.annotations.Test;

/**
 * @author Guillaume Smet
 */
@CandidateForTck
public class MostSpecificValueExtractorTest {

	@Test
	@TestForIssue(jiraKey = "HV-1306")
	public void mostSpecificValueExtractorFound() throws Exception {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValueExtractor( new IWrapper11ValueExtractor() )
				.addValueExtractor( new IWrapper111ValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Entity1>> violations = validator.validate( new Entity1( null ) );
		ConstraintViolationAssert.assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withPropertyPath( pathWith()
						.property( "wrapper" )
						.containerElement( "IWrapper11", false, null, null, Wrapper1.class, 0 )
				)
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1306")
	public void parallelValueExtractorDefinitionsCausesException() throws Exception {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValueExtractor( new IWrapper21ValueExtractor() )
				.addValueExtractor( new IWrapper211ValueExtractor() )
				.addValueExtractor( new IWrapper212ValueExtractor() )
				.addValueExtractor( new IWrapper22ValueExtractor() )
				.addValueExtractor( new IWrapper221ValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		try {
			validator.validate( new Entity2( null ) );
			fail( "An exception should have been thrown" );
		}
		catch (ConstraintDeclarationException e) {
			String message = e.getMessage();
			assertThat( message ).startsWith( "HV000219" );
			assertThat( message ).contains( Wrapper2.class.getName() );
			assertThat( message ).contains( IWrapper21ValueExtractor.class.getName() );
			assertThat( message ).doesNotContain( IWrapper211ValueExtractor.class.getName() );
			assertThat( message ).doesNotContain( IWrapper212ValueExtractor.class.getName() );
			assertThat( message ).contains( IWrapper22ValueExtractor.class.getName() );
			assertThat( message ).doesNotContain( IWrapper221ValueExtractor.class.getName() );
		}
	}

	private static class IWrapper11ValueExtractor implements ValueExtractor<IWrapper11<@ExtractedValue ?>> {

		@Override
		public void extractValues(IWrapper11<?> originalValue, ValueReceiver receiver) {
			receiver.value( "IWrapper11", originalValue.getProperty() );
		}
	}

	private static class IWrapper111ValueExtractor implements ValueExtractor<IWrapper111<@ExtractedValue ?>> {

		@Override
		public void extractValues(IWrapper111<?> originalValue, ValueReceiver receiver) {
			receiver.value( "IWrapper111", originalValue.getProperty() );
		}
	}

	private static class IWrapper21ValueExtractor implements ValueExtractor<IWrapper21<@ExtractedValue ?>> {

		@Override
		public void extractValues(IWrapper21<?> originalValue, ValueReceiver receiver) {
			receiver.value( "IWrapper21", originalValue.getProperty() );
		}
	}

	private static class IWrapper211ValueExtractor implements ValueExtractor<IWrapper211<@ExtractedValue ?>> {

		@Override
		public void extractValues(IWrapper211<?> originalValue, ValueReceiver receiver) {
			receiver.value( "IWrapper211", originalValue.getProperty() );
		}
	}

	private static class IWrapper212ValueExtractor implements ValueExtractor<IWrapper212<@ExtractedValue ?>> {

		@Override
		public void extractValues(IWrapper212<?> originalValue, ValueReceiver receiver) {
			receiver.value( "IWrapper212", originalValue.getProperty() );
		}
	}

	private static class IWrapper22ValueExtractor implements ValueExtractor<IWrapper22<@ExtractedValue ?>> {

		@Override
		public void extractValues(IWrapper22<?> originalValue, ValueReceiver receiver) {
			receiver.value( "IWrapper22", originalValue.getProperty() );
		}
	}

	private static class IWrapper221ValueExtractor implements ValueExtractor<IWrapper221<@ExtractedValue ?>> {

		@Override
		public void extractValues(IWrapper221<?> originalValue, ValueReceiver receiver) {
			receiver.value( "IWrapper221", originalValue.getProperty() );
		}
	}
}
