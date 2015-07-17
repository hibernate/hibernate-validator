/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.tckrunner.securitymanager;

import java.util.Collections;
import java.util.List;

import org.hibernate.beanvalidation.tck.util.IntegrationTestsMethodSelector;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.xml.XmlMethodSelector;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Main class for running the test suite programmatically, for debugging purposes.
 * <p>
 * Specify the following arguments when running this test:
 * <pre>
 * -Dvalidation.provider=org.hibernate.validator.HibernateValidator
 * -Djava.security.manager=default
 * -Djava.security.policy=target/test-classes/test.policy
 * -Djava.security.debug=access
 * -DexcludeIntegrationTests=true
 * -Darquillian.protocol=LocalSecurityManagerTesting
 * -Dorg.jboss.testharness.spi.StandaloneContainers=org.hibernate.jsr303.tck.util.StandaloneContainersImpl
 * </pre>
 *
 * @author Gunnar Morling
 */
public class TckRunner {

	public static void main(String[] args) {
		XmlSuite suite = new XmlSuite();
		suite.setName( "JSR-349-TCK" );

		XmlTest test = new XmlTest(suite);
		test.setName( "JSR-349-TCK" );

		List<XmlPackage> packages = Collections.singletonList( new XmlPackage( "org.hibernate.beanvalidation.tck.tests" ) );
		test.setXmlPackages( packages );

		// Alternatively e.g. use this for running single tests
		// List<XmlClass> classes = Collections.singletonList( new XmlClass( ValidateTest.class ) );
		// test.setXmlClasses( classes );

		XmlMethodSelector selector = new XmlMethodSelector();
		selector.setClassName( IntegrationTestsMethodSelector.class.getName() );
		test.setMethodSelectors( Collections.singletonList( selector  ) );

		TestListenerAdapter tla = new TestListenerAdapter();
		TestNG testng = new TestNG();
		testng.setXmlSuites( Collections.singletonList( suite ) );
		testng.addListener( tla );
		testng.run();

		for ( ITestResult failure: tla.getConfigurationFailures() ) {
			System.out.println( "Failure: " + failure.getName() );
			failure.getThrowable().printStackTrace();
		}

		for ( ITestResult result : tla.getFailedTests() ) {
			System.out.println( "Failed:" + result.getName() );
			result.getThrowable().printStackTrace();
		}
	}
}
