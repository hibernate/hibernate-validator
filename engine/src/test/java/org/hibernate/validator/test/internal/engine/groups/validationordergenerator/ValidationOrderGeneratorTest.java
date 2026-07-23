/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.groups.validationordergenerator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jakarta.validation.GroupDefinitionException;
import jakarta.validation.GroupSequence;
import jakarta.validation.ValidationException;
import jakarta.validation.groups.Default;

import org.hibernate.validator.internal.engine.groups.Group;
import org.hibernate.validator.internal.engine.groups.Sequence;
import org.hibernate.validator.internal.engine.groups.ValidationOrder;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.test.internal.engine.groups.validationorder.First;
import org.hibernate.validator.test.internal.engine.groups.validationorder.Last;
import org.hibernate.validator.test.internal.engine.groups.validationorder.Second;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * @author Hardy Ferentschik
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ValidationOrderGeneratorTest {

	ValidationOrderGenerator generator;

	@BeforeAll
	public void init() {
		generator = new ValidationOrderGenerator();
	}

	@Test
	public void testValidationOrderForNonInterface() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( String.class );
		assertThatThrownBy( () -> generator.getValidationOrder( groups ) )
				.isInstanceOf( ValidationException.class );
	}

	@Test
	public void testValidationOrderForNull() {
		assertThatThrownBy( () -> generator.getValidationOrder( null ) )
				.isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void testValidationOrderForEmptySet() {
		assertThatThrownBy( () -> generator.getValidationOrder( new HashSet<Class<?>>() ) )
				.isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void testCyclicGroupSequences() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( CyclicGroupSequence1.class );
		assertThatThrownBy( () -> generator.getValidationOrder( groups ) )
				.isInstanceOf( ValidationException.class );
	}

	@Test
	public void testCyclicGroupSequence() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( CyclicGroupSequence.class );
		assertThatThrownBy( () -> generator.getValidationOrder( groups ) )
				.isInstanceOf( ValidationException.class );
	}

	@Test
	public void testGroupDuplicates() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( First.class );
		groups.add( Second.class );
		groups.add( Last.class );
		ValidationOrder chain = generator.getValidationOrder( groups );
		int count = countGroups( chain );
		assertEquals( 3, count, "Wrong number of groups" );

		groups.clear();
		groups.add( First.class );
		groups.add( First.class );
		chain = generator.getValidationOrder( groups );
		count = countGroups( chain );
		assertEquals( 1, count, "Wrong number of groups" );

		groups.clear();
		groups.add( First.class );
		groups.add( Last.class );
		groups.add( First.class );
		chain = generator.getValidationOrder( groups );
		count = countGroups( chain );
		assertEquals( 2, count, "Wrong number of groups" );
	}

	@Test
	public void testGroupDefiningSequencePartOfGroupComposingSequence() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( Sequence1.class );
		assertThatThrownBy( () -> generator.getValidationOrder( groups ) )
				.isInstanceOf( GroupDefinitionException.class );
	}

	@Test
	public void testUnexpandableSequence() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( Sequence3.class );
		assertThatThrownBy( () -> generator.getValidationOrder( groups ) )
				.isInstanceOf( GroupDefinitionException.class );
	}

	@Test
	public void testExpandableSequenceWithInheritance() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( Sequence4.class );
		generator.getValidationOrder( groups );
	}

	@Test
	public void testSequenceResolution() {
		Set<Class<?>> groups = new HashSet<Class<?>>();
		groups.add( Address.Complete.class );
		ValidationOrder chain = generator.getValidationOrder( groups );
		Iterator<Sequence> sequences = chain.getSequenceIterator();
		List<Group> sequence = sequences.next().getComposingGroups();

		assertEquals( Default.class, sequence.get( 0 ).getDefiningClass(), "Wrong group" );
		assertEquals( Address.HighLevelCoherence.class, sequence.get( 1 ).getDefiningClass(), "Wrong group" );
	}

	private int countGroups(ValidationOrder chain) {
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
