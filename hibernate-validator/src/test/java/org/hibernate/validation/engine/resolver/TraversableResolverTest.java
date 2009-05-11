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
package org.hibernate.validation.engine.resolver;

import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 * @author Emmanuel Bernard
 */
public class TraversableResolverTest {
	@Test
	public void testCorrectPathsAreRequested() {
		Suit suit = new Suit();
		suit.setTrousers( new Trousers() );
		suit.setJacket( new Jacket() );
		suit.setSize( 3333 );
		suit.getTrousers().setLength( 32321 );
		suit.getJacket().setWidth( 432432 );

		SnifferTraversableResolver resolver = new SnifferTraversableResolver( suit );

		// TODO - Investigate why this cast is needed with Java 5. In Java 6 there is no problem.
		Configuration<?> config = (Configuration<?>) Validation.byDefaultProvider().configure().traversableResolver( resolver );

		ValidatorFactory factory = config.buildValidatorFactory();
		Validator v = factory.getValidator();

		//Raises an IllegalStateException if something goes wrong
		v.validate( suit, Default.class, Cloth.class );

		assertEquals( 5, resolver.getReachPaths().size() );
		assertEquals( 2, resolver.getCascadePaths().size() );
	}
}