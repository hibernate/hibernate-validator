/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import jakarta.validation.Configuration;
import jakarta.validation.Path;
import jakarta.validation.TraversableResolver;
import jakarta.validation.ValidationException;
import jakarta.validation.ValidatorFactory;

import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.testutil.TestForIssue;

import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
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

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000190.*")
	// UnableToDetermineSchemaVersionException
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

		@Override
		public boolean isReachable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
			return true;
		}

		@Override
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
