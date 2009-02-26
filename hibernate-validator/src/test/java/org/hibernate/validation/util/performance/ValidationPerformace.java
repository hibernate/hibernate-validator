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
package org.hibernate.validation.util.performance;

import java.util.ArrayList;
import java.util.List;

import com.clarkware.junitperf.TimedTest;
import junit.framework.Test;

import org.hibernate.validation.engine.ValidatorImplTest;

/**
 * Work in progress. Timings have no relevance. Using this class one can verify if applied changes affect the
 * performance of the framework.
 *
 * @author Hardy Ferentschik
 */
public class ValidationPerformace {

	public static Test suite() {

		long maxTimeInMillis = 1000;
		List<Class<?>> testClasses = new ArrayList<Class<?>>();
		testClasses.add( ValidatorImplTest.class );
		//testClasses.add( GroupTest.class );
		//testClasses.add( ConstraintCompositionTest.class );
		Test test = new JUnit4TestFactory( testClasses ).makeTestSuite();
		return new TimedTest( test, maxTimeInMillis );
	}

	public static void main(String args[]) {
		junit.textui.TestRunner.run( suite() );
	}
}
