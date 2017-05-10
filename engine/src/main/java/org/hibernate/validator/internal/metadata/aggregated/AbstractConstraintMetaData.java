/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.ElementKind;
import javax.validation.metadata.ContainerElementTypeDescriptor;
import javax.validation.metadata.GroupConversionDescriptor;

import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ContainerElementTypeDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.TypeArgumentConstraintLocation;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeVariables;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Base implementation for {@link ConstraintMetaData} with attributes common
 * to all type of meta data.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public abstract class AbstractConstraintMetaData implements ConstraintMetaData {
	private final String name;
	private final Type type;
	private final ElementKind constrainedMetaDataKind;
	@Immutable
	private final Set<MetaConstraint<?>> directConstraints;
	@Immutable
	private final Set<MetaConstraint<?>> containerElementsConstraints;
	@Immutable
	private final Set<MetaConstraint<?>>  allConstraints;
	private final boolean isCascading;
	private final boolean isConstrained;

	public AbstractConstraintMetaData(String name,
									  Type type,
									  Set<MetaConstraint<?>> directConstraints,
									  Set<MetaConstraint<?>> containerElementsConstraints,
									  ElementKind constrainedMetaDataKind,
									  boolean isCascading,
									  boolean isConstrained) {
		this.name = name;
		this.type = type;
		this.directConstraints = CollectionHelper.toImmutableSet( directConstraints );
		this.containerElementsConstraints = CollectionHelper.toImmutableSet( containerElementsConstraints );
		this.allConstraints = Stream.concat( directConstraints.stream(), containerElementsConstraints.stream() )
				.collect( Collectors.collectingAndThen( Collectors.toSet(), CollectionHelper::toImmutableSet ) );
		this.constrainedMetaDataKind = constrainedMetaDataKind;
		this.isCascading = isCascading;
		this.isConstrained = isConstrained;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public Iterator<MetaConstraint<?>> iterator() {
		return allConstraints.iterator();
	}

	public Set<MetaConstraint<?>> getAllConstraints() {
		return allConstraints;
	}

	public Set<MetaConstraint<?>> getDirectConstraints() {
		return directConstraints;
	}

	public Set<MetaConstraint<?>> getContainerElementsConstraints() {
		return containerElementsConstraints;
	}

	@Override
	public ElementKind getKind() {
		return constrainedMetaDataKind;
	}

	@Override
	public final boolean isCascading() {
		return isCascading;
	}

	@Override
	public boolean isConstrained() {
		return isConstrained;
	}

	@Override
	public String toString() {
		return "AbstractConstraintMetaData [name=" + name + ", type=" + type
				+ ", constrainedMetaDataKind=" + constrainedMetaDataKind
				+ ", directConstraints=" + directConstraints
				+ ", containerElementsConstraints=" + containerElementsConstraints
				+ ", isCascading=" + isCascading
				+ ", isConstrained=" + isConstrained + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		AbstractConstraintMetaData other = (AbstractConstraintMetaData) obj;
		if ( name == null ) {
			if ( other.name != null ) {
				return false;
			}
		}
		else if ( !name.equals( other.name ) ) {
			return false;
		}
		return true;
	}

	protected Set<ConstraintDescriptorImpl<?>> asDescriptors(Set<MetaConstraint<?>> constraints) {
		Set<ConstraintDescriptorImpl<?>> theValue = newHashSet();

		for ( MetaConstraint<?> oneConstraint : constraints ) {
			theValue.add( oneConstraint.getDescriptor() );
		}

		return theValue;
	}

	protected List<ContainerElementTypeDescriptor> asContainerElementTypeDescriptors(
			Set<MetaConstraint<?>> containerElementsConstraints, CascadingMetaData cascadingMetaData,
			boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {

		return asContainerElementTypeDescriptors(type, ContainerElementMetaConstraintTree.of( containerElementsConstraints ),
				cascadingMetaData, defaultGroupSequenceRedefined, defaultGroupSequence );
	}

	private List<ContainerElementTypeDescriptor> asContainerElementTypeDescriptors(Type type,
			ContainerElementMetaConstraintTree containerElementMetaConstraintTree, CascadingMetaData cascadingMetaData,
			boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		if ( type instanceof ParameterizedType) {
			List<ContainerElementTypeDescriptor> containerElementTypeDescriptors = new ArrayList<>();

			Type[] typeArguments = ( (ParameterizedType) type ).getActualTypeArguments();
			TypeVariable<?>[] typeParameters = ReflectionHelper.getClassFromType( type ).getTypeParameters();

			for ( int i = 0; i < typeArguments.length; i++) {
				Type typeArgument = typeArguments[i];
				TypeVariable<?> typeParameter = typeParameters[i];

				Set<MetaConstraint<?>> constraints = Collections.emptySet();
				ContainerElementMetaConstraintTree currentContainerElementMetaConstraintTree = null;

				if ( containerElementMetaConstraintTree != null && containerElementMetaConstraintTree.nodes.containsKey( typeParameter ) ) {
					currentContainerElementMetaConstraintTree = containerElementMetaConstraintTree.nodes.get( typeParameter );
					constraints = containerElementMetaConstraintTree.nodes.get( typeParameter ).constraints;
				}

				CascadingMetaData currentCascadingMetaData = null;
				boolean cascading = false;
				Set<GroupConversionDescriptor> groupConversionDescriptors = Collections.emptySet();

				if ( cascadingMetaData != null ) {
					for ( CascadingMetaData candidateCascadingMetaData : cascadingMetaData.getContainerElementTypesCascadingMetaData() ) {
						if ( candidateCascadingMetaData.getTypeParameter().equals( typeParameter ) ) {
							currentCascadingMetaData = candidateCascadingMetaData;
							cascading = currentCascadingMetaData.isCascading();
							groupConversionDescriptors = currentCascadingMetaData.getGroupConversionDescriptors();
						}
					}
				}

				containerElementTypeDescriptors.add( new ContainerElementTypeDescriptorImpl(
						typeArgument, TypeVariables.getTypeParameterIndex( typeParameter ),
						asDescriptors( constraints ),
						asContainerElementTypeDescriptors(typeArgument, currentContainerElementMetaConstraintTree, currentCascadingMetaData,
								defaultGroupSequenceRedefined, defaultGroupSequence),
						cascading,
						defaultGroupSequenceRedefined, defaultGroupSequence,
						groupConversionDescriptors ) );
			}

			return containerElementTypeDescriptors;
		}
		else if ( type instanceof GenericArrayType ) {
			// TODO Container element constraints are not supported for arrays in the Bean Validation specification. Let's ignore them for now.
			return Collections.emptyList();
		}
		else {
			return Collections.emptyList();
		}
	}

	private static class ContainerElementMetaConstraintTree {

		private Map<TypeVariable<?>, ContainerElementMetaConstraintTree> nodes = new HashMap<>();

		private Set<MetaConstraint<?>> constraints = new HashSet<>();

		private static ContainerElementMetaConstraintTree of(Set<MetaConstraint<?>> containerElementsConstraints) {
			ContainerElementMetaConstraintTree containerElementMetaConstraintTree = new ContainerElementMetaConstraintTree();

			for ( MetaConstraint<?> constraint : containerElementsConstraints) {
				ConstraintLocation currentLocation = constraint.getLocation();
				List<TypeVariable<?>> constraintPath = new ArrayList<>();
				while ( currentLocation instanceof TypeArgumentConstraintLocation ) {
					TypeArgumentConstraintLocation typeArgumentConstraintLocation = ((TypeArgumentConstraintLocation) currentLocation);
					constraintPath.add( typeArgumentConstraintLocation.getTypeParameter() );
					currentLocation = typeArgumentConstraintLocation.getDelegate();
				}
				Collections.reverse( constraintPath );

				containerElementMetaConstraintTree.addConstraint( constraintPath, constraint );
			}

			return containerElementMetaConstraintTree;
		}

		private void addConstraint(List<TypeVariable<?>> path, MetaConstraint<?> constraint) {
			ContainerElementMetaConstraintTree tree = this;
			for ( TypeVariable<?> typeParameter : path ) {
				tree = tree.nodes.computeIfAbsent( typeParameter, tp -> new ContainerElementMetaConstraintTree() );
			}
			tree.constraints.add( constraint );
		}
	}
}
