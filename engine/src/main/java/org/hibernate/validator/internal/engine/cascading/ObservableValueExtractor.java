/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.UnwrapByDefault;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.util.IgnoreJava8Requirement;

import javafx.beans.value.ObservableValue;

/**
 * A value extractor for JavaFX's {@link ObservableValue}, e.g. {@code Property<String>}.
 *
 * @author Gunnar Morling
 */
@IgnoreJava8Requirement
@UnwrapByDefault
public class ObservableValueExtractor implements ValueExtractor<ObservableValue<@ExtractedValue ?>> {

	static final ObservableValueExtractor INSTANCE = new ObservableValueExtractor();

	private ObservableValueExtractor() {
	}

	@Override
	public void extractValues(ObservableValue<?> originalValue, ValueExtractor.ValueReceiver receiver) {
		receiver.value( null, originalValue.getValue() );
	}
}
