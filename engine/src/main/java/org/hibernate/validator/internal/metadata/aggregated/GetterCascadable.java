/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.properties.Property;

/**
 * A {@link Cascadable} backed by a getter of a Java bean.
 *
 * @author Gunnar Morling
 * @author Marko Bekhta
 */
public class GetterCascadable extends PropertyCascadable {

	GetterCascadable(Property property, CascadingMetaData cascadingMetaData) {
		super( property, cascadingMetaData );
	}

	@Override
	public ConstraintLocationKind getConstraintLocationKind() {
		return ConstraintLocationKind.METHOD;
	}

	public static class Builder extends PropertyCascadable.Builder {

		protected Builder(ValueExtractorManager valueExtractorManager, Property property, CascadingMetaDataBuilder cascadingMetaDataBuilder) {
			super( valueExtractorManager, property, cascadingMetaDataBuilder );
		}

		@Override
		protected Cascadable create(Property property, CascadingMetaData cascadingMetaData) {
			return new GetterCascadable( property, cascadingMetaData );
		}
	}
}
