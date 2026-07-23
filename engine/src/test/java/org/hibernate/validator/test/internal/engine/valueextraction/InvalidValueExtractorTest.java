/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;
import jakarta.validation.valueextraction.ValueExtractorDefinitionException;

import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.api.Test;

/**
 * Test the exceptions thrown in case of an invalid {@link ValueExtractor}.
 *
 * @author Guillaume Smet
 */
public class InvalidValueExtractorTest {

	@Test
	public void severalExtractedValuesThrowException() {
		assertThatThrownBy( () -> ValidatorUtil.getConfiguration()
				.addValueExtractor( new SeveralExtractedValuesValueExtractor() )
				.buildValidatorFactory()
				.getValidator() )
				.isInstanceOf( ValueExtractorDefinitionException.class )
				.hasMessageMatching( "HV000204.*" );
	}

	@Test
	public void noExtractedValueThrowsException() {
		assertThatThrownBy( () -> ValidatorUtil.getConfiguration()
				.addValueExtractor( new NoExtractedValueValueExtractor() )
				.buildValidatorFactory()
				.getValidator() )
				.isInstanceOf( ValueExtractorDefinitionException.class )
				.hasMessageMatching( "HV000203.*" );
	}

	@Test
	public void boundWilcardTypeArgument1ThrowsException() {
		assertThatThrownBy( () -> ValidatorUtil.getConfiguration()
				.addValueExtractor( new BoundWildcardTypeArgumentValueExtractor1() )
				.buildValidatorFactory()
				.getValidator() )
				.isInstanceOf( ValueExtractorDefinitionException.class )
				.hasMessageMatching( "HV000225.*" );
	}

	@Test
	public void boundWilcardTypeArgument2ThrowsException() {
		assertThatThrownBy( () -> ValidatorUtil.getConfiguration()
				.addValueExtractor( new BoundWildcardTypeArgumentValueExtractor2() )
				.buildValidatorFactory()
				.getValidator() )
				.isInstanceOf( ValueExtractorDefinitionException.class )
				.hasMessageMatching( "HV000225.*" );
	}

	@Test
	public void boundWilcardTypeArgument3ThrowsException() {
		assertThatThrownBy( () -> ValidatorUtil.getConfiguration()
				.addValueExtractor( new BoundWildcardTypeArgumentValueExtractor3() )
				.buildValidatorFactory()
				.getValidator() )
				.isInstanceOf( ValueExtractorDefinitionException.class )
				.hasMessageMatching( "HV000225.*" );
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
