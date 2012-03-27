package org.hibernate.validator.test.internal.engine.groups.groupchain;

import java.util.ArrayList;
import java.util.List;
import javax.validation.GroupDefinitionException;
import javax.validation.groups.Default;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.engine.groups.Group;
import org.hibernate.validator.internal.engine.groups.GroupOrder;
import org.hibernate.validator.internal.engine.groups.Sequence;

import static org.testng.FileAssert.fail;

/**
 * @author Hardy Ferentschik
 */
public class GroupOrderTest {
	@Test
	public void testAssertDefaultGroupSequenceIsExpandableWithDefaultAtEndOfSequence() {
		// create a dummy sequence
		Group a = new Group( GroupA.class );
		Group b = new Group( GroupB.class );
		Group c = new Group( GroupC.class );
		Group defaultGroup = new Group( Default.class );

		List<Group> sequence = new ArrayList<Group>();
		sequence.add( a );
		sequence.add( b );
		sequence.add( c );
		sequence.add( defaultGroup );

		GroupOrder chain = new GroupOrder();
		chain.insertSequence( new Sequence( TestSequence.class, sequence ) );

		// create test default sequence
		List<Class<?>> defaultSequence = new ArrayList<Class<?>>();
		defaultSequence.add( Default.class );
		defaultSequence.add( GroupA.class );
		try {
			chain.assertDefaultGroupSequenceIsExpandable( defaultSequence );
			fail();
		}
		catch ( GroupDefinitionException e ) {
			// success
		}

		defaultSequence.clear();
		defaultSequence.add( GroupA.class );
		defaultSequence.add( Default.class );
		try {
			chain.assertDefaultGroupSequenceIsExpandable( defaultSequence );
			fail();
		}
		catch ( GroupDefinitionException e ) {
			// success
		}

		defaultSequence.clear();
		defaultSequence.add( Default.class );
		defaultSequence.add( GroupC.class );
		try {
			chain.assertDefaultGroupSequenceIsExpandable( defaultSequence );
			fail();
		}
		catch ( GroupDefinitionException e ) {
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
		Group defaultGroup = new Group( Default.class );

		List<Group> sequence = new ArrayList<Group>();
		sequence.add( defaultGroup );
		sequence.add( a );
		sequence.add( b );
		sequence.add( c );

		GroupOrder chain = new GroupOrder();
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
		catch ( GroupDefinitionException e ) {
			// success
		}

		defaultSequence.clear();
		defaultSequence.add( Default.class );
		defaultSequence.add( GroupC.class );
		try {
			chain.assertDefaultGroupSequenceIsExpandable( defaultSequence );
			fail();
		}
		catch ( GroupDefinitionException e ) {
			// success
		}

		defaultSequence.clear();
		defaultSequence.add( GroupC.class );
		defaultSequence.add( Default.class );
		try {
			chain.assertDefaultGroupSequenceIsExpandable( defaultSequence );
			fail();
		}
		catch ( GroupDefinitionException e ) {
			// success
		}
	}
}

interface GroupC {
}