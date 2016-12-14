/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.annotation.ElementType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ElementKind;
import javax.validation.metadata.GroupConversionDescriptor;
import javax.validation.metadata.ReturnValueDescriptor;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ReturnValueDescriptorImpl;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

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
	private final SetMultimap<TypeVariable<?>, MetaConstraint<?>> typeArgumentsConstraints;

	private final List<TypeVariable<?>> cascadingTypeParameters;

	public ReturnValueMetaData(Type type,
							   Set<MetaConstraint<?>> constraints,
							   SetMultimap<TypeVariable<?>, MetaConstraint<?>> typeArgumentsConstraints,
							   List<TypeVariable<?>> cascadingTypeParameters,
							   Map<Class<?>, Class<?>> groupConversions,
							   UnwrapMode unwrapMode) {
		super(
				RETURN_VALUE_NODE_NAME,
				type,
				constraints,
				ElementKind.RETURN_VALUE,
				!cascadingTypeParameters.isEmpty(),
				!constraints.isEmpty() || !cascadingTypeParameters.isEmpty() || !typeArgumentsConstraints.isEmpty(),
				unwrapMode
		);

		this.typeArgumentsConstraints = ImmutableSetMultimap.copyOf( typeArgumentsConstraints );
		this.cascadingTypeParameters = Collections.unmodifiableList( cascadingTypeParameters );
		this.cascadables = Collections.unmodifiableList( isCascading() ? Arrays.<Cascadable>asList( this ) : Collections.<Cascadable>emptyList() );
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
	public SetMultimap<TypeVariable<?>, MetaConstraint<?>> getTypeArgumentsConstraints() {
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
	public UnwrapMode getUnwrapMode(ConstraintLocation location) {
		return unwrapMode();
	}

	@Override
	public List<TypeVariable<?>> getCascadingTypeParameters() {
		return cascadingTypeParameters;
	}
}
