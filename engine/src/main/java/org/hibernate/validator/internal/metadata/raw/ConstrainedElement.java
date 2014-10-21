/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw;

import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;

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
	 * The kind of a {@link ConstrainedElement}. Can be used to determine an
	 * element's type when traversing over a collection of constrained elements.
	 *
	 * @author Gunnar Morling
	 */
	public enum ConstrainedElementKind {
		TYPE, FIELD, CONSTRUCTOR, METHOD, PARAMETER, TYPE_USE
	}

	/**
	 * Returns the kind of this constrained element.
	 *
	 * @return The kind of this constrained element.
	 */
	ConstrainedElementKind getKind();

	/**
	 * Returns the location of this constrained element.
	 *
	 * @return The location of this constrained element.
	 */
	ConstraintLocation getLocation();

	/**
	 * Returns a set containing the constraints specified for this constrained
	 * element.
	 *
	 * @return A set with this constrained element's constraints. May be empty,
	 *         but never {@code null}.
	 */
	Set<MetaConstraint<?>> getConstraints();

	/**
	 * Returns a map with the group conversions for this constrained element, as
	 * e.g. given using the {@code @ConvertGroup} annotation.
	 *
	 * @return A map with this constrained element's group conversions. May be
	 *         empty, but never {@code null}.
	 */
	Map<Class<?>, Class<?>> getGroupConversions();

	/**
	 * Whether cascading validation for the represented element shall be
	 * performed or not.
	 *
	 * @return <code>True</code>, if cascading validation for the represented
	 *         element shall be performed, <code>false</code> otherwise.
	 */
	boolean isCascading();

	/**
	 * Whether this element is constrained or not. This is the case, if this
	 * element has at least one constraint or a cascaded validation shall be
	 * performed for it.
	 *
	 * @return <code>True</code>, if this element is constrained,
	 *         <code>false</code> otherwise.
	 */
	boolean isConstrained();

	/**
	 * Determines how to handle unwrapping of validated values. Per default unwrapping occurs automatically
	 * based on the validated type and registered {@code ValidatedValueUnwrapper}s. It can also be explcitly configured
	 * using {@code UnwrapValidatedValue}.
	 *
	 * @return Returns the unwrapping mode for this constrained element.
	 * @see org.hibernate.validator.valuehandling.UnwrapValidatedValue
	 * @see org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper
	 */
	UnwrapMode unwrapMode();
}
