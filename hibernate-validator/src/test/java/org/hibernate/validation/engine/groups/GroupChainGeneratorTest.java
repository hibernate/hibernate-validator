// $Id$
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import javax.validation.ValidationException;
import javax.validation.groups.Default;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import org.hibernate.validation.engine.First;
import org.hibernate.validation.engine.Last;
import org.hibernate.validation.engine.Second;

/**
 * @author Hardy Ferentschik
 */
public class GroupChainGeneratorTest {

	GroupChainGenerator generator;

	@Before
	public void init() {
		generator = new GroupChainGenerator();
	}

	@Test(expected = ValidationException.class)
	public void testGroupChainForNonInterface() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( String.class );
		generator.getGroupChainFor( groups );
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGroupChainForNull() {
		generator.getGroupChainFor( null );
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGroupChainForEmptySet() {
		generator.getGroupChainFor( new HashSet<Class<?>>() );
	}

	@Test(expected = ValidationException.class)
	public void testCyclicGroupSequences() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( CyclicGroupSequence1.class );
		generator.getGroupChainFor( groups );
	}

	@Test(expected = ValidationException.class)
	public void testCyclicGroupSequence() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( CyclicGroupSequence.class );
		generator.getGroupChainFor( groups );
	}

	@Test
	public void testGroupDuplicates() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( First.class );
		groups.add( Second.class );
		groups.add( Last.class );
		GroupChain chain = generator.getGroupChainFor( groups );
		int count = countGroups( chain );
		assertEquals( "Wrong number of groups", 3, count );

		groups.clear();
		groups.add( First.class );
		groups.add( First.class );
		chain = generator.getGroupChainFor( groups );
		count = countGroups( chain );
		assertEquals( "Wrong number of groups", 1, count );

		groups.clear();
		groups.add( First.class );
		groups.add( Last.class );
		groups.add( First.class );
		chain = generator.getGroupChainFor( groups );
		count = countGroups( chain );
		assertEquals( "Wrong number of groups", 2, count );
	}

	private int countGroups(GroupChain chain) {
		Iterator<Group> groupIterator = chain.getGroupIterator();
		int count = 0;
		while (groupIterator.hasNext()) {
			groupIterator.next();
			count++;
		}
		return count;
	}

	@Test
	public void testSequenceResolution() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( Address.Complete.class );
		GroupChain chain = generator.getGroupChainFor( groups );
		Iterator<List<Group>> sequences = chain.getSequenceIterator();
		List<Group> sequence = sequences.next();

		assertEquals( "Wrong group", Default.class, sequence.get(0).getGroup() );
		assertEquals( "Wrong group", Address.HighLevelCoherence.class, sequence.get(1).getGroup() );
	}
}
