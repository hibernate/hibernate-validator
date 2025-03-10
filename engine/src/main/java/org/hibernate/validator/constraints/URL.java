/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
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
import jakarta.validation.OverridesAttribute;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.URL.List;

/**
 * Validates the annotated string is an URL.
 *
 * <p>
 * The parameters {@code protocol}, {@code host} and {@code port} are matched against the corresponding parts of the URL.
 * and an additional regular expression can be specified using {@code regexp} and {@code flags} to further restrict the
 * matching criteria.
 * </p>
 *
 * <p>
 * <b>Note</b>:
 * Per default the constraint validator for this constraint uses the {@code java.net.URL} constructor to validate the string.
 * This means that a matching protocol handler needs to be available. Handlers for the following protocols are guaranteed
 * to exist within a default JVM - http, https, ftp, file, and jar.
 * See also the Javadoc for <a href="http://docs.oracle.com/javase/7/docs/api/java/net/URL.html">URL</a>.
 * </p>
 * <p>
 * In case URLs with non default protocol handlers need to be validated, Hibernate Validator can be configured to use
 * a regular expression based URL validator only. This can be done programmatically via a {@code org.hibernate.validator.cfg.ConstraintMapping}:
 * <pre>
 * {@code
 * HibernateValidatorConfiguration config = Validation.byProvider( HibernateValidator.class )
 *     .getConfiguration( HibernateValidator.class );
 *
 * ConstraintMapping constraintMapping = config.createConstraintMapping();
 * constraintMapping
 *     .constraintDefinition( URL.class )
 *     .includeExistingValidators( false )
 *     .validatedBy( RegexpURLValidator.class );
 *
 * config.addMapping( constraintMapping );
 * }
 * </pre>
 * or via a constraint mapping configuration:
 * <pre>
 * {@code
 * <constraint-mappings
 *     xmlns="http://jboss.org/xml/ns/javax/validation/mapping"
 *     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *     xsi:schemaLocation="http://jboss.org/xml/ns/javax/validation/mapping validation-mapping-1.0.xsd">
 *
 *     <constraint-definition annotation="org.hibernate.validator.constraints.URL">
 *         <validated-by include-existing-validators="false">
 *             <value>org.hibernate.validator.constraintvalidators.RegexpURLValidator</value>
 *         </validated-by>
 *     </constraint-definition>
 * </constraint-mappings>
 * }
 * </pre>
 *
 * @see <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC2396</a>
 * @see org.hibernate.validator.cfg.ConstraintMapping#constraintDefinition(Class)
 * @author Hardy Ferentschik
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
@ReportAsSingleViolation
@Pattern(regexp = "")
public @interface URL {
	String message() default "{org.hibernate.validator.constraints.URL.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return the protocol (scheme) the annotated string must match, e.g. ftp or http.
	 *         Per default any protocol is allowed
	 */
	String protocol() default "";

	/**
	 * @return the host the annotated string must match, e.g. localhost. Per default any host is allowed
	 */
	String host() default "";

	/**
	 * @return the port the annotated string must match, e.g. 80. Per default any port is allowed
	 */
	int port() default -1;

	/**
	 * @return an additional regular expression the annotated URL must match. The default is any string ('.*')
	 */
	@OverridesAttribute(constraint = Pattern.class, name = "regexp")
	String regexp() default ".*";

	/**
	 * @return used in combination with {@link #regexp()} in order to specify a regular expression option
	 */
	@OverridesAttribute(constraint = Pattern.class, name = "flags")
	Pattern.Flag[] flags() default { };

	/**
	 * Defines several {@code @URL} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		URL[] value();
	}
}
