/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.engine;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import javax.validation.Configuration;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.ValidatorFactory;

import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.testutil.TestForIssue;

import static org.hibernate.validator.testutil.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertTrue;

/**
 * @author Hardy Ferentschik
 */
public class ConfigurationImplTest {

	@Test
	@TestForIssue(jiraKey = "HV-563")
	public void testCallBuildValidatorFactoryMultipleTimes() {
		final Configuration<HibernateValidatorConfiguration> configuration = getConfiguration();

		ValidatorFactory factory1 = configuration.buildValidatorFactory();
		assertNotNull( factory1 );

		ValidatorFactory factory2 = configuration.buildValidatorFactory();
		assertNotNull( factory2 );

		assertNotSame( factory1, factory2 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-563")
	public void testConfigurationReusableAndMutable() {
		final Configuration<HibernateValidatorConfiguration> configuration = getConfiguration();

		ValidatorFactory factory1 = configuration.buildValidatorFactory();
		assertNotNull( factory1 );

		configuration.traversableResolver( new TestTraversableResolver() );
		ValidatorFactory factory2 = configuration.buildValidatorFactory();
		assertNotNull( factory2 );

		assertNotSame( factory1.getTraversableResolver(), factory2.getTraversableResolver() );
		assertTrue( factory2.getTraversableResolver() instanceof TestTraversableResolver );
	}

	@Test
	@TestForIssue(jiraKey = "HV-563")
	public void testReusableConfigurationWithInputStream() throws Exception {
		final Configuration<HibernateValidatorConfiguration> configuration = getConfiguration();

		InputStream mappingStream = ConfigurationImplTest.class.getResourceAsStream( "mapping.xml" );

		try {
			configuration.addMapping( mappingStream );
			ValidatorFactory factory1 = configuration.buildValidatorFactory();
			assertNotNull( factory1 );

			ValidatorFactory factory2 = configuration.buildValidatorFactory();
			assertNotNull( factory2 );

			assertNotSame( factory1, factory2 );
		}
		finally {
			mappingStream.close();
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-563")
	public void testReusableConfigurationWithNonResettableInputStream() throws Exception {
		final Configuration<HibernateValidatorConfiguration> configuration = getConfiguration();

		InputStream mappingStream = ConfigurationImplTest.class.getResourceAsStream( "mapping.xml" );

		try {
			configuration.addMapping( new NonResettableInputStream( mappingStream ) );
			ValidatorFactory factory1 = configuration.buildValidatorFactory();
			assertNotNull( factory1 );

			ValidatorFactory factory2 = configuration.buildValidatorFactory();
			assertNotNull( factory2 );

			assertNotSame( factory1, factory2 );
		}
		finally {
			mappingStream.close();
		}
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000123.*")
	@TestForIssue(jiraKey = "HV-563")
	public void testReusableConfigurationWithClosedInputStream() throws Exception {
		final Configuration<HibernateValidatorConfiguration> configuration = getConfiguration();

		InputStream mappingStream = ConfigurationImplTest.class.getResourceAsStream( "mapping.xml" );

		try {
			configuration.addMapping( mappingStream );
			ValidatorFactory factory1 = configuration.buildValidatorFactory();
			assertNotNull( factory1 );
		}
		finally {
			mappingStream.close();
		}

		configuration.buildValidatorFactory();
	}

	private static class TestTraversableResolver implements TraversableResolver {

		public boolean isReachable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
			return true;
		}

		public boolean isCascadable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
			return true;
		}
	}

	private static class NonResettableInputStream extends InputStream {

		private InputStream delegate;

		public NonResettableInputStream(InputStream delegate) {
			this.delegate = delegate;
		}

		@Override
		public int read() throws IOException {
			return delegate.read();
		}
	}
}
