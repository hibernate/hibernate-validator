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
package org.hibernate.validator.engine.groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.validation.GroupDefinitionException;
import javax.validation.GroupSequence;
import javax.validation.ValidationException;

/**
 * Helper class used to resolve groups and sequences into a single chain of groups which can then be validated.
 *
 * @author Hardy Ferentschik
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class GroupChainGenerator {

	private final ConcurrentMap<Class<?>, List<Group>> resolvedSequences = new ConcurrentHashMap<Class<?>, List<Group>>();

	/**
	 * Generates a chain of groups to be validated given the specified validation groups.
	 *
	 * @param groups The groups specified at the validation call.
	 *
	 * @return an instance of {@code GroupChain} defining the order in which validation has to occur.
	 */
	public GroupChain getGroupChainFor(Collection<Class<?>> groups) {
		if ( groups == null || groups.size() == 0 ) {
			throw new IllegalArgumentException( "At least one groups has to be specified." );
		}

		for ( Class<?> clazz : groups ) {
			if ( !clazz.isInterface() ) {
				throw new ValidationException( "A group has to be an interface. " + clazz.getName() + " is not." );
			}
		}

		GroupChain chain = new GroupChain();
		for ( Class<?> clazz : groups ) {
			if ( isGroupSequence( clazz ) ) {
				insertSequence( clazz, chain );
			}
			else {
				Group group = new Group( clazz );
				chain.insertGroup( group );
				insertInheritedGroups( clazz, chain );
			}
		}

		return chain;
	}

	private boolean isGroupSequence(Class<?> clazz) {
		return clazz.getAnnotation( GroupSequence.class ) != null;
	}

	/**
	 * Recursively add inherited groups into the group chain.
	 *
	 * @param clazz The group interface
	 * @param chain The group chain we are currently building.
	 */
	private void insertInheritedGroups(Class<?> clazz, GroupChain chain) {
		for ( Class<?> inheritedGroup : clazz.getInterfaces() ) {
			Group group = new Group( inheritedGroup );
			chain.insertGroup( group );
			insertInheritedGroups( inheritedGroup, chain );
		}
	}

	private void insertSequence(Class<?> clazz, GroupChain chain) {
		List<Group> sequence = resolvedSequences.get( clazz );
		if ( sequence == null ) {
			sequence = resolveSequence( clazz, new ArrayList<Class<?>>() );
			// we expand the inherited groups only after we determined whether the sequence is expandable
			sequence = expandInheritedGroups( sequence );

			// cache already resolved sequences
			final List<Group> cachedResolvedSequence = resolvedSequences.putIfAbsent( clazz, sequence );
			if ( cachedResolvedSequence != null ) {
				sequence = cachedResolvedSequence;
			}
		}
		chain.insertSequence( sequence );
	}

	private List<Group> expandInheritedGroups(List<Group> sequence) {
		List<Group> expandedGroup = new ArrayList<Group>();
		for ( Group group : sequence ) {
			expandedGroup.add( group );
			addInheritedGroups( group, expandedGroup );
		}
		return expandedGroup;
	}

	private void addInheritedGroups(Group group, List<Group> expandedGroups) {
		for ( Class<?> inheritedGroup : group.getGroup().getInterfaces() ) {
			if ( isGroupSequence( inheritedGroup ) ) {
				throw new GroupDefinitionException(
						"Sequence definitions are not allowed as composing parts of a sequence."
				);
			}
			Group g = new Group( inheritedGroup, group.getSequence() );
			expandedGroups.add( g );
			addInheritedGroups( g, expandedGroups );
		}
	}

	private List<Group> resolveSequence(Class<?> group, List<Class<?>> processedSequences) {
		if ( processedSequences.contains( group ) ) {
			throw new GroupDefinitionException( "Cyclic dependency in groups definition" );
		}
		else {
			processedSequences.add( group );
		}
		List<Group> resolvedGroupSequence = new ArrayList<Group>();
		GroupSequence sequenceAnnotation = group.getAnnotation( GroupSequence.class );
		Class<?>[] sequenceArray = sequenceAnnotation.value();
		for ( Class<?> clazz : sequenceArray ) {
			if ( isGroupSequence( clazz ) ) {
				List<Group> tmpSequence = resolveSequence( clazz, processedSequences );
				addGroups( resolvedGroupSequence, tmpSequence );
			}
			else {
				List<Group> list = new ArrayList<Group>();
				list.add( new Group( clazz, group ) );
				addGroups( resolvedGroupSequence, list );
			}
		}
		return resolvedGroupSequence;
	}

	private void addGroups(List<Group> resolvedGroupSequence, List<Group> groups) {
		for ( Group tmpGroup : groups ) {
			if ( resolvedGroupSequence.contains( tmpGroup ) && resolvedGroupSequence.indexOf( tmpGroup ) < resolvedGroupSequence
					.size() - 1 ) {
				throw new GroupDefinitionException( "Unable to expand group sequence." );
			}
			resolvedGroupSequence.add( tmpGroup );
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "GroupChainGenerator" );
		sb.append( "{resolvedSequences=" ).append( resolvedSequences );
		sb.append( '}' );
		return sb.toString();
	}
}
