package org.hibernate.validator.test.engine.groups.groupchain;

import java.util.ArrayList;
import java.util.List;
import javax.validation.GroupDefinitionException;
import javax.validation.groups.Default;

import org.testng.annotations.Test;

import org.hibernate.validator.engine.groups.Group;
import org.hibernate.validator.engine.groups.GroupChain;

import static org.testng.FileAssert.fail;

/**
 * @author Hardy Ferentschik
 */
public class GroupChainTest {
	@Test
	public void testAssertDefaultGroupSequenceIsExpandableWithDefaultAtEndOfSequence() {
		// create a dummy sequence
		Group a = new Group( GroupA.class, TestSequence.class );
		Group b = new Group( GroupB.class, TestSequence.class );
		Group c = new Group( GroupC.class, TestSequence.class );
		Group defaultGroup = new Group(
				Default.class, TestSequence.class
		);
		List<Group> sequence = new ArrayList<Group>();
		sequence.add( a );
		sequence.add( b );
		sequence.add( c );
		sequence.add( defaultGroup );

		GroupChain chain = new GroupChain();
		chain.insertSequence( sequence );

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
		Group a = new Group( GroupA.class, TestSequence.class );
		Group b = new Group( GroupB.class, TestSequence.class );
		Group c = new Group( GroupC.class, TestSequence.class );
		Group defaultGroup = new Group(
				Default.class, TestSequence.class
		);
		List<Group> sequence = new ArrayList<Group>();
		sequence.add( defaultGroup );
		sequence.add( a );
		sequence.add( b );
		sequence.add( c );

		GroupChain chain = new GroupChain();
		chain.insertSequence( sequence );

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