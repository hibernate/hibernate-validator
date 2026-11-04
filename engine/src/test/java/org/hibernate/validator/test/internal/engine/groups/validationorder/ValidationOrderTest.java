/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.groups.validationorder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.GroupDefinitionException;
import jakarta.validation.groups.Default;

import org.hibernate.validator.internal.engine.groups.DefaultValidationOrder;
import org.hibernate.validator.internal.engine.groups.Group;
import org.hibernate.validator.internal.engine.groups.Sequence;

import org.junit.jupiter.api.Test;

/**
 * @author Hardy Ferentschik
 */
public class ValidationOrderTest {
	@Test
	public void testAssertDefaultGroupSequenceIsExpandableWithDefaultAtEndOfSequence() {
		// create a dummy sequence
		Group a = new Group( GroupA.class );
		Group b = new Group( GroupB.class );
		Group c = new Group( GroupC.class );
		Group defaultGroup = Group.DEFAULT_GROUP;

		List<Group> sequence = new ArrayList<Group>();
		sequence.add( a );
		sequence.add( b );
		sequence.add( c );
		sequence.add( defaultGroup );

		DefaultValidationOrder chain = new DefaultValidationOrder();
		chain.insertSequence( new Sequence( TestSequence.class, sequence ) );

		// create test default sequence
		List<Class<?>> defaultSequence = new ArrayList<Class<?>>();
		defaultSequence.add( Default.class );
		defaultSequence.add( GroupA.class );
		assertThatThrownBy( () -> chain.assertDefaultGroupSequenceIsExpandable( defaultSequence ) )
				.isInstanceOf( GroupDefinitionException.class );

		defaultSequence.clear();
		defaultSequence.add( GroupA.class );
		defaultSequence.add( Default.class );
		assertThatThrownBy( () -> chain.assertDefaultGroupSequenceIsExpandable( defaultSequence ) )
				.isInstanceOf( GroupDefinitionException.class );

		defaultSequence.clear();
		defaultSequence.add( Default.class );
		defaultSequence.add( GroupC.class );
		assertThatThrownBy( () -> chain.assertDefaultGroupSequenceIsExpandable( defaultSequence ) )
				.isInstanceOf( GroupDefinitionException.class );

		defaultSequence.clear();
		defaultSequence.add( GroupC.class );
		defaultSequence.add( Default.class );
		chain.assertDefaultGroupSequenceIsExpandable( defaultSequence );
	}


	@Test
	public void testAssertDefaultGroupSequenceIsExpandableWithDefaultAtBeginningOfSequence() {
		// create a dummy sequence
		Group a = new Group( GroupA.class );
		Group b = new Group( GroupB.class );
		Group c = new Group( GroupC.class );
		Group defaultGroup = Group.DEFAULT_GROUP;

		List<Group> sequence = new ArrayList<Group>();
		sequence.add( defaultGroup );
		sequence.add( a );
		sequence.add( b );
		sequence.add( c );

		DefaultValidationOrder chain = new DefaultValidationOrder();
		chain.insertSequence( new Sequence( TestSequence.class, sequence ) );

		// create test default sequence
		List<Class<?>> defaultSequence = new ArrayList<Class<?>>();
		defaultSequence.add( Default.class );
		defaultSequence.add( GroupA.class );
		chain.assertDefaultGroupSequenceIsExpandable( defaultSequence );


		defaultSequence.clear();
		defaultSequence.add( GroupA.class );
		defaultSequence.add( Default.class );
		assertThatThrownBy( () -> chain.assertDefaultGroupSequenceIsExpandable( defaultSequence ) )
				.isInstanceOf( GroupDefinitionException.class );

		defaultSequence.clear();
		defaultSequence.add( Default.class );
		defaultSequence.add( GroupC.class );
		assertThatThrownBy( () -> chain.assertDefaultGroupSequenceIsExpandable( defaultSequence ) )
				.isInstanceOf( GroupDefinitionException.class );

		defaultSequence.clear();
		defaultSequence.add( GroupC.class );
		defaultSequence.add( Default.class );
		assertThatThrownBy( () -> chain.assertDefaultGroupSequenceIsExpandable( defaultSequence ) )
				.isInstanceOf( GroupDefinitionException.class );
	}
}

interface GroupC {
}
