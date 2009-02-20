// $Id:$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine.groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.GroupSequence;
import javax.validation.ValidationException;

/**
 * Used to determine the execution order.
 *
 * @author Hardy Ferentschik
 */
public class GroupChainGenerator {

	private final Map<Class<?>, List<Group>> resolvedSequences = new HashMap<Class<?>, List<Group>>();

	public GroupChain getGroupChainFor(Collection<Class<?>> groups) {
		if ( groups == null || groups.size() == 0 ) {
			throw new IllegalArgumentException( "At least one groups has to be specified." );
		}

		for ( Class<?> clazz : groups ) {
			if ( !clazz.isInterface() ) {
				throw new ValidationException( "A groups has to be an interface. " + clazz.getName() + " is not." );
			}
		}

		GroupChain chain = new GroupChain();
		for ( Class<?> clazz : groups ) {
			if ( clazz.getAnnotation( GroupSequence.class ) == null ) {
				Group group = new Group( clazz );
				chain.insertGroup( group );
				insertInheritedGroups( clazz, chain );
			}
			else {
				insertSequence( clazz, chain );
			}
		}

		return chain;
	}

	private void insertInheritedGroups(Class<?> clazz, GroupChain chain) {
		for ( Class<?> extendedInterface : clazz.getInterfaces() ) {
			Group group = new Group( extendedInterface );
			chain.insertGroup( group );
			insertInheritedGroups(extendedInterface, chain);
		}
	}

	private void insertSequence(Class<?> clazz, GroupChain chain) {
		List<Group> sequence;
		if ( resolvedSequences.containsKey( clazz ) ) {
			sequence = resolvedSequences.get( clazz );
		}
		else {
			sequence = resolveSequence( clazz, new ArrayList<Class<?>>() );
		}
		chain.insertSequence( sequence );
	}

	private List<Group> resolveSequence(Class<?> group, List<Class<?>> processedSequences) {
		if ( processedSequences.contains( group ) ) {
			throw new ValidationException( "Cyclic dependecy in groups definition" );
		}
		else {
			processedSequences.add( group );
		}
		List<Group> resolvedGroupSequence = new ArrayList<Group>();
		GroupSequence sequenceAnnotation = group.getAnnotation( GroupSequence.class );
		Class<?>[] sequenceArray = sequenceAnnotation.value();
		for ( Class clazz : sequenceArray ) {
			if ( clazz.getAnnotation( GroupSequence.class ) == null ) {
				resolvedGroupSequence.add( new Group( clazz, group ) );
			}
			else {
				List<Group> tmpSequence = resolveSequence( clazz, processedSequences );
				resolvedGroupSequence.addAll( tmpSequence );
			}
		}
		resolvedSequences.put( group, resolvedGroupSequence );
		return resolvedGroupSequence;
	}
}
