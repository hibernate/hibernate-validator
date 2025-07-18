/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.Valid;

import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.test.internal.engine.serialization.Email;
import org.hibernate.validator.testutils.ListAppender;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ValidAnnotationTest extends AbstractConstrainedTest {

	private ListAppender logAppender;
	private Level originalLogLevel;
	private Logger targetLogger;

	/**
	 * @return true if the string matches the expected log message for deprecated use of @Value.
	 */
	private static boolean deprecatedUsedOfValueCode(String s) {
		return s.startsWith( "HV000270" );
	}

	@BeforeTest
	public void setUpLogger() {
		logAppender = new ListAppender( ValidAnnotationTest.class.getSimpleName() );
		logAppender.start();

		LoggerContext context = LoggerContext.getContext( false );
		targetLogger = context.getLogger( CascadingMetaDataBuilder.class.getName() );
		targetLogger.addAppender( logAppender );

		// Set level of log messages to WARN (if they are not already enabled)
		if ( targetLogger.getLevel().isMoreSpecificThan( Level.WARN ) ) {
			// Store the original log level to restore it later
			originalLogLevel = targetLogger.getLevel();

			// Override the log level for this test class only
			// Default tests will only print the error messages, we need to override it
			// so that we can capture the deprecated warning message
			Configurator.setLevel( CascadingMetaDataBuilder.class.getName(), Level.WARN );
			context.updateLoggers();
		}
	}

	@BeforeMethod
	public void cleanLogger() {
		logAppender.clear();
	}

	@AfterTest
	public void tearDownLogger() {
		targetLogger.removeAppender( logAppender );
		logAppender.stop();

		// Restore the original log level
		if ( originalLogLevel != null ) {
			Configurator.setLevel( CascadingMetaDataBuilder.class.getName(), originalLogLevel );
			// Update the logger context to apply changes
			LoggerContext context = LoggerContext.getContext( false );
			context.updateLoggers();
		}
	}

	@Test
	public void twiceWithList() {
		class Foo {

			@Valid
			private List<@Valid String> prop;

			public Foo(List<String> prop) {
				this.prop = prop;
			}
		}

		Foo foo = new Foo( List.of( "K1" ) );
		validator.validate( foo );

		assertThat( logAppender.getMessages() ).hasSize( 1 ).allMatch( ValidAnnotationTest::deprecatedUsedOfValueCode );
	}

	@Test
	public void onTheContainerWithList() {
		class Foo {

			@Valid
			private List<MyBean> prop;

			public Foo(List<MyBean> prop) {
				this.prop = prop;
			}
		}

		Foo foo = new Foo( List.of( new MyBean( "entry.list@email.com" ) ) );
		validator.validate( foo );

		assertThat( logAppender.getMessages() ).hasSize( 1 ).allMatch( ValidAnnotationTest::deprecatedUsedOfValueCode );
	}

	@Test
	public void twiceWithSet() {
		class Foo {

			@Valid
			private Set<@Valid String> prop;

			public Foo(Set<String> prop) {
				this.prop = prop;
			}
		}

		Foo foo = new Foo( Set.of( "K1" ) );
		validator.validate( foo );

		assertThat( logAppender.getMessages() ).hasSize( 1 ).allMatch( ValidAnnotationTest::deprecatedUsedOfValueCode );
	}

	@Test
	public void onTheContainerWithSet() {
		class Foo {

			@Valid
			private Set<MyBean> prop;

			public Foo(Set<MyBean> prop) {
				this.prop = prop;
			}
		}

		Foo foo = new Foo( Set.of( new MyBean( "entry.set@email.email" ) ) );
		validator.validate( foo );

		assertThat( logAppender.getMessages() ).hasSize( 1 ).allMatch( ValidAnnotationTest::deprecatedUsedOfValueCode );
	}

	@Test
	public void twiceWithMap() {
		class Foo {

			@Valid
			private Map<String, @Valid String> prop;

			public Foo(Map<String, String> prop) {
				this.prop = prop;
			}
		}

		Foo foo = new Foo( Map.of( "K1", "V1" ) );
		validator.validate( foo );

		assertThat( logAppender.getMessages() ).hasSize( 1 ).allMatch( ValidAnnotationTest::deprecatedUsedOfValueCode );
	}

	@Test
	public void onContainerWithMap() {
		class Foo {

			@Valid
			private Map<String, MyBean> prop;

			public Foo(Map<String, MyBean> prop) {
				this.prop = prop;
			}
		}

		Foo foo = new Foo( Map.of( "K1", new MyBean( "value1@email.email" ) ) );
		validator.validate( foo );

		assertThat( logAppender.getMessages() ).hasSize( 1 ).allMatch( ValidAnnotationTest::deprecatedUsedOfValueCode );
	}

	private static class MyBean {
		@Email
		private String email;

		public MyBean(String email) {
			this.email = email;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}
	}
}
