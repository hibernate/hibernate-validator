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
package org.hibernate.validator.test.internal.engine.groups.groupchaingenerator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.validation.GroupDefinitionException;
import javax.validation.GroupSequence;
import javax.validation.ValidationException;
import javax.validation.groups.Default;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.engine.groups.Group;
import org.hibernate.validator.internal.engine.groups.GroupOrder;
import org.hibernate.validator.internal.engine.groups.GroupOrderGenerator;
import org.hibernate.validator.internal.engine.groups.Sequence;
import org.hibernate.validator.test.internal.engine.groups.groupchain.First;
import org.hibernate.validator.test.internal.engine.groups.groupchain.Last;
import org.hibernate.validator.test.internal.engine.groups.groupchain.Second;

import static org.testng.Assert.assertEquals;

/**
 * @author Hardy Ferentschik
 */
public class GroupOrderGenerationTest {

	GroupOrderGenerator generator;

	@BeforeTest
	public void init() {
		generator = new GroupOrderGenerator();
	}

	@Test(expectedExceptions = ValidationException.class)
	public void testGroupChainForNonInterface() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( String.class );
		generator.getGroupOrderFor( groups );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testGroupChainForNull() {
		generator.getGroupOrderFor( null );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testGroupChainForEmptySet() {
		generator.getGroupOrderFor( new HashSet<Class<?>>() );
	}

	@Test(expectedExceptions = ValidationException.class)
	public void testCyclicGroupSequences() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( CyclicGroupSequence1.class );
		generator.getGroupOrderFor( groups );
	}

	@Test(expectedExceptions = ValidationException.class)
	public void testCyclicGroupSequence() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( CyclicGroupSequence.class );
		generator.getGroupOrderFor( groups );
	}

	@Test
	public void testGroupDuplicates() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( First.class );
		groups.add( Second.class );
		groups.add( Last.class );
		GroupOrder chain = generator.getGroupOrderFor( groups );
		int count = countGroups( chain );
		assertEquals( count, 3, "Wrong number of groups" );

		groups.clear();
		groups.add( First.class );
		groups.add( First.class );
		chain = generator.getGroupOrderFor( groups );
		count = countGroups( chain );
		assertEquals( count, 1, "Wrong number of groups" );

		groups.clear();
		groups.add( First.class );
		groups.add( Last.class );
		groups.add( First.class );
		chain = generator.getGroupOrderFor( groups );
		count = countGroups( chain );
		assertEquals( count, 2, "Wrong number of groups" );
	}

	@Test(expectedExceptions = GroupDefinitionException.class)
	public void testGroupDefiningSequencePartOfGroupComposingSequence() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( Sequence1.class );
		generator.getGroupOrderFor( groups );
	}

	@Test(expectedExceptions = GroupDefinitionException.class)
	public void testUnexpandableSequence() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( Sequence3.class );
		generator.getGroupOrderFor( groups );
	}

	@Test
	public void testExpandableSequenceWithInheritance() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( Sequence4.class );
		generator.getGroupOrderFor( groups );
	}

	@Test
	public void testSequenceResolution() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( Address.Complete.class );
		GroupOrder chain = generator.getGroupOrderFor( groups );
		Iterator<Sequence> sequences = chain.getSequenceIterator();
		List<Group> sequence = sequences.next().getComposingGroups();

		assertEquals( sequence.get( 0 ).getGroup(), Default.class, "Wrong group" );
		assertEquals( sequence.get( 1 ).getGroup(), Address.HighLevelCoherence.class, "Wrong group" );
	}

	private int countGroups(GroupOrder chain) {
		Iterator<Group> groupIterator = chain.getGroupIterator();
		int count = 0;
		while ( groupIterator.hasNext() ) {
			groupIterator.next();
			count++;
		}
		return count;
	}


	interface GroupA extends Default {
	}

	interface GroupB {
	}

	interface GroupC extends Sequence2 {
	}

	@GroupSequence({ GroupA.class, GroupC.class })
	interface Sequence1 {
	}

	@GroupSequence({ GroupB.class, GroupA.class })
	interface Sequence2 {
	}

	@GroupSequence({ Sequence2.class, GroupB.class })
	interface Sequence3 {
	}

	@GroupSequence({ Sequence2.class, GroupA.class })
	interface Sequence4 {
	}
}
