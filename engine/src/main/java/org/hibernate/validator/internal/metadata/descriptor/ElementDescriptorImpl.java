/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.descriptor;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jakarta.validation.groups.Default;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.ElementDescriptor;
import jakarta.validation.metadata.Scope;

import org.hibernate.validator.internal.engine.groups.Group;
import org.hibernate.validator.internal.engine.groups.ValidationOrder;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.metadata.core.ConstraintOrigin;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Describes a validated element (class, field or property).
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public abstract class ElementDescriptorImpl implements ElementDescriptor, Serializable {

	/**
	 * The type of the element
	 */
	private final Class<?> type;

	@Immutable
	private final Set<ConstraintDescriptorImpl<?>> constraintDescriptors;
	private final boolean defaultGroupSequenceRedefined;
	@Immutable
	private final List<Class<?>> defaultGroupSequence;

	public ElementDescriptorImpl(Type type,
								 Set<ConstraintDescriptorImpl<?>> constraintDescriptors,
								 boolean defaultGroupSequenceRedefined,
								 List<Class<?>> defaultGroupSequence) {
		this.type = (Class<?>) TypeHelper.getErasedType( type );
		this.constraintDescriptors = CollectionHelper.toImmutableSet( constraintDescriptors );
		this.defaultGroupSequenceRedefined = defaultGroupSequenceRedefined;
		this.defaultGroupSequence = CollectionHelper.toImmutableList( defaultGroupSequence );
	}

	@Override
	public final boolean hasConstraints() {
		return constraintDescriptors.size() != 0;
	}

	@Override
	public final Class<?> getElementClass() {
		return type;
	}

	@Override
	public final Set<ConstraintDescriptor<?>> getConstraintDescriptors() {
		return findConstraints().getConstraintDescriptors();
	}

	@Override
	public final ConstraintFinder findConstraints() {
		return new ConstraintFinderImpl();
	}

	private class ConstraintFinderImpl implements ConstraintFinder {
		private List<Class<?>> groups;
		private final EnumSet<ConstraintOrigin> definedInSet;
		private final EnumSet<ElementType> elementTypes;

		ConstraintFinderImpl() {
			elementTypes = EnumSet.of(
					ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR,
					ElementType.FIELD, ElementType.TYPE_USE ,
					//for a bean descriptor there will be no parameter constraints, so we can safely add this element type here
					ElementType.PARAMETER
			);

			definedInSet = EnumSet.allOf( ConstraintOrigin.class );
			groups = Collections.emptyList();
		}

		@Override
		public ConstraintFinder unorderedAndMatchingGroups(Class<?>... classes) {
			this.groups = newArrayList();
			for ( Class<?> clazz : classes ) {
				if ( Default.class.equals( clazz ) && defaultGroupSequenceRedefined ) {
					this.groups.addAll( defaultGroupSequence );
				}
				else {
					groups.add( clazz );
				}
			}
			return this;
		}

		@Override
		public ConstraintFinder lookingAt(Scope visibility) {
			if ( visibility.equals( Scope.LOCAL_ELEMENT ) ) {
				definedInSet.remove( ConstraintOrigin.DEFINED_IN_HIERARCHY );
			}
			return this;
		}

		@Override
		public ConstraintFinder declaredOn(ElementType... elementTypes) {
			this.elementTypes.clear();
			this.elementTypes.addAll( Arrays.asList( elementTypes ) );
			return this;
		}

		@Override
		public Set<ConstraintDescriptor<?>> getConstraintDescriptors() {
			Set<ConstraintDescriptor<?>> matchingDescriptors = new HashSet<ConstraintDescriptor<?>>();
			findMatchingDescriptors( matchingDescriptors );
			return CollectionHelper.toImmutableSet( matchingDescriptors );
		}

		@Override
		public boolean hasConstraints() {
			return getConstraintDescriptors().size() != 0;
		}

		private void addMatchingDescriptorsForGroup(Class<?> group, Set<ConstraintDescriptor<?>> matchingDescriptors) {
			for ( ConstraintDescriptorImpl<?> descriptor : constraintDescriptors ) {
				if ( definedInSet.contains( descriptor.getDefinedOn() ) && elementTypes.contains( descriptor.getConstraintLocationKind().getElementType() )
						&& descriptor.getGroups().contains( group ) ) {
					matchingDescriptors.add( descriptor );
				}
			}
		}

		private void findMatchingDescriptors(Set<ConstraintDescriptor<?>> matchingDescriptors) {
			if ( !groups.isEmpty() ) {
				ValidationOrder validationOrder = new ValidationOrderGenerator().getValidationOrder( groups );
				Iterator<Group> groupIterator = validationOrder.getGroupIterator();
				while ( groupIterator.hasNext() ) {
					Group g = groupIterator.next();
					addMatchingDescriptorsForGroup( g.getDefiningClass(), matchingDescriptors );
				}
			}
			else {
				for ( ConstraintDescriptorImpl<?> descriptor : constraintDescriptors ) {
					if ( definedInSet.contains( descriptor.getDefinedOn() ) && elementTypes.contains( descriptor.getConstraintLocationKind().getElementType() ) ) {
						matchingDescriptors.add( descriptor );
					}
				}
			}
		}
	}
}
