/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintDeclarationException;
import javax.validation.metadata.BeanDescriptor;

import org.hibernate.validator.internal.engine.groups.Sequence;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;

/**
 * Interface defining the meta data about the constraints defined in a given bean.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public interface BeanMetaData<T> extends Validatable {

	/**
	 * @return the class of the bean.
	 */
	Class<T> getBeanClass();

	/**
	 * Returns {@code true} if the bean class for this bean meta data has any constraints at all, {@code false} otherwise.
	 *
	 * @return {@code true} if the bean class for this bean meta data has any constraints at all, {@code false} otherwise.
	 */
	boolean hasConstraints();

	/**
	 * @return an instance of {@code ElementDescriptor} describing the bean this meta data applies for.
	 */
	BeanDescriptor getBeanDescriptor();

	/**
	 * Returns constraint-related meta data for the given property of this bean.
	 *
	 * @param propertyName The property name.
	 *
	 * @return Constraint-related meta data or {@code null} if no property with the given name exists.
	 */
	PropertyMetaData getMetaDataFor(String propertyName);

	/**
	 * Get the composition of the default group sequence.
	 * <p>
	 * If the bean state is given in parameter and the bean metadata has a default group sequence provider then the
	 * dynamic default group sequence composition is returned. In the other cases the default group sequence
	 * redefinition specified by BV is used.
	 * </p>
	 *
	 * @param beanState the bean state.
	 *
	 * @return a list of classes representing the default group sequence.
	 */
	List<Class<?>> getDefaultGroupSequence(T beanState);

	/**
	 * Returns a {@link org.hibernate.validator.internal.engine.groups.ValidationOrder} representing the default
	 * validation group sequence as configured through {@code @GroupSequence}/{@code @DefaultGroupSequenceProvider}. If
	 * this bean type does not re-declare the default validation group sequence {@link org.hibernate.validator.internal.engine.groups.ValidationOrder#DEFAULT_SEQUENCE}
	 * will be returned.
	 */
	// TODO: Ideally, a plain Sequence object should be returned here; I am using ValidationOrder for now to keep
	// backporting to 4.3 manageable. The expansion of sequences/groups should be moved from ValidationOrder into
	// Sequence and Group, respectively.
	Iterator<Sequence> getDefaultValidationSequence(T beanState);

	/**
	 * @return {@code true} if the entity redefines the default group sequence, {@code false} otherwise.
	 */
	boolean defaultGroupSequenceIsRedefined();

	/**
	 * @return A set of {@code MetaConstraint} instances encapsulating the information of all the constraints
	 *         defined on the bean. This collection includes constraints from super classes as well
	 */
	Set<MetaConstraint<?>> getMetaConstraints();

	/**
	 * @return A set of {@code MetaConstraint} instances encapsulating the information of all the constraints
	 *         defined on the bean directly (including constraints defined on implemented interfaces). It does not
	 *         contain constraints from super classes or interfaces implemented by super classes
	 */
	Set<MetaConstraint<?>> getDirectMetaConstraints();

	/**
	 * Returns the constraint-related meta data for the given method of the
	 * class represented by this bean meta data.
	 *
	 * @param method The method of interest.
	 *
	 * @return An aggregated view on the constraint related meta data from the
	 *         given method all the methods from super-types which it overrides
	 *         or implements.
	 *
	 * @throws ConstraintDeclarationException In case any of the rules for the declaration of method
	 * constraints described in the Bean Validation specification is violated.
	 */
	ExecutableMetaData getMetaDataFor(ExecutableElement method) throws ConstraintDeclarationException;

	/**
	 * @return Returns a list of classes representing the class hierarchy for the entity. The list start with the
	 *         element itself and goes up the hierarchy chain. Interfaces are not included.
	 */
	List<Class<? super T>> getClassHierarchy();
}
