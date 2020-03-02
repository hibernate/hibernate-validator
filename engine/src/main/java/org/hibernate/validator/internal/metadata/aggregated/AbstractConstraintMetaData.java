/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.validation.metadata.ContainerElementTypeDescriptor;
import jakarta.validation.metadata.GroupConversionDescriptor;

import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ContainerElementTypeDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.TypeArgumentConstraintLocation;
import org.hibernate.validator.internal.util.CollectionHelper;
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
									  boolean isCascading,
									  boolean isConstrained) {
		this.name = name;
		this.type = type;
		this.directConstraints = CollectionHelper.toImmutableSet( directConstraints );
		this.containerElementsConstraints = CollectionHelper.toImmutableSet( containerElementsConstraints );
		this.allConstraints = Stream.concat( directConstraints.stream(), containerElementsConstraints.stream() )
				.collect( Collectors.collectingAndThen( Collectors.toSet(), CollectionHelper::toImmutableSet ) );
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

	protected Set<ContainerElementTypeDescriptor> asContainerElementTypeDescriptors(
			Set<MetaConstraint<?>> containerElementsConstraints, CascadingMetaData cascadingMetaData,
			boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {

		return asContainerElementTypeDescriptors( type,
				ContainerElementMetaDataTree.of( cascadingMetaData, containerElementsConstraints ),
				defaultGroupSequenceRedefined, defaultGroupSequence );
	}

	private Set<ContainerElementTypeDescriptor> asContainerElementTypeDescriptors(Type type, ContainerElementMetaDataTree containerElementMetaDataTree,
			boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		Set<ContainerElementTypeDescriptor> containerElementTypeDescriptors = new HashSet<>();

		for ( Entry<TypeVariable<?>, ContainerElementMetaDataTree> entry : containerElementMetaDataTree.nodes.entrySet() ) {
			TypeVariable<?> childTypeParameter = entry.getKey();
			ContainerElementMetaDataTree childContainerElementMetaDataTree = entry.getValue();

			Set<ContainerElementTypeDescriptor> childrenDescriptors =
					asContainerElementTypeDescriptors( childContainerElementMetaDataTree.elementType, childContainerElementMetaDataTree,
					defaultGroupSequenceRedefined, defaultGroupSequence );

			containerElementTypeDescriptors.add( new ContainerElementTypeDescriptorImpl(
					childContainerElementMetaDataTree.elementType,
					childContainerElementMetaDataTree.containerClass, TypeVariables.getTypeParameterIndex( childTypeParameter ),
					asDescriptors( childContainerElementMetaDataTree.constraints ),
					childrenDescriptors,
					childContainerElementMetaDataTree.cascading,
					defaultGroupSequenceRedefined, defaultGroupSequence,
					childContainerElementMetaDataTree.groupConversionDescriptors ) );
		}

		return containerElementTypeDescriptors;
	}

	/**
	 * This data structure is used to join the cascading metadata information with the constraint violations. It is a
	 * temporary data structure and it should be kept that way.
	 * <p>
	 * We might consider in the future having a common tree structure for the cascading metadata and the constraint
	 * violations that would be built earlier and shared. This class shouldn't be taken as a model as this data
	 * structure should be made immutable.
	 */
	private static class ContainerElementMetaDataTree {

		private final Map<TypeVariable<?>, ContainerElementMetaDataTree> nodes = new HashMap<>();

		private Type elementType = null;

		private Class<?> containerClass;

		private final Set<MetaConstraint<?>> constraints = new HashSet<>();

		private boolean cascading = false;

		private Set<GroupConversionDescriptor> groupConversionDescriptors = new HashSet<>();

		private static ContainerElementMetaDataTree of(CascadingMetaData cascadingMetaData, Set<MetaConstraint<?>> containerElementsConstraints) {
			ContainerElementMetaDataTree containerElementMetaConstraintTree = new ContainerElementMetaDataTree();

			for ( MetaConstraint<?> constraint : containerElementsConstraints ) {
				ConstraintLocation currentLocation = constraint.getLocation();
				List<TypeVariable<?>> constraintPath = new ArrayList<>();
				while ( currentLocation instanceof TypeArgumentConstraintLocation ) {
					TypeArgumentConstraintLocation typeArgumentConstraintLocation = ( (TypeArgumentConstraintLocation) currentLocation );
					constraintPath.add( typeArgumentConstraintLocation.getTypeParameter() );
					currentLocation = typeArgumentConstraintLocation.getDelegate();
				}
				Collections.reverse( constraintPath );

				containerElementMetaConstraintTree.addConstraint( constraintPath, constraint );
			}

			if ( cascadingMetaData != null && cascadingMetaData.isContainer() && cascadingMetaData.isMarkedForCascadingOnAnnotatedObjectOrContainerElements() ) {
				containerElementMetaConstraintTree.addCascadingMetaData( new ArrayList<>(), cascadingMetaData.as( ContainerCascadingMetaData.class ) );
			}

			return containerElementMetaConstraintTree;
		}

		private void addConstraint(List<TypeVariable<?>> path, MetaConstraint<?> constraint) {
			ContainerElementMetaDataTree tree = this;
			for ( TypeVariable<?> typeArgument : path ) {
				tree = tree.nodes.computeIfAbsent( typeArgument, ta -> new ContainerElementMetaDataTree() );
			}

			TypeArgumentConstraintLocation constraintLocation = (TypeArgumentConstraintLocation) constraint.getLocation();

			tree.elementType = constraintLocation.getTypeForValidatorResolution();
			tree.containerClass = ( (TypeArgumentConstraintLocation) constraint.getLocation() ).getContainerClass();
			tree.constraints.add( constraint );
		}

		private void addCascadingMetaData(List<TypeVariable<?>> path, ContainerCascadingMetaData cascadingMetaData) {
			for ( ContainerCascadingMetaData nestedCascadingMetaData : cascadingMetaData.getContainerElementTypesCascadingMetaData() ) {
				List<TypeVariable<?>> nestedPath = new ArrayList<>( path );
				nestedPath.add( nestedCascadingMetaData.getTypeParameter() );

				ContainerElementMetaDataTree tree = this;
				for ( TypeVariable<?> typeArgument : nestedPath ) {
					tree = tree.nodes.computeIfAbsent( typeArgument, ta -> new ContainerElementMetaDataTree() );
				}

				tree.elementType = TypeVariables.getContainerElementType( nestedCascadingMetaData.getEnclosingType(), nestedCascadingMetaData.getTypeParameter() );
				tree.containerClass = nestedCascadingMetaData.getDeclaredContainerClass();
				tree.cascading = nestedCascadingMetaData.isCascading();
				tree.groupConversionDescriptors = nestedCascadingMetaData.getGroupConversionDescriptors();

				if ( nestedCascadingMetaData.isMarkedForCascadingOnAnnotatedObjectOrContainerElements() ) {
					addCascadingMetaData( nestedPath, nestedCascadingMetaData );
				}
			}
		}
	}
}
