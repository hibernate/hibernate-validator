/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.messageinterpolation;

import static org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper.escapeMessageParameter;
import static org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper.resolveToString;

import org.hibernate.validator.Incubating;

/**
 * Defines whether message parameters should be escaped to prevent injection into message interpolation expressions.
 *
 * @since 8.0.6
 */
@Incubating
public enum MessageParameterEscaping {

	/**
	 * Message parameter values are escaped before being stored.
	 * <p>
	 * This is the default behavior. The value is converted to its string representation and characters
	 * that have special meaning in message interpolation ({@code \}, <code>{</code>, <code>}</code>, {@code $})
	 * are escaped to prevent injection.
	 */
	ESCAPE( "escape" ) {
		@Override
		public Object apply(Object value) {
			if ( value == null || value instanceof Number || value instanceof Boolean ) {
				return value;
			}
			return escapeMessageParameter( resolveToString( value ) );
		}
	},

	/**
	 * Message parameter values are stored as-is, without any escaping.
	 * <p>
	 * Use this when the value is already escaped or when you are certain it does not contain
	 * characters that could be interpreted during message interpolation. Or when the value
	 * can and will be interpolated, but that is your intention to do so.
	 */
	NONE( "none" ) {
		@Override
		public Object apply(Object value) {
			return value;
		}
	};

	private final String externalRepresentation;

	MessageParameterEscaping(String externalRepresentation) {
		this.externalRepresentation = externalRepresentation;
	}

	public abstract Object apply(Object value);

	public static MessageParameterEscaping of(String value) {
		for ( MessageParameterEscaping level : values() ) {
			if ( level.externalRepresentation.equals( value ) ) {
				return level;
			}
		}

		return MessageParameterEscaping.valueOf( value );
	}
}
