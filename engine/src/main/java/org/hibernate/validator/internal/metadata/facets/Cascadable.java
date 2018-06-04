/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.facets;

import java.lang.reflect.Type;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaData;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;

/**
 * Provides a unified view on cascadable elements of all kinds, be it properties
 * of a Java bean, the arguments passed to an executable or the value returned
 * from an executable. Allows a unified handling of these elements in the
 * validation routine.
 *
 * @author Gunnar Morling
 */
public interface Cascadable {

	/**
	 * Returns the constraint location kind of the cascadable.
	 *
	 * @return Returns the constraint location kind of the cascadable.
	 */
	ConstraintLocationKind getConstraintLocationKind();

	/**
	 * Returns the data type of this cascadable, e.g. the type of a bean property or the
	 * return type of a method.
	 *
	 * @return This cascadable type.
	 */
	Type getCascadableType();

	/**
	 * Returns the value of this cacadable from the given parent.
	 */
	Object getValue(Object parent);

	/**
	 * Appends this cascadable element to the given path.
	 */
	void appendTo(PathImpl path);

	/**
	 * Returns cascading metadata of this cascadable element. Also contains the cascading metadata of the potential
	 * container element types.
	 */
	CascadingMetaData getCascadingMetaData();

	public interface Builder {

		void mergeCascadingMetaData(CascadingMetaDataBuilder cascadingMetaDataBuilder);

		Cascadable build();
	}
}
