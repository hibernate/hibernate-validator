/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.parameternameprovider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ExecutableValidator;

import org.hibernate.validator.parameternameprovider.ParanamerParameterNameProvider;
import org.hibernate.validator.testutil.ConstraintViolationAssert;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

import com.thoughtworks.paranamer.AnnotationParanamer;

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
		assertThat( parameterNames ).containsExactly( "level", "numberOfPlayers" );
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

		ConstraintViolationAssert.assertThat( violations ).containsOnlyViolations(
				violationOf( Min.class ).withPropertyPath( pathWith()
						.method( "pauseGame" )
						.parameter( "durationInSeconds", 0 )
				)
		);
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
