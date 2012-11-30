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
package org.hibernate.validator.test.internal.engine.groups.validationorder;

import java.util.ArrayList;
import java.util.List;
import javax.validation.GroupDefinitionException;
import javax.validation.groups.Default;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.engine.groups.DefaultValidationOrder;
import org.hibernate.validator.internal.engine.groups.Group;
import org.hibernate.validator.internal.engine.groups.Sequence;

import static org.testng.FileAssert.fail;

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
		Group defaultGroup = new Group( Default.class );

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