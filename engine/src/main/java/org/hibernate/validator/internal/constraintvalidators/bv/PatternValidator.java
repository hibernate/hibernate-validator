/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Hardy Ferentschik
 */
public class PatternValidator implements HibernateConstraintValidator<Pattern, CharSequence> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private java.util.regex.Pattern pattern;
	private String escapedRegexp;

	@Override
	public void initialize(ConstraintDescriptor<Pattern> constraintDescriptor, HibernateConstraintValidatorInitializationContext initializationContext) {
		Pattern parameters = constraintDescriptor.getAnnotation();
		Pattern.Flag[] flags = parameters.flags();
		int intFlag = 0;
		for ( Pattern.Flag flag : flags ) {
			intFlag = intFlag | flag.getValue();
		}

		try {
			pattern = initializationContext.getSharedData( PatternConstraintInitializer.class, PatternConstraintInitializer::getInstance )
					.of( parameters.regexp(), intFlag );
		}
		catch (PatternSyntaxException e) {
			throw LOG.getInvalidRegularExpressionException( e );
		}

		escapedRegexp = InterpolationHelper.escapeMessageParameter( parameters.regexp() );
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}

		if ( constraintValidatorContext instanceof HibernateConstraintValidatorContext ) {
			constraintValidatorContext.unwrap( HibernateConstraintValidatorContext.class ).addMessageParameter( "regexp", escapedRegexp );
		}

		Matcher m = pattern.matcher( value );
		return m.matches();
	}

	private static final class PatternConstraintInitializer {
		private final Map<PatternKey, java.util.regex.Pattern> cache;

		public static PatternConstraintInitializer getInstance() {
			//TODO: do we cache the instance and share it?
			return new PatternConstraintInitializer();
		}

		private PatternConstraintInitializer() {
			this.cache = new ConcurrentHashMap<>();
		}

		public java.util.regex.Pattern of(String pattern, int flags) {
			return cache.computeIfAbsent( new PatternKey( pattern, flags ), key -> java.util.regex.Pattern.compile( pattern, flags ) );
		}

		private record PatternKey(String pattern, int flags) {
		}
	}
}
