package org.hibernate.validator.internal.engine.messageinterpolation;

/**
 * Describes the type of the interpolation term.
 *
 * @author Hardy Ferentschik
 */
public enum InterpolationTermType {
	/**
	 * EL message expression, eg ${foo}.
	 */
	EL,

	/**
	 * Message parameter, eg {foo}.
	 */
	PARAMETER
}
