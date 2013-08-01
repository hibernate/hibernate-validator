/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.parameternameprovider;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableValidator;

import com.thoughtworks.paranamer.AnnotationParanamer;
import org.testng.annotations.Test;

import org.hibernate.validator.parameternameprovider.ParanamerParameterNameProvider;
import org.hibernate.validator.testutil.TestForIssue;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ValidatorUtil.getConfiguration;

/**
 * Test for {@link ParanamerParameterNameProvider}.
 *
 * @author Gunnar Morling
 */
@TestForIssue(jiraKey = "HV-802")
public class ParanamerParameterNameProviderTest {

	@Test
	public void shouldReturnParameterNamesFromDebugSymbolsForMethod() throws Exception {
		ParanamerParameterNameProvider parameterNameProvider = new ParanamerParameterNameProvider();
		Method method = ComputerGame.class.getMethod( "startGame", String.class, int.class );

		List<String> parameterNames = parameterNameProvider.getParameterNames( method );
		assertThat( parameterNames ).containsExactly( "level", "numberOfPlayers" );
	}

	@Test
	public void shouldReturnParameterNamesFromDebugSymbolsForConstructor() throws Exception {
		ParanamerParameterNameProvider parameterNameProvider = new ParanamerParameterNameProvider();
		Constructor<ComputerGame> constructor = ComputerGame.class.getConstructor( String.class );

		List<String> parameterNames = parameterNameProvider.getParameterNames( constructor );
		assertThat( parameterNames ).containsExactly( "title" );
	}

	@Test
	public void shouldReturnParameterNamesFromCustomParanamer() throws Exception {
		ParanamerParameterNameProvider parameterNameProvider = new ParanamerParameterNameProvider( new CustomAnnotationParanamer() );

		Constructor<ComputerGame> constructor = ComputerGame.class.getConstructor( String.class );
		List<String> parameterNames = parameterNameProvider.getParameterNames( constructor );
		assertThat( parameterNames ).containsExactly( "gameTitle" );

		Method method = ComputerGame.class.getMethod( "pauseGame", int.class );
		parameterNames = parameterNameProvider.getParameterNames( method );
		assertThat( parameterNames ).containsExactly( "durationInSeconds" );
	}

	@Test
	public void shouldReturnDefaultValuesAsFallBack() throws Exception {
		ParanamerParameterNameProvider parameterNameProvider = new ParanamerParameterNameProvider( new CustomAnnotationParanamer() );

		Method method = ComputerGame.class.getMethod( "startGame", String.class, int.class );
		List<String> parameterNames = parameterNameProvider.getParameterNames( method );
		assertThat( parameterNames ).containsExactly( "arg0", "arg1" );
	}

	@Test
	public void shouldUseParanamerProviderDuringValidation() throws Exception {
		ExecutableValidator executableValidator = getConfiguration()
				.parameterNameProvider( new ParanamerParameterNameProvider( new CustomAnnotationParanamer() ) )
				.buildValidatorFactory()
				.getValidator()
				.forExecutables();

		Object object = new ComputerGame( "Giovanni Brothers" );
		Method method = ComputerGame.class.getMethod( "pauseGame", int.class );
		Object[] parameterValues = new Object[] { -2 };

		Set<ConstraintViolation<Object>> violations = executableValidator.validateParameters(
				object,
				method,
				parameterValues
		);

		assertCorrectPropertyPaths( violations, "pauseGame.durationInSeconds" );
	}

	@SuppressWarnings("unused")
	private static class ComputerGame {

		public ComputerGame(@Named("gameTitle") String title) {
		}

		public void startGame(@NotNull String level, @Min(1) int numberOfPlayers) {
		}

		public void pauseGame(@Named("durationInSeconds") @Min(1) int duration) {
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	private @interface Named {
		String value();
	}

	/**
	 * A custom annotation based {@code Paranamer} implementation using the {@code Named} annotation.
	 */
	private static class CustomAnnotationParanamer extends AnnotationParanamer {

		@Override
		protected boolean isNamed(Annotation annotation) {
			return Named.class == annotation.annotationType();
		}

		@Override
		protected String getNamedValue(Annotation annotation) {
			return ( (Named) annotation ).value();
		}
	}
}
