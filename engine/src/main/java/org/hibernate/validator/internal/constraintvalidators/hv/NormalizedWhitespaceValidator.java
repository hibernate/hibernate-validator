package org.hibernate.validator.internal.constraintvalidators.hv;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.NormalizedWhitespace;
import org.hibernate.validator.constraints.NormalizedWhitespace.Rule;

/**
 * Checks that whitespace in a {@link CharSequence} conforms to a set of {@link Rule rules}.
 *
 * @author Koen Aers
 */
public class NormalizedWhitespaceValidator implements ConstraintValidator<NormalizedWhitespace, CharSequence> {

	private Set<Rule> rules;

	@Override
	public void initialize(NormalizedWhitespace constraintAnnotation) {
		rules = EnumSet.noneOf( Rule.class );
		rules.addAll( Arrays.asList( constraintAnnotation.value() ) );
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null || value.length() == 0 ) {
			return true;
		}

		boolean onlySpace = rules.contains( Rule.ONLY_SPACE );
		boolean noTabs = rules.contains( Rule.NO_TABS ) || onlySpace;
		boolean noFormFeed = rules.contains( Rule.NO_FORM_FEED ) || onlySpace;
		boolean noConsecutive = rules.contains( Rule.NO_CONSECUTIVE );

		boolean previousWasWhitespace = false;
		for ( int i = 0; i < value.length(); i++ ) {
			char c = value.charAt( i );
			boolean isWhitespace = Character.isWhitespace( c );

			if ( isWhitespace ) {
				if ( noConsecutive && previousWasWhitespace ) {
					return false;
				}
				if ( noTabs && c == '\t' ) {
					return false;
				}
				if ( noFormFeed && ( c == '\f' || c == '\u000B' ) ) {
					return false;
				}
				if ( onlySpace && c != ' ' ) {
					return false;
				}
			}

			previousWasWhitespace = isWhitespace;
		}

		return true;
	}
}
