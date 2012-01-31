/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.metadata.aggregated;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.hibernate.validator.metadata.core.ConstraintHelper;
import org.hibernate.validator.metadata.core.ConstraintOrigin;
import org.hibernate.validator.metadata.core.MetaConstraint;
import org.hibernate.validator.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.metadata.raw.ConstrainedElement;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Builds {@link ConstraintMetaData} instances for the
 * {@link ConstrainedElement} objects representing one method or property in a
 * type's inheritance hierarchy.
 *
 * @author Gunnar Morling
 */
public abstract class MetaDataBuilder {

	protected final ConstraintHelper constraintHelper;

	protected MetaDataBuilder(ConstraintHelper constraintHelper) {
		this.constraintHelper = constraintHelper;
	}

	/**
	 * Whether this builder allows to add the given element or not. This is the
	 * case if the specified element relates to the same property or method with
	 * which this builder was instantiated.
	 *
	 * @param constrainedElement The element to check.
	 *
	 * @return <code>true</code> if the given element can be added to this
	 *         builder, <code>false</code> otherwise.
	 */
	public abstract boolean accepts(ConstrainedElement constrainedElement);

	/**
	 * Adds the given element to this builder. It must be checked with
	 * {@link #accepts(ConstrainedElement)} before, whether this is allowed or
	 * not.
	 *
	 * @param constrainedElement The element to add.
	 */
	public abstract void add(ConstrainedElement constrainedElement);

	/**
	 * Creates a new, read-only {@link ConstraintMetaData} object with all
	 * constraint information related to the method or property represented by
	 * this builder.
	 *
	 * @return A {@link ConstraintMetaData} object.
	 */
	public abstract ConstraintMetaData build();

	/**
	 * Adapts the given constraints to the given bean type. In case a constraint
	 * is defined locally at the bean class the original constraint will be
	 * returned without any modifications. If a constraint is defined in the
	 * hierarchy (interface or super class) a new constraint will be returned
	 * with an origin of {@link org.hibernate.validator.metadata.core.ConstraintOrigin#DEFINED_IN_HIERARCHY}. If a
	 * constraint is defined on an interface, the interface type will
	 * additionally be part of the constraint's groups (implicit grouping).
	 *
	 * @param beanClass The bean type to which the constraint shall be adapted.
	 * @param constraints The constraints that shall be adapted. The constraints themselves
	 * will not be altered.
	 *
	 * @return A constraint adapted to the given bean type.
	 */
	protected Set<MetaConstraint<?>> adaptOriginsAndImplicitGroups(Class<?> beanClass,
																   Set<MetaConstraint<?>> constraints) {
		Set<MetaConstraint<?>> adaptedConstraints = newHashSet();

		for ( MetaConstraint<?> oneConstraint : constraints ) {
			adaptedConstraints.add(
					adaptOriginAndImplicitGroup(
							beanClass, oneConstraint
					)
			);
		}
		return adaptedConstraints;
	}

	private <A extends Annotation> MetaConstraint<A> adaptOriginAndImplicitGroup(
			Class<?> beanClass, MetaConstraint<A> constraint) {

		ConstraintOrigin definedIn = definedIn( beanClass, constraint.getLocation().getBeanClass() );

		if ( definedIn == ConstraintOrigin.DEFINED_LOCALLY ) {
			return constraint;
		}

		Class<?> constraintClass = constraint.getLocation().getBeanClass();

		ConstraintDescriptorImpl<A> descriptor = new ConstraintDescriptorImpl<A>(
				constraint.getDescriptor().getAnnotation(),
				constraintHelper,
				constraintClass.isInterface() ? constraintClass : null,
				constraint.getElementType(),
				definedIn
		);

		return new MetaConstraint<A>(
				descriptor,
				constraint.getLocation()
		);
	}

	/**
	 * @param rootClass The root class. That is the class for which we currently
	 * create a {@code BeanMetaData}
	 * @param hierarchyClass The class on which the current constraint is defined on
	 *
	 * @return Returns {@code ConstraintOrigin.DEFINED_LOCALLY} if the
	 *         constraint was defined on the root bean,
	 *         {@code ConstraintOrigin.DEFINED_IN_HIERARCHY} otherwise.
	 */
	private ConstraintOrigin definedIn(Class<?> rootClass, Class<?> hierarchyClass) {
		if ( hierarchyClass.equals( rootClass ) ) {
			return ConstraintOrigin.DEFINED_LOCALLY;
		}
		else {
			return ConstraintOrigin.DEFINED_IN_HIERARCHY;
		}
	}
}
