/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ConstraintValidatorInitializationHelper.initialize;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.OverridesAttribute;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.constraintvalidators.RegexpURLValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.PatternValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.URLValidator;
import org.hibernate.validator.internal.util.RegexUtil;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class URLConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testURLNumber() {
		Foo foo = new Foo( "http://example.com" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testURLInvalid() {
		Foo foo = new Foo( "not an url" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( URL.class )
		);
	}

	@Test
	public void testStandaloneConstraint() {
		assertTrue( validator.getConstraintsForClass( Foo.class )
				.getConstraintsForProperty( "url" ).getConstraintDescriptors().iterator().next()
				.getComposingConstraints().isEmpty() );
	}

	@DataProvider
	public Object[][] urlValidators() {
		return new Object[][] { { false }, { true } };
	}

	@Test(dataProvider = "urlValidators")
	public void testAdditionalRegexMatchesSeparatePattern(boolean regexpValidator) {
		try ( ValidatorFactory factory = factory( regexpValidator ) ) {
			Validator validator = factory.getValidator();
			for ( CharSequence value : new CharSequence[] {
					null,
					"",
					"http://example.com/allowed",
					"http://example.com/ALLOWED",
					"http://example.com/denied",
					"http://example.com/allowed/extra",
					"http://example.com/prefix/allowed",
					"ftp://example.com/allowed",
					"not a URL",
					"http://example.com/allowed\n",
					new StringBuilder( "http://example.com/ALLOWED" ) } ) {
				assertEquals( validator.validateValue( RegexURLs.class, "refined", value ).isEmpty(),
						validator.validateValue( RegexURLs.class, "composed", value ).isEmpty(), String.valueOf( value ) );
				assertEquals( validator.validateValue( RegexURLs.class, "multipleFlags", value ).isEmpty(),
						validator.validateValue( RegexURLs.class, "composed", value ).isEmpty(), String.valueOf( value ) );
			}
			assertTrue( validator.validateValue( RegexURLs.class, "refined", null ).isEmpty() );
			assertTrue( validator.validateValue( RegexURLs.class, "refined", "http://example.com/ALLOWED" ).isEmpty() );
			assertEquals( validator.validateValue( RegexURLs.class, "refined", "" ).size(), 1 );
			assertEquals( validator.validateValue( RegexURLs.class, "refined", "http://example.com/denied" ).size(), 1 );
			assertEquals( validator.validateValue( RegexURLs.class, "refined", "http://example.com/allowed/extra" ).size(), 1 );
			assertEquals( validator.validateValue( RegexURLs.class, "refined", "http://example.com/prefix/allowed" ).size(), 1 );
		}
	}

	@Test(dataProvider = "urlValidators")
	public void testRegexpMessageMatchesLegacyComposition(boolean regexpValidator) {
		try ( ValidatorFactory factory = factory( regexpValidator ) ) {
			Set<ConstraintViolation<RegexURLs>> violations = factory.getValidator()
					.validateValue( RegexURLs.class, "escaped", "http://example.com" );
			assertEquals( violations.size(), 1 );
			Set<ConstraintViolation<RegexURLs>> legacyViolations = factory.getValidator()
					.validateValue( RegexURLs.class, "legacyEscaped", "http://example.com" );
			assertEquals( legacyViolations.size(), 1 );
			assertEquals( violations.iterator().next().getMessage(), legacyViolations.iterator().next().getMessage() );
		}
	}

	@Test(dataProvider = "urlValidators")
	public void testCompiledPatternsAreSharedOnlyWithinInitializationContext(boolean regexpValidator) throws Exception {
		java.util.regex.Pattern first = assertFactoryPatternSharing( regexpValidator );
		java.util.regex.Pattern second = assertFactoryPatternSharing( regexpValidator );
		assertNotSame( first, second, "Distinct factories must not share the compiled-pattern cache" );
	}

	private static java.util.regex.Pattern assertFactoryPatternSharing(boolean regexpValidator) throws Exception {
		HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		if ( regexpValidator ) {
			ConstraintMapping mapping = configuration.createConstraintMapping();
			mapping.constraintDefinition( URL.class ).includeExistingValidators( false ).validatedBy( RegexpURLValidator.class );
			configuration.addMapping( mapping );
		}
		RecordingValidatorFactory recorder = new RecordingValidatorFactory( configuration.getDefaultConstraintValidatorFactory() );
		configuration.constraintValidatorFactory( recorder );
		try ( ValidatorFactory factory = configuration.buildValidatorFactory() ) {
			assertNoViolations( factory.getValidator().validate( new SharedRegexURLs() ) );
			assertEquals( recorder.urls.size(), 3 );
			java.util.regex.Pattern shared = null;
			java.util.regex.Pattern otherFlags = null;
			java.util.regex.Pattern otherRegexp = null;
			for ( URLInitialization initialization : recorder.urls ) {
				URL annotation = initialization.descriptor().getAnnotation();
				java.util.regex.Pattern cached = RegexUtil.compile( annotation.regexp(), annotation.flags(), initialization.context() );
				// Factory bootstrap has already called descriptor initialize followed by annotation initialize.
				assertSame( compiledPattern( initialization.validator() ), cached );
				boolean matchedPatternValidator = false;
				for ( PatternValidator validator : recorder.patterns ) {
					matchedPatternValidator |= compiledPattern( validator ) == cached;
				}
				assertTrue( matchedPatternValidator, "Pattern and URL must reuse the same compiled Pattern" );

				HibernateConstraintValidator<URL, CharSequence> alternative = regexpValidator ? new URLValidator() : new RegexpURLValidator();
				alternative.initialize( initialization.descriptor(), initialization.context() );
				alternative.initialize( annotation );
				assertSame( compiledPattern( alternative ), cached, "Both supplied URL validators must use the context cache" );
				assertTrue( alternative.isValid( "http://example.com/path", null ) );

				if ( annotation.regexp().equals( SharedRegexURLs.REGEXP ) ) {
					if ( annotation.flags().length == 0 ) {
						shared = cached;
					}
					else {
						otherFlags = cached;
					}
				}
				else {
					otherRegexp = cached;
				}
			}
			assertTrue( shared != null && otherFlags != null && otherRegexp != null );
			assertNotSame( shared, otherRegexp, "Cache keys must include the regular expression" );
			assertNotSame( shared, otherFlags, "Cache keys must include requested flags" );
			assertEquals( shared.flags(), otherFlags.flags(), "Embedded flags expand the compiled flags independently of the cache key" );
			return shared;
		}
	}

	private static java.util.regex.Pattern compiledPattern(Object validator) throws Exception {
		Class<?> type = validator instanceof PatternValidator ? PatternValidator.class
				: validator instanceof URLValidator ? URLValidator.class
				: RegexpURLValidator.class;
		Field field = type.getDeclaredField( type == PatternValidator.class ? "pattern" : "regexp" );
		field.setAccessible( true );
		return (java.util.regex.Pattern) field.get( validator );
	}

	private record URLInitialization(   Object validator, ConstraintDescriptor<URL> descriptor,
										HibernateConstraintValidatorInitializationContext context) {
	}

	private static class RecordingValidatorFactory implements ConstraintValidatorFactory {
		private final ConstraintValidatorFactory delegate;
		private final List<PatternValidator> patterns = new ArrayList<>();
		private final List<URLInitialization> urls = new ArrayList<>();

		private RecordingValidatorFactory(ConstraintValidatorFactory delegate) {
			this.delegate = delegate;
		}

		@Override
		public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
			if ( key == URLValidator.class ) {
				return key.cast( new URLValidator() {
					@Override
					public void initialize(ConstraintDescriptor<URL> descriptor, HibernateConstraintValidatorInitializationContext context) {
						super.initialize( descriptor, context );
						urls.add( new URLInitialization( this, descriptor, context ) );
					}
				} );
			}
			if ( key == RegexpURLValidator.class ) {
				return key.cast( new RegexpURLValidator() {
					@Override
					public void initialize(ConstraintDescriptor<URL> descriptor, HibernateConstraintValidatorInitializationContext context) {
						super.initialize( descriptor, context );
						urls.add( new URLInitialization( this, descriptor, context ) );
					}
				} );
			}
			T instance = delegate.getInstance( key );
			if ( instance instanceof PatternValidator ) {
				patterns.add( (PatternValidator) instance );
			}
			return instance;
		}

		@Override
		public void releaseInstance(ConstraintValidator<?, ?> instance) {
			delegate.releaseInstance( instance );
		}
	}

	private static class SharedRegexURLs {
		private static final String REGEXP = "(?Ui)http://example[.]com/path";

		@URL(regexp = REGEXP)
		private String url = "http://example.com/path";

		@Pattern(regexp = REGEXP)
		private String pattern = "http://example.com/path";

		@URL(regexp = REGEXP, flags = { Pattern.Flag.UNICODE_CASE, Pattern.Flag.CASE_INSENSITIVE })
		private String otherFlagsURL = "http://example.com/path";

		@Pattern(regexp = REGEXP, flags = { Pattern.Flag.CASE_INSENSITIVE, Pattern.Flag.UNICODE_CASE })
		private String otherFlagsPattern = "http://example.com/path";

		@URL(regexp = "(?Ui)http://example[.]com/(?:path)")
		private String otherURL = "http://example.com/path";

		@Pattern(regexp = "(?Ui)http://example[.]com/(?:path)")
		private String otherPattern = "http://example.com/path";
	}

	private static ValidatorFactory factory(boolean regexpValidator) {
		HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		if ( regexpValidator ) {
			ConstraintMapping mapping = configuration.createConstraintMapping();
			mapping.constraintDefinition( URL.class ).includeExistingValidators( false ).validatedBy( RegexpURLValidator.class );
			configuration.addMapping( mapping );
		}
		return configuration.buildValidatorFactory();
	}

	@Constraint(validatedBy = { })
	@URL
	@Pattern(regexp = "")
	@ReportAsSingleViolation
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface LegacyURL {
		String message() default "{regexp}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

		@OverridesAttribute(constraint = Pattern.class, name = "regexp")
		String regexp();
	}

	private static class RegexURLs {
		@URL(regexp = "HTTP://EXAMPLE[.]COM/ALLOWED", flags = Pattern.Flag.CASE_INSENSITIVE)
		private CharSequence refined;

		@URL
		@Pattern(regexp = "HTTP://EXAMPLE[.]COM/ALLOWED", flags = Pattern.Flag.CASE_INSENSITIVE)
		private CharSequence composed;

		@URL(regexp = "HTTP://EXAMPLE[.]COM/ALLOWED # comment", flags = { Pattern.Flag.CASE_INSENSITIVE, Pattern.Flag.COMMENTS })
		private CharSequence multipleFlags;

		@URL(regexp = "\\Q${validatedValue}\\{unknown}\\E", message = "{regexp}")
		private CharSequence escaped;

		@LegacyURL(regexp = "\\Q${validatedValue}\\{unknown}\\E")
		private CharSequence legacyEscaped;
	}

	private static class Foo {

		@URL
		private final String url;

		public Foo(String url) {
			this.url = url;
		}
	}
}
