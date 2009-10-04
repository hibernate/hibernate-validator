// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.metadata;

import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ElementDescriptor;
import javax.validation.metadata.Scope;

import org.hibernate.validator.engine.groups.Group;
import org.hibernate.validator.engine.groups.GroupChain;
import org.hibernate.validator.engine.groups.GroupChainGenerator;

/**
 * Describe a validated element (class, field or property).
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class ElementDescriptorImpl implements ElementDescriptor {
	private final Class<?> type;
	private final Set<ConstraintDescriptorImpl<?>> constraintDescriptors = new HashSet<ConstraintDescriptorImpl<?>>();

	public ElementDescriptorImpl(Class<?> type) {
		this.type = type;
	}

	public void addConstraintDescriptor(ConstraintDescriptorImpl constraintDescriptor) {
		constraintDescriptors.add( constraintDescriptor );
	}

	public boolean hasConstraints() {
		return constraintDescriptors.size() != 0;
	}

	public Class<?> getElementClass() {
		return type;
	}

	public Set<ConstraintDescriptor<?>> getConstraintDescriptors() {
		return findConstraints().getConstraintDescriptors();
	}

	public ConstraintFinder findConstraints() {
		return new ConstraintFinderImpl();
	}

	private class ConstraintFinderImpl implements ConstraintFinder {
		private List<Class<?>> groups;
		private Set<ConstraintOrigin> definedInSet;
		private Set<ElementType> elementTypes;

		ConstraintFinderImpl() {
			elementTypes = new HashSet<ElementType>();
			elementTypes.add( ElementType.TYPE );
			elementTypes.add( ElementType.METHOD );
			elementTypes.add( ElementType.FIELD );
			definedInSet = new HashSet<ConstraintOrigin>();
			definedInSet.add( ConstraintOrigin.DEFINED_LOCALLY );
			definedInSet.add( ConstraintOrigin.DEFINED_IN_HIERARCHY );
			groups = Collections.emptyList();
		}

		public ConstraintFinder unorderedAndMatchingGroups(Class<?>... classes) {
			this.groups = Arrays.asList( classes );
			return this;
		}

		public ConstraintFinder lookingAt(Scope visibility) {
			if ( visibility.equals( Scope.LOCAL_ELEMENT ) ) {
				definedInSet.remove( ConstraintOrigin.DEFINED_IN_HIERARCHY );
			}
			return this;
		}

		public ConstraintFinder declaredOn(ElementType... elementTypes) {
			this.elementTypes.clear();
			this.elementTypes.addAll( Arrays.asList( elementTypes ) );
			return this;
		}

		public Set<ConstraintDescriptor<?>> getConstraintDescriptors() {

			Set<ConstraintDescriptor<?>> matchingDescriptors = new HashSet<ConstraintDescriptor<?>>();
			findMatchingDescriptors( matchingDescriptors );
			return Collections.unmodifiableSet( matchingDescriptors );
		}

		private void findMatchingDescriptors(Set<ConstraintDescriptor<?>> matchingDescriptors) {
			if ( !groups.isEmpty() ) {
				GroupChain groupChain = new GroupChainGenerator().getGroupChainFor( groups );
				Iterator<Group> groupIterator = groupChain.getGroupIterator();
				while ( groupIterator.hasNext() ) {
					Group g = groupIterator.next();
					addMatchingDescriptorsForGroup( g.getGroup(), matchingDescriptors );
				}
			}
			else {
				for ( ConstraintDescriptorImpl<?> descriptor : constraintDescriptors ) {
					if ( definedInSet.contains( descriptor.getDefinedOn() ) && elementTypes.contains( descriptor.getElementType() ) ) {
						matchingDescriptors.add( descriptor );
					}
				}
			}
		}

		public boolean hasConstraints() {
			return getConstraintDescriptors().size() != 0;
		}

		private void addMatchingDescriptorsForGroup(Class<?> group, Set<ConstraintDescriptor<?>> matchingDescriptors) {
			for ( ConstraintDescriptorImpl<?> descriptor : constraintDescriptors ) {
				if ( definedInSet.contains( descriptor.getDefinedOn() ) && elementTypes.contains( descriptor.getElementType() )
						&& descriptor.getGroups().contains( group ) ) {
					matchingDescriptors.add( descriptor );
				}
			}
		}
	}
}
