/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.security.SecureClassLoader;
import java.util.Enumeration;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.internal.IgnoreForbiddenApisErrors;
import org.hibernate.validator.testutils.ValidatorUtil;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.testng.annotations.Test;

public class XmlMappingMixedWithServiceLoaderAndProgrammaticDefinitionTest {

	@Test
	public void constraintAppliedInXmlDefinitionIsAppliedThroughProgrammaticMapping() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingMixedWithServiceLoaderAndProgrammaticDefinitionTest.class.getResourceAsStream( "hv-1949-mapping.xml" ) );
		ConstraintMapping constraintMapping = configuration.createConstraintMapping();
		configuration.addMapping( constraintMapping );

		constraintMapping.constraintDefinition( MyOtherConstraint.class )
				.includeExistingValidators( true )
				.validatedBy( MyOtherConstraint.MyOtherConstraintValidator.class );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();

		assertThat( validator.validate( new Foo() ) ).containsOnlyViolations(
				violationOf( MyOtherConstraint.class ).withProperty( "string" )
		);
		assertThat( validator.validate( new Bar() ) ).containsOnlyViolations(
				violationOf( MyOtherConstraint.class ).withProperty( "string" )
		);
	}

	@Test
	public void constraintAppliedInXmlDefinitionIsAppliedThroughServiceLoading() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingMixedWithServiceLoaderAndProgrammaticDefinitionTest.class.getResourceAsStream( "hv-1949-mapping.xml" ) );
		configuration.externalClassLoader( new ServiceLoaderTestingClassLoader() );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();

		assertThat( validator.validate( new Foo() ) ).containsOnlyViolations(
				violationOf( MyOtherConstraint.class ).withProperty( "string" )
		);
		assertThat( validator.validate( new Bar() ) ).containsOnlyViolations(
				violationOf( MyOtherConstraint.class ).withProperty( "string" )
		);
	}

	@Test
	public void constraintAppliedInXmlDefinitionIsAppliedThroughXmlOverriddenWithProgrammatic() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingMixedWithServiceLoaderAndProgrammaticDefinitionTest.class.getResourceAsStream( "hv-1949-mapping.xml" ) );
		configuration.addMapping( XmlMappingMixedWithServiceLoaderAndProgrammaticDefinitionTest.class.getResourceAsStream( "hv-1949-constraint.xml" ) );

		ConstraintMapping constraintMapping = configuration.createConstraintMapping();
		configuration.addMapping( constraintMapping );

		constraintMapping.constraintDefinition( MyOtherConstraint.class )
				.includeExistingValidators( false )
				.validatedBy( MyOtherConstraint.MyOtherOtherConstraintValidator.class );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();

		assertNoViolations( validator.validate( new Foo() ) );
		assertNoViolations( validator.validate( new Bar() ) );
	}

	@Test
	public void constraintAppliedProgrammaticallyDefinitionIsAppliedThroughXml() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingMixedWithServiceLoaderAndProgrammaticDefinitionTest.class.getResourceAsStream( "hv-1949-constraint.xml" ) );
		ConstraintMapping constraintMapping = configuration.createConstraintMapping();
		configuration.addMapping( constraintMapping );

		constraintMapping.type( Foo.class )
				.field( "string" )
				.constraint( new MyOtherConstraintDef() );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();

		assertThat( validator.validate( new Foo() ) ).containsOnlyViolations(
				violationOf( MyOtherConstraint.class ).withProperty( "string" )
		);
		assertThat( validator.validate( new Bar() ) ).containsOnlyViolations(
				violationOf( MyOtherConstraint.class ).withProperty( "string" )
		);
	}

	@Test
	public void constraintValidatorLoadedByServiceLoaderOverriddenByProgrammaticDefinition() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration();
		configuration.externalClassLoader( new ServiceLoaderTestingClassLoader() );

		ConstraintMapping constraintMapping = configuration.createConstraintMapping();
		configuration.addMapping( constraintMapping );

		constraintMapping.constraintDefinition( MyOtherConstraint.class )
				.includeExistingValidators( false )
				.validatedBy( MyOtherConstraint.MyOtherOtherConstraintValidator.class );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();

		assertNoViolations( validator.validate( new Foo() ) );
		assertNoViolations( validator.validate( new Bar() ) );
	}

	public static class Foo {
		public String string;
	}

	public static class Bar {
		@MyOtherConstraint
		public String string;
	}

	public static class MyOtherConstraintDef extends ConstraintDef<MyOtherConstraintDef, MyOtherConstraint> {

		protected MyOtherConstraintDef() {
			super( MyOtherConstraint.class );
		}
	}

	@Documented
	@Constraint(validatedBy = {})
	@Target({ TYPE, METHOD, FIELD })
	@Retention(RUNTIME)
	public @interface MyOtherConstraint {

		String message() default "MyOtherConstraint is not valid";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};

		class MyOtherConstraintValidator implements ConstraintValidator<MyOtherConstraint, Object> {

			@Override
			public boolean isValid(Object value, ConstraintValidatorContext context) {
				return false;
			}
		}

		class MyOtherOtherConstraintValidator implements ConstraintValidator<MyOtherConstraint, Object> {

			@Override
			public boolean isValid(Object value, ConstraintValidatorContext context) {
				return true;
			}
		}
	}

	/*
	 * A classloader that allows to use a `META-INF/services/jakarta.validation.ConstraintValidator`
	 * defined in the tests rather than reading it from an actual file.
	 */
	@IgnoreForbiddenApisErrors(reason = "Need a Java 20 API to create URLs with a custom handler.")
	private static class ServiceLoaderTestingClassLoader extends SecureClassLoader {

		private static final String SERVICE_FILE = "META-INF/services/" + ConstraintValidator.class.getName();

		public ServiceLoaderTestingClassLoader() {
			super( ServiceLoaderTestingClassLoader.class.getClassLoader() );
		}

		@Override
		public Enumeration<URL> getResources(String name) throws IOException {
			if ( SERVICE_FILE.equals( name ) ) {
				URL url = new URL( "protocol", "host", -1, "file", new URLStreamHandler() {
					@Override
					protected URLConnection openConnection(URL u) {
						return new URLConnection( u ) {
							@Override
							public void connect() {
							}

							@Override
							public InputStream getInputStream() {
								return new ByteArrayInputStream(
										MyOtherConstraint.MyOtherConstraintValidator.class.getName()
												.getBytes( StandardCharsets.UTF_8 )
								);
							}
						};
					}
				} );
				return new Enumeration<>() {
					private boolean hasMore = true;

					@Override
					public boolean hasMoreElements() {
						return hasMore;
					}

					@Override
					public URL nextElement() {
						hasMore = false;
						return url;
					}
				};
			}
			return super.getResources( name );
		}
	}

}
