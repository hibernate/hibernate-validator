/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling;

import java.util.List;
import java.util.Map;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;
import javax.validation.valueextraction.ValueExtractorDefinitionException;

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
				.addCascadedValueExtractor( new SeveralExtractedValuesValueExtractor() )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test(expectedExceptions = ValueExtractorDefinitionException.class, expectedExceptionsMessageRegExp = "HV000203.*")
	public void noExtractedValueThrowsException() {
		ValidatorUtil.getConfiguration()
				.addCascadedValueExtractor( new NoExtractedValueValueExtractor() )
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

}
