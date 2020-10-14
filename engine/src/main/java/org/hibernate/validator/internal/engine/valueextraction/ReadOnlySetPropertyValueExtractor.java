/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.Optional;
import java.util.Set;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.IgnoreForbiddenApisErrors;
import org.hibernate.validator.internal.engine.path.NodeImpl;

import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.value.ObservableValue;

/**
 * A value extractor for JavaFX's {@link ReadOnlySetProperty}.
 * <p>
 * It is necessary to define one as {@link ReadOnlySetProperty} inherits from both {@link Set} and {@link ObservableValue} and
 * it is not possible to determine the corresponding {@link ValueExtractor} without creating a specific one.
 *
 * @author Guillaume Smet
 */
@SuppressWarnings("restriction")
@IgnoreForbiddenApisErrors(reason = "Usage of JavaFX classes")
class ReadOnlySetPropertyValueExtractor implements ValueExtractor<ReadOnlySetProperty<@ExtractedValue ?>> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new ReadOnlySetPropertyValueExtractor(), ReadOnlySetProperty.class,
			ReadOnlySetProperty.class.getTypeParameters()[0], false, Optional.empty() );

	private ReadOnlySetPropertyValueExtractor() {
	}

	@Override
	public void extractValues(ReadOnlySetProperty<?> originalValue, ValueExtractor.ValueReceiver receiver) {
		for ( Object object : originalValue ) {
			receiver.iterableValue( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, object );
		}
	}
}
