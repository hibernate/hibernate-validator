package org.hibernate.validator.internal.constraintvalidators.hv;

import java.lang.invoke.MethodHandles;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.MaxLines;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Checks that a character sequence has at most the specified number of lines.
 *
 * @since 9.2
 */
public class MaxLinesValidator implements ConstraintValidator<MaxLines, CharSequence> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private int maxLines;

	@Override
	public void initialize(MaxLines parameters) {
		maxLines = parameters.value();
		if ( maxLines < 1 ) {
			throw LOG.getMinCannotBeNegativeException();
		}
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}
		int lines = 1;
		boolean previousWasCR = false;
		for ( int i = 0; i < value.length(); i++ ) {
			char c = value.charAt( i );
			if ( c == '\n' && previousWasCR ) {
				// \r\n counts as a single line break; the \r already incremented
				previousWasCR = false;
				continue;
			}
			previousWasCR = ( c == '\r' );
			if ( c == '\n' || c == '\r' || c == '\u0085' || c == '\u2028' || c == '\u2029' ) {
				lines++;
			}
			if ( lines > maxLines ) {
				return false;
			}
		}
		return true;
	}
}
