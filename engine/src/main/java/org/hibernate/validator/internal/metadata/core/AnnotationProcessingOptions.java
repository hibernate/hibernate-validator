/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.core;

import org.hibernate.validator.internal.properties.Constrainable;

/**
 * An  {@code AnnotationProcessingOptions} instance keeps track of annotations which should be ignored as configuration source.
 * The main validation source for Bean Validation is annotation and alternate configuration sources use this class
 * to override/ignore existing annotations.
 *
 * @author Hardy Ferentschik
 */
public interface AnnotationProcessingOptions {
	boolean areClassLevelConstraintsIgnoredFor(Class<?> clazz);

	boolean areMemberConstraintsIgnoredFor(Constrainable constrainable);

	boolean areReturnValueConstraintsIgnoredFor(Constrainable constrainable);

	boolean areCrossParameterConstraintsIgnoredFor(Constrainable constrainable);

	boolean areParameterConstraintsIgnoredFor(Constrainable constrainable, int index);

	void merge(AnnotationProcessingOptions annotationProcessingOptions);
}
