/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cfg;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import jakarta.validation.BootstrapConfiguration;
import jakarta.validation.ClockProvider;
import jakarta.validation.Configuration;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.TraversableResolver;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.spi.BootstrapState;
import jakarta.validation.spi.ConfigurationState;
import jakarta.validation.spi.ValidationProvider;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.DefaultClockProvider;
import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.resolver.TraversableResolvers;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

/**
 * @author Guillaume Smet
 */
public class NonHibernateValidatorConfigTest {

	@Test
	@TestForIssue(jiraKey = "HV-1821")
	public void testNonHibernateValidatorConfig() {
		ValidatorFactory validatorFactory = Validation.byProvider( NonHibernateValidatorProvider.class )
				.configure()
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		assertThat( validator.validate( new Bean() ) ).containsOnlyViolations( violationOf( NotNull.class ) );
	}

	public static final class NonHibernateValidatorProvider implements ValidationProvider<NonHibernateValidatorConfiguration> {

		@Override
		public NonHibernateValidatorConfiguration createSpecializedConfiguration(BootstrapState state) {
			return new NonHibernateValidatorConfiguration();
		}

		@Override
		public Configuration<?> createGenericConfiguration(BootstrapState state) {
			return new NonHibernateValidatorConfiguration();
		}

		@Override
		public ValidatorFactory buildValidatorFactory(ConfigurationState configurationState) {
			return new ValidatorFactoryImpl( configurationState );
		}
	}

	public static final class NonHibernateValidatorConfiguration implements Configuration<NonHibernateValidatorConfiguration>, ConfigurationState {

		private final MessageInterpolator defaultMessageInterpolator;
		private final TraversableResolver defaultTraversableResolver;
		private final ConstraintValidatorFactory defaultConstraintValidatorFactory;
		private final ParameterNameProvider defaultParameterNameProvider;
		private final ClockProvider defaultClockProvider;

		public NonHibernateValidatorConfiguration() {
			this.defaultMessageInterpolator = new ParameterMessageInterpolator();
			this.defaultTraversableResolver = TraversableResolvers.getDefault();
			this.defaultConstraintValidatorFactory = new ConstraintValidatorFactoryImpl();
			this.defaultParameterNameProvider = new DefaultParameterNameProvider();
			this.defaultClockProvider = DefaultClockProvider.INSTANCE;
		}

		@Override
		public NonHibernateValidatorConfiguration ignoreXmlConfiguration() {
			return this;
		}

		@Override
		public NonHibernateValidatorConfiguration messageInterpolator(MessageInterpolator interpolator) {
			return this;
		}

		@Override
		public NonHibernateValidatorConfiguration traversableResolver(TraversableResolver resolver) {
			return this;
		}

		@Override
		public NonHibernateValidatorConfiguration constraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
			return this;
		}

		@Override
		public NonHibernateValidatorConfiguration parameterNameProvider(ParameterNameProvider parameterNameProvider) {
			return this;
		}

		@Override
		public NonHibernateValidatorConfiguration clockProvider(ClockProvider clockProvider) {
			return this;
		}

		@Override
		public NonHibernateValidatorConfiguration addValueExtractor(ValueExtractor<?> extractor) {
			return this;
		}

		@Override
		public NonHibernateValidatorConfiguration addMapping(InputStream stream) {
			return this;
		}

		@Override
		public NonHibernateValidatorConfiguration addProperty(String name, String value) {
			return this;
		}

		@Override
		public MessageInterpolator getDefaultMessageInterpolator() {
			return defaultMessageInterpolator;
		}

		@Override
		public TraversableResolver getDefaultTraversableResolver() {
			return defaultTraversableResolver;
		}

		@Override
		public ConstraintValidatorFactory getDefaultConstraintValidatorFactory() {
			return defaultConstraintValidatorFactory;
		}

		@Override
		public ParameterNameProvider getDefaultParameterNameProvider() {
			return defaultParameterNameProvider;
		}

		@Override
		public ClockProvider getDefaultClockProvider() {
			return defaultClockProvider;
		}

		@Override
		public BootstrapConfiguration getBootstrapConfiguration() {
			return null;
		}

		@Override
		public ValidatorFactory buildValidatorFactory() {
			return new NonHibernateValidatorProvider().buildValidatorFactory( this );
		}

		@Override
		public boolean isIgnoreXmlConfiguration() {
			return true;
		}

		@Override
		public MessageInterpolator getMessageInterpolator() {
			return defaultMessageInterpolator;
		}

		@Override
		public Set<InputStream> getMappingStreams() {
			return Collections.emptySet();
		}

		@Override
		public Set<ValueExtractor<?>> getValueExtractors() {
			return Collections.emptySet();
		}

		@Override
		public ConstraintValidatorFactory getConstraintValidatorFactory() {
			return defaultConstraintValidatorFactory;
		}

		@Override
		public TraversableResolver getTraversableResolver() {
			return defaultTraversableResolver;
		}

		@Override
		public ParameterNameProvider getParameterNameProvider() {
			return defaultParameterNameProvider;
		}

		@Override
		public ClockProvider getClockProvider() {
			return defaultClockProvider;
		}

		@Override
		public Map<String, String> getProperties() {
			return Collections.emptyMap();
		}
	}

	public static final class Bean {

		@NotNull
		public String property;
	}
}
