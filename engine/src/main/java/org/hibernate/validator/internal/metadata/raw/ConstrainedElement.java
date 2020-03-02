/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw;

import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;

import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;

/**
 * Represents a (potentially) constrained Java element such as a type, field or
 * method. Such an element has a set of {@link org.hibernate.validator.internal.metadata.core.MetaConstraint}s and can be
 * marked for a cascaded validation. Furthermore each constrained element has a
 * {@link ConfigurationSource configuration source} which determines its origin.
 * <p>
 * The hierarchy of constrained elements resembles the physical structure of the
 * represented Java types. In particular it doesn't provide the notion of
 * properties and it doesn't aggregate meta data for overridden elements in an
 * inheritance hierarchy.
 * </p>
 * <p>
 * Identity of implementations is based on the element location and constraint
 * source. That means that for instance in a set there can be two configurations
 * for one and the same Java field created by two different configuration
 * sources (e.g. via annotation and XML) but not two configurations for the same
 * field originating from one configuration source.
 * </p>
 * <p>
 * Implementations are strictly read-only.
 * </p>
 *
 * @author Gunnar Morling
 */
public interface ConstrainedElement extends Iterable<MetaConstraint<?>> {

	/**
	 * Returns the kind of this constrained element.
	 *
	 * @return The kind of this constrained element.
	 */
	ConstrainedElementKind getKind();

	/**
	 * Returns a set containing the constraints specified for this constrained
	 * element.
	 *
	 * @return A set with this constrained element's constraints. May be empty,
	 *         but never {@code null}.
	 */
	Set<MetaConstraint<?>> getConstraints();

	/**
	 * Returns the type argument constraints of this element, if any.
	 */
	Set<MetaConstraint<?>> getTypeArgumentConstraints();

	/**
	 * Returns the cascading metadata (e.g. {@link Valid} and {@link ConvertGroup}) for the element and the potential
	 * container elements.
	 */
	CascadingMetaDataBuilder getCascadingMetaDataBuilder();

	/**
	 * Whether this element is constrained or not. This is the case, if this
	 * element has at least one constraint or a cascaded validation shall be
	 * performed for it.
	 *
	 * @return {@code True}, if this element is constrained,
	 *         {@code false} otherwise.
	 */
	boolean isConstrained();

	/**
	 * Returns the configuration source contributing this constrained element.
	 */
	ConfigurationSource getSource();

	/**
	 * The kind of a {@link ConstrainedElement}. Can be used to determine an
	 * element's type when traversing over a collection of constrained elements.
	 *
	 * @author Gunnar Morling
	 */
	enum ConstrainedElementKind {

		TYPE,
		FIELD,
		CONSTRUCTOR,
		METHOD,
		PARAMETER,
		GETTER;

		public boolean isExecutable() {
			return this == CONSTRUCTOR || isMethod();
		}

		public boolean isMethod() {
			return this == METHOD || this == GETTER;
		}
	}
}
