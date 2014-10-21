/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.annotation.ElementType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ElementKind;
import javax.validation.metadata.GroupConversionDescriptor;
import javax.validation.metadata.ReturnValueDescriptor;

import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ReturnValueDescriptorImpl;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.facets.Validatable;

/**
 * Represents the constraint related meta data of the return value of a method
 * or constructor.
 *
 * @author Gunnar Morling
 */
public class ReturnValueMetaData extends AbstractConstraintMetaData
		implements Validatable, Cascadable {

	private static final String RETURN_VALUE_NODE_NAME = null;

	private final List<Cascadable> cascadables;
	private final GroupConversionHelper groupConversionHelper;

	/**
	 * Type arguments constraints for this return value
	 */
	private final Set<MetaConstraint<?>> typeArgumentsConstraints;

	public ReturnValueMetaData(Type type,
							   Set<MetaConstraint<?>> constraints,
							   Set<MetaConstraint<?>> typeArgumentsConstraints,
							   boolean isCascading,
							   Map<Class<?>, Class<?>> groupConversions,
							   UnwrapMode unwrapMode) {
		super(
				RETURN_VALUE_NODE_NAME,
				type,
				constraints,
				ElementKind.RETURN_VALUE,
				isCascading,
				!constraints.isEmpty() || isCascading || !typeArgumentsConstraints.isEmpty(),
				unwrapMode
		);

		this.typeArgumentsConstraints = Collections.unmodifiableSet( typeArgumentsConstraints );
		this.cascadables = Collections.unmodifiableList( isCascading ? Arrays.asList( this ) : Collections.<Cascadable>emptyList() );
		this.groupConversionHelper = new GroupConversionHelper( groupConversions );
		this.groupConversionHelper.validateGroupConversions( isCascading(), this.toString() );
	}

	@Override
	public Iterable<Cascadable> getCascadables() {
		return cascadables;
	}

	@Override
	public Class<?> convertGroup(Class<?> originalGroup) {
		return groupConversionHelper.convertGroup( originalGroup );
	}

	@Override
	public Set<GroupConversionDescriptor> getGroupConversionDescriptors() {
		return groupConversionHelper.asDescriptors();
	}

	@Override
	public ElementType getElementType() {
		return ElementType.METHOD;
	}

	@Override
	public Set<MetaConstraint<?>> getTypeArgumentsConstraints() {
		return this.typeArgumentsConstraints;
	}

	@Override
	public ReturnValueDescriptor asDescriptor(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		return new ReturnValueDescriptorImpl(
				getType(),
				asDescriptors( getConstraints() ),
				isCascading(),
				defaultGroupSequenceRedefined,
				defaultGroupSequence,
				groupConversionHelper.asDescriptors()
		);
	}
}
