package org.hibernate.validator.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.constraints.Base64.List;

/**
 * The annotated character sequence must be a valid Base64 encoded string as defined by
 * <a href="https://www.rfc-editor.org/rfc/rfc4648">RFC 4648</a>.
 * <p>
 * By default the "base64" alphabet ({@code A-Z}, {@code a-z}, {@code 0-9}, {@code +}, {@code /}) of
 * RFC 4648 section 4 is expected. When {@link #urlSafe()} is {@code true}, the "URL and Filename safe"
 * alphabet ({@code A-Z}, {@code a-z}, {@code 0-9}, {@code -}, {@code _}) of RFC 4648 section 5 is expected
 * instead. The two alphabets are not mixed: {@code -} and {@code _} are invalid in the default mode,
 * {@code +} and {@code /} are invalid in URL-safe mode.
 * <p>
 * The validation relies on {@link java.util.Base64.Decoder}:
 * <ul>
 *     <li>Padding with {@code =} is optional; unpadded values are accepted. This deviates from
 *     RFC 4648 section 3.2, which requires padding unless explicitly stated otherwise by the
 *     specification referring to it. When padding is present, it must be correct.</li>
 *     <li>Line separators, whitespace and any other character outside the selected alphabet are rejected,
 *     as recommended by RFC 4648 section 3.3. This includes any character above {@code U+00FF}.</li>
 *     <li>A dangling final unit consisting of a single character is rejected.</li>
 *     <li>Non-zero unused bits in the final unit (e.g. {@code QR==} instead of {@code QQ==}) are accepted,
 *     so the canonical encoding of the decoded bytes is not guaranteed.</li>
 * </ul>
 * <p>
 * The value is fully decoded during validation and the decoded bytes are discarded. Validation runs in
 * linear time and does not use regular expressions, but allocates memory proportional to the length of
 * the value (roughly 1.75 bytes per character). Combine with {@code @Size} to bound the length of
 * untrusted input.
 * <p>
 * {@code null} values and empty character sequences are considered valid.
 *
 * @author Andrea Boriero
 * @since 9.2
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
@Incubating
public @interface Base64 {

	String message() default "{org.hibernate.validator.constraints.Base64.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return {@code true} to validate against the "URL and Filename safe" alphabet of RFC 4648 section 5
	 * ({@code -} and {@code _} instead of {@code +} and {@code /}), {@code false} (default) to validate
	 * against the "base64" alphabet of RFC 4648 section 4
	 */
	boolean urlSafe() default false;

	/**
	 * Defines several {@code @Base64} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		Base64[] value();
	}
}
