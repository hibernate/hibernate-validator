/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.reflect.Type;
import java.util.List;
import javax.validation.ElementKind;
import javax.validation.metadata.ElementDescriptor;

import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;

/**
 * An aggregated view of the constraint related meta data for a given bean/type
 * element and all the elements in the inheritance hierarchy which it overrides
 * or implements.
 *
 * @author Gunnar Morling
 */
public interface ConstraintMetaData extends Iterable<MetaConstraint<?>> {

	/**
	 * Returns the name of this meta data object.
	 *
	 * @return This meta data object's name.
	 */
	String getName();

	/**
	 * Returns the data type of this meta data object, e.g. the type of a bean property or the
	 * return type of a method.
	 *
	 * @return This meta data object's type.
	 */
	Type getType();

	/**
	 * Returns the {@link ElementKind kind} of this meta data object.
	 *
	 * @return The {@link ElementKind kind} of this meta data object.
	 */
	ElementKind getKind();

	/**
	 * Whether this meta data object is marked for cascaded validation or not.
	 *
	 * @return {@code true}if this object is marked for cascaded validation, {@code false} otherwise.
	 */
	boolean isCascading();

	/**
	 * Whether this meta data object is constrained by any means or not.
	 *
	 * @return {@code true} if this object is marked for cascaded validation or has any constraints, {@code false} otherwise.
	 */
	boolean isConstrained();

	/**
	 * Returns this meta data object's corresponding representation in the
	 * descriptor model.
	 *
	 * @param defaultGroupSequenceRedefined Whether the bean hosting the represented element has a
	 * redefined default group sequence or not.
	 * @param defaultGroupSequence The default group sequence of the bean hosting the represented
	 * element.
	 *
	 * @return This meta data object's corresponding descriptor model
	 *         representation. Implementations should return a specific sub type
	 *         of {@link ElementDescriptor}.
	 */
	ElementDescriptor asDescriptor(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence);

	/**
	 * Defines how the validated values needs to be treated in case there is a potential unrapper specified for its type
	 *
	 * @return the {@code ValidatedValueUnwrapMode} to be used for this constraint.
	 */
	UnwrapMode unwrapMode();
}
