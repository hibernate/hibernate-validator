/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.validationorder;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.GroupDefinitionException;
import jakarta.validation.groups.Default;

import org.hibernate.validator.internal.engine.groups.DefaultValidationOrder;
import org.hibernate.validator.internal.engine.groups.Group;
import org.hibernate.validator.internal.engine.groups.Sequence;
import org.testng.annotations.Test;

import static org.testng.Assert.fail;

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
		try {
			chain.assertDefaultGroupSequenceIsExpandable( defaultSequence );
			fail();
		}
		catch (GroupDefinitionException e) {
			// success
		}

		defaultSequence.clear();
		defaultSequence.add( GroupA.class );
		defaultSequence.add( Default.class );
		try {
			chain.assertDefaultGroupSequenceIsExpandable( defaultSequence );
			fail();
		}
		catch (GroupDefinitionException e) {
			// success
		}

		defaultSequence.clear();
		defaultSequence.add( Default.class );
		defaultSequence.add( GroupC.class );
		try {
			chain.assertDefaultGroupSequenceIsExpandable( defaultSequence );
			fail();
		}
		catch (GroupDefinitionException e) {
			// success
		}

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
		try {
			chain.assertDefaultGroupSequenceIsExpandable( defaultSequence );
			fail();
		}
		catch (GroupDefinitionException e) {
			// success
		}

		defaultSequence.clear();
		defaultSequence.add( Default.class );
		defaultSequence.add( GroupC.class );
		try {
			chain.assertDefaultGroupSequenceIsExpandable( defaultSequence );
			fail();
		}
		catch (GroupDefinitionException e) {
			// success
		}

		defaultSequence.clear();
		defaultSequence.add( GroupC.class );
		defaultSequence.add( Default.class );
		try {
			chain.assertDefaultGroupSequenceIsExpandable( defaultSequence );
			fail();
		}
		catch (GroupDefinitionException e) {
			// success
		}
	}
}

interface GroupC {
}
