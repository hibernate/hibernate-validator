/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.properties.javabean;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.internal.properties.javabean.JavaBeanConstructor;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ListAppender;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.testng.annotations.Test;

public class JavaBeanExecutableTest {

	@Test
	@TestForIssue(jiraKey = "HV-1634")
	public void testGenericTypeParametersWithImplicitParameters() throws NoSuchMethodException, SecurityException {
		JavaBeanConstructor constructor = new JavaBeanConstructor( Bean.class.getDeclaredConstructors()[0] );

		assertThat( constructor.getParameters() ).hasSize( 3 );

		ParameterizedType parameterizedType1 = (ParameterizedType) constructor.getParameterGenericType( 1 );
		assertThat( parameterizedType1.getRawType() ).isEqualTo( Map.class );

		ParameterizedType parameterizedType2 = (ParameterizedType) constructor.getParameterGenericType( 2 );
		assertThat( parameterizedType2.getRawType() ).isEqualTo( List.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1634")
	public void testGenericTypeParametersWithoutImplicitParameters() throws NoSuchMethodException, SecurityException {
		JavaBeanConstructor constructor = new JavaBeanConstructor( StaticBean.class.getDeclaredConstructors()[0] );

		assertThat( constructor.getParameters() ).hasSize( 2 );

		ParameterizedType parameterizedType0 = (ParameterizedType) constructor.getParameterGenericType( 0 );
		assertThat( parameterizedType0.getRawType() ).isEqualTo( Map.class );

		ParameterizedType parameterizedType1 = (ParameterizedType) constructor.getParameterGenericType( 1 );
		assertThat( parameterizedType1.getRawType() ).isEqualTo( List.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1634")
	public void testGenericTypeParametersWithSyntheticParameters() throws NoSuchMethodException, SecurityException {
		JavaBeanConstructor constructor = new JavaBeanConstructor( MyEnum.class.getDeclaredConstructors()[0] );

		assertThat( constructor.getParameters() ).hasSize( 4 );

		ParameterizedType parameterizedType1 = (ParameterizedType) constructor.getParameterGenericType( 2 );
		assertThat( parameterizedType1.getRawType() ).isEqualTo( Map.class );

		ParameterizedType parameterizedType2 = (ParameterizedType) constructor.getParameterGenericType( 3 );
		assertThat( parameterizedType2.getRawType() ).isEqualTo( List.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1852")
	public void validatingEnumConstantDoesNotLogMissingParameterMetadataWarning() {
		LoggerContext context = LoggerContext.getContext( false );
		Logger logger = context.getLogger( "org.hibernate.validator" );
		Level originalLogLevel = logger.getLevel();
		ListAppender logAppender = new ListAppender( "hv-1852" );
		logAppender.start();
		logger.addAppender( logAppender );

		try {
			if ( logger.getLevel().isMoreSpecificThan( Level.WARN ) ) {
				Configurator.setLevel( "org.hibernate.validator", Level.WARN );
				context.updateLoggers();
			}

			try ( ValidatorFactory factory = Validation.buildDefaultValidatorFactory() ) {
				factory.getValidator().validate( SecurityProtocol.VALUE1 );
			}

			assertThat( logAppender.getMessages( Level.WARN ) )
					.noneMatch( message -> message.contains( "HV000254" )
							|| message.contains( "Missing parameter metadata" ) );
		}
		finally {
			logger.removeAppender( logAppender );
			logAppender.stop();

			if ( originalLogLevel != null ) {
				Configurator.setLevel( "org.hibernate.validator", originalLogLevel );
				context.updateLoggers();
			}
		}
	}

	private class Bean {

		private Bean(Map<String, String> map, List<Integer> list) {
		}
	}

	private static class StaticBean {

		private StaticBean(Map<String, String> map, List<Integer> list) {
		}
	}

	private enum MyEnum {

		;

		private MyEnum(Map<String, String> map, List<Integer> list) {
		}
	}

	private enum SecurityProtocol {
		VALUE1( false );

		@SuppressWarnings("unused")
		private final boolean param1;

		SecurityProtocol(boolean param1) {
			this.param1 = param1;
		}
	}
}
