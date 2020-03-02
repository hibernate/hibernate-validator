/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import java.util.List;
import java.util.Map;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;
import jakarta.validation.valueextraction.ValueExtractorDefinitionException;

import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

/**
 * Test the exceptions thrown in case of an invalid {@link ValueExtractor}.
 *
 * @author Guillaume Smet
 */
public class InvalidValueExtractorTest {

	@Test(expectedExceptions = ValueExtractorDefinitionException.class, expectedExceptionsMessageRegExp = "HV000204.*")
	public void severalExtractedValuesThrowException() {
		ValidatorUtil.getConfiguration()
				.addValueExtractor( new SeveralExtractedValuesValueExtractor() )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test(expectedExceptions = ValueExtractorDefinitionException.class, expectedExceptionsMessageRegExp = "HV000203.*")
	public void noExtractedValueThrowsException() {
		ValidatorUtil.getConfiguration()
				.addValueExtractor( new NoExtractedValueValueExtractor() )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test(expectedExceptions = ValueExtractorDefinitionException.class, expectedExceptionsMessageRegExp = "HV000225.*")
	public void boundWilcardTypeArgument1ThrowsException() {
		ValidatorUtil.getConfiguration()
				.addValueExtractor( new BoundWildcardTypeArgumentValueExtractor1() )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test(expectedExceptions = ValueExtractorDefinitionException.class, expectedExceptionsMessageRegExp = "HV000225.*")
	public void boundWilcardTypeArgument2ThrowsException() {
		ValidatorUtil.getConfiguration()
				.addValueExtractor( new BoundWildcardTypeArgumentValueExtractor2() )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test(expectedExceptions = ValueExtractorDefinitionException.class, expectedExceptionsMessageRegExp = "HV000225.*")
	public void boundWilcardTypeArgument3ThrowsException() {
		ValidatorUtil.getConfiguration()
				.addValueExtractor( new BoundWildcardTypeArgumentValueExtractor3() )
				.buildValidatorFactory()
				.getValidator();
	}

	private class SeveralExtractedValuesValueExtractor implements ValueExtractor<Map<@ExtractedValue ?, @ExtractedValue ?>> {

		@Override
		public void extractValues(Map<?, ?> originalValue, ValueReceiver receiver) {
			throw new IllegalStateException( "May not be called" );
		}
	}

	private class NoExtractedValueValueExtractor implements ValueExtractor<List<?>> {

		@Override
		public void extractValues(List<?> originalValue, ValueReceiver receiver) {
			throw new IllegalStateException( "May not be called" );
		}
	}

	private class BoundWildcardTypeArgumentValueExtractor1 implements ValueExtractor<List<@ExtractedValue ? extends String>> {

		@Override
		public void extractValues(List<? extends String> originalValue, ValueExtractor.ValueReceiver receiver) {
			throw new IllegalStateException( "May not be called" );
		}
	}

	private class BoundWildcardTypeArgumentValueExtractor2 implements ValueExtractor<List<@ExtractedValue String>> {

		@Override
		public void extractValues(List<String> originalValue, ValueExtractor.ValueReceiver receiver) {
			throw new IllegalStateException( "May not be called" );
		}
	}

	private class BoundWildcardTypeArgumentValueExtractor3 implements ValueExtractor<List<@ExtractedValue ? super String>> {

		@Override
		public void extractValues(List<? super String> originalValue, ValueExtractor.ValueReceiver receiver) {
			throw new IllegalStateException( "May not be called" );
		}
	}
}
