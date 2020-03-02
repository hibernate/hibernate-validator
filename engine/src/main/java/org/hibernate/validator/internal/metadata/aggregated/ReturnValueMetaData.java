/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.validation.ElementKind;
import jakarta.validation.metadata.ReturnValueDescriptor;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ReturnValueDescriptorImpl;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Represents the constraint related meta data of the return value of a method
 * or constructor.
 *
 * @author Gunnar Morling
 */
public class ReturnValueMetaData extends AbstractConstraintMetaData
		implements Validatable, Cascadable {

	private static final String RETURN_VALUE_NODE_NAME = null;

	@Immutable
	private final List<Cascadable> cascadables;

	private final CascadingMetaData cascadingMetaData;

	public ReturnValueMetaData(Type type,
							   Set<MetaConstraint<?>> constraints,
							   Set<MetaConstraint<?>> containerElementsConstraints,
							   CascadingMetaData cascadingMetaData) {
		super(
				RETURN_VALUE_NODE_NAME,
				type,
				constraints,
				containerElementsConstraints,
				cascadingMetaData.isMarkedForCascadingOnAnnotatedObjectOrContainerElements(),
				!constraints.isEmpty() || containerElementsConstraints.isEmpty() || cascadingMetaData.isMarkedForCascadingOnAnnotatedObjectOrContainerElements()
		);


		this.cascadables = isCascading() ? Collections.singletonList( this ) : Collections.emptyList();
		this.cascadingMetaData = cascadingMetaData;
	}

	@Override
	public Iterable<Cascadable> getCascadables() {
		return cascadables;
	}

	@Override
	public boolean hasCascadables() {
		return !cascadables.isEmpty();
	}

	@Override
	public ConstraintLocationKind getConstraintLocationKind() {
		return ConstraintLocationKind.METHOD;
	}

	@Override
	public ReturnValueDescriptor asDescriptor(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		return new ReturnValueDescriptorImpl(
				getType(),
				asDescriptors( getDirectConstraints() ),
				asContainerElementTypeDescriptors( getContainerElementsConstraints(), cascadingMetaData, defaultGroupSequenceRedefined, defaultGroupSequence ),
				cascadingMetaData.isCascading(),
				defaultGroupSequenceRedefined,
				defaultGroupSequence,
				cascadingMetaData.getGroupConversionDescriptors()
		);
	}

	@Override
	public Object getValue(Object parent) {
		return parent;
	}

	@Override
	public Type getCascadableType() {
		return getType();
	}

	@Override
	public void appendTo(PathImpl path) {
		path.addReturnValueNode();
	}

	@Override
	public CascadingMetaData getCascadingMetaData() {
		return cascadingMetaData;
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.RETURN_VALUE;
	}
}
