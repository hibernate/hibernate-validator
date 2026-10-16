/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import java.util.regex.Matcher;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper;
import org.hibernate.validator.internal.util.RegexUtil;
import org.hibernate.validator.messageinterpolation.MessageParameterEscaping;

/**
 * @author Hardy Ferentschik
 */
public class PatternValidator implements HibernateConstraintValidator<Pattern, CharSequence> {

	private java.util.regex.Pattern pattern;
	private String escapedRegexp;

	@Override
	public void initialize(ConstraintDescriptor<Pattern> constraintDescriptor, HibernateConstraintValidatorInitializationContext initializationContext) {
		Pattern parameters = constraintDescriptor.getAnnotation();
		pattern = RegexUtil.compile( parameters.regexp(), parameters.flags(), initializationContext );

		escapedRegexp = InterpolationHelper.escapeMessageParameter( parameters.regexp() );
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}

		if ( constraintValidatorContext instanceof HibernateConstraintValidatorContext ) {
			constraintValidatorContext.unwrap( HibernateConstraintValidatorContext.class ).addMessageParameter( "regexp", escapedRegexp, MessageParameterEscaping.NONE );
		}

		Matcher m = pattern.matcher( value );
		return m.matches();
	}
}
