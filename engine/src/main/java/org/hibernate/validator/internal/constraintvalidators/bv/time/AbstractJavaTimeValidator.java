/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.function.LongPredicate;

import javax.validation.ClockProvider;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Base class for all time validators that are based on the {@code java.time} package.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 */
public abstract class AbstractJavaTimeValidator<C extends Annotation, T> implements
		HibernateConstraintValidator<C, T> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private ConstraintKind constraintKind;

	protected ClockProvider clockProvider;

	protected Duration tolerance;

	@Override
	public void initialize(ConstraintDescriptor<C> constraintDescriptor, HibernateConstraintValidatorInitializationContext initializationContext) {
		this.clockProvider = initializationContext.getClockProvider();
		this.tolerance = initializationContext.getClockSkewTolerance();
		this.constraintKind = ConstraintKind.from( constraintDescriptor.getAnnotation().annotationType() );
	}

	@Override
	public boolean isValid(T value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		Clock reference;

		try {
			reference = clockProvider.getClock();
		}
		catch (Exception e) {
			throw LOG.getUnableToGetCurrentTimeFromClockProvider( e );
		}

		Duration result = getDifference( reference, value );

		return constraintKind.isValid.test( Duration.ZERO.compareTo( result ) );
	}

	protected abstract Duration getDifference(Clock reference, T value);

	protected Temporal adjustCurrentTime(Temporal nowValue) {
		return isFuture() ? nowValue.plus( tolerance ) : nowValue.minus( tolerance );
	}

	protected enum ConstraintKind {

		PAST( Past.class, result -> result < 0 ),
		FUTURE( Future.class, result -> result > 0 ),
		PAST_OR_PRESENT( PastOrPresent.class, result -> result >= 0 ),
		FUTURE_OR_PRESENT( FutureOrPresent.class, result -> result <= 0 ),;

		private Class<? extends Annotation> annotation;
		private LongPredicate isValid;

		ConstraintKind(Class<? extends Annotation> annotation, LongPredicate isValid) {
			this.annotation = annotation;
			this.isValid = isValid;
		}

		public static ConstraintKind from(Class<? extends Annotation> annotation) {
			return Arrays.stream( ConstraintKind.values() ).filter( kind -> kind.annotation.equals( annotation ) )
					.findFirst()
					.orElseThrow( () -> new IllegalStateException( "type " + annotation + " is not allowed" ) );
		}
	}

	protected boolean isFuture() {
		return ConstraintKind.FUTURE.equals( constraintKind ) || ConstraintKind.FUTURE_OR_PRESENT.equals( constraintKind );
	}

}
